package sra.scripts.utils.iteration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
//import java.util.function.Function;



import org.apache.commons.csv.CSVRecord;

import fr.cea.ig.lfw.utils.iteration.MappingIterable;
import sra.scripts.utils.CSVParsing;


public class CSVIterable extends MappingIterable <CSVRecord, List<String>> {
	public CSVIterable (File file, char delimiteur) throws IOException {
		this(CSVParsing.parse(file, delimiteur));
	}

	// CSVParser est un Iterable<CSVRecord>
	public CSVIterable(Iterable<CSVRecord> i) {
		super(i, t -> { List <String> l = new ArrayList<>();
						for (String s: t) {
							l.add(s);
						}
						return l;
					  });	
	}
}


//import java.io.File;
//import java.io.IOException;
//import java.util.Iterator;
//import java.util.List;
//
//import org.apache.commons.csv.CSVParser;
//
///**
// * 
// * @author sgas
// *
// */
//public class CSVIterable implements ZenIterable <List<String>> {
//	
//	// Facade devant l'iterable
//	private final CSVParser csvParser; // Iterable sur du CSVRecord
//
//	/**
//	 * 
//	 * @param  file
//	 * @param  delimiteur
//	 * @throws IOException
//	 */
//	public CSVIterable (File file, char delimiteur) throws IOException {
//		this(CSVParsing.parse(file, delimiteur));
//	}
//	/**
//	 * 
//	 * @param p
//	 */
//	public CSVIterable (CSVParser csvParser){
//		this.csvParser = csvParser;
//	}
//	/* equivalent du MappingIterator 
//	 * (non-Javadoc)
//	 * @see java.lang.Iterable#iterator()
//	 */
//	@Override
//	public Iterator<List<String>> iterator() {
//		return new CSVIterator(csvParser);
//	}
//	
//}
//
