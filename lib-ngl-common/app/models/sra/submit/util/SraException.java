package models.sra.submit.util;

import models.sra.submit.sra.instance.Submission;
import validation.ContextValidation;

public class SraException extends RuntimeException {
	private static final play.Logger.ALogger logger = play.Logger.of(SraException.class);

	private static final long serialVersionUID = 1L;
	private final String key;

	public SraException() {
		super();
		key = "Error";
	}
	
	public SraException(String message, Throwable cause) {
		super(message, cause);
		key = "Error";
	}
	
	public SraException(String message) {
		super(message);
		key = "Error";
	}
	
	public SraException(String key, String message) {
		super(message);
		this.key = key;
	}
	
	public SraException(Throwable cause) {
		super(cause);
		key = "Error";
	}
	
	public String getKey() {
		return key;
	}
	
	public static void assertSubmissionStateCode(Submission submission, String stateCode) throws SraException {
		if (! submission.state.code.equals(stateCode)) {
			String message = "La soumission " + submission.code + " est invalide, "
			    + " etat attendu = " + stateCode 
				+ " ,etat de la soumission = " + submission.state.code;
			throw new SraException (message);
		}		
	}

//	
//	public static <T  extends DBObject & IStateReference> void assertObjectStateCode (T t, String stateCode) throws SraException {
//		if (! t.getState().code.equals(stateCode)) {
//			String message = "Le code de l'objet " + t.getState().code + " est invalide, "
//			    + " etat attendu = " + stateCode 
//				+ " ,etat de l'objet = " + t.getState().code;
//			throw new SraException (message);
//		}		
//	}
	


	public static void assertNoError(ContextValidation ctx) throws SraValidationException {
		if (ctx.hasErrors()) {
			ctx.displayErrors(logger, "debug");
			throw new SraValidationException(ctx);
		}
	}
	
}