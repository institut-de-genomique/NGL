package models.laboratory.reagent.instance;

import java.util.Date;
import java.util.function.Supplier;

import models.laboratory.common.description.State;
import models.utils.InstanceConstants;
import ngl.refactoring.MiniDAO;
import validation.ContextValidation;
import validation.reagent.instance.ReagentValidationHelper;
import validation.utils.ValidationHelper;

public class Reagent extends AbstractDeclaration {
	
	public static final Supplier<MiniDAO<Reagent>> find        = MiniDAO.createSupplier(InstanceConstants.REAGENT_INSTANCE_COLL_NAME, Reagent.class); 
	public static final Supplier<MiniDAO<Reagent>> catalogFind = MiniDAO.createSupplier(InstanceConstants.REAGENT_CATALOG_COLL_NAME,  Reagent.class); 
	
	public String boxCode; // To delete
	
	public String catalogCode;
	public String boxBarCode;
	public String kitCode;
	
	public Date startToUseDate;
	public Date stopToUseDate;
	
	public State state;
	
	public Date expirationDate;
	
	public String providerID;
	public String lotNumber;
	
	public String boxCatalogRefCode;
	
	public String stockInformation;

	@Override
	public void validate(ContextValidation contextValidation) {
		ValidationHelper.validateNotEmpty(contextValidation, code,           "code");
		ValidationHelper.validateNotEmpty(contextValidation, catalogCode,    "catalogCode");
		ValidationHelper.validateNotEmpty(contextValidation, providerID,     "providerID");
		ValidationHelper.validateNotEmpty(contextValidation, receptionDate,  "receptionDate");
		ValidationHelper.validateNotEmpty(contextValidation, expirationDate, "expirationDate");
		ValidationHelper.validateNotEmpty(contextValidation, state,          "state");
		ValidationHelper.validateNotEmpty(contextValidation, orderCode,      "orderCode");

		if (!contextValidation.hasErrors()) {
			ReagentValidationHelper.validateCodePrimary(contextValidation, this, InstanceConstants.REAGENT_INSTANCE_COLL_NAME);
			ReagentValidationHelper.validateReagentCatalogCode(catalogCode, contextValidation);
		}
	}
	
}
