package fr.cea.ig.ngl.dao.sra;

import javax.inject.Inject;

import fr.cea.ig.ngl.dao.GenericMongoDAO;
import models.sra.submit.sra.instance.Submission;
import models.sra.submit.util.SraException;
import models.utils.InstanceConstants;

public class SubmissionDAO extends GenericMongoDAO<Submission> {
	
	@Inject
	public SubmissionDAO() {
		super(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class);
	}
		
	
	/**
	 * Met à jour la soumission dans la base pour la trace information, 
	 * l'etat et son historique.
	 * @param submission     soumission à mettre à jour  dans la base pour les champs state et traceInformation
	 */
	public void saveStateAndTrace(Submission submission) {
//		this.update(DBQuery.is("code", submission.code), 
//					DBUpdate.set("state", submission.getState())
//						 	.set("traceInformation", submission.getTraceInformation()));
		DAOTools.saveStateAndTrace(this, submission);
	}	
	
	/**
	 * Retourne la soumission dont le code est indique si elle existe dans la base, 
	 * sinon declenche une {@link SraException}
	 * @param  submissionCode code de la soumission
	 * @return                soumission
	 * @throws SraException   error
	 */
	public Submission getSubmission(String submissionCode) throws SraException {
		Submission submission = this.getObject(submissionCode);
		if (submission == null) {
			throw new SraException("getSubmission", "code '" + submissionCode + "' absent de la base");
		}
		return submission;
	}	
	
}
