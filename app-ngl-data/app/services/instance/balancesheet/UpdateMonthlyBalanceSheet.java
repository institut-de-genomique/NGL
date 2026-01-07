package services.instance.balancesheet;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
import fr.cea.ig.ngl.NGLConfig;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import mail.MailServiceException;
import mail.MailServices;
import models.laboratory.balancesheet.instance.BalanceSheet;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;
import rules.services.RulesException;
import services.instance.AbstractImportData;
import services.instance.balancesheet.BalanceSheetUtils.Collection;
import services.instance.balancesheet.BalanceSheetUtils.Type;
import validation.ContextValidation;

public class UpdateMonthlyBalanceSheet extends AbstractImportData {
	
	/**
	 * Class logger.
	 */
	protected final play.Logger.ALogger logger;
	
	private MailServices mailServices;
	
	private NGLConfig config;

	public UpdateMonthlyBalanceSheet(NGLApplication app, NGLConfig config) {
		super("UpdateMonthlyBalanceSheet", app);
		logger = play.Logger.of(UpdateMonthlyBalanceSheet.class);
		mailServices = new MailServices();
		this.config = config;
	}

	@Override
	public void runImport(ContextValidation ctx)
			throws SQLException, DAOException, MongoException, RulesException, APIValidationException, APIException {
		if(isFirstDayOfMonth()) {
			Instant start = Instant.now();
			int currentYear = Calendar.getInstance().get(Calendar.YEAR);
			logger.info("Start computing monthly balancesheets");
			Stream.of(Type.values()).forEach((Type type) -> this.computeBalanceSheetsFor(type, currentYear));
			logger.info("End computing monthly balancesheets");
			Instant end = Instant.now();
			String executionDate = start.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
			Duration duration = Duration.between(start, end);
			this.sendSuccessMail(executionDate, duration);
		} else {
			logger.info("Skip monthly balancesheets execution : today is not the first day of the month");
		}
	}
	
	private void sendSuccessMail(String executionDate, Duration duration) {
		long hours = duration.toHours();
		long minutes = duration.minusHours(hours).toMinutes();
		long seconds = duration.minusHours(hours).minusMinutes(minutes).getSeconds();
		String subject = "UpdateMonthlyBalanceSheet - INFO - Exécution du recalcul mensuel des bilans";
		String message = "UpdateMonthlyBalanceSheet exécuté le " + executionDate + " pour une durée totale de " + hours + "h" + minutes + "m" + seconds + "s";
		try {
			BalanceSheetUtils.sendSuccessMail(this.mailServices, config, subject, message);
		} catch (MailServiceException | UnsupportedEncodingException e) {
			logger.error("Unable to send success mail!",e);
		}
	}

	private boolean isFirstDayOfMonth() {
		LocalDate currentdate = LocalDate.now();
		return currentdate.getDayOfMonth() == 1;
	}
	
	private void computeBalanceSheetsFor(Type type, int currentYear) {
		int firstYear = this.findFirstYearForType(type);
		for(int y = firstYear; y <= currentYear; y++) {
			String year = Integer.toString(y);
			this.computeBalanceSheet(type, year);
		}
	}
	
	private void computeBalanceSheet(Type type, String year) {	
		logger.debug("Start monthly balancesheet for year = {}; type = {}.", year, type.value);
		ProcessBalanceSheet process = new ProcessBalanceSheet();
		BalanceSheet bs = BalanceSheetUtils.findBalanceSheet(year, type.value);
		if(bs == null) {
			bs = process.createBalanceSheet(year, type, BalanceSheetUtils.requests(type));
		} else {
			process.overwriteBalanceSheet(bs, type, BalanceSheetUtils.requests(type));
		}
		MongoDBDAO.save(InstanceConstants.BALANCE_SHEET_COLL_NAME, bs);
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
