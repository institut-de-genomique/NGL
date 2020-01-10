package fr.cea.ig.ngl.dao.protocols;

import java.util.List;

import javax.inject.Inject;

import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.GenericAPI;
import models.laboratory.protocol.instance.Protocol;
import models.utils.dao.DAOException;

public class ProtocolsAPI extends GenericAPI<ProtocolsDAO, Protocol> {
	
	private static final String NOT_AUTHORIZED = "not authorized";

	@Inject
	public ProtocolsAPI(ProtocolsDAO dao) {
		super(dao);
	}

	/**
	 * All protocol instances.
	 * @return              Protocol iterable
	 * @throws DAOException DAO error
	 */
	public Iterable<Protocol> all() throws DAOException {
		return dao.all();
	}
	
	@Override
	protected List<String> authorizedUpdateFields() {
		return null;
	}

	@Override
	protected List<String> defaultKeys() {
		return null;
	}

	@Override
	public Protocol create(Protocol input, String currentUser) throws APIException {
		throw new APIException(NOT_AUTHORIZED);
	}

	@Override
	public Protocol update(Protocol input, String currentUser) throws APIException {
		throw new APIException(NOT_AUTHORIZED);
	}

	@Override
	public Protocol update(Protocol input, String currentUser, List<String> fields) throws APIException {
		throw new APIException(NOT_AUTHORIZED);
	}
	
}
