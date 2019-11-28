package services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.sra.submit.util.SraException;

/**
 * 
 * @author sgas
 *
 */
public class ColumnParser {

	//private Map<String, String> mapAllowedFields =  new HashMap<String, String>();
	private List<String> allowedFields =  new ArrayList<>();

	private String keyField = ""; // champs cle unique du fichier permettant d'identifier l'objet.
	private String separator= "\\|";
	
	private String pattern_LineVide = "^\\s*$";   // pour ignorer les lignes sans caracteres visibles  
	private java.util.regex.Pattern plv = Pattern.compile(pattern_LineVide); 

	private String pattern_LineComment = "^\\s*#";   // pour ignorer les lignes de commentaire
	private java.util.regex.Pattern plc = Pattern.compile(pattern_LineComment);
	private String pattern_Comment = "^([^#]+)#"; 	
	private java.util.regex.Pattern pc = Pattern.compile(pattern_Comment);

	//private String [] nameFieldsInFile;
	
	/*
	 * 
	 * @param keyField 
	 * @param separator
	 * Constructeur.
	 */
	public ColumnParser(String keyField, String separator) {
		//System.out.println("keyField " + keyField);
		if (! keyField.matches("\\S+") || keyField.isEmpty() ) {
			throw new RuntimeException("Text has no visible content", new IllegalArgumentException());
		}
		this.keyField  = keyField;
		this.separator = separator;
	}
	
	/*
	 * @param allowedFields
	 * @return boolean 
	 * Retourne vrai si la liste des noms de champs autorises contient le nom du champ clé.
	 */
	public boolean setAllowedFields(List<String> userAllowedFields) {
		Boolean isKeyAllowed = false;
		//System.out.println("cle = '"+keyField+"'");	
		Tools tools = new Tools();
		for (String permitedField: userAllowedFields) {
			if (tools.clean(permitedField).equalsIgnoreCase(keyField)) {
				//System.out.println("permitedField = '" + permitedField + "'");
				isKeyAllowed = true;
			}
			//mapAllowedFields.put(permitedField, "");
			this.allowedFields.add(tools.clean(permitedField));
		}
		return(isKeyAllowed);
	}
	
	/*
	 * Declenche SNTS exception si cle d'indexation n'est pas un nom de champs, ou si nom de champs
	 * n'est pas autorisé.
	 * @param first line of userFile de la forme nomChamps_1 separator nomChamps_2 separator ....
	 * @return tableau des noms de champs dans le fichier d'entree.
	 */	
	protected String[] validateHeaders(String line) throws SraException {
		String legende = line;
		String [] nameFieldsInFile = legende.split(separator);
		// On verifie que la cle d'indexation est bien presente dans le header :
		Boolean isKeyInHeader = false;
		Tools tools = new Tools();
		//System.out.println("dans validateHeaders, cle d'indexation = '"+keyField+"'");
		for(int i =0; i < nameFieldsInFile.length ; i++) {
			nameFieldsInFile[i] = tools.clean(nameFieldsInFile[i]).toLowerCase();
			//System.out.println("dans validateHeaders, nom champs = '"+nameFieldsInFile[i]+"'");
			if ( nameFieldsInFile[i].equalsIgnoreCase(keyField)) {
				isKeyInHeader = true;
			}
		}
		if (! isKeyInHeader ) {
			throw new SraException("Cle d'indexation '"+ keyField + "' absente dans l'entete du fichier");
		}
		// On verifie que tous les champs du fichier sont autorisees. Vrai par defaut,
		// si aucune restriction et vraie si les champs du fichiers existent bien 
		// dans la liste des champs autorises installee.
		if (allowedFields.size() > 0) {
			for(int i =0; i < nameFieldsInFile.length ; i++) {
				//System.out.println(nameFieldsInFile[i]);
				//nameFieldsInFile[i] = tools.clean(nameFieldsInFile[i]).toLowerCase();
				if (! allowedFields.contains(nameFieldsInFile[i])) {
					throw new SraException("Champ '"+nameFieldsInFile[i]+"' non autorise");
				}
			}
		}
		return nameFieldsInFile;
	}
	

//	public List load(File file, IUserObjectFactory factory) throws SraException {
//	public List<Object> load(InputStream inputStream, IUserObjectFactory factory) throws SraException {
	// Should accept a properly typed IUserObjectFactory<T>
	public <T> List<T> load(InputStream inputStream, IUserObjectFactory<T> factory) throws SraException {
//		List<Object> userCnsObject = new ArrayList<Object>();
		List<T> userCnsObject = new ArrayList<>();
		if (inputStream == null) {
			throw new SraException("le flux '" + inputStream + "'n'existe pas ou n'est pas lisible");
		}
		try {
			//BufferedReader input_buffer = new BufferedReader(new FileReader(file));
			BufferedReader input_buffer = new BufferedReader(new InputStreamReader(inputStream));

//			int cp = 1;
			String [] nameFieldsInFile = null;
			String [] valFieldsInFile = null;
			String legend = "";
			String ligne;
			int cp_no_empty = 0;
			while ((ligne = input_buffer.readLine()) != null) {
//				cp++;
				ligne = ligne + " "; // pour eviter \nseparator si dernier champs vide
				Matcher mlv = plv.matcher(ligne);
				Matcher mlc = plc.matcher(ligne);
				Matcher mc  = pc.matcher(ligne);

				// Appel de find obligatoire pour pouvoir récupérer $1 ...$n
				if ( mlv.find() ) { // si ligne vide, ignorer la ligne
					//System.out.println("!!!!!!!!!!!!!!!!!ligne vide :" + ligne);
					continue;
				}
				if ( mlc.find() ) { // si ligne de commentaires, ignorer la ligne
					//System.out.println("!!!!!!!!!!!!!!!!!commentaires ignores :" + ligne);
					continue;
				}
				
				cp_no_empty++;
				// Oter la partie commentaire :
				if ( mc.find() ) { 
					ligne = mc.group(1);
				}
				
				if (cp_no_empty == 1) {
					legend = ligne;
					nameFieldsInFile = this.validateHeaders(ligne);
				}
				valFieldsInFile = ligne.split(separator);
				//System.out.println("valFieldsInFile = " + valFieldsInFile.length);
				//System.out.println("nameFieldsInFile = " + nameFieldsInFile.length);	
				if (nameFieldsInFile == null) {
					throw new SraException("pas de nom de champ dans le fichier");
				} else if (valFieldsInFile.length != nameFieldsInFile.length) {
					throw new SraException("line no match whith legend, line=: '" + ligne +"' legend='"+ legend);
				}
				Map<String, String> mapLine =  new HashMap<>();
				//System.out.println("cle de mapLine='"+mapLine.get(keyField)+"'");
				for(int i =0; i < nameFieldsInFile.length ; i++) {
					//System.out.println("nameFieldsInFile = " + nameFieldsInFile[i]);
					Tools tools = new Tools();
					mapLine.put(nameFieldsInFile[i], tools.clean(valFieldsInFile[i]));
					System.out.println("Champ '" + nameFieldsInFile[i] + "' et valeur '" + tools.clean(valFieldsInFile[i]) + "'");
				} 	
				userCnsObject.add(factory.create(mapLine));
			}	
		} catch (IOException e) {
			e.printStackTrace();
			throw new SraException("", e);
		}
		return userCnsObject;
	}
	
	//public Map loadMap(File file, IUserObjectFactory factory) throws SraException {
	
//	public Map loadMap(InputStream inputStream, IUserObjectFactory factory) throws SraException {
//		Map<String, Object> mapUserObject = new HashMap<String, Object>(); 
	public <T> Map<String,T> loadMap(InputStream inputStream, IUserObjectFactory<T> factory) throws SraException {
//		Map<String, Object> mapUserObject = new HashMap<String, Object>(); 
		if (inputStream == null)
			throw new SraException("le flux '" + inputStream + "'n'existe pas ou n'est pas lisible");
		Map<String, T> mapUserObject = new HashMap<>(); // String, Object>(); 
//		try {
//			//BufferedReader input_buffer = new BufferedReader(new FileReader(file));
//			BufferedReader input_buffer = new BufferedReader(new InputStreamReader(inputStream));
		try (BufferedReader input_buffer = new BufferedReader(new InputStreamReader(inputStream))) {
//			int cp = 1;
			String [] nameFieldsInFile = null;
			String [] valFieldsInFile = null;
			
			String ligne;
//			String legend = "";
			int cp_no_empty = 0;

			while ((ligne = input_buffer.readLine()) != null) {
//				cp++;
				ligne = ligne + " ";
				Matcher mlv = plv.matcher(ligne);
				Matcher mlc = plc.matcher(ligne);
				Matcher mc = pc.matcher(ligne);

				// Appel de find obligatoire pour pouvoir récupérer $1 ...$n
				if ( mlv.find() ) { // si ligne vide, ignorer la ligne
					//System.out.println("!!!!!!!!!!!!!!!!!ligne vide :" + ligne);
					continue;
				}
				if ( mlc.find() ) { // si ligne de commentaires, ignorer la ligne
					//System.out.println("!!!!!!!!!!!!!!!!!commentaires ignores :" + ligne);
					continue;
				}
				cp_no_empty++;
				// Oter la partie commentaire :
				if ( mc.find() ) { 
					ligne = mc.group(1);
				}
			
				if (cp_no_empty == 1) {
//					legend = ligne;
					nameFieldsInFile = this.validateHeaders(ligne);
					continue;
				}
				valFieldsInFile = ligne.split(separator);
				if (nameFieldsInFile == null) {
					throw new SraException("pas de nom de champ dans le fichier");
				} else if (valFieldsInFile.length != nameFieldsInFile.length) {
					//System.out.println("!!!!!!!!!!!!!!!!!ligne  :" + ligne);
					throw new SraException("no match pour la ligne : '" + ligne + "'");
				}
				Map<String, String> mapLine =  new HashMap<>();
				Tools tools = new Tools();
				for (int i =0; i < nameFieldsInFile.length ; i++) {
					mapLine.put(nameFieldsInFile[i], tools.clean(valFieldsInFile[i]));
					//System.out.println("Champ '" + nameFieldsInFile[i] + "' et valeur '"+tools.clean(valFieldsInFile[i])+"'");
				} 
				//System.out.println("cle de mapLine='"+mapLine.get(keyField)+"'");
				mapUserObject.put(mapLine.get(keyField), factory.create(mapLine));				
			}	
		} catch (IOException e) {
			e.printStackTrace();
			throw new SraException("", e);
		}
		return mapUserObject;
	}
	
}
