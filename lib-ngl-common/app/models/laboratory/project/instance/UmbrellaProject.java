package models.laboratory.project.instance;

import com.fasterxml.jackson.annotation.JsonIgnore;

import validation.ContextValidation;
import validation.IValidation;
import validation.project.instance.ProjectValidationHelper;

import models.laboratory.common.instance.TraceInformation;
import models.utils.InstanceConstants;
import org.mongojack.MongoCollection;
import fr.cea.ig.DBObject;

/**
 * Instance UmbrellaProject is stocked in a MongoDB Collection named "ngl_project.UmbrellaProject"
 * Use by "ngl-projects" application
 * 
 * @author dnoisett
 * 21-08-2014
 */

@MongoCollection(name="UmbrellaProject")
public class UmbrellaProject extends DBObject implements IValidation {

	public String name;
	public TraceInformation traceInformation;
	
	
	@Override
	@JsonIgnore
	public void validate(ContextValidation contextValidation) {
		ProjectValidationHelper.validateIdPrimary(contextValidation, this);
		ProjectValidationHelper.validateCodePrimary(contextValidation, this, InstanceConstants.UMBRELLA_PROJECT_COLL_NAME);
		ProjectValidationHelper.validateTraceInformationRequired(contextValidation, traceInformation);
	}
	
}
