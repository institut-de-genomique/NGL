package models.laboratory.project.instance;

import org.mongojack.MongoCollection;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fr.cea.ig.DBObject;
import models.laboratory.common.instance.TraceInformation;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.IValidation;
import validation.common.instance.CommonValidationHelper;

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
		CommonValidationHelper.validateIdPrimary               (contextValidation, this);
		CommonValidationHelper.validateCodePrimary             (contextValidation, this, InstanceConstants.UMBRELLA_PROJECT_COLL_NAME);
		CommonValidationHelper.validateTraceInformationRequired(contextValidation, traceInformation);
	}
	
}
