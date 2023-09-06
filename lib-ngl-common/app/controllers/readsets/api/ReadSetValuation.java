package controllers.readsets.api;

import models.laboratory.common.instance.Valuation;
import validation.ContextValidation;
import validation.IValidation;
import validation.utils.ValidationHelper;

public class ReadSetValuation implements IValidation {
	public Valuation productionValuation;
	public Valuation bioinformaticValuation;
	
	@Override
	public void validate(ContextValidation contextValidation) {
		ValidationHelper.validateNotEmpty             (contextValidation, productionValuation, "productionValuation");
		ValidationHelper.validateNotEmpty             (contextValidation, bioinformaticValuation, "bioinformaticValuation");
	}
	
}
