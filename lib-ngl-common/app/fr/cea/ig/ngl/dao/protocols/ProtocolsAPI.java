package fr.cea.ig.ngl.dao.protocols;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.mongojack.DBCursor;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.GenericAPI;
import fr.cea.ig.ngl.dao.descriptionHistories.DescriptionHistoriesAPI;
import models.laboratory.descriptionHistory.instance.DescriptionHistory;
import models.laboratory.protocol.instance.Protocol;
import models.laboratory.run.instance.Analysis;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;
import play.Logger;
import validation.ContextValidation;

public class ProtocolsAPI extends GenericAPI<ProtocolsDAO, Protocol> {
	
	private static final String NOT_AUTHORIZED = "not authorized";

	private DescriptionHistoriesAPI descHAPI;

	@Inject
	public ProtocolsAPI(ProtocolsDAO dao, DescriptionHistoriesAPI descHAPI) {
		super(dao);
		this.descHAPI = descHAPI;
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
		return Collections.emptyList();
	}

	@Override
	protected List<String> defaultKeys() {
		return Collections.emptyList();
	}

	@Override
	public Protocol create(Protocol input, String currentUser) throws APIException {
		ContextValidation ctxVal = ContextValidation.createCreationContext(currentUser);
		input.validate(ctxVal);
		if (!ctxVal.hasErrors()) {
			return dao.saveObject(input);
		} else {
			throw new APIValidationException(INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
		}
	}

	public void remove(String code, String currentUser) throws APIException{
		ContextValidation ctxVal = ContextValidation.createDeleteContext(currentUser);
		Protocol protocolInBase = dao.getObject(code);

		if (protocolInBase == null) {
			throw new APIValidationException(INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
		}

		protocolInBase.validate(ctxVal);

		if (!ctxVal.hasErrors()) {
			dao.deleteByCode(code);
			
		}else{
			throw new APIValidationException(INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
		} 
			
	}

	

	public Protocol update(Protocol input, String currentUser, String comment) throws APIException {
		ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);

		Protocol protocolInBase = dao.getObject(input.code);

		if (protocolInBase == null) {
			throw new APIValidationException(INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
		}

		DescriptionHistory descHistory = new DescriptionHistory();
		descHistory.code = input.code;
		descHistory.date = new Date();
		descHistory.user = currentUser;
		descHistory.type = Protocol.class.getName();
		descHistory.objMAjJson = DescriptionHistory.asObjMAjJson(get(input.code));
		descHistory.comment = comment; 

		input._id = protocolInBase._id;
		
		input.validate(ctxVal);

		if (ctxVal.hasErrors()) {
			throw new APIValidationException(INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
		} 

		dao.update(input);

		this.descHAPI.create(descHistory, currentUser);

		Protocol protocol = get(input.code);
			
		return protocol;	
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
