package validation.reagent.instance;

import models.laboratory.reagent.description.AbstractCatalog;
import validation.ContextValidation;

public class ReceptionValidationHelper {

	public static void validateEmptyCatalogOnUpdate(ContextValidation contextValidation, AbstractCatalog catalog,
			String property) {
		if (catalog != null && (contextValidation.isCreationMode() || contextValidation.isUpdateMode())) {
			contextValidation.addError(property, "error.notnull", catalog);
		}
	}

}
