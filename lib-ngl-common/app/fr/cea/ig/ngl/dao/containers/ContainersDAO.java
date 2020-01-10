package fr.cea.ig.ngl.dao.containers;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.dao.GenericMongoDAO;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.utils.InstanceConstants;

public class ContainersDAO extends GenericMongoDAO<Container> {

	public ContainersDAO() {
		super(InstanceConstants.CONTAINER_COLL_NAME, Container.class);
	}
	
	public void updateStorageCode(String code, String storageCode, TraceInformation ti) {
		MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, ContainerSupport.class, 
				DBQuery.and(DBQuery.is("support.code", code)), 
				DBUpdate.set("support.storageCode", storageCode).set("traceInformation", ti));
	}

}
