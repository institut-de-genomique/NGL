package validation.reagent.instance;

import models.laboratory.reagent.instance.Reagent;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;

public class ReagentValidationHelper {

	// -------------------------------------------------------------
	// renamed and arguments reordered
	
	/**
	 * Validate an optional reagent catalog code.
	 * @param code              reagent ctalog code
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateReagentCatalogCodeOptional(ContextValidation, String)}
	 */
	@Deprecated
	public static void validateReagentCatalogCode(String code, ContextValidation contextValidation) {
		ReagentValidationHelper.validateReagentCatalogCodeOptional(contextValidation, code);
	}
	
	/**
	 * Validate an optional reagent catalog code.
	 * @param contextValidation validation context
	 * @param code              reagent ctalog code
	 */
	public static void validateReagentCatalogCodeOptional(ContextValidation contextValidation, String code) {
		CommonValidationHelper.validateCodeForeignOptional(contextValidation, Reagent.catalogFind.get(), code);
	}
	
	// -------------------------------------------------------------
//	// renamed and arguments reordered
//	
//	/**
//	 * Validate an optional box code.
//	 * @param code              box code
//	 * @param contextValidation validation context
//	 * @deprecated use {@link #validateBoxCodeOptional(ContextValidation, String)}
//	 */
//	public static void validateBoxCode(String code, ContextValidation contextValidation) {
//		ReagentValidationHelper.validateBoxCodeOptional(contextValidation, code);
//	}
//
//	/**
//	 * Validate an optional box code.
//	 * @param contextValidation validation context
//	 * @param code              box code
//	 */
//	public static void validateBoxCodeOptional(ContextValidation contextValidation, String code) {
//		CommonValidationHelper.validateCodeForeignOptional(contextValidation, Box.find.get(), code);
//	}
//
//	// -------------------------------------------------------------
	
}
