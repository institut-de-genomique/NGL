package fr.cea.ig.ngl.dao.reagents;

import javax.inject.Inject;

import fr.cea.ig.ngl.dao.GenericMongoDAO;
import models.laboratory.reagent.description.AbstractCatalog;
import models.utils.InstanceConstants;

public class CatalogsDAO extends GenericMongoDAO<AbstractCatalog> {

	@Inject
	public CatalogsDAO() {
		super(InstanceConstants.REAGENT_CATALOG_COLL_NAME, AbstractCatalog.class);
	}

}
