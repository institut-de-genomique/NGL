package models.laboratory.reagent.instance;

import validation.ContextValidation;
import validation.IValidation;

public class ReagentUsed implements IValidation {

	public String kitCatalogCode;     // The ref of the kit in the catalog (description)
	public String boxCatalogCode;     // The ref of the box in the catalog (description)
	public String reagentCatalogCode; // The ref of the reagent in the catalog (description)
	
	public String code;               // the code of the reagent (instance)
	public String boxCode;            // the code of the box (instance)
	
	public String description;
	
	/**
	 * No validation done.
	 */
	@Override
	public void validate(ContextValidation contextValidation) {
		//DescriptionValidationHelper.validationReagentTypeCode(categoryCode,contextValidation);
	}
	
}
