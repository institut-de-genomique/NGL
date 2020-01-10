package services;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.sra.submit.common.instance.UserExperimentType;
import models.sra.submit.util.SraException;

public class UserExperimentTypeParser {
	static final String EXPERIMENT_ALIAS = "experiment_alias";
	static final String LIBRARY_STRATEGY = "library_strategy";
	static final String LIBRARY_SOURCE = "library_source";
	static final String LIBRARY_SELECTION = "library_selection";
	static final String LIBRARY_PROTOCOL = "library_protocol";
	static final String LIBRARY_NAME = "library_name";
	static final String NOMINAL_LENGTH = "nominal_length";
	static final String TITLE = "title";

	private List<String> allowedFields = new ArrayList<>();
	private String keyField;
	private String separator;
	

	public UserExperimentTypeParser() {
		this.init();
		
	}
	
	private void init() {
		allowedFields.add(LIBRARY_STRATEGY);
		allowedFields.add(LIBRARY_SOURCE);
		allowedFields.add(LIBRARY_SELECTION);
		allowedFields.add(LIBRARY_PROTOCOL);
		allowedFields.add(LIBRARY_NAME);
		allowedFields.add(NOMINAL_LENGTH);
		allowedFields.add(TITLE);
		allowedFields.add(EXPERIMENT_ALIAS);

		keyField = EXPERIMENT_ALIAS;
		separator = "\\|";
	}
	
	public List<UserExperimentType> load(InputStream inputStream) throws SraException {
		List<UserExperimentType> listExperiments = new ArrayList<>();
		if (inputStream != null) {
			ColumnParser parser = new ColumnParser(keyField, separator);
			if (parser.setAllowedFields(allowedFields)) {
				// listExperiments = (List<UserExperimentType>)parser.load(inputStream, new UserExperimentTypeParserFactory());
				listExperiments = parser.load(inputStream, new UserExperimentTypeParserFactory());
			} else {
				throw new SraException("Probleme lors de l'installation des champs autorises");
			}
		}
		return listExperiments;
	}	
	
	public Map<String, UserExperimentType> loadMap(InputStream inputStream) throws SraException {
		Map<String, UserExperimentType> mapExperiments = new HashMap<>();
		if (inputStream != null) {
			ColumnParser parser = new ColumnParser(keyField, separator);
			if (parser.setAllowedFields(allowedFields)) {
				// mapExperiments = (Map<String, UserExperimentType>)parser.loadMap(inputStream, new UserExperimentTypeParserFactory());
				mapExperiments = parser.loadMap(inputStream, new UserExperimentTypeParserFactory());
			} else {
				throw new SraException("Probleme lors de l'installation des champs autorises");
			}
		}
		return mapExperiments;
	}	
	
}
