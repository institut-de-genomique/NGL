package sra.parser;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import models.sra.submit.sra.instance.UserExperimentType;
import models.sra.submit.util.SraException;
import services.IUserObjectFactory;


public class UserExperimentTypeParserFactory implements IUserObjectFactory<UserExperimentType> {
	
	private UserObjectFactory factory = new UserObjectFactory(); // factory des objets CnsSRA
	
	@Override
	public UserExperimentType create(Map<String, String> line) throws SraException {
		UserExperimentType userExperimentType = factory.createUserExperimentType();
		userExperimentType.setCode(line.get(UserExperimentTypeParser.EXPERIMENT_CODE));
		// Pour champs facultatifs verifier si valeur existe

		if(line.get(UserExperimentTypeParser.LAST_BASE_COORD) != null) {
			userExperimentType.setLastBaseCoordonnee(line.get(UserExperimentTypeParser.LAST_BASE_COORD));
		}
		if(StringUtils.isNotBlank(line.get(UserExperimentTypeParser.SPOT_LENGTH))) {
			userExperimentType.setSpotLength(line.get(UserExperimentTypeParser.SPOT_LENGTH));
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

