package validation.reagent.instance;

import models.laboratory.reagent.description.AbstractCatalog;
import validation.ContextValidation;
import validation.utils.ValidationHelper;

public class ReceptionValidationHelper {
	
	public static final play.Logger.ALogger logger = play.Logger.of(ReceptionValidationHelper.class);
	
	/**
	 * Reception state context parameter key.
	 */
	public static final String FIELD_STATE_RECEPTION_CONTEXT = "stateReceptionContext";
	
	/**
	 * State context.
	 */
	public static final String STATE_CONTEXT_IMPORT_FILE_ILLUMINA = "importFileIllumina";
	
	public static boolean validateMendatoryProperty(ContextValidation contextValidation, Object object, String property) {
		if (ValidationHelper.isEmpty(object)) {
			logger.debug("validateMendatoryProperty failed {} : {} {}", property,  object == null ? null : object.getClass(), object);
			contextValidation.addError("Erreur Réception :", "error.reagent.reception.mapping.row.mendatory.property", property);
			return false;
		}
        return true;
	}

	public static void validateEmptyCatalogOnUpdate(ContextValidation contextValidation, AbstractCatalog catalog,
			String property) {
		if (catalog != null && (contextValidation.isCreationMode() || contextValidation.isUpdateMode())) {
			contextValidation.addError(property, "error.notnull", catalog);
		}
	}

}
