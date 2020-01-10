package validation.reagent.instance;

import models.laboratory.reagent.description.KitCatalog;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;

public class KitValidationHelper {
	
	// renamed and arguments reordered
	
	/**
	 * Validate kit catalog code.
	 * @param code              kit catalog code
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateKitCatalogCodeOptional(ContextValidation, String)}
	 */
	@Deprecated
	public static void validateKitCatalogCode(String code, ContextValidation contextValidation) {
		KitValidationHelper.validateKitCatalogCodeOptional(contextValidation, code);
	}
	
	/**
	 * Validate kit catalog code.
	 * @param contextValidation validation context
	 * @param code              kit catalog code
	 */
	public static void validateKitCatalogCodeOptional(ContextValidation contextValidation, String code) {
		CommonValidationHelper.validateCodeForeignOptional(contextValidation, KitCatalog.catalogFind.get(), code);
	}

}
