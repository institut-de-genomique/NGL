package services;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import models.sra.submit.common.instance.UserExperimentType;
import models.sra.submit.util.SraException;


public class UserExperimentTypeParserFactory implements IUserObjectFactory<UserExperimentType> {
	
	private UserObjectFactory factory = new UserObjectFactory(); // factory des objets CnsSRA
	
	@Override
	public UserExperimentType create(Map<String, String> line) throws SraException {
		UserExperimentType userExperimentType = factory.createUserExperimentType();
		userExperimentType.setAlias(line.get(UserExperimentTypeParser.EXPERIMENT_ALIAS));
		// Pour champs facultatifs verifier si valeur existe
		if(StringUtils.isNotBlank(line.get(UserExperimentTypeParser.LIBRARY_NAME))) {
			userExperimentType.setLibraryName(line.get(UserExperimentTypeParser.LIBRARY_NAME));
		}
		if(StringUtils.isNotBlank(line.get(UserExperimentTypeParser.LIBRARY_SELECTION))) {
			userExperimentType.setLibrarySelection(line.get(UserExperimentTypeParser.LIBRARY_SELECTION));
		}
		if(StringUtils.isNotBlank(line.get(UserExperimentTypeParser.LIBRARY_SOURCE))){
			userExperimentType.setLibrarySource(line.get(UserExperimentTypeParser.LIBRARY_SOURCE));
		}
		if(StringUtils.isNotBlank(line.get(UserExperimentTypeParser.LIBRARY_STRATEGY))) {
			userExperimentType.setLibraryStrategy(line.get(UserExperimentTypeParser.LIBRARY_STRATEGY));
		}
		if(StringUtils.isNotBlank(line.get(UserExperimentTypeParser.LIBRARY_PROTOCOL))) {
			userExperimentType.setLibraryProtocol(line.get(UserExperimentTypeParser.LIBRARY_PROTOCOL));
		}	
		if(StringUtils.isNotBlank(line.get(UserExperimentTypeParser.NOMINAL_LENGTH))) {
			userExperimentType.setNominalLength(line.get(UserExperimentTypeParser.NOMINAL_LENGTH));
		}
		if(StringUtils.isNotBlank(line.get(UserExperimentTypeParser.TITLE))) {
			userExperimentType.setTitle(line.get(UserExperimentTypeParser.TITLE));
		}	
		return userExperimentType;
	}


}
//public class UserExperimentTypeParserFactory implements IUserObjectFactory{
//	private UserObjectFactory factory = new UserObjectFactory(); // factory des objets CnsSRA 
//	@Override
//	public Object create(Map<String, String> line) throws SraException {
//		UserExperimentType userExperimentType = factory.createUserExperimentType();
//		userExperimentType.setAlias(line.get(UserExperimentTypeParser.EXPERIMENT_ALIAS));
//		// Pour champs facultatifs verifier si valeur existe
//		if(StringUtils.isNotBlank(line.get(UserExperimentTypeParser.LIBRARY_NAME))) {
//			userExperimentType.setLibraryName(line.get(UserExperimentTypeParser.LIBRARY_NAME));
//		}
//		if(StringUtils.isNotBlank(line.get(UserExperimentTypeParser.LIBRARY_SELECTION))) {
//			userExperimentType.setLibrarySelection(line.get(UserExperimentTypeParser.LIBRARY_SELECTION));
//		}
//		if(StringUtils.isNotBlank(line.get(UserExperimentTypeParser.LIBRARY_SOURCE))){
//			userExperimentType.setLibrarySource(line.get(UserExperimentTypeParser.LIBRARY_SOURCE));
//		}
//		if(StringUtils.isNotBlank(line.get(UserExperimentTypeParser.LIBRARY_STRATEGY))) {
//			userExperimentType.setLibraryStrategy(line.get(UserExperimentTypeParser.LIBRARY_STRATEGY));
//		}
//		if(StringUtils.isNotBlank(line.get(UserExperimentTypeParser.LIBRARY_PROTOCOL))) {
//			userExperimentType.setLibraryProtocol(line.get(UserExperimentTypeParser.LIBRARY_PROTOCOL));
//		}	
//		if(StringUtils.isNotBlank(line.get(UserExperimentTypeParser.NOMINAL_LENGTH))) {
//			userExperimentType.setNominalLength(line.get(UserExperimentTypeParser.NOMINAL_LENGTH));
//		}
//		if(StringUtils.isNotBlank(line.get(UserExperimentTypeParser.TITLE))) {
//			userExperimentType.setTitle(line.get(UserExperimentTypeParser.TITLE));
//		}	
//		return userExperimentType;
//	}
//
//
//}
