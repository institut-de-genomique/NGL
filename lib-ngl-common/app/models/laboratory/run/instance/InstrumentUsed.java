package models.laboratory.run.instance;

import models.laboratory.instrument.description.Instrument;
import models.laboratory.instrument.description.InstrumentUsedType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import validation.ContextValidation;
import validation.IValidation;
import validation.common.instance.CommonValidationHelper;

/**
 * Instrument used to obtain the run.
 * 
 * @author galbini
 *
 */
public class InstrumentUsed implements IValidation {

	// specific class for the run;
	public String code;
	public String typeCode;

	/**
	 * Validate the required instrument code and the required instrument type code. 
	 */
	@JsonIgnore
	@Override
	public void validate(ContextValidation contextValidation) {
		CommonValidationHelper.validateCodeForeignRequired(contextValidation, Instrument.miniFind.get(),         code,     "code");
		CommonValidationHelper.validateCodeForeignRequired(contextValidation, InstrumentUsedType.miniFind.get(), typeCode, "typeCode");
	}

}
