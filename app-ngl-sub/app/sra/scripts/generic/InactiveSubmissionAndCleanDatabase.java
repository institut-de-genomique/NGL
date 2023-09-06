package sra.scripts.generic;



import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithArgs;
import fr.cea.ig.ngl.dao.sra.SubmissionDAO;
import javax.inject.Inject;

import models.sra.submit.sra.instance.Submission;
import sra.api.submission.SubmissionNewAPITools;

/*
 * Ne marche pas  !!!!!
* Script pour inactiver une soumission dans la base lorsque la suppression des AC a ete realisée par l'EBI.
* Laisse la soumission dans l'etat F-SUB. 
* Remplace les AC de la soumission, des experiments, et runs par suppressed_AC et rend les readsets
* de nouveaux soumettables.
* Declenche une erreur si la soumission contient des study ou samples.
* 
* Exemple de lancement :
* http://localhost:9000/sra/scripts/run/sra.scripts.generic.InactiveSubmissionAndCleanDatabase?submissionCode=code_soumission_1
* @author sgas
*
*/

//utiliser directement la methode SubmissionNewAPITools.rollbackSubmission recopiée ici pour pouvoir etre utilise sur prod pas encore dans nouveau workflow.
public class InactiveSubmissionAndCleanDatabase extends ScriptWithArgs<InactiveSubmissionAndCleanDatabase.MyParam> {
	private final SubmissionDAO         submissionDAO;
	private final SubmissionNewAPITools submissionNewAPITools;
	
	@Inject
	public InactiveSubmissionAndCleanDatabase(SubmissionDAO       submissionDAO,
					  						SubmissionNewAPITools   submissionNewAPITools) {
		this.submissionDAO     = submissionDAO;
		this.submissionNewAPITools = submissionNewAPITools;
	}

	// ma structure de controle et stockage des arguments de l'url
	public static class MyParam {
		public String submissionCode;
	}

	@Override
	public void execute(MyParam args) throws Exception {
		println("Demande de deletion de la soumission %s" , args.submissionCode);
		Submission submission = submissionDAO.getObject(args.submissionCode);
		if (submission != null) {
			println("Demande d'inactivation de la soumission %s" , submission.code);
		}
		String user  = "ngsrg";
		submissionNewAPITools.inactiveCreationSubmissionWithoutSampleNorStudy(submission, user);
	}

}
