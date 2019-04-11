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

public class KitCatalogValidationHelper extends CommonValidationHelper {

	public static void validateExperimentTypes(List<String> experimentTypes, ContextValidation contextValidation) {
		for (String et : experimentTypes)
//			BusinessValidationHelper.validateRequiredDescriptionCode(contextValidation, et, "experimentTypeCodes", ExperimentType.find.get(),true);
			validateCodeForeignRequired(contextValidation, ExperimentType.miniFind.get(), et, "experimentTypeCodes", true);
	}

	public static void validateKitCatalogCode(String kitCatalogCode, ContextValidation contextValidation) {
		if (ValidationHelper.validateNotEmpty(contextValidation, kitCatalogCode, "kitCatalogCode")) {
			if (!MongoDBDAO.checkObjectExist(InstanceConstants.REAGENT_CATALOG_COLL_NAME, KitCatalog.class, DBQuery.is("code",kitCatalogCode))) {
				contextValidation.addError("kitCatalogCode", ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG, kitCatalogCode);
			}
		}
	}

	public static void validateBoxCatalogCode(String boxCatalogCode, ContextValidation contextValidation) {
		if (ValidationHelper.validateNotEmpty(contextValidation, boxCatalogCode, "boxCatalogCode")) {
			if (!MongoDBDAO.checkObjectExist(InstanceConstants.REAGENT_CATALOG_COLL_NAME, BoxCatalog.class, DBQuery.and(DBQuery.is("code",boxCatalogCode)))){
				contextValidation.addError("boxCatalogCode", ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG, boxCatalogCode);
			}
		}
	}
	
}
