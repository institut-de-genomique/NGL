package scripts.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tools {
	private static final play.Logger.ALogger logger = play.Logger.of(Tools.class);

	// Renvoie une copie de la chaine d'entree debarrassee des espaces et guillemets en debut et fin de chaine
	public static String clean(String chaine) {
		String cleanChaine = chaine;

		String pattern = "^\\s*\"*\\s*([^\\s\"]+.*)"; // si chaine ne contient aucun caractere visible
		java.util.regex.Pattern p = Pattern.compile(pattern);		


		String pattern2 = "(.*[^\\s\"]+)\\s*\"*\\s*$";  // pour supprimer les espaces en debut de chaine
		java.util.regex.Pattern p2 = Pattern.compile(pattern2);		

		Matcher m = p.matcher(cleanChaine);

		if ( m.find() ) { 
			//log.debug("$1="+ms.group(1));
			cleanChaine = m.group(1);
			//log.debug("ok m1, cleanChaine='"+cleanChaine+"'");
		} 

		Matcher m2 = p2.matcher(cleanChaine);
		//log.debug("cleanChaine d'entree = '"+cleanChaine+"'");
		if ( m2.find() ) { 
			//log.debug("$1="+ms.group(1));
			cleanChaine = m2.group(1);
			//log.debug("ok m2, cleanChaine='"+cleanChaine+"'");
		}

		return cleanChaine;	
	}
	
	public List<String> parseFileCodes (File fileCodes) throws Exception {
		logger.debug("toto fileCodes=" + fileCodes);
		List <String> userCodes = new ArrayList<String>();
		
		if ( ! fileCodes.exists()) {
			return userCodes;
		} 
		String ligne = "";
		String pattern_string = "\\s*(\\S+)\\s*";
		String pattern_string_c = "([^#]*)#";
		Pattern p_c = Pattern.compile(pattern_string_c);

		Pattern p = Pattern.compile(pattern_string);
		try (BufferedReader input_buffer = new BufferedReader(new FileReader(fileCodes))) {
			while ((ligne = input_buffer.readLine()) != null) {	
				// ignorer ce qui suit le signe de commentaire
				Matcher m_c = p_c.matcher(ligne);
				if (!m_c.find()) {
				} else {
					ligne = m_c.group(1);
				}
				// ignorer lignes sans caracteres visibles
				if (ligne.matches("^\\s*$")){
					continue;
				}

				Matcher m = p.matcher(ligne);
				if (!m.find()) {
					throw new Exception("Probleme de format avec la ligne : '" + ligne +"'");
				}
				String code = m.group(1); //Code
				userCodes.add(code);
			} 
		} catch (IOException e) {
			throw new Exception("Probleme lors du chargement du fichier ", e);
		}

		return userCodes;
	}


	

}
