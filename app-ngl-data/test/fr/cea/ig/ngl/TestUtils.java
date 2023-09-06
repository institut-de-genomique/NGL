package fr.cea.ig.ngl;

import java.text.SimpleDateFormat;

/**
 * Classe regroupant plusieurs variables aidant aux tests : formatter de date, constantes, ...
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
public final class TestUtils {
    
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
}
