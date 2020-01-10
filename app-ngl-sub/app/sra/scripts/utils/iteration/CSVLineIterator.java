package sra.scripts.utils.iteration;

//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileReader;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Iterator;
//import java.util.List;
//import java.util.function.Function;
//import java.nio.charset.Charset;
//import org.apache.commons.csv.CSVParser;
//import org.apache.commons.csv.CSVRecord;
//
//
//class CSVIterator extends  MappingIterator<CSVRecord, List<String>> {
//	public CSVIterator (CSVParser p) {
//		this(p.iterator());
//	}
//	
//
//	public CSVIterator (Iterator<CSVRecord> i) {
//		super(i, new Function<CSVRecord, List<String>>() {
//			@Override
//			public List<String> apply(CSVRecord t) {
//				List <String> l = new ArrayList<>();
//				for (String s: t) {
//					l.add(s);
//				}
//				return l;
//			}
//		});
//	}
//	
//	
//}

