package controllers.admin.supports.api;

import fr.cea.ig.DBObject;
import validation.ContextValidation;
import validation.IValidation;
import validation.utils.ValidationHelper;

// Looks like a kind of command object. The representation looks like
// the union of all the possible commands arguments. The validation method
// is never called so it's just an illusion (removing 'implements IValidation'
// still compiles). The action attribute would probably have been better
// handled by command subclasses.

public class NGLObject extends DBObject implements IValidation {
//public class NGLObject extends DBObject {

	public enum Action { delete, replace, exchange, add }

	public String typeCode;		
	public String collectionName;

	public String contentPropertyNameUpdated;
	public String projectCode;
	public String sampleCode;
	public String currentValue;
	public String newValue;
	public String readSetToSwitchCode;

	public String action;
	public Long   nbOccurrences;

	@Override
	public void validate(ContextValidation contextValidation) {
		ValidationHelper.validateNotEmpty(contextValidation, code,                       "code");
		ValidationHelper.validateNotEmpty(contextValidation, collectionName,             "collectionName");
		ValidationHelper.validateNotEmpty(contextValidation, contentPropertyNameUpdated, "contentPropertyNameUpdated");
		ValidationHelper.validateNotEmpty(contextValidation, projectCode,                "projectCode");
		ValidationHelper.validateNotEmpty(contextValidation, sampleCode,                 "sampleCode");
		ValidationHelper.validateNotEmpty(contextValidation, currentValue,               "currentValue");

		if (ValidationHelper.validateNotEmpty(contextValidation, action, "action") && Action.replace.toString().equals(action)) {
			ValidationHelper.validateNotEmpty(contextValidation, newValue, "newValue");
		}
	}

}
