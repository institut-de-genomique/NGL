package sra.parser;

import java.util.Map;

import models.sra.submit.sra.instance.UserSampleType;
import models.sra.submit.util.SraException;
//import play.Logger;
import services.IUserObjectFactory;

public class UserSampleTypeParserFactory implements IUserObjectFactory<UserSampleType> {
	
	private UserObjectFactory factory = new UserObjectFactory(); // factory des objets User 

	@Override
	public UserSampleType create(Map<String, String> line) throws SraException{
		UserSampleType userSampleType = factory.createUserSampleType();
		userSampleType.setCode(line.get(UserSampleTypeParser.SAMPLE_CODE));
		// Pour champs facultatifs verifier si valeur existe
		if(line.get(UserSampleTypeParser.DESCRIPTION) != null) {
			userSampleType.setDescription(line.get(UserSampleTypeParser.DESCRIPTION));
		}
		if(line.get(UserSampleTypeParser.TITLE) != null) {
			userSampleType.setTitle(line.get(UserSampleTypeParser.TITLE));
		}
		if(line.get(UserSampleTypeParser.ATTRIBUTES) != null) {
			userSampleType.setAttributes(line.get(UserSampleTypeParser.ATTRIBUTES));
		}		
		if(line.get(UserSampleTypeParser.ANONYMIZED_NAME) != null) {
			userSampleType.setAnonymizedName(line.get(UserSampleTypeParser.ANONYMIZED_NAME));
		}
		return userSampleType;
	}

}

