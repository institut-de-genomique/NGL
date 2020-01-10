package services;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import models.sra.submit.common.instance.UserCloneType;
import models.sra.submit.util.SraException;

public class UserCloneTypeParserFactory  implements IUserObjectFactory<UserCloneType> {
	
	private UserObjectFactory factory = new UserObjectFactory(); // factory des objets User 

	@Override
	public UserCloneType create(Map<String, String> line)  throws SraException {
		UserCloneType userCloneType = factory.createUserCloneType();
		userCloneType.setAlias(line.get(UserCloneTypeParser.CLONE_ALIAS));
		if (StringUtils.isNotBlank(line.get(UserCloneTypeParser.STUDY_AC))) {
			userCloneType.setStudyAc(line.get(UserCloneTypeParser.STUDY_AC));
		}
		if (StringUtils.isNotBlank(line.get(UserCloneTypeParser.SAMPLE_AC))) {
			userCloneType.setSampleAc(line.get(UserCloneTypeParser.SAMPLE_AC));
		}
		return userCloneType;
	}

}


//public class UserCloneTypeParserFactory  implements IUserObjectFactory {
//	
//	private UserObjectFactory factory = new UserObjectFactory(); // factory des objets User 
//
//	@Override
//	public Object create(Map<String, String> line)  throws SraException {
//		UserCloneType userCloneType = factory.createUserCloneType();
//		userCloneType.setAlias(line.get(UserCloneTypeParser.CLONE_ALIAS));
//		if (StringUtils.isNotBlank(line.get(UserCloneTypeParser.STUDY_AC))) {
//			userCloneType.setStudyAc(line.get(UserCloneTypeParser.STUDY_AC));
//		}
//		if (StringUtils.isNotBlank(line.get(UserCloneTypeParser.SAMPLE_AC))) {
//			userCloneType.setSampleAc(line.get(UserCloneTypeParser.SAMPLE_AC));
//		}
//		return userCloneType;
//	}
//
//}
