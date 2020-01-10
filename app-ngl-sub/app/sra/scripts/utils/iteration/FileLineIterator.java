package sra.scripts.utils.iteration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


/**
 * Iterateur qui permet de manipuler chaque ligne du fichier comme une liste de champs (String)
 * @author sgas
 *
 */
public class FileLineIterator implements Iterator <List<String>> {
	
	private BufferedReader bf;
	String line;
	private final String regEx;
	
	/**
	 * Separateur de champs par defaut.
	 */
	public static final String DEFAULT_REGEX = "\\s+";
	
	/**
	 * Constructeur.
	 * @param file          Fichier
	 * @param regEx         Separateur de champs dans la ligne
	 * @throws IOException  Erreur acces fichier
	 */
	FileLineIterator(File file, String regEx) throws IOException {
		this.regEx = regEx;
		bf = new BufferedReader(new FileReader(file));
		line = bf.readLine();
	}

	/**
	 * Constructeur qui utilise le separateur de champs par defaut {@link #DEFAULT_REGEX}.
	 * @param file          Fichier
	 * @throws IOException  Erreur acces fichier
	 */	
	public FileLineIterator(File file) throws IOException {
		this(file, DEFAULT_REGEX); // appel du constructeur 
	}
	/**
	 * 	Retourne vrai s'il existe encore une ligne dans le fichier.
	 */
	@Override
	public boolean hasNext() {
		return line != null;
	}

	/**
	 *  Retourne la liste des champs de la ligne suivante.
	 */
	@Override
	public List<String> next() {
		List<String> tmp = Arrays.asList(line.split(regEx));
		try {
			line = bf.readLine();
		} catch (IOException e) {
			throw  new RuntimeException(e);
		}
		return tmp;
	}
}
