package sra.scripts.off;


import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
//import org.mongojack.DBQuery;

import org.mongojack.DBQuery;

import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.Submission;


//http://localhost:9000/sra/scripts/run/sra.scripts.Delete_runs_tara



public class Delete_runs_tara extends ScriptNoArgs {
	private final SubmissionAPI     submissionAPI;
	private final ExperimentAPI     experimentAPI;
//	private final NGLApplication    app;

	@Inject
	public Delete_runs_tara(SubmissionAPI  submissionAPI,
					 		ExperimentAPI  experimentAPI,
					 		NGLApplication app) {
		this.submissionAPI     = submissionAPI;
		this.experimentAPI     = experimentAPI;
//		this.app               = app;
	}



	@Override
	public void execute() throws Exception {
		
	    List<String> runAccessions = new ArrayList<>();
	    runAccessions.add("ERR562464");
	    runAccessions.add("ERR562455");
	    runAccessions.add("ERR562658");
	    runAccessions.add("ERR562698");
	    runAccessions.add("ERR562709");
	    runAccessions.add("ERR562496");
	    runAccessions.add("ERR562375");
	    
	    List <String> runCodes = new ArrayList<String>();
		for (String  runAccession : runAccessions) {
			
			List<Experiment> experiments = experimentAPI.dao_findAsList(DBQuery.in("run.accession", runAccession));
			println("Nombre d'experiments avec runAccession " + runAccession + " = " + experiments.size());
			for(Experiment experiment : experiments) {
				if (! runCodes.contains(experiment.run.code)) {
					runCodes.add(experiment.run.code);
				}
				experiment.run.accession = "suppressed_" + experiment.run.accession;
				experiment.run.code = "suppressed_" + experiment.run.code;
				experimentAPI.dao_saveObject(experiment);
				printfln("ok pour sauvegarde de la l'experiment=%s", experiment.code +
						" avec run.accession " + experiment.run.accession + " et run.code = "+ experiment.run.code);	

			} // end for experiments
		} // end for runAccessions
		
		for(String runCode : runCodes) {
			List <Submission> submissions = submissionAPI.dao_find(DBQuery.in("runCodes", runCode)).toList();
			println("Nombre de soumission avec runCode " + runCode + " = " + submissions.size());
			for (Submission submission : submissions) {
				List <String> submissionRunCodes = new ArrayList<String>();
				
				println("Soumission " + submission.code + " avec runCode "+ runCode);
				for (String submissionRunCode : submission.runCodes) {
					if (submissionRunCode.equalsIgnoreCase(runCode)) {
						println("submission " + submission.code + " avec runCode " + runCode);
						submissionRunCodes.add("suppressed_" + runCode);
					} else {
						submissionRunCodes.add(runCode);
					}
				}
				submission.runCodes = submissionRunCodes;
				submissionAPI.dao_saveObject(submission);
				printfln("ok pour sauvegarde de la soumission=%s", submission.code);				
			}// end for submissions

		}// end for runCodes

	}

}
