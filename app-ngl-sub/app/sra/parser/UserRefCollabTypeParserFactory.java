package sra.parser;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import models.sra.submit.sra.instance.UserRefCollabType;
import models.sra.submit.util.SraException;
import services.IUserObjectFactory;

public class UserRefCollabTypeParserFactory  implements IUserObjectFactory<UserRefCollabType> {
	
	private UserObjectFactory factory = new UserObjectFactory(); // factory des objets User 

	@Override
	public UserRefCollabType create(Map<String, String> line)  throws SraException {
		UserRefCollabType userRefCollabType = factory.createUserRefCollabType();
		userRefCollabType.setAlias(line.get(UserRefCollabTypeParser.REF_COLLAB));
		if (StringUtils.isNotBlank(line.get(UserRefCollabTypeParser.STUDY_AC))) {
			userRefCollabType.setStudyAc(line.get(UserRefCollabTypeParser.STUDY_AC));
		}
		if (StringUtils.isNotBlank(line.get(UserRefCollabTypeParser.SAMPLE_AC))) {
			userRefCollabType.setSampleAc(line.get(UserRefCollabTypeParser.SAMPLE_AC));
		}
		return userRefCollabType;
	}

}

