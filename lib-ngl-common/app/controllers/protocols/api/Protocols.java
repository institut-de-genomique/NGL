package controllers.protocols.api;

import javax.inject.Inject;

import controllers.NGLAPIController;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.protocols.ProtocolsAPI;
import fr.cea.ig.ngl.dao.protocols.ProtocolsDAO;
import models.laboratory.protocol.instance.Protocol;

public class Protocols extends NGLAPIController<ProtocolsAPI, ProtocolsDAO, Protocol> {

	@Inject
	public Protocols(NGLApplication app, ProtocolsAPI api) {
		super(app, api, ProtocolsSearchForm.class);
	}

	@Override
	public Protocol saveImpl() throws APIException {
		return api().create(null, getCurrentUser());
	}

	@Override
	public Protocol updateImpl(String code) throws Exception, APIException, APIValidationException {
		return api().update(null, getCurrentUser());
	}	
	
}
