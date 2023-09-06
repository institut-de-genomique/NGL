package sra.scripts.off;


import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.Submission;


/*
* Script pour corriger les codes des experiments et runs des soumissions BMR et BEZ pour lesquelles il y a deja eu une soumission à l'EBI
* puis deletion qui doivent etre soumis sous un nouveau nom.

* Exemple de lancement :
* http://localhost:9000/sra/scripts/run/sra.scripts.Debug_BMR_BEZ
* @author sgas
*
*/
public class Debug_BMR_BEZ extends ScriptNoArgs {
//	private static final play.Logger.ALogger logger = play.Logger.of(Debug_BDA.class);

	private final SubmissionAPI     submissionAPI;
	private final ExperimentAPI     experimentAPI;
//	private final NGLApplication    app;

	@Inject
	public Debug_BMR_BEZ(SubmissionAPI     submissionAPI,
					 ExperimentAPI     experimentAPI,
					 NGLApplication    app) {

		this.submissionAPI     = submissionAPI;
		this.experimentAPI     = experimentAPI;
//		this.app               = app;
	}



	@Override
	public void execute() throws Exception {
		List <String> codesSubmission = new ArrayList<>();
		codesSubmission.add("GSC_BMR_49PH4KIHP");
		codesSubmission.add("GSC_BEZ_49PH4O5MW");
		for (String codeSub : codesSubmission) {
			List <String> newExperimentCodes = new ArrayList<>();
			List <String> newRunCodes = new ArrayList<>();
			Submission submission = submissionAPI.dao_getObject(codeSub);
			for (String expCode : submission.experimentCodes) {
				Experiment experiment = experimentAPI.get(expCode);
				String newExperimentCode = "replace_" + experiment.code;
				experiment.code = newExperimentCode;
				experiment.run.expCode = newExperimentCode;
				String newRunCode = "replace_" + experiment.run.code;
				experiment.run.code = newRunCode;
				newExperimentCodes.add(newExperimentCode);
				newRunCodes.add(newRunCode);
				experimentAPI.dao_saveObject(experiment);
			}
			submission.experimentCodes = newExperimentCodes;
			submission.runCodes = newRunCodes;
			String newSubmissionCode = "replace_" + codeSub;
			submission.code = newSubmissionCode;
			submissionAPI.dao_saveObject(submission);
		}
	}



}
