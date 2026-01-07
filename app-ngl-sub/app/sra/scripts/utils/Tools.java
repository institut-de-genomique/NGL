package sra.scripts.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import play.data.validation.ValidationError;
import validation.ContextValidation;

public class Tools {

	/**
	 * Fait la difference entre 2 collections.
	 * @param plus collection de depart
	 * @param min  collection à soustraire
	 * @return     ensemble (set) correspondant à collection plus - collection min
	 * @param <A> type des elements des ensembles
	 */
	public static <A> Set<A> subtract(Collection<A> plus, Collection<A> min) {
		Set<A> diff = new HashSet<>(plus);
		diff.removeAll(min);
		return diff;
	}

	public static void writeInFile(String pathOutputFile, String message) throws IOException {
		try (BufferedWriter output_buffer = new BufferedWriter(new java.io.FileWriter(new File(pathOutputFile)))) {
			output_buffer.write(message);
		} 
	}
	
	// copie les erreurs du context de validation B vers le context de validation A 
	
	/**
	 * copie les erreurs du context de validation B vers le context de validation A 
	 * @param ctxA context de validation final
	 * @param ctxB context de validation à copier dans final
	 */	public static void addErreurCxtBInCtxA(ContextValidation ctxA, ContextValidation ctxB) {
		if(ctxB.hasErrors()) {
			// Reporter les erreurs dans intermediaireCtx : 
			//String messages = "";
			for (Map.Entry<String,List<ValidationError>> e : ctxB.getErrors().entrySet()) {
				for (ValidationError validationError : e.getValue()) {
					for (String message : validationError.messages()) {
						ctxA.appendPath(e.getKey()).addError("", message);
					}
				}
			}				
		}
	}
	
}
