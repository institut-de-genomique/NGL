package sra.parser;

import models.sra.submit.common.instance.UserRefCollabType;
import models.sra.submit.common.instance.UserExperimentExtendedType;
import models.sra.submit.common.instance.UserExperimentType;
import models.sra.submit.common.instance.UserSampleType;

public class UserObjectFactory {

	/**
     * Create a new CnsFactory that can be used to create new instances of schema derived classes for package: fr.genoscope.lis.devsi.sra
     * 
     */
    public UserObjectFactory() {	
    }
    
    /**
     * Create an instance of UserRefCollabType.
     * @return instance of UserRefCollabType
     */
    public UserRefCollabType createUserRefCollabType() {
        return new UserRefCollabType();
    }

    /**
     * Create an instance of UserExperimentType.
     * @return instance of UserExperimentType
     */
    public UserExperimentType createUserExperimentType() {
        return new UserExperimentType();
    }
    
    /**
     * Create an instance of UserExperimentType.
     * @return instance of UserExperimentType
     */
    public UserSampleType createUserSampleType() {
        return new UserSampleType();
    }

    /**
     * Create an instance of UserExperimentExtendedType. (ajout modification par user de studyAccession et studySample si update à l'EBI)
     * @return instance of UserExperimentExtendedType
     */
	public UserExperimentExtendedType createUserExperimentExtendedType() {
        return new UserExperimentExtendedType();
	}
}
