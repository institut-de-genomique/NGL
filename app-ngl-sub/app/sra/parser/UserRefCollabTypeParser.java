package sra.parser;
// import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.sra.submit.sra.instance.UserRefCollabType;
import models.sra.submit.util.SraException;

public class UserRefCollabTypeParser {
	private static final play.Logger.ALogger logger = play.Logger.of(UserRefCollabTypeParser.class);

	static final String REF_COLLAB = "refcollab";
	static final String STUDY_AC = "study_id";
	static final String SAMPLE_AC = "sample_id";

	private List<String> allowedFields = new ArrayList<>();
	private String keyField;
	private String separator;
	
	public UserRefCollabTypeParser() {
		this.init();
		
	}
	
	private void init() {
		allowedFields.add(REF_COLLAB);
		allowedFields.add(STUDY_AC);
		allowedFields.add(SAMPLE_AC);

		keyField = REF_COLLAB;
		separator = "\\|";
	}
	
	public List<UserRefCollabType> load(InputStream inputStream) throws SraException {
		List<UserRefCollabType> listUserRefCollab = new ArrayList<>();
		if (inputStream != null) {
			ColumnParser parser = new ColumnParser(keyField, separator);
			if (parser.setAllowedFields(allowedFields)) {
				//listUserRefCollab = (List<UserRefCollabType>)parser.load(inputStream, new UserRefCollabTypeParserFactory());
				listUserRefCollab = parser.load(inputStream, new UserRefCollabTypeParserFactory());
				//logger.debug("taille listUserRefCollab = " + listUserRefCollab.size());
				for(  UserRefCollabType userRefCollab : listUserRefCollab) {
					//logger.debug("userRefCollab.alias "+ userRefCollab.getAlias());
					//logger.debug("userRefCollab.sampleAC "+ userRefCollab.getSampleAc());
					//logger.debug("userRefCollab.studyAC "+ userRefCollab.getStudyAc());
				}
			} else {
				throw new SraException("Probleme lors de l'installation des champs autorises");
			}
		}
		return listUserRefCollab;
	}
	
	
	public Map<String, UserRefCollabType> loadMap(InputStream inputStream) throws SraException {
		Map<String, UserRefCollabType> mapUserRefCollab = new HashMap<>();
		if (inputStream == null) {
			//Logger.debug("HHHHHHHHHH le flux '" + inputStream + "' pour les refCollab n'existe pas ou n'est pas lisible");
			throw new SraException("le flux '" + inputStream + "' pour les refCollab n'existe pas ou n'est pas lisible");
		} 
		//logger.debug("HHHHHHHHHHHHH le flux '" + inputStream + "' pour les refCollab existe bien");
		//logger.debug("keyField = '" + keyField + "' et separator = '" + separator + "'");
		ColumnParser parser = new ColumnParser(keyField, separator);
		//logger.debug("Dans UserRefCollabTypeParser.loadMap avant setAllowedFields");
		if (parser.setAllowedFields(allowedFields)) {
			//logger.debug("Dans UserRefCollabTypeParser.loadMap avant parser.loadMap");
			mapUserRefCollab = parser.loadMap(inputStream, new UserRefCollabTypeParserFactory());
		} else {
			throw new SraException("Probleme lors de l'installation des champs autorises");
		}
		//logger.debug("Dans UserRefCollabTypeParser.loadMap  Apres loadMap");
		
		return mapUserRefCollab;
	}

}
