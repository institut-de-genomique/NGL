package sra.scripts.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;


public class CSVParsing {
	public static final CSVParser parse(File file, char delimiter) throws IOException {
		return CSVParser.parse(file, Charset.forName("UTF-8"), CSVFormat.EXCEL.withDelimiter(delimiter));
	}
}