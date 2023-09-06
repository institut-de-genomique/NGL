package models.sra.submit.util;

import models.sra.submit.sra.instance.Submission;

public class SubmissionTypeException extends SraException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public SubmissionTypeException(Submission submission) {
		super("Type de soumission non gerée :" + submission.type 
				+ " pour la soumission " + submission.code);
	}
	

}
