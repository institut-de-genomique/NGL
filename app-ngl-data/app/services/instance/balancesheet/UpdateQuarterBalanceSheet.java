package services.instance.balancesheet;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import org.mongojack.Aggregation;
import org.mongojack.DBQuery;
import org.mongojack.DBSort;

import com.mongodb.MongoException;

import fr.cea.ig.DBObject;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import models.laboratory.balancesheet.instance.BalanceSheet;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;
import rules.services.RulesException;
import services.instance.AbstractImportData;
import services.instance.balancesheet.BalanceSheetUtils.Collection;
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
			int firstYear = this.findFirstYearForType(type);
			for(int y = firstYear; y <= lastYear; y++) {
				String year = Integer.toString(y);
				logger.debug("Start quarter balancesheet for year = {}; type = {}.", year, type.value);
				ProcessBalanceSheet process = new ProcessBalanceSheet();
				BalanceSheet bs = BalanceSheetUtils.findBalanceSheet(year, type.value);
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
	
	public int findFirstYearForType(Type type) throws NoSuchElementException {
		return Stream.of(BalanceSheetUtils.requests(type))
			// parallelize aggregations
			.parallel()
			.unordered()
			// get collections used by requests
			.map(ComputationRequest::getCollection)
			.distinct()
			// aggregate to find most ancient year in each collection
			.map((Collection collection) -> {
				return MongoDBDAO.aggregate(collection.colectionPath, FirstYearObject.class, Aggregation
						.match(DBQuery.is(BalanceSheetUtils.typePath(collection), BalanceSheetUtils.typeValue(collection, type)))
						.sort(DBSort.asc(collection.startDatePath))
						.limit(1)
						.project("year", Aggregation.Expression.year(Aggregation.Expression.date(collection.startDatePath)))
						);
			})
			// extract year value
			.map(Iterable::iterator)
			.filter(Iterator::hasNext)
			.map(Iterator::next)
			.mapToInt(firstYearObject -> firstYearObject.year)
			// get most recent year from previous (most ancient common year among collections)
			.max()
			.getAsInt();
	}
	
	private static final class FirstYearObject extends DBObject {	
		public Integer year;
	}

}
