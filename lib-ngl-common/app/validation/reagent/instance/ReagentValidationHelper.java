package validation.reagent.instance;

import models.laboratory.reagent.instance.Box;
import models.laboratory.reagent.instance.Reagent;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;

public class ReagentValidationHelper extends CommonValidationHelper {
	
	public static void validateReagentCatalogCode(String code, ContextValidation contextValidation) {
//		validateExistInstanceCode(contextValidation, code, Reagent.class, InstanceConstants.REAGENT_CATALOG_COLL_NAME);
		validateCodeForeignOptional(contextValidation, Reagent.catalogFind.get(), code);
	}
	
	public static void validateBoxCode(String code, ContextValidation contextValidation) {
//		validateExistInstanceCode(contextValidation, code, Box.class, InstanceConstants.REAGENT_INSTANCE_COLL_NAME);
		validateCodeForeignOptional(contextValidation, Box.find.get(), code);
	}
	
}
