package models.laboratory.reagent.instance;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonInclude;

import models.laboratory.reagent.description.BoxCatalog;
import validation.ContextValidation;
import validation.reagent.instance.ReceptionValidationHelper;

public class ReagentReception extends AbstractReception {

	/* Catalogues pour les consultations. */
	@JsonInclude(NON_NULL)
	public BoxCatalog catalogBox;

	@Override
	public void validate(ContextValidation contextValidation) {
		super.validate(contextValidation);
		ReceptionValidationHelper.validateEmptyCatalogOnUpdate(contextValidation, this.catalogBox, "catalogBox");
	}

}
