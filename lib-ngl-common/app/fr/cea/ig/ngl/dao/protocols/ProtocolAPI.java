package fr.cea.ig.ngl.dao.protocols;

import javax.inject.Inject;
import javax.inject.Singleton;

import fr.cea.ig.ngl.dao.api.APIException;
import models.laboratory.protocol.instance.Protocol;
import models.utils.dao.DAOException;

@Singleton
public class ProtocolAPI {

	private final ProtocolDAO dao;
	
	@Inject
	public ProtocolAPI(ProtocolDAO dao) {
		this.dao = dao;
	}
	
	public Iterable<Protocol> all() throws DAOException,APIException {
		return dao.all();
	}
	
}
