package controllers.migration;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.processes.instance.Process;
import models.utils.InstanceConstants;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import play.Logger;
import play.mvc.Result;
import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;

public class MigrationContainerFields extends CommonController {
	
	public static Result migration(){
		Logger.info("Start MigrationContainerFields");
		
		
		List<Experiment> purifExperiments =MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.is("categoryCode", "purification")).toList();
		purifExperiments.parallelStream().filter(t->t.outputContainerCodes!=null).forEach(p->{
			p.outputContainerCodes.parallelStream().forEach(c->{
				MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME,Container.class,DBQuery.is("code",c).notExists("fromPurificationCode").notExists("fromPurificationTypeCode"),DBUpdate.set("fromPurificationTypeCode",p.typeCode).set("fromPurificationCode",p.code));
			});
			
		});
		
		
		List<Experiment> transfertExperiments =MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.is("categoryCode", "transfert")).toList();
		
		transfertExperiments.parallelStream().filter(t->t.outputContainerSupportCodes!=null).forEach(t->{
			t.outputContainerSupportCodes.parallelStream().forEach(c->{
				MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME,Container.class,DBQuery.is("support.code",c).notExists("fromTransfertTypeCode").notExists("fromTransfertCode"),DBUpdate.set("fromTransfertTypeCode",t.typeCode).set("fromTransfertCode",t.code));
			}); 
		});
		
		
		
		List<Experiment> newSampleExperiments=MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.in("typeCode", "tag-pcr","dna-rna-extraction")).toList();
		
		Logger.debug("Nb experiment new sample"+newSampleExperiments.size());
		
		newSampleExperiments.forEach(e->{
			  e.atomicTransfertMethods.stream().collect(Collectors.toMap(a->a.inputContainerUseds.get(0),a->a.outputContainerUseds))
			  .forEach((input,outputs)->{
				  
				  Logger.debug("Input "+input.code+", nb output "+outputs.size());
				  
				  PropertySingleValue projectCodeProperty=new PropertySingleValue(input.contents.get(0).projectCode);
				  PropertySingleValue sampleCodeProperty=new PropertySingleValue(input.contents.get(0).sampleCode);
				  PropertySingleValue sampleTypeCodeProperty=new PropertySingleValue(input.contents.get(0).sampleTypeCode);

				  if(outputs!=null){
				  List<String> outputContainers=outputs.stream().map(o->o.code).collect(Collectors.toList());
				  List<String> outputSampleCodes=outputs.stream().filter(o->o.contents!=null).map(o->o.contents.get(0).sampleCode).collect(Collectors.toList());
				  
				  Logger.debug("contents.sampleCode"+outputSampleCodes.toString());
				  Logger.debug("code"+outputContainers.toString());
				  
				  if(sampleCodeProperty.value.equals("BUF_AAAA")){
					  Logger.error("*********BUF_AAAA*************");
					  Logger.error("Update ");
				  }
				  
				  MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME,Container.class,DBQuery.in("contents.sampleCode",outputSampleCodes).notEquals("contents.sampleCode", sampleCodeProperty.value),
						  DBUpdate.set("contents.$.properties.fromProjectCode",projectCodeProperty)
						  		.set("contents.$.properties.fromSampleCode",sampleCodeProperty)
						  		.set("contents.$.properties.fromSampleTypeCode",sampleTypeCodeProperty));
				  
				  MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME,Container.class,DBQuery.in("code",outputContainers)
						  ,DBUpdate.set("contents.0.properties.fromProjectCode",projectCodeProperty)
						  .set("contents.0.properties.fromSampleCode",sampleCodeProperty)
						  .set("contents.0.properties.fromSampleTypeCode",sampleTypeCodeProperty)
						  );
				  
				  }
			  });
		});
		
		return ok("Migration Finish !");

	}
	
public static Result migrationOld(){
		
		Logger.info("Start MigrationContainerFields");
		
		//MongoDBResult<Container> results = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class);
		
		//1 remove calculedVolume
		MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.exists("calculedVolume"),DBUpdate.unset("calculedVolume"));
		
		//2 rename mesuredQuantity to quantity, mesuredConcentration to concentration
		MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.exists("code"),
				DBUpdate.rename("mesuredQuantity", "quantity")
						.rename("mesuredVolume", "volume")
						.rename("mesuredConcentration", "concentration")
						.rename("inputProcessCodes", "processCodes")
						.rename("fromExperimentTypeCodes", "fromTransformationTypeCodes")
				);
		
		
		MongoDBDAO.update(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, Container.class, DBQuery.exists("code"),
				DBUpdate.rename("fromExperimentTypeCodes", "fromTransformationTypeCodes")
				);
		
		//3 move processTypeCode to processTypeCodes
		MongoDBResult<ContainerOld> results = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, ContainerOld.class, DBQuery.exists("processTypeCode"));
		
		results.toList().forEach(c -> {
			MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, ContainerOld.class, DBQuery.is("code",c.code),
					DBUpdate.set("processTypeCodes",Collections.singleton(c.processTypeCode)).unset("processTypeCode")) ;
		});
		
		
		//4 move Experiment inputProcessCodes
		MongoDBResult<ExperimentOld> results2 = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, ExperimentOld.class, DBQuery.exists("atomicTransfertMethods.inputContainerUseds.inputProcessCodes"));
		results2.toList().forEach(e -> {
			e.atomicTransfertMethods.forEach(atm -> {
				atm.inputContainerUseds.forEach(icu -> {
					if(null != icu.fromExperimentTypeCodes && icu.fromExperimentTypeCodes.size() > 0){
						icu.fromTransformationTypeCodes = icu.fromExperimentTypeCodes;						
					}
					if(null != icu.inputProcessCodes && icu.inputProcessCodes.size() > 0){
						icu.processCodes = icu.inputProcessCodes;					
					}
					icu.fromExperimentTypeCodes = null;
					icu.inputProcessCodes = null;					
				});
			});
			
			MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, e) ;
		});
		//5 move Experiment fromTransformationTypeCodes
		MongoDBResult<ExperimentOld> results3 = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, ExperimentOld.class, DBQuery.exists("atomicTransfertMethods.inputContainerUseds.fromExperimentTypeCodes"));
		results3.toList().forEach(e -> {
			e.atomicTransfertMethods.forEach(atm -> {
				atm.inputContainerUseds.forEach(icu -> {
					
					if(null != icu.fromExperimentTypeCodes && icu.fromExperimentTypeCodes.size() > 0){
						icu.fromTransformationTypeCodes = icu.fromExperimentTypeCodes;						
					}
					if(null != icu.inputProcessCodes && icu.inputProcessCodes.size() > 0){
						icu.processCodes = icu.inputProcessCodes;					
					}
					
					
					icu.fromExperimentTypeCodes = null;
					icu.inputProcessCodes = null;
				});
			});
			
			MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, e) ;
		});
		
		//6 migration on processes
		MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.exists("sampleOnInputContainer.mesuredQuantity"),
				DBUpdate.rename("sampleOnInputContainer.mesuredQuantity", "sampleOnInputContainer.containerQuantity"));
		
		MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.exists("sampleOnInputContainer.mesuredVolume"),
				DBUpdate.rename("sampleOnInputContainer.mesuredVolume", "sampleOnInputContainer.containerVolume"));
		
		
		MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.exists("sampleOnInputContainer.mesuredConcentration"),
				DBUpdate.rename("sampleOnInputContainer.mesuredConcentration", "sampleOnInputContainer.containerConcentration"));
		
		MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.exists("containerInputCode"),
				DBUpdate.rename("containerInputCode", "inputContainerCode"));
		
		MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.exists("newContainerSupportCodes"),
				DBUpdate.rename("newContainerSupportCodes", "outputContainerSupportCodes"));
		
		/*
		   Query control Container
			{$or :[
			{"calculedVolume":{$exists:1}},
			{"mesuredQuantity":{$exists:1}},
			{"mesuredVolume":{$exists:1}},
			{"mesuredConcentration":{$exists:1}},
			{"inputProcessCodes":{$exists:1}},
			{"fromExperimentTypeCodes":{$exists:1}},
			{"processTypeCode":{$exists:1}}
			]}
		  
		   Query control Support
			{$or :[
			
			{"fromExperimentTypeCodes":{$exists:1}}
			]}	
			
		   Query control experiment
			{$or :[
			
			{"atomicTransfertMethods.inputContainerUseds.inputProcessCodes":{$exists:1}}
			{"atomicTransfertMethods.inputContainerUseds.fromExperimentTypeCodes":{$exists:1}}
			
			]}		   
		  Query control Process
		  {$or :[
			{"sampleOnInputContainer.mesuredQuantity":{$exists:1}},
			{"sampleOnInputContainer.mesuredVolume":{$exists:1}},
			{"sampleOnInputContainer.mesuredConcentration":{$exists:1}},
			{"sampleOnInputContainer.containerInputCode":{$exists:1}}
			]}
		 */
		
		return ok("Migration Finish");

		
		//
	}
}
