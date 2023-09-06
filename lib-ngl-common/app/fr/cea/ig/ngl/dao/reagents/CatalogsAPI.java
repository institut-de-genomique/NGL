package fr.cea.ig.ngl.dao.reagents;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.mongojack.DBQuery;

import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.GenericAPI;
import models.laboratory.reagent.description.AbstractCatalog;
import models.laboratory.reagent.description.KitCatalog;

public class CatalogsAPI extends GenericAPI<CatalogsDAO, AbstractCatalog> {
	
	private static final String CATALOG_REF_CODE = "catalogRefCode";
	
	private static final String PROVIDER_CODE = "providerCode";
	
	private static final String KIT_CATALOG_NAME = "name";
	
	private static final String KIT_CATALOG_CODE = "kitCatalogCode";
	
	private static final String CATEGORY = "category";
	
	private static final String CATEGORY_KIT = "Kit";

	private static final List<String> authorizedUpdateFields = Collections.unmodifiableList(Arrays.asList());
	private static final List<String> defaultKeys = Collections.unmodifiableList(Arrays.asList());

	@Inject
	public CatalogsAPI(CatalogsDAO dao) {
		super(dao);
	}

	@Override
	protected List<String> authorizedUpdateFields() {
		return authorizedUpdateFields;
	}

	@Override
	protected List<String> defaultKeys() {
		return defaultKeys;
	}

	@Override
	public AbstractCatalog create(AbstractCatalog input, String currentUser)
			throws APIValidationException, APIException {
		throw new UnsupportedOperationException();
	}

	@Override
	public AbstractCatalog update(AbstractCatalog input, String currentUser)
			throws APIException, APIValidationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public AbstractCatalog update(AbstractCatalog input, String currentUser, List<String> fields)
			throws APIException, APIValidationException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get catalogs by catalogRefCode.
	 * @param catalogRefCode
	 * @return catalogs
	 */
	public Iterable<AbstractCatalog> getByRef(String catalogRefCode) {
		return this.dao.find(DBQuery.and(DBQuery.is(CATALOG_REF_CODE, catalogRefCode)));
	}

	/**
	 * Get Reagent or Box catalog by catalogRefCode and its kitCatalog's providerCode and name.
	 * @param catalogRefCode
	 * @param providerCode
	 * @param kitCatalogName
	 * @return reagent or box catalog
	 */
	public AbstractCatalog getByRefAndProvider(String catalogRefCode, String providerCode, String kitCatalogName) {
		Iterator<AbstractCatalog> kitCatalogs = this.dao.find(
				DBQuery.and(
						DBQuery.is(CATEGORY, CATEGORY_KIT), 
						DBQuery.is(PROVIDER_CODE, providerCode), 
						DBQuery.is(KIT_CATALOG_NAME, kitCatalogName)
						)
				).iterator();
		// Si aucun résultat
		if (!kitCatalogs.hasNext()) {
			throw new IllegalStateException("No catalog found for name : " + kitCatalogName + " and provider : " + providerCode);
		}
		KitCatalog kitCatalog = (KitCatalog) kitCatalogs.next();
		// Si plusieurs kitCatalogs correspondent, il y a une erreur
		if (kitCatalogs.hasNext()) {
			throw new IllegalStateException("More than one result for catalog Reference : " + catalogRefCode + " and provider : " + providerCode);
		}
		Iterator<AbstractCatalog> catalogs = this.dao.find(
				DBQuery.and(
						DBQuery.is(CATALOG_REF_CODE, catalogRefCode), 
						DBQuery.is(KIT_CATALOG_CODE, kitCatalog.code)
						)
				).iterator();
		// Si aucun résultat
		if (!catalogs.hasNext()) {
			return null;
		}
		AbstractCatalog catalog = catalogs.next();
		// Si plusieurs catalogs correspondent, il y a une erreur
		if (catalogs.hasNext()) {
			throw new IllegalStateException("More than one result for catalog Reference : " + catalogRefCode + " (" + kitCatalogName + ") and provider : " + providerCode);
		} return catalog;
	}

}
