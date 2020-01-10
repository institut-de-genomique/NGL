package sra.scripts.utils.iteration;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import fr.cea.ig.lfw.utils.ZenIterable;


public class FileLineIterable implements ZenIterable <List<String>> {
	File file;
	String regEx;
	
	public FileLineIterable (File file, String regEx) {
		this.file = file;
		this.regEx = regEx;
	}
	
	public FileLineIterable (File file) {
		this(file, FileLineIterator.DEFAULT_REGEX);
	}	
	
	@Override
	public Iterator<List<String>> iterator() {
		try {
			return new FileLineIterator(file, regEx);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	
}