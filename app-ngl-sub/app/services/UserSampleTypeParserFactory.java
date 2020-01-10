package services;

import java.util.Map;
import models.sra.submit.util.SraException;
import models.sra.submit.common.instance.UserSampleType;

public class UserSampleTypeParserFactory implements IUserObjectFactory<UserSampleType> {
	
	private UserObjectFactory factory = new UserObjectFactory(); // factory des objets User 

	@Override
	public UserSampleType create(Map<String, String> line) throws SraException{
		UserSampleType userSampleType = factory.createUserSampleType();
		userSampleType.setAlias(line.get(UserSampleTypeParser.SAMPLE_ALIAS));
		// Pour champs facultatifs verifier si valeur existe
		if(line.get(UserSampleTypeParser.DESCRIPTION) != null) {
			userSampleType.setDescription(line.get(UserSampleTypeParser.DESCRIPTION));
		}
		if(line.get(UserSampleTypeParser.TITLE) != null) {
			userSampleType.setTitle(line.get(UserSampleTypeParser.TITLE));
		}
		if(line.get(UserSampleTypeParser.COMMON_NAME) != null) {
			userSampleType.setCommonName(line.get(UserSampleTypeParser.COMMON_NAME));
		}
		if(line.get(UserSampleTypeParser.COMMON_NAME) != null) {
			userSampleType.setCommonName(line.get(UserSampleTypeParser.COMMON_NAME));
		}
		
		if(line.get(UserSampleTypeParser.ANONYMIZED_NAME) != null) {
			userSampleType.setAnonymizedName(line.get(UserSampleTypeParser.ANONYMIZED_NAME));
		}
		return userSampleType;
	}

}

//public class UserSampleTypeParserFactory implements IUserObjectFactory{
//	private UserObjectFactory factory = new UserObjectFactory(); // factory des objets User 
//
//	@Override
//	public Object create(Map<String, String> line) throws SraException{
//		UserSampleType userSampleType = factory.createUserSampleType();
//		userSampleType.setAlias(line.get(UserSampleTypeParser.SAMPLE_ALIAS));
//		// Pour champs facultatifs verifier si valeur existe
//		if(line.get(UserSampleTypeParser.DESCRIPTION) != null) {
//			userSampleType.setDescription(line.get(UserSampleTypeParser.DESCRIPTION));
//		}
//		if(line.get(UserSampleTypeParser.TITLE) != null) {
//			userSampleType.setTitle(line.get(UserSampleTypeParser.TITLE));
//		}
//		if(line.get(UserSampleTypeParser.COMMON_NAME) != null) {
//			userSampleType.setCommonName(line.get(UserSampleTypeParser.COMMON_NAME));
//		}
//		if(line.get(UserSampleTypeParser.COMMON_NAME) != null) {
//			userSampleType.setCommonName(line.get(UserSampleTypeParser.COMMON_NAME));
//		}
//		
//		if(line.get(UserSampleTypeParser.ANONYMIZED_NAME) != null) {
//			userSampleType.setAnonymizedName(line.get(UserSampleTypeParser.ANONYMIZED_NAME));
//		}
//		return userSampleType;
//	}
//
//}
