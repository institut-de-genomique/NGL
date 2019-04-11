package validation.reagent.instance;

import models.laboratory.reagent.description.KitCatalog;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;

public class KitValidationHelper extends CommonValidationHelper {
	
	public static void validateKitCatalogCode(String code, ContextValidation contextValidation) {
//		validateExistInstanceCode(contextValidation, code, KitCatalog.class, InstanceConstants.REAGENT_CATALOG_COLL_NAME);
		validateCodeForeignOptional(contextValidation, KitCatalog.catalogFind.get(), code);
	}
	
}
