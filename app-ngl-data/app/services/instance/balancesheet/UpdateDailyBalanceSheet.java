package services.instance.balancesheet;

import java.sql.SQLException;
import java.util.Calendar;

import com.mongodb.MongoException;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import models.laboratory.balancesheet.instance.BalanceSheet;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;
import rules.services.RulesException;
import services.instance.AbstractImportData;
import services.instance.balancesheet.BalanceSheetUtils.Type;
import validation.ContextValidation;

public class UpdateDailyBalanceSheet extends AbstractImportData {
	
	/**
	 * Class logger.
	 */
	protected final play.Logger.ALogger logger; 

	public UpdateDailyBalanceSheet(NGLApplication app) {
		super("UpdateDailyBalanceSheet", app);
		logger = play.Logger.of(UpdateDailyBalanceSheet.class);
	}

	@Override
	public void runImport(ContextValidation ctx)
			throws SQLException, DAOException, MongoException, RulesException, APIValidationException, APIException {
		String currentYear = Integer.toString(Calendar.getInstance().get(Calendar.YEAR));
		logger.debug("Start daily balancesheet for year {}.", currentYear);
		for(Type type: Type.values()) {
			ProcessBalanceSheet process = new ProcessBalanceSheet();
			BalanceSheet bs = BalanceSheetUtils.findBalanceSheet(currentYear, type.value);
			if(bs == null) {
				bs = process.createBalanceSheet(currentYear, type, BalanceSheetUtils.requests(type));
			} else {
				process.overwriteBalanceSheet(bs, type, BalanceSheetUtils.requests(type));
			}
			MongoDBDAO.save(InstanceConstants.BALANCE_SHEET_COLL_NAME, bs);
		}
		logger.debug("End daily balancesheet.");
	}

}
