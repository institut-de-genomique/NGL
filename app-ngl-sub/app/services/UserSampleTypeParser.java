package services;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.sra.submit.common.instance.UserSampleType;
import models.sra.submit.util.SraException;


public class UserSampleTypeParser {
	
	static final String SAMPLE_ALIAS    = "sample_alias";
	static final String SCIENTIFIC_NAME = "scientific_name";
	static final String DESCRIPTION     = "description";
	static final String COMMON_NAME     = "common_name";
	static final String TITLE           = "title";
	static final String ANONYMIZED_NAME = "anonymized_name";

	
	private List<String> allowedFields = new ArrayList<>();
	private String keyField;
	private String separator;

	//private ObjectFactory factory = new ObjectFactory();// factory des objets SRA dans le package entities


	public UserSampleTypeParser() {
		this.init();
	}
	
	private void init() {
		allowedFields.add(SAMPLE_ALIAS);
		allowedFields.add(SCIENTIFIC_NAME);
		allowedFields.add(TITLE);
		allowedFields.add(COMMON_NAME);
		allowedFields.add(DESCRIPTION);
		allowedFields.add(ANONYMIZED_NAME);
		keyField = SAMPLE_ALIAS;
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
