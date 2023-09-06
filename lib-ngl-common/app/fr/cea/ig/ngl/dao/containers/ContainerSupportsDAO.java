package fr.cea.ig.ngl.dao.containers;

import fr.cea.ig.ngl.dao.GenericMongoDAO;
import models.laboratory.container.instance.ContainerSupport;
import models.utils.InstanceConstants;

public class ContainerSupportsDAO extends GenericMongoDAO<ContainerSupport> {
	public ContainerSupportsDAO() {
		super(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class);
	}
}
