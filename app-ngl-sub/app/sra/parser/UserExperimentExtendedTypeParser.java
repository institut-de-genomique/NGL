package sra.parser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.sra.submit.sra.instance.UserExperimentExtendedType;
import models.sra.submit.util.SraException;

public class UserExperimentExtendedTypeParser {
	// champs, cle de map :
	static final String EXPERIMENT_CODE   = "experiment_code";
	// liste des champs editables pour l'utilisateur :
	static final String LIBRARY_STRATEGY  = "library_strategy";
	static final String LIBRARY_SOURCE    = "library_source";
	static final String LIBRARY_SELECTION = "library_selection";
	static final String LIBRARY_PROTOCOL  = "library_construction_protocol";
	static final String NOMINAL_LENGTH    = "library_layout_nominal_length";
	static final String TITLE             = "title";
	static final String SPOT_LENGTH       = "spot_length";
	static final String LAST_BASE_COORD   = "last_base_coordonnee";
	static final String STUDY_AC   = "study_ac";
	static final String SAMPLE_AC  = "sample_ac";



	private List<String> allowedFields = new ArrayList<>();
	private String keyField;
	private String separator;
	

	public UserExperimentExtendedTypeParser() {
		this.init();
		
	}
	
	private void init() {
		allowedFields.add(LIBRARY_STRATEGY);
		allowedFields.add(LIBRARY_SOURCE);
		allowedFields.add(LIBRARY_SELECTION);
		allowedFields.add(LIBRARY_PROTOCOL);
		allowedFields.add(NOMINAL_LENGTH);
		allowedFields.add(TITLE);
		allowedFields.add(EXPERIMENT_CODE);
		allowedFields.add(LAST_BASE_COORD);
		allowedFields.add(SPOT_LENGTH);
		allowedFields.add(STUDY_AC);
		allowedFields.add(SAMPLE_AC);

		keyField = EXPERIMENT_CODE;
		separator = "\\|";
	}
	
	public List<UserExperimentExtendedType> load(InputStream inputStream) throws SraException {
		List<UserExperimentExtendedType> listExperiments = new ArrayList<>();
		//Logger.debug("Dans load");

		if (inputStream != null) {
			ColumnParser parser = new ColumnParser(keyField, separator);
			
			if (parser.setAllowedFields(allowedFields)) {
				// listExperiments = (List<UserExperimentExtendeType>)parser.load(inputStream, new UserExperimentExtendedTypeParserFactory());
				listExperiments = parser.load(inputStream, new UserExperimentExtendedTypeParserFactory());
			} else {
				throw new SraException("Probleme lors de l'installation des champs autorises");
			}
		}
		return listExperiments;
	}	
	
	public Map<String, UserExperimentExtendedType> loadMap(InputStream inputStream) throws SraException {
		Map<String, UserExperimentExtendedType> mapExperiments = new HashMap<>();
		//Logger.debug("Dans UserExperimentExtendedTypeParser.loadMap");
		if (inputStream != null) {
			
			ColumnParser parser = new ColumnParser(keyField, separator);
			if (parser.setAllowedFields(allowedFields)) {
				// mapExperiments = (Map<String, UserExperimentType>)parser.loadMap(inputStream, new UserExperimentTypeParserFactory());
				mapExperiments = parser.loadMap(inputStream, new UserExperimentExtendedTypeParserFactory());
			} else {
				throw new SraException("Probleme lors de l'installation des champs autorises");
			}
		}
		return mapExperiments;
	}	
	
}
