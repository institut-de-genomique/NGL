package models.sra.submit.util;

import java.util.List;
import java.util.Map;

import com.mysql.jdbc.Messages;

import play.Logger;
import play.Logger.ALogger;
import play.data.validation.ValidationError;
import validation.ContextValidation;

public class SraValidationException extends RuntimeException{
//	public class SraValidationException extends SraException {
	private static final long serialVersionUID = 1L;
	private static final play.Logger.ALogger logger = play.Logger.of(SraValidationException.class);

	public final ContextValidation ctx;
	
	public SraValidationException(ContextValidation ctx) {
		// affichage pour l'utilisateur des erreurs sur console ou dans interface:
		// idem que precedemment sauf en mode debug => pas d'envoie de mail
		ctx.displayErrors(logger, "debug");
		this.ctx = ctx; 
		// construction du message d'erreur à envoyer par mail :
		String messages = "";
		for (Map.Entry<String,List<ValidationError>> e : ctx.getErrors().entrySet()) {
			for (ValidationError validationError : e.getValue()) {
				for (String message : validationError.messages())
					messages = messages + e.getKey() + " : " +  message;
			}
		}
		// affichage en error sur la console => envoie d'un seul mail
		Logger.error("SraValidationException : messages");
	}

	public ContextValidation getContext() {
		return ctx;
	}
}
