package services;

import models.sra.submit.common.instance.UserCloneType;
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
     * Create an instance of UserCloneType.
     * @return instance of UserCloneType
     */
    public UserCloneType createUserCloneType() {
        return new UserCloneType();
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
    
}
