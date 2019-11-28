package services.instance.balancesheet;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import com.google.common.base.Strings;

import fr.cea.ig.DBObject;
import models.laboratory.balancesheet.instance.ByCategory;
import models.laboratory.balancesheet.instance.Computation;
import models.laboratory.balancesheet.instance.Month;
import services.instance.balancesheet.BalanceSheetUtils.Category;

/**
 * Wrapper class for MongoDB BalanceSheet Partial Result.
 * @author aprotat
 *
 */
public class ComputationPartialResults extends DBObject {

	// Should contain only one result
	public List<ComputationPartialResult> results;
	public Date maxDate;

	public ComputationPartialResults() {
		results = new ArrayList<ComputationPartialResult>(0);
	}

	/**
	 * Fill the current BalanceSheet computation with the partial result informations.
	 * @param computation the current BalanceSheet's StatisticGlobal object
	 */
	public void fillComputation(Computation computation) {
		this.setAtomicallyMaxDate(computation);
		switch(Category.fromName(_id)){
		case MONTH: results.forEach(result -> result.fillYearStatistics(computation));
		break;
		case SEQUENCING_TYPE: results.forEach(result -> result.fillSequencingTypeStatistics(computation));
		break;
		case SAMPLE_TYPE: results.forEach(result -> result.fillSampleTypeStatistics(computation));
		break;
		case PROJECT: results.forEach(result -> result.fillProjectStatistics(computation));
		break;
		default: break;
		}
	}

	/**
	 * Update the current BalanceSheet computation with the partial result informations.
	 * @param computation the current BalanceSheet's StatisticGlobal object
	 * @param reducer the statistic's reducer function
	 */
	public void updateComputation(Computation computation, ResultReducer reducer) {
		this.setAtomicallyMaxDate(computation);
		switch(Category.fromName(_id)){
		case MONTH: results.forEach(result -> result.updateYearStatistics(computation, reducer));
		break;
		case SEQUENCING_TYPE: results.forEach(result -> result.updateSequencingTypeStatistics(computation, reducer));
		break;
		case SAMPLE_TYPE: results.forEach(result -> result.updateSampleTypeStatistics(computation, reducer));
		break;
		case PROJECT: results.forEach(result -> result.updateProjectStatistics(computation, reducer));
		break;
		default: break;
		}
	}
	
	private void setAtomicallyMaxDate(Computation computation) {
		boolean keepGoing = true;
		while(keepGoing) {
			Date computationMaxDate = computation.propertyDateMax.get();
			if(computationMaxDate == null || computationMaxDate.before(maxDate)) {
				keepGoing = !computation.propertyDateMax.compareAndSet(computationMaxDate, maxDate);
			} else {
				keepGoing = false;
			}
		}
	}

	/**
	 * Wrapper class for BalanceSheet Aggregation Result by Month, SequencingType, SampleType or Project.
	 * @author aprotat
	 *
	 */
	public static final class ComputationPartialResult {
		
		private static final String UNKNOWN_SAMPLE_TYPE = "not-defined";

		public String category;
		public Double value;
		public Integer nbElements;
		public List<MonthResult> monthly;

		public ComputationPartialResult() {
			monthly = new ArrayList<MonthResult>(0);
		}

		public void fillYearStatistics(Computation computation) {
			computation.result.value = value;
			computation.result.nbElements = nbElements;
			this.fillMonthlyStatistics(computation.monthly);
		}

		public void updateYearStatistics(Computation computation, ResultReducer reducer) {
			computation.result.value = reducer.reduce(computation.result.value, computation.result.nbElements, value, nbElements);
			computation.result.nbElements += nbElements;
			this.updateMonthlyStatistics(computation.monthly, reducer);
		}

		public void fillSequencingTypeStatistics(Computation computation) {
			computation.by.sequencingTypes.add(this.fillCategory());
		}

		public void updateSequencingTypeStatistics(Computation computation, ResultReducer reducer) {
			try {
				ByCategory category = computation.by.sequencingTypes.stream()
						.filter((ByCategory byCategory) -> byCategory.label.equals(this.category))
						.findFirst().get();
				this.updateCategory(category, reducer);
			} catch(NoSuchElementException exception) {
				this.fillSequencingTypeStatistics(computation);
			}
		}
		
		/**
		 * Les ReadSets sans sample-type (type échantillon) sont traités comme ayant pour sample-type: {@code unknown}.
		 */
		private void setUnknownSampleType() {
			if(Strings.isNullOrEmpty(this.category)) {
				this.category = UNKNOWN_SAMPLE_TYPE;
			}
		}

		public void fillSampleTypeStatistics(Computation computation) {
			this.setUnknownSampleType();
			computation.by.sampleTypes.add(this.fillCategory());
		}

		public void updateSampleTypeStatistics(Computation computation, ResultReducer reducer) {
			this.setUnknownSampleType();
			try {
				ByCategory category = computation.by.sampleTypes.stream()
						.filter((ByCategory byCategory) -> byCategory.label.equals(this.category))
						.findFirst().get();
				this.updateCategory(category, reducer);
			} catch(NoSuchElementException exception) {
				this.fillSampleTypeStatistics(computation);
			}
		}

		public void fillProjectStatistics(Computation computation) {
			computation.by.projects.add(this.fillCategory());
		}

		public void updateProjectStatistics(Computation computation, ResultReducer reducer) {
			try {
				ByCategory category = computation.by.projects.stream()
						.filter((ByCategory byCategory) -> byCategory.label.equals(this.category))
						.findFirst().get();
				this.updateCategory(category, reducer);
			} catch(NoSuchElementException exception) {
				this.fillProjectStatistics(computation);
			}
		}

		public ByCategory fillCategory() {
			ByCategory category = new ByCategory();
			category.label = this.category;
			category.result.value = value;
			category.result.nbElements = nbElements;
			this.fillMonthlyStatistics(category.monthly);
			return category;
		}

		public void updateCategory(ByCategory category, ResultReducer reducer) {
			category.result.value = reducer.reduce(category.result.value, category.result.nbElements, value, nbElements);
			category.result.nbElements += nbElements;
			this.updateMonthlyStatistics(category.monthly, reducer);
		}

		public void fillMonthlyStatistics(List<Month> monthlyStatistics) {
			monthly.forEach((MonthResult monthResult) -> {
				Month month = new Month();
				month.month = monthResult.month;
				month.result.value = monthResult.value;
				month.result.nbElements = monthResult.nbElements;
				monthlyStatistics.add(month);
			});
		}

		public void updateMonthlyStatistics(List<Month> monthlyStatistics, ResultReducer reducer) {
			Set<Month> remainingMonthlyStatistics = new HashSet<>();
			remainingMonthlyStatistics.addAll(monthlyStatistics);
			monthly.forEach((MonthResult monthResult) -> {
				boolean found = false;
				for(Month month: remainingMonthlyStatistics) {
					if(month.month == monthResult.month) {
						month.result.value = reducer.reduce(month.result.value, month.result.nbElements, monthResult.value, monthResult.nbElements);
						month.result.nbElements += monthResult.nbElements;
						remainingMonthlyStatistics.remove(month);
						found = true;
						break;
					}
				}
				if(!found) {
					Month month = new Month();
					month.month = monthResult.month;
					month.result.value = monthResult.value;
					month.result.nbElements = monthResult.nbElements;
					monthlyStatistics.add(month);
				}
			});
		}
	}

	/**
	 * Wrapper class for month statistic.
	 * @author aprotat
	 *
	 */
	public static final class MonthResult {

		public Integer month;
		public Double value;
		public Integer nbElements;

		public MonthResult() {}
	}

}
