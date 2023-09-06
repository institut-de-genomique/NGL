package fr.cea.ig.ngl.dao.protocols;

import javax.inject.Inject;

import fr.cea.ig.ngl.dao.GenericMongoDAO;
// import fr.cea.ig.MongoDBDAO;
import models.laboratory.protocol.instance.Protocol;
import models.utils.InstanceConstants;

public class ProtocolsDAO extends GenericMongoDAO<Protocol> {
	
	@Inject
	public ProtocolsDAO() {
		super(InstanceConstants.PROTOCOL_COLL_NAME, Protocol.class);
	}
	
}
