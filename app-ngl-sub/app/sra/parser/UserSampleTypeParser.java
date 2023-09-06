package sra.parser;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.sra.submit.sra.instance.UserSampleType;
import models.sra.submit.util.SraException;


public class UserSampleTypeParser {
	
	static final String SAMPLE_CODE     = "sample_code";
	static final String DESCRIPTION     = "description";
	static final String TITLE           = "title";
	static final String ANONYMIZED_NAME = "anonymized_name";
	static final String ATTRIBUTES      = "attributes";

	
	private List<String> allowedFields = new ArrayList<>();
	private String keyField;
	private String separator;

	//private ObjectFactory factory = new ObjectFactory();// factory des objets SRA dans le package entities


	public UserSampleTypeParser() {
		this.init();
	}
	
	private void init() {
		allowedFields.add(SAMPLE_CODE);
		allowedFields.add(TITLE);
		allowedFields.add(ATTRIBUTES);
		allowedFields.add(DESCRIPTION);
		allowedFields.add(ANONYMIZED_NAME);
		keyField = SAMPLE_CODE;
		separator = "\\|";
	}
	
	public List<UserSampleType> load(InputStream inputStream) throws SraException {
		List<UserSampleType> listUserSamples = new ArrayList<>();
		
		if (inputStream != null) {
			ColumnParser parser = new ColumnParser(keyField, separator);
			if (parser.setAllowedFields(allowedFields)) {
				// listUserSamples = (List<UserSampleType>)parser.load(inputStream, new UserSampleTypeParserFactory());
				listUserSamples = parser.load(inputStream, new UserSampleTypeParserFactory());
			} else {
				throw new SraException("Probleme lors de l'installation des champs autorises");
			}
		}
		return listUserSamples;
	}
	
	public Map<String, UserSampleType> loadMap(InputStream inputStream) throws SraException {
		Map<String, UserSampleType> mapUserSamples = new HashMap<>();
		if (inputStream != null) {
			ColumnParser parser = new ColumnParser(keyField, separator);
			if (parser.setAllowedFields(allowedFields)) {
				// mapUserSamples = (Map<String, UserSampleType>)parser.loadMap(inputStream, new UserSampleTypeParserFactory());
				mapUserSamples = parser.loadMap(inputStream, new UserSampleTypeParserFactory());
			} else {
				throw new SraException("Probleme lors de l'installation des champs autorises");
			}
		}
		return mapUserSamples;
	}
	
}
