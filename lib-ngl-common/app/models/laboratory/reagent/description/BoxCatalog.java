package models.laboratory.reagent.description;

import models.utils.InstanceConstants;

import org.mongojack.DBQuery;

import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.reagentCatalogs.instance.KitCatalogValidationHelper;
import validation.utils.ValidationConstants;
import validation.utils.ValidationHelper;
import fr.cea.ig.MongoDBDAO;

public class BoxCatalog extends AbstractCatalog {
	
	public String  kitCatalogCode;
	public Double  storageConditions;
	public Integer possibleUseNumber;

	@Override
	public void validate(ContextValidation contextValidation) {
		ValidationHelper.validateNotEmpty(contextValidation, name, "name");
		ValidationHelper.validateNotEmpty(contextValidation, catalogRefCode, "catalogRefCode");
		CommonValidationHelper.validateIdPrimary(contextValidation, this);
		CommonValidationHelper.validateTraceInformationRequired(contextValidation, traceInformation);
		if (!contextValidation.hasErrors()) {
			CommonValidationHelper.validateCodePrimary(contextValidation, this, InstanceConstants.REAGENT_CATALOG_COLL_NAME);
			KitCatalogValidationHelper.validateKitCatalogCode(contextValidation, kitCatalogCode);
			if (contextValidation.isCreationMode()) {
				if (MongoDBDAO.checkObjectExist(InstanceConstants.REAGENT_CATALOG_COLL_NAME, ReagentCatalog.class, DBQuery.and(DBQuery.is("catalogRefCode",catalogRefCode), DBQuery.is("kitCatalogCode",kitCatalogCode)))) {
					contextValidation.addError("catalogRefCode", ValidationConstants.ERROR_NOTUNIQUE_MSG, catalogRefCode);
				}
				if (MongoDBDAO.checkObjectExist(InstanceConstants.REAGENT_CATALOG_COLL_NAME, ReagentCatalog.class, DBQuery.and(DBQuery.is("name",name), DBQuery.is("kitCatalogCode",kitCatalogCode)))) {
					contextValidation.addError("name", ValidationConstants.ERROR_NOTUNIQUE_MSG, name);
				}
			}
		}
	}
	
}
