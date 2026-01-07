package services.sra;

import java.util.List;
import java.util.Map;

import fr.cea.ig.DBObject;
import models.sra.submit.sra.instance.IStateReference;
import play.Logger;
import play.data.validation.ValidationError;
import validation.ContextValidation;

/**
 * Attention 
 * e.getMessages() est null pour cette exception, utiliser seulement e.getContext().getErrors() pour récuperer message d'erreur
 * @param ctx context de validation
 */
public class SraValidationException extends RuntimeException{
	
	
	public String getMessage() {
		return super.getMessage();
	}
	
//	public class SraValidationException extends SraException {
	private static final long serialVersionUID = 1L;
	private static final play.Logger.ALogger logger = play.Logger.of(SraValidationException.class);

	public final ContextValidation ctx;

	public SraValidationException(ContextValidation ctx) {
		// construction du message d'erreur (recuperer depuis ctx) dans le constructeur parent
		// possible de faire ca dans constructeur car methode message declaré en static
		super(message(ctx));
		this.ctx = ctx; 
		// affichage en error sur la console => envoie d'un seul mail
		// NGL-5286 : limiter envoie mail d'erreurs car envoyé à l'utilisateur via controller :
		logger.debug("SraValidationException : " +  message(ctx));
	}
	
    /**
     * Met dans l'exception le message d'erreur correspondant à l'ensemble des erreurs du context de validation
     * @param   ctx  contexte de validation
     * @return  message message 
     */
	public static String message(ContextValidation ctx) {
		StringBuilder messages = new StringBuilder();
		for (Map.Entry<String,List<ValidationError>> e : ctx.getErrors().entrySet()) {
			for (ValidationError validationError : e.getValue()) {
				for (String message : validationError.messages()) {
					messages.append(e.getKey()).append(" : ").append(message).append("\n");
				}
			}
		}
		return messages.toString();
	}

	public ContextValidation getContext() {
		return ctx;
	}
	
	
	public static <T  extends DBObject & IStateReference> void _assertObjectStateCode (T t, String stateCode, ContextValidation contextValidation)  {
		if (! t.getState().code.equals(stateCode)) {
			String message = "L'objet " + t.getState().code + " est invalide, "
			    + " etat attendu = " + stateCode 
				+ " ,etat de l'objet = " + t.getState().code;
			contextValidation.addError("assertSubmissionStateCode : ", message);
		}		
	}	

}
