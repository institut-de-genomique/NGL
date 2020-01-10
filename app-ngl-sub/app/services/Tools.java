package services;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
//import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.xerces.impl.dv.util.Base64;

import models.sra.submit.util.SraException;

public class Tools {
	
	public List<String> loadReadSet(InputStream inputStream) throws SraException {	
		List<String> listReadSet = new ArrayList<>();
		if (inputStream == null) {
			return listReadSet;
		}
		try {
			BufferedReader input_buffer = new BufferedReader(new InputStreamReader(inputStream));
			String ligne = "";
			String pattern_string = "^\\s*\"*([^\"]+)\\s*\"*";
			Pattern p = Pattern.compile(pattern_string);
			//String pattern_string_c = "([^#]*)#";
			String pattern_string_c = "^\\s*#.*";
			Pattern p_c = Pattern.compile(pattern_string_c);
//			boolean legend = false;	
			while ((ligne = input_buffer.readLine()) != null) {					
				if (this.clean(ligne).equalsIgnoreCase("readSetCode")) {
//					legend = true;
					continue;
				}
				if (this.clean(ligne).equalsIgnoreCase("lotseqname")) {
//					legend = true;
					continue;
				}
				// ignorer ce qui suit le signe de commentaire
				Matcher m_c = p_c.matcher(ligne);
				if (m_c.find()) {
					continue;
				}
				// ignorer lignes sans caracteres visibles
				if (ligne.matches("^\\s*$")){
					continue;
				}
				Matcher m = p.matcher(ligne);
				// Appel de find obligatoire pour pouvoir récupérer $1
				if (!m.find()) {
					throw new SraException("Probleme de format avec la ligne : '" + ligne +"'");
				}
				String readSetCode = m.group(1); //readSetCode
				readSetCode = clean(readSetCode);
				//System.out.println("readSetCode = '"+ readSetCode +"'");
				if (! listReadSet.contains(readSetCode)) {
					listReadSet.add(readSetCode);
				}
			} 
			//log.debug("legend="+legend);
		} catch (IOException e) {
			throw new SraException("Probleme lors du chargement du fichier ", e);
		}
		return listReadSet;
	}
	
/*	public Map<String, String> loadLotSeqName(File fileSelectLotSeqName) throws SraException {
		Map<String, String> mapLotSeqName = new HashMap<String, String>();
		if (fileSelectLotSeqName.exists()) {
			//log.debug("Fichier de selection des lotSeqName existe bien : " + selectLotSeqName);
			BufferedReader input_buffer = null;
			try {
				input_buffer = new BufferedReader(new FileReader(fileSelectLotSeqName));
			} catch (FileNotFoundException e) {
				throw new SraException("Probleme lors du chargement du fichier ", e);
			}
			String ligne = "";
			String pattern_string = "^\\s*\"*([^\"]+)\\s*\"*";
			Pattern p = Pattern.compile(pattern_string);
			String pattern_string_c = "([^#]*)#";
			Pattern p_c = Pattern.compile(pattern_string_c);

			boolean legend = false;
			try {
				while ((ligne = input_buffer.readLine()) != null) {					
					if (this.clean(ligne).equalsIgnoreCase("lotseqname")) {
						continue;
					}
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
					// Appel de find obligatoire pour pouvoir récupérer $1
					if (!m.find()) {
						throw new SraException("Probleme de format avec la ligne : '" + ligne +"'");
					}
					String lotSeqName = m.group(1);
					//log.debug("lotSeqName = "+ lotSeqName);
					mapLotSeqName.put(lotSeqName, "");
				}
				//log.debug("legend="+legend);
			} catch (IOException e) {
				throw new SraException("Probleme lors du chargement du fichier ", e);
			}
		}		
		return mapLotSeqName;
	}
*/
	public Map<String, String> loadMd5File(File md5File) throws SraException {
		Map<String, String> mapMd5 = new HashMap<>();

		// Recuperation des signature md5 si le fichier 'md5.txt' existe dans le repertoire courant de soumission :
		if ( md5File.exists()) {
//			BufferedReader input_buffer = null;
//			try {
//				input_buffer = new BufferedReader(new FileReader(md5File));
//			} catch (FileNotFoundException e) {
//				throw new SraException("Probleme lors du chargement du fichier ", e);
//			}
			String ligne = "";
			String pattern_string = "(\\S+)\\s+(\\S+)";
			String pattern_string_c = "([^#]*)#";
			Pattern p_c = Pattern.compile(pattern_string_c);

			Pattern p = Pattern.compile(pattern_string);
			String ebi_md5Hex = null;
			String ebiRelatifName;
//			try {
			try (BufferedReader input_buffer = new BufferedReader(new FileReader(md5File))) {
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
						throw new SraException("Probleme de format avec la ligne : '" + ligne +"'");
					}
					ebi_md5Hex = m.group(1);
					ebiRelatifName = m.group(2);
					mapMd5.put(ebiRelatifName, ebi_md5Hex);
				}
			} catch (IOException e) {
				throw new SraException("Probleme lors du chargement du fichier ", e);
			}
		}
		return mapMd5;
	}

	// Renvoie une copie de la chaine d'entree debarrassee des espaces en debut et fin de chaine:
	// et debarassee des guillements en debut et fin
	public String clean(String chaine) {
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

	public static InputStream decodeBase64(String inputBase64){
		byte[] dataBytes = Base64.decode(inputBase64);
		return  new ByteArrayInputStream(dataBytes);
	}

}
