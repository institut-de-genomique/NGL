package fr.cea.ig.ngl.dao.balancesheets;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.GenericAPI;
import models.laboratory.balancesheet.instance.BalanceSheet;

public class BalanceSheetsAPI extends GenericAPI<BalanceSheetsDAO, BalanceSheet> {
	
    private final static List<String> AUTHORIZED_UPDATE_FIELDS = Arrays.asList();
    private final static List<String> DEFAULT_KEYS             = Arrays.asList("year", "type");

	@Inject
	public BalanceSheetsAPI(BalanceSheetsDAO dao) {
		super(dao);
	}

	@Override
	protected List<String> authorizedUpdateFields() {
		return AUTHORIZED_UPDATE_FIELDS;
	}

	@Override
	protected List<String> defaultKeys() {
		return DEFAULT_KEYS;
	}

	@Override
	public BalanceSheet create(BalanceSheet input, String currentUser) throws APIValidationException, APIException {
		throw new APIException("Operation not supported!");
	}

	@Override
	public BalanceSheet update(BalanceSheet input, String currentUser) throws APIException, APIValidationException {
		throw new APIException("Operation not supported!");
	}

	@Override
	public BalanceSheet update(BalanceSheet input, String currentUser, List<String> fields)
			throws APIException, APIValidationException {
		throw new APIException("Operation not supported!");
	}

}
