package fr.cea.ig.ngl.dao.reagents;

import javax.inject.Inject;

import fr.cea.ig.ngl.dao.GenericMongoDAO;
import models.laboratory.reagent.instance.AbstractReception;
import models.utils.InstanceConstants;

public class ReceptionsDAO extends GenericMongoDAO<AbstractReception> {

	@Inject
	public ReceptionsDAO() {
		super(InstanceConstants.REAGENT_RECEPTION_COLL_NAME, AbstractReception.class);
	}

}
