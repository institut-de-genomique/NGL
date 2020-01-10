package validation.reagentCatalogs.instance;

import java.util.List;

import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.reagent.description.BoxCatalog;
import models.laboratory.reagent.description.KitCatalog;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.utils.ValidationConstants;
import validation.utils.ValidationHelper;

public class KitCatalogValidationHelper {

	// --------------------------------------------------------------------------
	// renamed and arguments reordered
	
	/**
	 * Validate a list of required experiment type codes. 
	 * @param experimentTypes   experiment type codes 
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateExperimentTypeCodes(ContextValidation, List)}
	 */
	@Deprecated
	public static void validateExperimentTypes(List<String> experimentTypes, ContextValidation contextValidation) {
		KitCatalogValidationHelper.validateExperimentTypeCodes(contextValidation, experimentTypes);
	}
	
	/**
	 * Validate a list of required experiment type codes. 
	 * @param contextValidation validation context
	 * @param experimentTypes   experiment type codes 
	 */
	public static void validateExperimentTypeCodes(ContextValidation contextValidation, List<String> experimentTypes) {
		for (String et : experimentTypes)
			CommonValidationHelper.validateCodeForeignRequired(contextValidation, ExperimentType.miniFind.get(), et, "experimentTypeCodes", true);
	}

	// --------------------------------------------------------------------------
	// arguments reordered
	
	/**
	 * Validate a required kit catalog code.
	 * @param kitCatalogCode    kit catalog code
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateKitCatalogCode(ContextValidation, String)}
	 */
	@Deprecated
	public static void validateKitCatalogCode(String kitCatalogCode, ContextValidation contextValidation) {
		KitCatalogValidationHelper.validateKitCatalogCode(contextValidation, kitCatalogCode);
	}

	/**
	 * Validate a required kit catalog code.
	 * @param contextValidation validation context
	 * @param kitCatalogCode    kit catalog code
	 */
	public static void validateKitCatalogCode(ContextValidation contextValidation, String kitCatalogCode) {
		if (ValidationHelper.validateNotEmpty(contextValidation, kitCatalogCode, "kitCatalogCode")) {
			if (!MongoDBDAO.checkObjectExist(InstanceConstants.REAGENT_CATALOG_COLL_NAME, KitCatalog.class, DBQuery.is("code",kitCatalogCode))) {
				contextValidation.addError("kitCatalogCode", ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG, kitCatalogCode);
			}
		}
	}

	// --------------------------------------------------------------------------
	// arguments reordered

	/**
	 * Validate a required box catalog code.
	 * @param boxCatalogCode    box catalog code
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateBoxCatalogCode(ContextValidation, String)}
	 */
	@Deprecated
	public static void validateBoxCatalogCode(String boxCatalogCode, ContextValidation contextValidation) {
		KitCatalogValidationHelper.validateBoxCatalogCode(contextValidation, boxCatalogCode);
	}

	/**
	 * Validate a required box catalog code.
	 * @param contextValidation validation context
	 * @param boxCatalogCode    box catalog code
	 */
	public static void validateBoxCatalogCode(ContextValidation contextValidation, String boxCatalogCode) {
		if (ValidationHelper.validateNotEmpty(contextValidation, boxCatalogCode, "boxCatalogCode")) {
			if (!MongoDBDAO.checkObjectExist(InstanceConstants.REAGENT_CATALOG_COLL_NAME, BoxCatalog.class, DBQuery.and(DBQuery.is("code",boxCatalogCode)))){
				contextValidation.addError("boxCatalogCode", ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG, boxCatalogCode);
			}
		}
	}

}
