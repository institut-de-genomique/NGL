package fr.cea.ig.ngl.dao.api;

import javax.inject.Inject;
import javax.inject.Singleton;

import fr.cea.ig.ngl.dao.ReagentCatalogDAO;
import models.laboratory.reagent.description.BoxCatalog;
import models.laboratory.reagent.description.KitCatalog;
import models.laboratory.reagent.description.ReagentCatalog;
import models.utils.dao.DAOException;

@Singleton
public class ReagentCatalogAPI {

	private final ReagentCatalogDAO dao;
	
	@Inject
	public ReagentCatalogAPI(ReagentCatalogDAO dao) {
		this.dao = dao;
	}
	
	public Iterable<KitCatalog> getKitCatalogs() throws DAOException, APIException {
		return dao.getKitCatalogs();
	}
	
	public Iterable<BoxCatalog> getBoxCatalogs() throws DAOException, APIException {
		return dao.getBoxCatalogs();
	}
	
	public Iterable<ReagentCatalog> getReagentCatalogs() throws DAOException, APIException { 
		return dao.getReagentCatalogs();
	}

}
