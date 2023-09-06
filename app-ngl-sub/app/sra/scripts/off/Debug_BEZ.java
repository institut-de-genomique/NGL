package sra.scripts.off;


import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
//import org.mongojack.DBQuery;

//import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithArgs;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import models.sra.submit.sra.instance.Submission;



public class Debug_BEZ extends ScriptWithArgs<Debug_BEZ.MyParam> {
//	private static final play.Logger.ALogger logger = play.Logger.of(Debug_BDA.class);

	private final SubmissionAPI     submissionAPI;
	private final ExperimentAPI     experimentAPI;
//	private final NGLApplication    app;

	@Inject
	public Debug_BEZ(SubmissionAPI     submissionAPI,
					 ExperimentAPI     experimentAPI,
					 NGLApplication    app) {

		this.submissionAPI     = submissionAPI;
		this.experimentAPI     = experimentAPI;
//		this.app               = app;
	}


	// ma structure de controle et stockage des arguments de l'url
	public static class MyParam {
		public String submissionCode;
	}


	@Override
	public void execute(MyParam args) throws Exception {
		String submissionCode = "suppressed_CNS_BEZ_21_10_2016";
		Submission submission = submissionAPI.dao_getObject(submissionCode);
		
	    List<String> experimentCodes = new ArrayList<>();
	    List<String> runCodes = new ArrayList<>();
	    
		for (String expCode : submission.experimentCodes) {
			expCode=expCode.replace("suppressed_","");
			println("experimentCode%s", expCode);
			
			if (!experimentCodes.contains(expCode)) {
				experimentCodes.add(expCode);
			}
		}
			
		for (String runCode : submission.runCodes) {
			runCode=runCode.replace("suppressed_","");
			if (!runCodes.contains(runCode)) {
				runCodes.add(runCode);
			}	
		}
		
		String submissionAccession = submission.accession;
		
		submission.accession= submissionAccession.replace("suppressed","");
		submission.experimentCodes = experimentCodes;
		submission.runCodes = runCodes;
		println("ok pour sauvegarde de la soumission=%s", submission.code);				

		submissionAPI.dao_saveObject(submission);

	}
	

}
