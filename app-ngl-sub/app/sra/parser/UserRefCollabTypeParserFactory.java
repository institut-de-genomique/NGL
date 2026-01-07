package sra.parser;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import models.sra.submit.sra.instance.UserRefCollabType;
import services.sra.SraException;
import sra.services.IUserObjectFactory;

public class UserRefCollabTypeParserFactory  implements IUserObjectFactory<UserRefCollabType> {
	
	private UserObjectFactory factory = new UserObjectFactory(); // factory des objets User 

	@Override
	public UserRefCollabType create(Map<String, String> line)  throws SraException {
		UserRefCollabType userRefCollabType = factory.createUserRefCollabType();
		userRefCollabType.setRefCollabName(line.get(UserRefCollabTypeParser.REF_COLLAB));
		if (StringUtils.isNotBlank(line.get(UserRefCollabTypeParser.STUDY_ID))) {
			userRefCollabType.setStudyAc(line.get(UserRefCollabTypeParser.STUDY_ID));
		}
		if (StringUtils.isNotBlank(line.get(UserRefCollabTypeParser.SAMPLE_ID))) {
			userRefCollabType.setSampleAc(line.get(UserRefCollabTypeParser.SAMPLE_ID));
		}
		return userRefCollabType;
	}

}

