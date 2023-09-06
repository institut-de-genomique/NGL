package controllers.reagents.io.receptions.helpers;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import controllers.reagents.io.receptions.mappers.row.RequiredPropertiesRowMapper.RequiredProperties;
import fr.cea.ig.ngl.dao.reagents.CatalogsAPI;
import models.laboratory.reagent.description.AbstractCatalog;
import models.laboratory.reagent.description.BoxCatalog;
import models.laboratory.reagent.description.ReagentCatalog;
import models.laboratory.reagent.instance.AbstractReception;
import models.laboratory.reagent.instance.BoxReception;
import models.laboratory.reagent.instance.ReagentReception;

public class ReceptionBuilder {
	
	/**
	 * Catalogs API
	 */
	private final CatalogsAPI catalogsAPI;
	
	@Inject
	public ReceptionBuilder(CatalogsAPI catalogsAPI) {
		this.catalogsAPI = catalogsAPI;
	}
	
	private void crashIfBlankRequiredProperty(RequiredProperties requiredProperties) {
		if (StringUtils.isBlank(requiredProperties.getCatalogRefCode())) {
			throw new IllegalStateException("Import reagent-reception: catalogRefCode cannot be empty!");
		} else if (StringUtils.isBlank(requiredProperties.getProviderCode())) {
			throw new IllegalStateException("Import reagent-reception: provider cannot be empty!");
		} else if (StringUtils.isBlank(requiredProperties.getKitCatalogName())) {
			throw new IllegalStateException("Import reagent-reception: kitCatalogName cannot be empty!");
		}
	}
	
	private AbstractCatalog getCatalog(RequiredProperties requiredProperties) {
		String catalogRefCode = requiredProperties.getCatalogRefCode();
		String provider = requiredProperties.getProviderCode();
		String kitCatalogName = requiredProperties.getKitCatalogName();
		return this.catalogsAPI.getByRefAndProvider(catalogRefCode, provider, kitCatalogName);
	}
	
	private void crashIfNullCatalog(AbstractCatalog catalog, RequiredProperties properties) {
		if (catalog == null) {
			throw new IllegalStateException(
			"Import reagent-reception: cannot find catalog for " + 
			"Reference '" + String.valueOf(properties.getCatalogRefCode()) + 
			"', provider '" + String.valueOf(properties.getProviderCode()) + 
			"' and kitCatalog name '" + String.valueOf(properties.getKitCatalogName()) + 
			"'");
		}
	}
	
	private AbstractReception createEmptyReception(AbstractCatalog catalog) {
		if (catalog instanceof BoxCatalog) {
			return new BoxReception();
		} else if (catalog instanceof ReagentCatalog) {
			return new ReagentReception();
		} else {
			throw new IllegalStateException("Import reagent-reception: unknown catalog type for Reference '"
					+ String.valueOf(catalog.catalogRefCode) + "'");
		}
	}
	
	public AbstractReception createEmptyReception(RequiredProperties requiredProperties) {
		crashIfBlankRequiredProperty(requiredProperties);
		final AbstractCatalog catalog = getCatalog(requiredProperties);
		crashIfNullCatalog(catalog, requiredProperties);
		return createEmptyReception(catalog);
	}
}
