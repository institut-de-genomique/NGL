package models.laboratory.project.instance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fr.cea.ig.DBObject;
import models.laboratory.common.instance.Comment;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.TraceInformation;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.IValidation;
import validation.common.instance.CommonValidationHelper;
import validation.project.instance.UmbrellaProjectValidationHelper;

/**
 * Instance UmbrellaProject is stocked in a MongoDB Collection named "ngl_project.UmbrellaProject"
 * Use by "ngl-projects" application
 * 
 * @author dnoisett
 * 21-08-2014
 */
public class UmbrellaProject extends DBObject implements IValidation {

	public String 						name;
	public String                     	typeCode;
	public String                     	categoryCode;
	public TraceInformation 			traceInformation;

	public String 						description;
	public Map<String, PropertyValue> 	properties = new HashMap<>();
	public List<Comment> 				comments = new ArrayList<Comment>();
	
	@Override
	@JsonIgnore
	public void validate(ContextValidation contextValidation) {
		CommonValidationHelper.validateIdPrimary               (contextValidation, this);
		CommonValidationHelper.validateCodePrimary             (contextValidation, this, InstanceConstants.UMBRELLA_PROJECT_COLL_NAME);
		CommonValidationHelper.validateTraceInformationRequired(contextValidation, traceInformation);

		// Ne fonctionne pas pour le moment.
		// UmbrellaProjectValidationHelper.validateUmbrellaProjectTypeCodeRequired      (contextValidation, typeCode, properties); 
		UmbrellaProjectValidationHelper.validateUmbrellaProjectCategoryCodeRequired  (contextValidation, categoryCode);
	}
}
