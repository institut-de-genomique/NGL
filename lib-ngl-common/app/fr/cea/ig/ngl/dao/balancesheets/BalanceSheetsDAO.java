package fr.cea.ig.ngl.dao.balancesheets;

import javax.inject.Inject;

import fr.cea.ig.ngl.dao.GenericMongoDAO;
import models.laboratory.balancesheet.instance.BalanceSheet;
import models.utils.InstanceConstants;

public class BalanceSheetsDAO extends GenericMongoDAO<BalanceSheet> {

	@Inject
	public BalanceSheetsDAO() {
		super(InstanceConstants.BALANCE_SHEET_COLL_NAME, BalanceSheet.class);
	}

}
