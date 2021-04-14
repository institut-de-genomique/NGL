package sra.scripts.off;
//package sra.scripts;
//
//import javax.inject.Inject;
//
//import org.apache.commons.lang3.StringUtils;
//
//import fr.cea.ig.lfw.controllers.scripts.ScriptNoArgs;
//import fr.cea.ig.ngl.NGLApplication;
//import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
//import fr.cea.ig.ngl.dao.sra.ExperimentDAO;
//import models.laboratory.common.instance.TraceInformation;
//import models.sra.submit.sra.instance.Experiment;
//import play.libs.Json;
//import validation.ContextValidation;
//
//public class DebugExperimentFor454 extends ScriptNoArgs {
//	private final ExperimentAPI    experimentAPI;
//	private final ExperimentDAO    experimentDAO;
//	private final NGLApplication   app;
//
//	@Inject 
//	public DebugExperimentFor454(ExperimentAPI     experimentAPI,
//								 ExperimentDAO     experimentDAO,
//								 NGLApplication     app) {
//		this.experimentAPI    = experimentAPI;
//		this.experimentDAO    = experimentDAO;
//		this.app              = app;
//	}
//	
//	@Override
//	public void execute() throws Exception {
//		for (Experiment experiment : experimentAPI.dao_all()) {
//			String adminComment = "";
//			String stateCode = "F-SUB";
//			String stateUser = "william";
//            TraceInformation traceInformation = new TraceInformation(stateUser);
//            boolean isSauvegarde = false;
//            
//            if ((experiment.state.code == null || StringUtils.isBlank(experiment.state.code))
//            		&& StringUtils.isNotBlank(experiment.accession)) {
//            	printfln("correction stateCode pour experiment.code=%s", experiment.code);	
//            	isSauvegarde = true;
//            	experiment.state.code = stateCode;
//            }
//            if ((experiment.state.code == null || StringUtils.isBlank(experiment.state.user))
//            		&& StringUtils.isNotBlank(experiment.accession)) {
//            	printfln("correction stateUser pour experiment.code=%s", experiment.code);				
//            	isSauvegarde = true;
//            	experiment.state.user = stateUser;
//            }
//            if (experiment.traceInformation == null
//            		&& (experiment.accession != null || StringUtils.isNotBlank(experiment.accession))) {
//            	printfln("correction traceInformation pour experiment.code=%s", experiment.code);				
//            	experiment.traceInformation = traceInformation;
//            	if (StringUtils.isNotBlank(experiment.adminComment)) {
//            		printfln("correction adminComment pour experiment.code=%s", experiment.code);				
//                	isSauvegarde = true;
//          		experiment.adminComment = adminComment;
//            	}
//            }
//            if (experiment.title != null && experiment.title.startsWith("alias")) {
//            	String title = experiment.title.replace("alias=\"", "");
//            	title = title.replace("\"", "");
//            	isSauvegarde = true;
//            	experiment.title = title;
//            	printfln("correction title (new title = %s) pour experiment.code=%s", experiment.title, experiment.code);				
//            }
//            ContextValidation contextValidation = ContextValidation.createUpdateContext(stateUser); // on veut updater l'experiment sans run pour son run
//            experiment.validate(contextValidation);
//            if (!contextValidation.hasErrors() && isSauvegarde) {
//                printfln("sauvegarde de l' experiment.code=%s", experiment.code);
//                //experimentDAO.update(experiment);
//            }
//
//		}		
//	}
//
//}
