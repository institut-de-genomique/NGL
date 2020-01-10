package validation.reagent.instance;

import models.laboratory.reagent.instance.Box;
import models.laboratory.reagent.instance.Kit;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;

public class BoxValidationHelper {
	
	// -----------------------------------------------------------------------------
	// renamed and arguments reordered
	
	/**
	 * Validate a box catalog code.
	 * @param code              box catalog code
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateBoxCatalogCodeOptional(ContextValidation, String)}
	 */
	@Deprecated
	public static void validateBoxCatalogCode(String code, ContextValidation contextValidation) {
		validateBoxCatalogCodeOptional(contextValidation, code);
	}
	
	/**
	 * Validate a box catalog code.
	 * @param contextValidation validation context
	 * @param code              box catalog code
	 */
	public static void validateBoxCatalogCodeOptional(ContextValidation contextValidation, String code) {
		CommonValidationHelper.validateCodeForeignOptional(contextValidation, Box.catalogFind.get(), code);
	}
	
	// --------------------------------------------------------------------------------
	
	/**
	 * Validate a kit code.
	 * @param code              kit code
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateKitCodeOptional(ContextValidation, String)}
	 */
	@Deprecated
	public static void validateKitCode_(String code, ContextValidation contextValidation) {
		validateKitCodeOptional(contextValidation, code);
	}

	/**
	 * Validate a kit code.
	 * @param contextValidation validation context
	 * @param code              kit code
	 */
	public static void validateKitCodeOptional(ContextValidation contextValidation, String code) {
		CommonValidationHelper.validateCodeForeignOptional(contextValidation, Kit.find.get(), code);
	}

}
