package controllers.migration;

import java.util.ArrayList;
import java.util.List;

import models.laboratory.experiment.instance.Experiment;
import models.utils.InstanceConstants;
import models.utils.instance.SampleHelper;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;
import org.mongojack.JacksonDBCollection;

import play.Logger;
import play.Logger.ALogger;
import play.mvc.Result;
import validation.ContextValidation;
import validation.processes.instance.ProcessValidationHelper;
import controllers.CommonController;
import controllers.migration.models.ProcessOld;
import fr.cea.ig.MongoDBDAO;
import models.laboratory.processes.instance.Process;

public class MigrationProcessSample extends CommonController {
	
	private static final String PROCESS_COLL_NAME_BCK = InstanceConstants.PROCESS_COLL_NAME+"_BCK";

	static ALogger logger=Logger.of("MigrationProcessSample");

	public static Result migration(){

		Logger.info("Start point of Migration Process");

		JacksonDBCollection<ProcessOld, String> containersCollBck = MongoDBDAO.getCollection(PROCESS_COLL_NAME_BCK, ProcessOld.class);
		if(containersCollBck.count() == 0){
		
			Logger.info("Migration Process start");
			
			backupContainerCollection();
			
			List<ProcessOld> oldProcesses = MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, ProcessOld.class).toList();
			Logger.debug("migre "+oldProcesses.size()+" process");
			for (ProcessOld process : oldProcesses) {
				migrationProcess(process);
			}
			
			//TO VALID
			migrationProcessExperimentWithNewSample();
			
			Logger.info("Migration process end");

		}else{
			Logger.info("Migration Process already execute !");
		}
		Logger.info("Migration Process finish");
		return ok("Migration Process Finish");
	}

	private static void migrationProcessExperimentWithNewSample() {
		List<String> experimentTypeCodes=new ArrayList<String>();
		experimentTypeCodes.add("tag-pcr");
		experimentTypeCodes.add("dna-rna-extraction");
		List<Experiment> experiments=MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.in("typeCode",experimentTypeCodes)).toList();
	
		Logger.debug("Nb Experiments "+experiments.size());
		experiments.stream().forEach(e->{
			Logger.debug("Experiment "+e.code +" de type "+e.typeCode);
			e.atomicTransfertMethods.stream().forEach(a->{
				a.outputContainerUseds.stream().forEach(container->{

					List<Process> processes=MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class, 
							DBQuery.or(DBQuery.in("outputContainerSupportCodes",a.inputContainerUseds.get(0).locationOnContainerSupport.code).in("sampleCodes", a.inputContainerUseds.get(0).contents.get(0).sampleCode),DBQuery.is("inputContainerCode",a.inputContainerUseds.get(0).code)).in("experimentCodes", e.code).in("outputContainerSupportCodes", container.locationOnContainerSupport.code)).toList();				
					if(processes.size()==0){
						Logger.error("No process for input container "+a.inputContainerUseds.get(0).code+", sample code "+a.inputContainerUseds.get(0).contents.get(0).sampleCode);
					}else {

						Logger.debug("OutputContainer "+container.code);										
						for(Process p:processes){
							Logger.debug("Process "+p.code+"  new sampleCode "+container.contents.get(0).sampleCode+", new project code"+container.contents.get(0).projectCode);
							p.sampleCodes.add(container.contents.get(0).sampleCode);
							p.projectCodes.add(container.contents.get(0).projectCode);
							MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class,DBQuery.is("code", p.code),DBUpdate.set("projectCodes", p.projectCodes).set("sampleCodes", p.sampleCodes));
						}
					}
				});
				
			});
		});
	}

	private static void migrationProcess(ProcessOld process) {
		
		process.sampleCodes=SampleHelper.getSampleParent(process.sampleCode);
		process.projectCodes=SampleHelper.getProjectParent(process.sampleCodes);
		
		process.sampleCode=null;
		process.projectCode=null;
		
		ContextValidation contextValidation=new ContextValidation("migration");
		ProcessValidationHelper.validateProjectCodes(process.projectCodes, contextValidation);
		ProcessValidationHelper.validateSampleCodes(process.sampleCodes, contextValidation);
		
		if(!contextValidation.hasErrors()){
			MongoDBDAO.save(InstanceConstants.PROCESS_COLL_NAME, process);
		}else {
			
			contextValidation.displayErrors(logger);
			Logger.error("ERROR VALIDATION "+process.code);
		}
		
	}

	private static void backupContainerCollection() {
		Logger.info("\tCopie "+InstanceConstants.PROCESS_COLL_NAME+" start");
		MongoDBDAO.save(PROCESS_COLL_NAME_BCK, MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, ProcessOld.class).toList());
		Logger.info("\tCopie "+InstanceConstants.PROCESS_COLL_NAME+" end");
	}

}
