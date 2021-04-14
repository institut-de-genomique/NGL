package sra.scripts.off;
//package sra.scripts;
//
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.io.File;
//
//import models.sra.submit.common.instance.Submission;
//import models.sra.submit.sra.instance.Experiment;
//import models.sra.submit.util.SraCodeHelper;
//import services.XmlServices;
//
//import static ngl.refactoring.state.SRASubmissionStateNames.N;
//import models.laboratory.common.instance.State;
//
//import javax.inject.Inject;
//
//import org.apache.commons.csv.CSVParser;
//import org.apache.commons.csv.CSVRecord;
//
//
//import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
////import fr.cea.ig.ngl.NGLApplication;
//import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
//import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
////import fr.cea.ig.ngl.dao.sra.ExperimentDAO;
//import sra.scripts.utils.CSVParsing;
//import validation.ContextValidation;
//
//public class DebugSubmissionExperiment_BRR extends ScriptNoArgs {
//	private static final play.Logger.ALogger logger = play.Logger.of(DebugSubmissionExperiment_BRR.class);
//
//	private final SubmissionAPI submissionAPI;
//	private final ExperimentAPI    experimentAPI;
////	private final ExperimentDAO    experimentDAO;
////	private final NGLApplication   app;
//	private final SraCodeHelper sraCodeHelper;
//	private final XmlServices xmlServices;
//	 
//	@Inject 
//	public DebugSubmissionExperiment_BRR(ExperimentAPI     experimentAPI,
//										 SubmissionAPI     submissionAPI,
//										 SraCodeHelper     sraCodeHelper,
//										 XmlServices       xmlServices) {
//		this.submissionAPI = submissionAPI;
//		this.experimentAPI = experimentAPI;
//		this.sraCodeHelper = sraCodeHelper;
//		this.xmlServices = xmlServices;
////		this.experimentDAO    = experimentDAO;
////		this.app              = app;
//	}
//	
//
//	
//	
//
//	@Override
//	public void execute() throws Exception {
////		String submissionCode = "GSC_BBD_BEH_BGY_BGZ_BHA_BRR_BRW_BRX_BRY_48RB53TA3";
////		Submission oldsubmission = submissionAPI.get(submissionCode);
////		List<String> experimentCodesOldSubmission = new ArrayList<String>();
////		List<String> experimentCodesNewSubmission = new ArrayList<String>();
////		List<String> runCodesOldSubmission = new ArrayList<String>();
////		List<String> runCodesNewSubmission = new ArrayList<String>();
////		String studyCodeNewSubmission = "STUDY_BRR_BRW_BRX_BRY_4ANA1L3A9";
////		String studyAcNewSubmission = "ERP118081";
////
////		for (String runCode : oldsubmission.runCodes) {
////			String expCode = runCode.replace("run", "exp");
////			println("expCode = " + expCode); 
////			Experiment experiment = experimentAPI.dao_getObject(expCode);
////			println("exp.projectCode= '" + experiment.projectCode +"'"); 
////
////			if (experiment.projectCode.equals("BRR")|| 
////				experiment.projectCode.equals("BRW")||
////				experiment.projectCode.equals("BRX")||
////				experiment.projectCode.equals("BRY")) {
////				experiment.studyCode = studyCodeNewSubmission;
////				experiment.studyAccession = studyAcNewSubmission;
////				experimentCodesNewSubmission.add(expCode);
////				runCodesNewSubmission.add(runCode);
////				experimentAPI.dao_saveObject(experiment);
////			} else {
////				experimentCodesOldSubmission.add(expCode);
////				runCodesOldSubmission.add(runCode);
////			}
////		}
////		
////		// sauver l'ancienne soumission avec la liste d'experiments restreinte:
////		oldsubmission.experimentCodes = experimentCodesOldSubmission;
////		oldsubmission.runCodes = runCodesOldSubmission;
////
////		submissionAPI.dao_saveObject(oldsubmission);
////		List<String> projectCodes = new ArrayList<String>();
////		projectCodes.add("BRR");
////		projectCodes.add("BRW");
////		projectCodes.add("BRX");
////		projectCodes.add("BRY");
////
////
////		// creer la nouvelle soumission avec liste d'experiment enleves à l'ancienne soumission :
////
//////		DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy");	
////		Date courantDate = new java.util.Date();
//////		String st_my_date = dateFormat.format(courantDate);	
////		String user = "william";
////		Submission submission = new Submission(user, projectCodes);
////		submission.code = sraCodeHelper.generateSubmissionCode(projectCodes);
////		submission.creationDate = courantDate;
////		println("submissionCode="+ submission.code);
////		submission.state = new State(N, user);
////		submission.release = false; // soumission toujours en confidentielle et levee de confidentialite du
////		                            // study par l'utilisateur via interface.
////		submission.configCode = oldsubmission.configCode;
////		submission.refStudyCodes.add(studyCodeNewSubmission);
////		ContextValidation contextValidation = ContextValidation.createCreationContext(user);
////		submission.studyCode = studyCodeNewSubmission;
////		submission.experimentCodes = experimentCodesNewSubmission;
////		submission.runCodes = runCodesNewSubmission;
////
////		submission.validate(contextValidation);
////
////		if (! contextValidation.hasErrors()) {
////			submission.studyCode = null;
////
////			submissionAPI.dao_saveObject(submission);
////		} else {
////			contextValidation.displayErrors(logger);
////		}
//		
//		
//		
//		
//		
//		
//		
//		
//		
//		
//		String submissionCode = "GSC_BRR_BRW_BRX_BRY_4ASE2QK1N";
//		String resultDirectory = "/env/cns/home/sgas/GSC_BRR_BRW_BRX_BRY_4ASE2QK1N";
//		xmlServices.writeAllXml(submissionCode, resultDirectory);
//
//	}
//
//}
