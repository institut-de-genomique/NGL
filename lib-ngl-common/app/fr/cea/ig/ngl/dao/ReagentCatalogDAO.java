package fr.cea.ig.ngl.dao;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.reagent.description.BoxCatalog;
import models.laboratory.reagent.description.KitCatalog;
import models.laboratory.reagent.description.ReagentCatalog;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;

@Singleton
public class ReagentCatalogDAO {
	
	// Not needed, placeholder
	@Inject
	public ReagentCatalogDAO() {
	}
	
	public Iterable<KitCatalog> getKitCatalogs() throws DAOException {
		return MongoDBDAO.find(InstanceConstants.REAGENT_CATALOG_COLL_NAME, KitCatalog.class, DBQuery.is("category", "Kit")).cursor;
	}
	
	public Iterable<BoxCatalog> getBoxCatalogs() throws DAOException {
		return MongoDBDAO.find(InstanceConstants.REAGENT_CATALOG_COLL_NAME, BoxCatalog.class, DBQuery.is("category", "Box")).cursor;
	}
	
	public Iterable<ReagentCatalog> getReagentCatalogs() throws DAOException { 
		return MongoDBDAO.find(InstanceConstants.REAGENT_CATALOG_COLL_NAME, ReagentCatalog.class, DBQuery.is("category", "Reagent")).cursor;
	}
	
}
