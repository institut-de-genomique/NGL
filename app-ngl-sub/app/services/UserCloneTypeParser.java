package services;
// import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.sra.submit.common.instance.UserCloneType;
import models.sra.submit.util.SraException;

public class UserCloneTypeParser {

	static final String CLONE_ALIAS = "clone_alias";
	static final String STUDY_AC = "study_ac";
	static final String SAMPLE_AC = "sample_ac";

	private List<String> allowedFields = new ArrayList<>();
	private String keyField;
	private String separator;
	
	public UserCloneTypeParser() {
		this.init();
		
	}
	
	private void init() {
		allowedFields.add(CLONE_ALIAS);
		allowedFields.add(STUDY_AC);
		allowedFields.add(SAMPLE_AC);

		keyField = CLONE_ALIAS;
		separator = "\\|";
	}
	
//	public List<UserCloneType> load(File file) throws SraException {
	public List<UserCloneType> load(InputStream inputStream) throws SraException {
		List<UserCloneType> listUserClones = new ArrayList<>();
		if (inputStream != null) {
			ColumnParser parser = new ColumnParser(keyField, separator);
			if (parser.setAllowedFields(allowedFields)) {
				// listUserClones = (List<UserCloneType>)parser.load(inputStream, new UserCloneTypeParserFactory());
				listUserClones = parser.load(inputStream, new UserCloneTypeParserFactory());
			} else {
				throw new SraException("Probleme lors de l'installation des champs autorises");
			}
		}
		return listUserClones;
	}
	
	
	public Map<String, UserCloneType> loadMap(InputStream inputStream) throws SraException {
		Map<String, UserCloneType> mapUserClones = new HashMap<>();
		
		if (inputStream!=null) {
			ColumnParser parser = new ColumnParser(keyField, separator);
			if (parser.setAllowedFields(allowedFields)) {
				// mapUserClones = (Map<String, UserCloneType>)parser.loadMap(inputStream, new UserCloneTypeParserFactory());
				mapUserClones = parser.loadMap(inputStream, new UserCloneTypeParserFactory());
			} else {
				throw new SraException("Probleme lors de l'installation des champs autorises");
			}
		}
		return mapUserClones;
	}

}
