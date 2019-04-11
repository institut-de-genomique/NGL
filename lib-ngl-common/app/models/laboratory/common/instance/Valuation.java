package models.laboratory.common.instance;

import java.util.Date;
import java.util.Set;

import validation.ContextValidation;
import validation.IValidation;
import validation.common.instance.CommonValidationHelper;
import validation.utils.ValidationHelper;

public class Valuation implements IValidation {
	
	/**
	 * Validity.
	 */
	public TBoolean valid = TBoolean.UNSET;
	
	/**
	 * Creation date.
	 */
    public Date date;
    
    /**
     * Creation user. 
     */
    public String user;
    
    /**
     * Extra info.
     */
    public Set<String> resolutionCodes;
    
    /**
     * Name of the rules to validate containing instance.
     */
    public String criteriaCode;
    
    /**
     * Comment.
     */
    public String comment;
    
    /**
     * Validate this valuation (context parameter {@link CommonValidationHelper#FIELD_TYPE_CODE}).
     */
	@Override
	@Deprecated
	public void validate(ContextValidation contextValidation) {
		ValidationHelper.validateNotEmpty             (contextValidation, valid, "valid");
		if (!TBoolean.UNSET.equals(valid)) {
			ValidationHelper.validateNotEmpty         (contextValidation, date, "date");
			ValidationHelper.validateNotEmpty         (contextValidation, user, "user");
		}
		CommonValidationHelper.validateResolutionCodes(contextValidation, resolutionCodes);
		// TODO : resolution si different de zero
		CommonValidationHelper.validateCriteriaCode   (contextValidation, criteriaCode); 
	}
	
	public void validate(ContextValidation contextValidation, String typeCode) {
		ValidationHelper.validateNotEmpty             (contextValidation, valid, "valid");
		if (!TBoolean.UNSET.equals(valid)) {
			ValidationHelper.validateNotEmpty         (contextValidation, date, "date");
			ValidationHelper.validateNotEmpty         (contextValidation, user, "user");
		}
		CommonValidationHelper.validateResolutionCodes(contextValidation, typeCode, resolutionCodes);
		// TODO : resolution si different de zero
		CommonValidationHelper.validateCriteriaCode   (contextValidation, typeCode, criteriaCode); 
	}

}
