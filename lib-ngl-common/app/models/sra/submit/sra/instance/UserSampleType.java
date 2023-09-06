package models.sra.submit.sra.instance;


import org.apache.commons.lang3.StringUtils;

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
		String mess = "UserSampleType ";
		if (StringUtils.isNotBlank(code)) {
			mess += "." + code;
		}
		
		contextValidation.addKeyToRootKeyName(mess);
		ValidationHelper.validateNotEmpty(contextValidation, code, "code");
		SraValidationHelper.validateFreeText(contextValidation, this.getDescription(), "description");
		SraValidationHelper.validateFreeText(contextValidation, this.getTitle(), "title");
		SraValidationHelper.validateFreeText(contextValidation, this.getAnonymizedName(), "");	
		//NGL-4235 : on veut laisser passer des samples invalides, sans attributes, qui pourront etre completes à la validation. 
		switch (contextValidation.getMode()) {
		case CREATION:
			SraValidationHelper   .validateAttributes (contextValidation,"attributes", this.attributes);
			break;
		case DELETE:
			SraValidationHelper   .validateAttributes (contextValidation,"attributes", this.attributes);
			break;
		case UPDATE:
			SraValidationHelper   .newValidateAttributesRequired (contextValidation,"attributes", this.attributes);
			break;
		case NOT_DEFINED:
			SraValidationHelper   .validateAttributes (contextValidation,"attributes", this.attributes);
			break;
		}
		contextValidation.removeKeyFromRootKeyName(mess);

	}
}
