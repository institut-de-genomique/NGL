package models.laboratory.parameter.map;


import validation.ContextValidation;
import validation.IValidation;
import validation.parameter.instance.MapParameterValidationHelper;
import validation.utils.ValidationHelper;

public class MapParameterEntry implements IValidation {

	public String comment;
	public Entry entry ;
	
	@Override
	public void validate(ContextValidation contextValidation ) {
		if(ValidationHelper.validateNotEmpty(contextValidation,entry,"entry")) {
			if(ValidationHelper.validateNotEmpty(contextValidation,entry.parent,"parent")) {
				MapParameterValidationHelper.validateParentExist(contextValidation, entry.parent);
			}
			if(ValidationHelper.validateNotEmpty(contextValidation,entry.child,"child")) {
				MapParameterValidationHelper.validateChildExist(contextValidation, entry.child);
			}
		}
	}
	
	public static class Entry {

		public  String parent; 
		public  String child;
		
	}
}