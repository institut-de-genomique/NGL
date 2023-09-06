package fr.cea.ig.ngl.dao.parameters;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.NotImplementedException;

import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.GenericAPI;
import models.laboratory.parameter.Parameter;

public class ParametersAPI extends GenericAPI<ParametersDAO, Parameter> {
	
	@Inject
	public ParametersAPI(ParametersDAO dao) {
		super(dao);
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
	public Parameter create(Parameter input, String currentUser) throws APIValidationException, APIException {
		throw new NotImplementedException();
	}


	@Override
	public Parameter update(Parameter input, String currentUser) throws APIException, APIValidationException {
		throw new NotImplementedException();

	}


	@Override
	public Parameter update(Parameter input, String currentUser, List<String> fields)
			throws APIException, APIValidationException {
		throw new NotImplementedException();

	}



	
}

