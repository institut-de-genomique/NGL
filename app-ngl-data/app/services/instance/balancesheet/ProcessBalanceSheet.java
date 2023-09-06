package services.instance.balancesheet;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import org.mongojack.Aggregation;
import org.mongojack.Aggregation.Expression;
import org.mongojack.Aggregation.Group.Accumulator;
import org.mongojack.Aggregation.Pipeline;
import org.mongojack.DBProjection;
import org.mongojack.DBProjection.ProjectionBuilder;
import org.mongojack.DBQuery;

import com.google.common.base.Strings;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.balancesheet.instance.BalanceSheet;
import models.laboratory.balancesheet.instance.Computation;
import services.instance.balancesheet.BalanceSheetUtils.Collection;
import services.instance.balancesheet.BalanceSheetUtils.Type;

public class ProcessBalanceSheet {

	private static final String ID_FIELD = "_id";
	private static final String ID_CATEGORY_FIELD = "_id.category";
	private static final String ID_MONTH_FIELD = "_id.month";
	private static final String CATEGORY_FIELD = "category";
	private static final String MONTH_FIELD = "month";
	private static final String MONTHLY_FIELD = "monthly";
	private static final String RESULTS_FIELD = "results";

	/**
	 * Used for incremental update, not used in production
	 */

	private static final String MAX_DATE_FIELD = "maxDate";

	private Date getFirstDayOfYear(String year) {
		LocalDate ld = LocalDate.ofYearDay(Integer.parseInt(year), 1).with(TemporalAdjusters.firstDayOfYear());
		return Date.from(ld.atTime(0, 0, 0, 0).toInstant(OffsetDateTime.now().getOffset()));
	}

	private Date getLastDayOfYear(String year) {
		LocalDate ld = LocalDate.ofYearDay(Integer.parseInt(year), 1).with(TemporalAdjusters.lastDayOfYear());
		return Date.from(ld.atTime(23, 59, 59,  999999999).toInstant(OffsetDateTime.now().getOffset()));
	}

	private Pipeline<?> match(String year, BalanceSheetUtils.Type type, ComputationRequest request, Date startDate) {
		return Aggregation.match(
				DBQuery.and(
						request.withAdditionalMatches(
								DBQuery.greaterThan(request.getCollection().startDatePath, startDate),
								DBQuery.lessThanEquals(request.getCollection().startDatePath, this.getLastDayOfYear(year)),
								DBQuery.is(BalanceSheetUtils.typePath(request.getCollection()), BalanceSheetUtils.typeValue(request.getCollection(), type))
								)
						)
				);
	}

	/* To delete in MongoDB 4.2 */
	private ProjectionBuilder setCorrectGMT(BalanceSheetUtils.Collection collection, String data, String category) {
		BasicDBList list = new BasicDBList();
		list.add("$" + collection.startDatePath);
		list.add(2*60*60000);

		BasicDBObject pb = DBProjection.exclude().append(collection.startDatePath, new BasicDBObject("$add", list));
		if(!Strings.isNullOrEmpty(data)) {
			pb = pb.append(data, "$" + data);
		}
		if(!Strings.isNullOrEmpty(category)) {
			pb = pb.append(category, "$" + category);
		}
		return (ProjectionBuilder) pb;
	}

	private Aggregation.Expression<Object> byCategoryAndMonth(BalanceSheetUtils.Collection collection, String category){
		Map<String, Aggregation.Expression<?>> byCategory = new HashMap<>();
		byCategory.put(MONTH_FIELD, Aggregation.Expression.month(Aggregation.Expression.date(collection.startDatePath)));
		byCategory.put(CATEGORY_FIELD, Aggregation.Expression.path(Strings.isNullOrEmpty(category) ? MONTH_FIELD : category));
		return Aggregation.Expression.object(byCategory);
	}
	
	private Map<String, Accumulator> startAccumulating(BalanceSheetUtils.Collection collection, ComputationRequest request) {
		Map<String, Accumulator> map = request.addComputationAccumulators(new HashMap<>());
		map.put(MAX_DATE_FIELD, Aggregation.Group.max(collection.startDatePath));
		return map;
	}

	private Map<String, Accumulator> accumulateByCategory(ComputationRequest request){
		Map<String, Accumulator> map = request.addGlobalComputationAccumulators(new HashMap<>());
		map.put(MAX_DATE_FIELD, Aggregation.Group.max(MAX_DATE_FIELD));

		Map<String, Aggregation.Expression<?>> month = request.keepComputationAccumulators(new HashMap<>());
		month.put(MONTH_FIELD, Aggregation.Expression.path(ID_MONTH_FIELD));

		map.put(MONTHLY_FIELD, Aggregation.Group.list(Aggregation.Expression.object(month)));
		return map;
	}

	private Map<String, Accumulator> collectResults(ComputationRequest request){
		Map<String, Aggregation.Expression<?>> result = request.keepComputationAccumulators(new HashMap<>());
		result.put(CATEGORY_FIELD, Aggregation.Expression.path(ID_FIELD));
		result.put(MONTHLY_FIELD, Aggregation.Expression.path(MONTHLY_FIELD));

		Map<String, Accumulator> map = new HashMap<>();
		map.put(RESULTS_FIELD, Aggregation.Group.list(Aggregation.Expression.object(result)));
		map.put(MAX_DATE_FIELD, Aggregation.Group.max(MAX_DATE_FIELD));
		return map;
	}

	private void processComputationRequest(String year, BalanceSheetUtils.Type type, Computation currentComputation, ComputationRequest request, boolean update) {
		Collection collection = request.getCollection();
		String propertyPath = request.getProperty();
		Stream.of(request.getCollection().categories)
		// parallelize aggregations
		.parallel()
		.unordered()
		// create MongoJack Aggregation Pipeline
		.map((BalanceSheetUtils.Category category) -> {
			String categoryPath = BalanceSheetUtils.categoryPath(request.getCollection(), category);
			return this.match(year, type, request, update ? currentComputation.propertyDateMax.get() : this.getFirstDayOfYear(year))
					.project(this.setCorrectGMT(collection, propertyPath, categoryPath))
					.group(this.byCategoryAndMonth(collection, categoryPath), this.startAccumulating(collection, request))
					.group(Aggregation.Expression.path(ID_CATEGORY_FIELD), this.accumulateByCategory(request))
					.group(Aggregation.Expression.literal(category.name), this.collectResults(request))
					.projectFields(ID_FIELD, RESULTS_FIELD, MAX_DATE_FIELD);
		})
		// process pipeline in collection
		.map((Pipeline<Expression<?>> pipeline) -> {
			return MongoDBDAO.aggregate(request.getCollection().colectionPath, ComputationPartialResults.class, pipeline);
		})
		// get first result (aggregation should return a list containing one result)
		.map(Iterable::iterator)
		.filter(Iterator::hasNext)
		.map(Iterator::next)
		// fill or update computation with aggregations values
		.forEach(update ? (partialResult) -> partialResult.updateComputation(currentComputation, request.resultReducer()) :
			(partialResult) -> partialResult.fillComputation(currentComputation, request.resultReducer()));
	}

	private void processComputationRequest(String year, BalanceSheetUtils.Type type, Computation currentComputation, ComputationRequest request) {
		this.processComputationRequest(year, type, currentComputation, request, false);
	}

	private Computation createComputation(ComputationRequest request) {
		Computation computation = new Computation();
		computation.collection = request.getCollection().name;
		computation.matches = request.getMatches();
		computation.propertyDate = request.getCollection().startDatePath;
		computation.property = request.getProperty();
		computation.method = request.getMethod().value;
		return computation;
	}

	private boolean isSameComputation(ComputationRequest request, Computation computation) {
		return request.getMethod().value.equals(computation.method) && request.sameMatches(computation.matches) &&
				(request.getProperty() == null ? computation.property == null : request.getProperty().equals(computation.property));
	}

	private void applyToExistingComputations(BiConsumer<Computation, ComputationRequest> applyIfExist, BalanceSheet bs, Type type, ComputationRequest...requests) {
		Set<Computation> computationSet = new HashSet<>(bs.computations);
		for(ComputationRequest request:requests) {
			Computation currentComputation = null;
			for(Computation computation : computationSet) {
				if(this.isSameComputation(request, computation)) {
					currentComputation = computation;
					computationSet.remove(computation);
					break;
				}
			}
			if(currentComputation == null) {
				/* Register new computation in balancesheet */
				currentComputation = this.createComputation(request);
				bs.computations.add(currentComputation);
				this.processComputationRequest(bs.year, type, currentComputation, request);
			} else {
				/* Apply modifications to balancesheet's computation */
				applyIfExist.accept(currentComputation, request);
			}
		}
	}

	/**
	 * Create a new BalanceSheet and attach each request result to it.
	 * @param year
	 * @param type
	 * @param requests
	 * @return Newly created BalanceSheet
	 */
	public BalanceSheet createBalanceSheet(String year, BalanceSheetUtils.Type type, ComputationRequest... requests) {
		BalanceSheet bs = new BalanceSheet();
		bs.year = year;
		bs.type = type.value;
		bs.code = type.value + year;
		bs.lastUpdateDate = new Date();
		for(ComputationRequest request: requests) {
			Computation currentComputation = this.createComputation(request);
			bs.computations.add(currentComputation);
			this.processComputationRequest(year, type, currentComputation, request);
		} return bs;
	}

	/**
	 * For each request, update result if previous statistic exists or register a new one.
	 * </br><b>Used for incremental update, not available in production for now.</b>
	 * @param bs
	 * @param type
	 * @param requests
	 */
	public void updateBalanceSheet(BalanceSheet bs, BalanceSheetUtils.Type type, ComputationRequest... requests) {
		bs.lastUpdateDate = new Date();
		this.applyToExistingComputations(
				(Computation currentComputation, ComputationRequest request) -> {
					this.processComputationRequest(bs.year, type, currentComputation, request, true);
				},
				bs, type, requests);
	}

	/**
	 * For each request, overwrite result if previous statistic exists or register a new one.
	 * @param bs
	 * @param type
	 * @param requests
	 */
	public void overwriteBalanceSheet(BalanceSheet bs, Type type, ComputationRequest[] requests) {
		bs.lastUpdateDate = new Date();
		this.applyToExistingComputations(
				(Computation currentComputation, ComputationRequest request) -> {
					BalanceSheetUtils.clearComputationLists(currentComputation);
					this.processComputationRequest(bs.year, type, currentComputation, request);
				},
				bs, type, requests);
	}

}
