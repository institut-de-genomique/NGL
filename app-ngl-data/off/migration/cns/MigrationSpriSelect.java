package controllers.migration.cns;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.container.instance.tree.ParentContainers;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.OneToManyContainer;
import models.laboratory.experiment.instance.OutputContainerUsed;
import models.laboratory.processes.instance.Process;
import models.utils.InstanceConstants;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import play.Logger;
import play.Logger.ALogger;
import play.mvc.Result;
import fr.cea.ig.MongoDBDAO;

public class MigrationSpriSelect extends MigrationExperimentProperties{

	protected static ALogger logger=Logger.of("MigrationSpriSelect");

	public static Result migration() {
				migrationSpriSelect();
				return ok("Migration Spri Select Finish");
	}
	
	private static void migrationSpriSelect() {

		List<Container> containers=MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.is("contents.properties.expectedSize.value", "ss0.6/0.53").in("fromTransformationTypeCodes","sizing")).toList();

		Logger.debug("Nb container ss to update "+containers.size());
		
		Set<String> experimentCodes=new HashSet<String>();

		//Update Container
		for(Container c:containers){
			experimentCodes.addAll(c.fromTransformationCodes);
			if(c.fromTransformationTypeCodes.contains("sizing") && c.fromTransformationTypeCodes.size()==1 ){
				String newCode=c.fromTransformationCodes.iterator().next().replace("SIZING", "SPRI-SELECT");
				c.fromTransformationCodes.clear();
				c.fromTransformationCodes.add(newCode);
				c.fromTransformationTypeCodes.clear();
				c.fromTransformationTypeCodes.add("spri-select");
				//issu de l'experience
				if(c.fromPurificationCode==null && c.fromTransfertCode==null){
					c.treeOfLife.from.experimentCode=newCode;
					c.treeOfLife.from.experimentTypeCode="spri-select";
					//issu de normalisation ou purification
				}else {
					for(ParentContainers pc: c.treeOfLife.from.containers){
						pc.fromTransformationCodes.clear();
						pc.fromTransformationTypeCodes.clear();
						pc.fromTransformationCodes.add(newCode);
						pc.fromTransformationTypeCodes.add("spri-select");
					}
			
				}
				Logger.debug("Container "+c.code+" udpate");
				MongoDBDAO.save(InstanceConstants.CONTAINER_COLL_NAME,c);
				MongoDBDAO.update(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME,ContainerSupport.class,DBQuery.is("code", c.support.code),DBUpdate.set("fromTransformationTypeCodes", c.fromTransformationTypeCodes));
				
			}else {
				logger.error("Particular container "+c.code+" typeCodes"+c.fromTransformationTypeCodes+ ", codes "+c.fromTransformationCodes);
			}
		}
		
		Logger.debug("nb experiments "+experimentCodes.size());
		//Update Experiment and Process
		experimentCodes.forEach(exp->{	
			Experiment experiment=MongoDBDAO.findByCode(InstanceConstants.EXPERIMENT_COLL_NAME,Experiment.class,exp);
			
			if(experiment!=null){
				String newCode=exp.replace("SIZING", "SPRI-SELECT");
				Logger.debug("Experiment "+exp+" replace by new code "+newCode);

				//Update libProcessTypeCode in output container life and readSet
				String newKeyProperty="libProcessTypeCode";
				PropertyValue propValue = new PropertySingleValue("DC");

				MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.is("code",exp),DBUpdate.set("typeCode","spri-select").set("code", newCode).set("protocolCode","spri_select"));
				
				List<Process> processes=MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class,DBQuery.in("experimentCodes",exp)).toList();
				Logger.debug("Nb Processus "+processes.size());
				List<String> processCodes=processes.stream().map(p->p.code).collect(Collectors.toList());
				
				MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.in("code",processCodes),DBUpdate.push("experimentCodes", newCode).set("properties."+newKeyProperty, propValue),true);
				MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.in("code",processCodes),DBUpdate.pull("experimentCodes", exp),true);
				
				MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.in("code",processCodes).is("typeCode", "metagenomic-process-with-sizing"),DBUpdate.set("typeCode","metagenomic-process-with-spri-select"),true);
				MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.in("code",processCodes).is("typeCode", "sizing-stk-illumina-depot"),DBUpdate.set("typeCode","spri-select-stk-illumina-depot"),true);
				MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.in("code",processCodes).is("typeCode", "ampli-sizing-stk-illumina-depot"),DBUpdate.set("typeCode","ampli-spri-select-stk-illumina-depot"),true);
				
				//TODO update inputProcessTypeCodes in Experiment				
				MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.in("inputProcessCodes",processCodes).in("inputProcessTypeCodes", "metagenomic-process-with-sizing"),DBUpdate.push("inputProcessTypeCodes","metagenomic-process-with-spri-select"),true);
				MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.in("inputProcessCodes",processCodes).in("inputProcessTypeCodes", "sizing-stk-illumina-depot"),DBUpdate.push("inputProcessTypeCodes","spri-select-stk-illumina-depot"),true);
				MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.in("inputProcessCodes",processCodes).in("inputProcessTypeCodes", "ampli-sizing-stk-illumina-depot"),DBUpdate.push("inputProcessTypeCodes","ampli-spri-select-stk-illumina-depot"),true);
				
				MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.in("inputProcessCodes",processCodes).in("inputProcessTypeCodes", "metagenomic-process-with-spri-select"),DBUpdate.pull("inputProcessTypeCodes","metagenomic-process-with-sizing"),true);
				MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.in("inputProcessCodes",processCodes).in("inputProcessTypeCodes", "spri-select-stk-illumina-depot"),DBUpdate.pull("inputProcessTypeCodes","sizing-stk-illumina-depot"),true);
				MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.in("inputProcessCodes",processCodes).in("inputProcessTypeCodes", "ampli-spri-select-stk-illumina-depot"),DBUpdate.pull("inputProcessTypeCodes","ampli-sizing-stk-illumina-depot"),true);
				
				Set<String> inputContainers=new HashSet<String>();
				
				//Update Experiment properties libProcessTypeCode if key comes from Process
				List<String> processCodesUpdateExperiment=new ArrayList<String>();
				for(Process p:processes){
					//Search first experiment in process
					if(p.properties.containsKey(newKeyProperty)){
						processCodesUpdateExperiment.add(p.code);
						inputContainers.add(p.inputContainerCode);
					}

				}
				
				List<Experiment> firstExperimentProcess=MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class,DBQuery.in("inputProcessCodes",processCodesUpdateExperiment).in("inputContainerCodes", inputContainers)).toList();

				for(Experiment e:firstExperimentProcess){
					Logger.debug("Experiment first process "+e.code);
					
					e.atomicTransfertMethods.stream().forEach(atm->{
						
						//Get inputContainer
						OutputContainerUsed output = atm.outputContainerUseds.iterator().next();
						
						Logger.debug("Exp atm");
						//Get sampleCode and tag
						String sampleCode = output.contents.iterator().next().sampleCode;
						
						String tag = null;
						try{
							tag=(String) output.contents.iterator().next().properties.get("tag").getValue();
						} catch(Exception ex){
						
						}
						
						Logger.debug("Sample code "+sampleCode+" Tag "+tag+" property "+newKeyProperty+ " value "+propValue.value);
						updateOutputContainerTreeOfLife(output, sampleCode, tag, newKeyProperty, propValue, false);
						
					});
				}
				
			}else {
				logger.error("Experiment "+exp+" not exists");
			}
		});
	};
	
	
	
	
}
