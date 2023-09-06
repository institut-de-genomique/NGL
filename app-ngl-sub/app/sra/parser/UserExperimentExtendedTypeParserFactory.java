package sra.parser;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import models.sra.submit.sra.instance.UserExperimentExtendedType;
import models.sra.submit.util.SraException;
import services.IUserObjectFactory;


public class UserExperimentExtendedTypeParserFactory implements IUserObjectFactory<UserExperimentExtendedType> {
	
	private UserObjectFactory factory = new UserObjectFactory(); // factory des objets CnsSRA
	
	@Override
	public UserExperimentExtendedType create(Map<String, String> line) throws SraException {
		UserExperimentExtendedType userExperimentExtendedType = factory.createUserExperimentExtendedType();
		userExperimentExtendedType.setCode(line.get(UserExperimentExtendedTypeParser.EXPERIMENT_CODE));
		
		// Pour champs facultatifs verifier si valeur existe

		if(line.get(UserExperimentExtendedTypeParser.LAST_BASE_COORD) != null) {
			userExperimentExtendedType.setLastBaseCoordonnee(line.get(UserExperimentExtendedTypeParser.LAST_BASE_COORD));
		}
		if(StringUtils.isNotBlank(line.get(UserExperimentExtendedTypeParser.SPOT_LENGTH))) {
			userExperimentExtendedType.setSpotLength(line.get(UserExperimentExtendedTypeParser.SPOT_LENGTH));
		}
		if(StringUtils.isNotBlank(line.get(UserExperimentExtendedTypeParser.LIBRARY_SELECTION))) {
			userExperimentExtendedType.setLibrarySelection(line.get(UserExperimentExtendedTypeParser.LIBRARY_SELECTION));
		}
		if(StringUtils.isNotBlank(line.get(UserExperimentExtendedTypeParser.LIBRARY_SOURCE))){
			userExperimentExtendedType.setLibrarySource(line.get(UserExperimentExtendedTypeParser.LIBRARY_SOURCE));
		}
		if(StringUtils.isNotBlank(line.get(UserExperimentExtendedTypeParser.LIBRARY_STRATEGY))) {
			userExperimentExtendedType.setLibraryStrategy(line.get(UserExperimentExtendedTypeParser.LIBRARY_STRATEGY));
		}
		if(StringUtils.isNotBlank(line.get(UserExperimentExtendedTypeParser.LIBRARY_PROTOCOL))) {
			userExperimentExtendedType.setLibraryProtocol(line.get(UserExperimentExtendedTypeParser.LIBRARY_PROTOCOL));
		}	
		if(StringUtils.isNotBlank(line.get(UserExperimentExtendedTypeParser.NOMINAL_LENGTH))) {
			userExperimentExtendedType.setNominalLength(line.get(UserExperimentExtendedTypeParser.NOMINAL_LENGTH));
		}
		if(StringUtils.isNotBlank(line.get(UserExperimentExtendedTypeParser.TITLE))) {
			userExperimentExtendedType.setTitle(line.get(UserExperimentExtendedTypeParser.TITLE));
		}	
		if(StringUtils.isNotBlank(line.get(UserExperimentExtendedTypeParser.STUDY_AC))) {
			userExperimentExtendedType.setStudyAccession(line.get(UserExperimentExtendedTypeParser.STUDY_AC));
		}
		if(StringUtils.isNotBlank(line.get(UserExperimentExtendedTypeParser.SAMPLE_AC))) {
			userExperimentExtendedType.setSampleAccession(line.get(UserExperimentExtendedTypeParser.SAMPLE_AC));
		}
		return userExperimentExtendedType;
	}

}

