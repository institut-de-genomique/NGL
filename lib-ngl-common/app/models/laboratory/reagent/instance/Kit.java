package models.laboratory.reagent.instance;

import java.util.function.Supplier;

import models.utils.InstanceConstants;
import ngl.refactoring.MiniDAO;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.reagent.instance.KitValidationHelper;
import validation.utils.ValidationHelper;

public class Kit extends AbstractDeclaration {
	
	public static final Supplier<MiniDAO<Kit>> find = MiniDAO.createSupplier(InstanceConstants.REAGENT_INSTANCE_COLL_NAME, Kit.class); 

	public String catalogCode;

	@Override
	public void validate(ContextValidation contextValidation) {
		ValidationHelper.validateNotEmpty(contextValidation, catalogCode,   "catalogCode");
		ValidationHelper.validateNotEmpty(contextValidation, code,          "code");
		ValidationHelper.validateNotEmpty(contextValidation, receptionDate, "receptionDate");
		if (!contextValidation.hasErrors()) {
			CommonValidationHelper.validateCodePrimary           (contextValidation, this, InstanceConstants.REAGENT_INSTANCE_COLL_NAME);
			// As there is a validate not empty for the catalog code, this is like a required code
			// instead of an optional.
			KitValidationHelper   .validateKitCatalogCodeOptional(contextValidation, catalogCode);
		}
	}
	
}
