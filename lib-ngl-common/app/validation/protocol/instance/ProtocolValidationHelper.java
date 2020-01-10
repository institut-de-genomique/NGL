package validation.protocol.instance;

import static validation.utils.ValidationHelper.validateNotEmpty;

import validation.ContextValidation;
import validation.utils.ValidationConstants;

public class ProtocolValidationHelper {

	// renamed and arguments reordered
	
	/**
	 * Validate a required protocol category code.
	 * @param categoryCode      protocol category code
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateProtocolCategoryCodeRequired(ContextValidation, String)}
	 */
	@Deprecated
	public static void validateProtocolCategoryCode(String categoryCode, ContextValidation contextValidation) {
		ProtocolValidationHelper.validateProtocolCategoryCodeRequired(contextValidation, categoryCode);
	}
	
	/**
	 * Validate a required protocol category code.
	 * @param contextValidation validation context
	 * @param categoryCode      protocol category code
	 */
	public static void validateProtocolCategoryCodeRequired(ContextValidation contextValidation, String categoryCode) {
		if (validateNotEmpty(contextValidation, categoryCode, "code")) {
			if (!models.laboratory.experiment.description.ProtocolCategory.find.get().isCodeExist(categoryCode)) {
				contextValidation.addError("protocoles.categoryCode", ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG, categoryCode);
			}
		}
	}

}
