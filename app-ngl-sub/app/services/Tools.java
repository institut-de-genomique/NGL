package services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
//import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.xerces.impl.dv.util.Base64;

import models.sra.submit.sra.instance.UserRefCollabType;
import models.sra.submit.util.SraException;

public class Tools {
	
	// constructeur privé => les ulisateurs ne pourront pas faire d'instance de cette classe
	// complement statique.
	private Tools() {}
	
	public static List<String> loadReadSet(InputStream inputStream) throws SraException {	
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
				if (clean(ligne).equalsIgnoreCase("readSetCode")) {
//					legend = true;
					continue;
				}
				if (clean(ligne).equalsIgnoreCase("lotseqname")) {
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
	
	public static List<String> loadFirstword(InputStream inputStream) throws SraException {	
		List<String> listWords = new ArrayList<>();
		if (inputStream == null) {
			return listWords;
		}
		try {
			BufferedReader input_buffer = new BufferedReader(new InputStreamReader(inputStream));
			String ligne = "";
			String pattern_string = "^\\s*\"*([^\"]+)\\s*\"*";
			Pattern p = Pattern.compile(pattern_string);
			//String pattern_string_c = "([^#]*)#";
			String pattern_string_c = "^\\s*#.*";
			Pattern p_c = Pattern.compile(pattern_string_c);
			while ((ligne = input_buffer.readLine()) != null) {					
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
				String word = m.group(1); //word
				word = clean(word);
				//System.out.println("word = '"+ word +"'");
				if (! listWords.contains(word)) {
					listWords.add(word);
				}
			} 
			//log.debug("legend="+legend);
		} catch (IOException e) {
			throw new SraException("Probleme lors du chargement du fichier ", e);
		}
		return listWords;
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
	public static Map<String, String> loadMd5File(File md5File) throws SraException {
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
					ebiRelatifName = ebiRelatifName.replaceAll(".*/", "");
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

	public static InputStream decodeBase64(String inputBase64) {
		//Logger.debug("Dans Tools.decodeBase64");
		byte[] dataBytes = Base64.decode(inputBase64);
		return  new ByteArrayInputStream(dataBytes);
	}
	
	
	// Ecrit dans le fichier indiqué, la map submission.userRefCollab dans le format des 
	// des fichiers userRefCollabToAC
	public static void writeUserRefCollabToAc(Map<String,UserRefCollabType> mapUserRefCollab, File outputFile) throws SraException {
		if(mapUserRefCollab.isEmpty()) {
			throw new SraException("parametre map vide");
		}

		try (BufferedWriter output_buffer = new BufferedWriter(new java.io.FileWriter(outputFile))) {
			// Ecriture de la legende du fichier :
			output_buffer.write(String.format("%s\n", "#-----------------------------------"));
			output_buffer.write(String.format("%15s|%10s|%10s\n", "refCollab", "study_ac", "sample_ac"));
			output_buffer.write(String.format("%s\n", "#-----------------------------------"));
			for (Iterator<Entry<String, UserRefCollabType>> iterator = mapUserRefCollab.entrySet().iterator(); iterator.hasNext();) {
			  Entry<String, UserRefCollabType> entry = iterator.next();
			  String refCollab = entry.getKey();
			  String studyAc = entry.getValue().getStudyAc();
			  String sampleAc = entry.getValue().getSampleAc();
			  // Ecriture des valeurs :
			  output_buffer.write(String.format("%15s|%10s|%10s\n", refCollab, studyAc, sampleAc));
			}
		} catch (IOException e) {
			e.printStackTrace();	
			throw new SraException("Probleme avec le fichier " + e.getMessage());
		}
		
	}
	// Ecrit dans le fichier indiqué, la liste des readsets dans le format des 
	// des fichiers readsetCodes
	public static void writeReadSet(List<String> readsetCodes, File outputFile) throws SraException {
		if(readsetCodes.isEmpty()) {
			throw new SraException("parametre readsetCodes vide");
		}

		try (BufferedWriter output_buffer = new BufferedWriter(new java.io.FileWriter(outputFile))) {
			// Ecriture de la legende du fichier :
			output_buffer.write(String.format("%s\n", "#-----------------------------------"));
			output_buffer.write(String.format("%s\n", "readSetCode"));
			output_buffer.write(String.format("%s\n", "#-----------------------------------"));
			for (String readsetCode : readsetCodes) {
			  // Ecriture des valeurs :
			  output_buffer.write(String.format("%s\n", readsetCode));
			}
		} catch (IOException e) {
			e.printStackTrace();	
			throw new SraException("Probleme avec le fichier " + e.getMessage());
		}
		
	}
	
}
