package sra.scripts.generic;



import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithArgs;
import fr.cea.ig.ngl.dao.sra.SubmissionDAO;
import models.sra.submit.sra.instance.Submission;

import javax.inject.Inject;

import sra.api.submission.SubmissionNewAPITools;

/*
* Script pour deleter une soumission dans l'etat 'nouveau' ou 'valide' dans la base enlevant les samples et experiments si non utilises par ailleurs et 
* en remettant les readset au status NONE
* Exemple de lancement :
* http://localhost:9000/sra/scripts/run/sra.scripts.generic.DeleteSubmissionAndCleanDatabase?submissionCode=code_soumission_1
* @author sgas
*
*/
//SGAS
//utiliser directement la methode SubmissionNewAPITools.rollbackSubmission recopiée ici pour pouvoir etre utilise sur prod pas encore dans nouveau workflow.
public class DeleteSubmissionAndCleanDatabase extends ScriptWithArgs<DeleteSubmissionAndCleanDatabase.MyParam> {
	private final SubmissionDAO         submissionDAO;
	private final SubmissionNewAPITools submissionNewAPITools;
	
	@Inject
	public DeleteSubmissionAndCleanDatabase(SubmissionDAO       submissionDAO,
					  						SubmissionNewAPITools   submissionNewAPITools) {
		this.submissionDAO         = submissionDAO;
		this.submissionNewAPITools = submissionNewAPITools;
	}

	// ma structure de controle et stockage des arguments de l'url
	public static class MyParam {
		public String submissionCode;
	}

	@Override
	public void execute(MyParam args) throws Exception {
		Submission submission = submissionDAO.getObject(args.submissionCode);
		if (submission == null) {
			println("submission " + args.submissionCode + " absente de la base");
			return;
		} else {
			println("Demande de deletion de la soumission %s" , submission.code);
		}
		String user  = "ngsrg";
		submissionNewAPITools.rollbackSubmission(submission, user);
		println("Supression de la base de la soumission "+ submission.code);
	}

}
