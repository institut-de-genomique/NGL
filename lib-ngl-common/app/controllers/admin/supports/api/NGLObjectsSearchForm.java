package controllers.admin.supports.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import controllers.ListForm;
import validation.ContextValidation;
import validation.IValidation;
import validation.utils.ValidationHelper;

public class NGLObjectsSearchForm extends ListForm implements IValidation {
	
	public List<String> collectionNames;
	
	public List<String> codes;
	public String codeRegex;
	
	public String projectCode;
	public String sampleCode;
	
    public Map<String, List<String>> contentProperties = new HashMap<>();
    public String contentPropertyNameUpdated;
	
    @Override
	public void validate(ContextValidation contextValidation) {
		ValidationHelper.validateNotEmpty(contextValidation, collectionNames,            "collectionNames");
		ValidationHelper.validateNotEmpty(contextValidation, projectCode,                "projectCode");
		ValidationHelper.validateNotEmpty(contextValidation, sampleCode,                 "sampleCode");
		ValidationHelper.validateNotEmpty(contextValidation, contentProperties,          "contentProperties");
		ValidationHelper.validateNotEmpty(contextValidation, contentPropertyNameUpdated, "contentPropertyNameUpdated");
		if (!contextValidation.hasErrors()) {
			ValidationHelper.validateNotEmpty(contextValidation, contentProperties.get(contentPropertyNameUpdated), "contentProperties." + contentPropertyNameUpdated);
		}
	}
    
}
