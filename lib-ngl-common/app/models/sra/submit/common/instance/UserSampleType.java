package models.sra.submit.common.instance;

import play.Logger;
import validation.ContextValidation;
import validation.sra.SraValidationHelper;
import validation.utils.ValidationHelper;

public class UserSampleType {
	private String code           = null;
	private String title          = null;
	private String anonymizedName = null;
	private String description    = null;
	private String attributes     = null;

	
	public UserSampleType() {
	}
	
	public UserSampleType(String code) {
		this.setCode(code);
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}


	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAttributes() {
		return attributes;
	}

	public void setAttributes(String attributes) {
		this.attributes = attributes;
	}
	public String getAnonymizedName() {
		return anonymizedName;
	}

	public void setAnonymizedName(String anonymizedName) {
		this.anonymizedName = anonymizedName;
	}
	
	public void validate(ContextValidation contextValidation) {
		Logger.debug("Dans UserSampleType.validate: ");
		contextValidation = contextValidation.appendPath("sample");
		if (ValidationHelper.validateNotEmpty(contextValidation, code, "code")) 
			contextValidation = contextValidation.appendPath(code);				
		SraValidationHelper.validateFreeText(contextValidation, this.getDescription(), "description");
		SraValidationHelper.validateFreeText(contextValidation, this.getTitle(), "title");
		SraValidationHelper.validateFreeText(contextValidation, this.getAnonymizedName(), "");
		SraValidationHelper.validateAttributes (contextValidation,"attributes", this.getAttributes());
	}
}
