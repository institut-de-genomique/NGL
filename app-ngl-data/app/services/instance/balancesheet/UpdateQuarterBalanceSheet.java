package services.instance.balancesheet;

import java.sql.SQLException;
import java.util.Calendar;

import org.mongojack.DBQuery;

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

public class UpdateQuarterBalanceSheet extends AbstractImportData {
	
	/**
	 * Class logger.
	 */
	protected final play.Logger.ALogger logger; 

	public UpdateQuarterBalanceSheet(NGLApplication app) {
		super("UpdateQuarterBalanceSheet", app);
		logger = play.Logger.of(UpdateQuarterBalanceSheet.class);
	}

	@Override
	public void runImport(ContextValidation ctx)
			throws SQLException, DAOException, MongoException, RulesException, APIValidationException, APIException {
		int lastYear = Calendar.getInstance().get(Calendar.YEAR);
		logger.debug("Start quarter balancesheets.");
		for(Type type: Type.values()) {
			for(int y = type.firstYear; y <= lastYear; y++) {
				String year = Integer.toString(y);
				logger.debug("Start quarter balancesheet for year = {}; type = {}.", year, type.value);
				ProcessBalanceSheet process = new ProcessBalanceSheet();
				BalanceSheet bs = MongoDBDAO.findOne(InstanceConstants.BALANCE_SHEET_COLL_NAME, BalanceSheet.class, DBQuery.and(DBQuery.is("year", year), DBQuery.is("type", type.value)));
				if(bs == null) {
					bs = process.createBalanceSheet(year, type, BalanceSheetUtils.requests(type));
				} else {
					process.overwriteBalanceSheet(bs, type, BalanceSheetUtils.requests(type));
				}
				MongoDBDAO.save(InstanceConstants.BALANCE_SHEET_COLL_NAME, bs);
			}
		}
		logger.debug("End quarter balancesheets");
	}

}
