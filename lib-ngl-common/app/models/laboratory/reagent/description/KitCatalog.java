package models.laboratory.reagent.description;

import java.util.List;
import java.util.function.Supplier;

import models.utils.InstanceConstants;
import ngl.refactoring.MiniDAO;

import org.mongojack.DBQuery;

import validation.ContextValidation;
import validation.reagentCatalogs.instance.KitCatalogValidationHelper;
import validation.utils.ValidationConstants;
import validation.utils.ValidationHelper;
import fr.cea.ig.MongoDBDAO;

public class KitCatalog extends AbstractCatalog {
	
	public static final Supplier<MiniDAO<KitCatalog>> catalogFind = MiniDAO.createSupplier(InstanceConstants.REAGENT_CATALOG_COLL_NAME, KitCatalog.class);
	 
	public String providerRefName;
	public String providerCode;
	public List<String> experimentTypeCodes;

	@Override
	public void validate(ContextValidation contextValidation) {
		ValidationHelper.validateNotEmpty(contextValidation, name, "name");
		ValidationHelper.validateNotEmpty(contextValidation, providerRefName, "providerRefName");
		ValidationHelper.validateNotEmpty(contextValidation, providerCode, "providerCode");
		ValidationHelper.validateNotEmpty(contextValidation, catalogRefCode, "catalogRefCode");
		ValidationHelper.validateNotEmpty(contextValidation, experimentTypeCodes, "experimentTypeCodes");
		if (!contextValidation.hasErrors()) {
			KitCatalogValidationHelper.validateCodePrimary(contextValidation, this, InstanceConstants.REAGENT_CATALOG_COLL_NAME);
			KitCatalogValidationHelper.validateExperimentTypes(experimentTypeCodes, contextValidation);
			if (contextValidation.isCreationMode()) {
				if (MongoDBDAO.checkObjectExist(InstanceConstants.REAGENT_CATALOG_COLL_NAME, KitCatalog.class, DBQuery.and(DBQuery.is("name",name)))) {
					contextValidation.addError("name", ValidationConstants.ERROR_NOTUNIQUE_MSG, name);
				}
				if (MongoDBDAO.checkObjectExist(InstanceConstants.REAGENT_CATALOG_COLL_NAME, KitCatalog.class, DBQuery.and(DBQuery.is("providerRefName",providerRefName)))) {
					contextValidation.addError("providerRefName", ValidationConstants.ERROR_NOTUNIQUE_MSG, providerRefName);
				}
			}
		}
	}
	
}
