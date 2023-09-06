package fr.cea.ig.ngl.utils;

import java.text.SimpleDateFormat;
import java.util.List;

import validation.ContextValidation;

/**
 * Classe regroupant plusieurs variables aidant aux tests : formatter de date, constantes, ...
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
public final class TestUtils {
	
	private TestUtils() {}
    
    /**
	 * Formatteur de date avec le format "dd/MM/yyyy".
	 */
	public static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy");
	
	/**
	 * Formatteur de date avec le format "yyMMdd".
	 */
	public static final SimpleDateFormat SDF2 = new SimpleDateFormat("yyMMdd");

	/**
	 * Constante définissant une taille initiale de liste.
	 */
	public static final int LIST_SIZE = 5;

	/**
	 * Utilisateur utilisé pour les tests d'NGL.
	 */
	public static final String CURRENT_USER = "jcharpen";

	/**
	 * Mail utilisé pour les tests d'NGL.
	 */
	public static final String MAIL_TEST = "jcharpen@genoscope.cns.fr";

	/**
	 * Serveur SMTP utilisé pour les tests d'NGL.
	 */
	public static final String SMTP_TEST = "smtp.genoscope.cns.fr";
	
	/**
	 * La méthode errorCount() du ContextValidation n'est pas exacte car elle renvoie le nombre de clefs.</br>
	 * Dans le cas de plusieurs erreurs utilisant la même clef elle ne les comptabilisera pas.</br>
	 * Cette méthode renvoie la somme des erreurs pour toutes les clefs.</br>
	 * 
	 * @param contextValidation validation context
	 * @return true error count
	 */
	public static final int trueErrorCount(ContextValidation contextValidation) {
		return contextValidation.getErrors().values().stream().mapToInt(List::size).sum();
	}
}
