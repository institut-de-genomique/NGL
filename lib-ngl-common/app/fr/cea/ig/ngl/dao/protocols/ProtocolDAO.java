package fr.cea.ig.ngl.dao.protocols;

import javax.inject.Inject;
import javax.inject.Singleton;

import fr.cea.ig.ngl.dao.GenericMongoDAO;
// import fr.cea.ig.MongoDBDAO;
import models.laboratory.protocol.instance.Protocol;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;

@Singleton
public class ProtocolDAO {

	private final GenericMongoDAO<Protocol> gdao;
	
	@Inject
	public ProtocolDAO() {
		gdao = new GenericMongoDAO<>(InstanceConstants.PROTOCOL_COLL_NAME,Protocol.class);
	}
	
	public Iterable<Protocol> all() throws DAOException {
		return gdao.all();
	}
	
}
