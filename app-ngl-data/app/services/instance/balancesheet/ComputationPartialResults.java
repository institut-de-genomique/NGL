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
	public void fillComputation(Computation computation, ResultReducer reducer) {
		this.setAtomicallyMaxDate(computation);
		switch(Category.fromName(_id)){
		case MONTH: results.forEach(result -> result.fillYearComputation(computation));
		break;
		case SEQUENCING_TYPE: results.forEach(result -> result.fillSequencingTypeComputation(computation));
		break;
		case SAMPLE_TYPE: results.forEach(result -> result.fillSampleTypeComputation(computation, reducer));
		break;
		case PROJECT: results.forEach(result -> result.fillProjectComputation(computation));
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
		case MONTH: results.forEach(result -> result.updateYearComputation(computation, reducer));
		break;
		case SEQUENCING_TYPE: results.forEach(result -> result.updateSequencingTypeComputation(computation, reducer));
		break;
		case SAMPLE_TYPE: results.forEach(result -> result.updateSampleTypeComputation(computation, reducer));
		break;
		case PROJECT: results.forEach(result -> result.updateProjectComputation(computation, reducer));
		break;
		default: break;
		}
	}
	
	/**
	 * </br><b>Used for incremental update (not available in production for now).</b>
	 * @param computation
	 */
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

		/**
		 * Create or Override yearly values.
		 * @param computation
		 */
		public void fillYearComputation(Computation computation) {
			computation.result.value = value;
			computation.result.nbElements = nbElements;
			this.fillMonthlyComputation(computation.monthly);
		}

		/**
		 * Create or Update yearly values.
		 * @param computation
		 * @param reducer
		 */
		public void updateYearComputation(Computation computation, ResultReducer reducer) {
			computation.result.value = reducer.reduce(computation.result.value, computation.result.nbElements, value, nbElements);
			computation.result.nbElements += nbElements;
			this.updateMonthlyComputation(computation.monthly, reducer);
		}

		/**
		 * Create or Override SequencingType values.
		 * @param computation
		 */
		public void fillSequencingTypeComputation(Computation computation) {
			computation.by.sequencingTypes.add(this.fillCategory());
		}

		/**
		 * Create or Update SequencingType values.
		 * @param computation
		 * @param reducer
		 */
		public void updateSequencingTypeComputation(Computation computation, ResultReducer reducer) {
			try {
				ByCategory category = computation.by.sequencingTypes.stream()
						.filter((ByCategory byCategory) -> byCategory.label.equals(this.category))
						.findFirst().get();
				this.updateCategory(category, reducer);
			} catch(NoSuchElementException exception) {
				this.fillSequencingTypeComputation(computation);
			}
		}
		
		/**
		 * Les ReadSets sans sample-type (type échantillon) sont traités comme ayant pour sample-type: {@code unknown}.</br>
		 * Il existe donc deux sources de sample-type {@code unknown}: les ReadSets sans sample-type et ceux taggés comme {@code unknown}.</br>
		 * Si le sample-type est {@code unknown}, une recherche est lancée dans la {@code computation} 
		 * pour trouver un sample-type {@code unknown} existant et les combiner.
		 */
		private boolean setUnknownSampleType(Computation computation, ResultReducer reducer) {
			if(Strings.isNullOrEmpty(this.category)) {
				this.category = UNKNOWN_SAMPLE_TYPE;
			} 
			if(UNKNOWN_SAMPLE_TYPE.equals(this.category)) {
				this.updateKnownSampleTypeComputation(computation, reducer);
				return true;
			} return false;
		}
		
		/**
		 * Fonction appelée une fois le sample-type traité par {@link setUnknownSampleType}.
		 * @param computation
		 */
		private void fillKnownSampleTypeComputation(Computation computation) {
			computation.by.sampleTypes.add(this.fillCategory());
		}

		/**
		 * Create or Override SampleType values.
		 * @param computation
		 * @param reducer
		 */
		public void fillSampleTypeComputation(Computation computation, ResultReducer reducer) {
			if(!this.setUnknownSampleType(computation, reducer)) {
				this.fillKnownSampleTypeComputation(computation);
			}
		}
		
		/**
		 * Fonction appelée une fois le sample-type traité par {@link setUnknownSampleType}.
		 * @param computation
		 * @param reducer
		 */
		private void updateKnownSampleTypeComputation(Computation computation, ResultReducer reducer) {
			try {
				ByCategory category = computation.by.sampleTypes.stream()
						.filter((ByCategory byCategory) -> byCategory.label.equals(this.category))
						.findFirst().get();
				this.updateCategory(category, reducer);
			} catch(NoSuchElementException exception) {
				this.fillKnownSampleTypeComputation(computation);
			}
		}

		/**
		 * Create or Update SampleType values.
		 * @param computation
		 * @param reducer
		 */
		public void updateSampleTypeComputation(Computation computation, ResultReducer reducer) {
			if(!this.setUnknownSampleType(computation, reducer)) {
				this.updateKnownSampleTypeComputation(computation, reducer);
			}
		}

		/**
		 * Create or Override Project values.
		 * @param computation
		 */
		public void fillProjectComputation(Computation computation) {
			computation.by.projects.add(this.fillCategory());
		}

		/**
		 * Create or Update Project values.
		 * @param computation
		 * @param reducer
		 */
		public void updateProjectComputation(Computation computation, ResultReducer reducer) {
			try {
				ByCategory category = computation.by.projects.stream()
						.filter((ByCategory byCategory) -> byCategory.label.equals(this.category))
						.findFirst().get();
				this.updateCategory(category, reducer);
			} catch(NoSuchElementException exception) {
				this.fillProjectComputation(computation);
			}
		}

		private ByCategory fillCategory() {
			ByCategory category = new ByCategory();
			category.label = this.category;
			category.result.value = value;
			category.result.nbElements = nbElements;
			this.fillMonthlyComputation(category.monthly);
			return category;
		}

		private void updateCategory(ByCategory category, ResultReducer reducer) {
			category.result.value = reducer.reduce(category.result.value, category.result.nbElements, value, nbElements);
			category.result.nbElements += nbElements;
			this.updateMonthlyComputation(category.monthly, reducer);
		}

		private void fillMonthlyComputation(List<Month> monthlyComputations) {
			monthly.forEach((MonthResult monthResult) -> {
				Month month = new Month();
				month.month = monthResult.month;
				month.result.value = monthResult.value;
				month.result.nbElements = monthResult.nbElements;
				monthlyComputations.add(month);
			});
		}

		private void updateMonthlyComputation(List<Month> monthlyComputations, ResultReducer reducer) {
			Set<Month> remainingMonthlyComputations = new HashSet<>(monthlyComputations);
			monthly.forEach((MonthResult monthResult) -> {
				boolean found = false;
				for(Month month: remainingMonthlyComputations) {
					if(month.month == monthResult.month) {
						month.result.value = reducer.reduce(month.result.value, month.result.nbElements, monthResult.value, monthResult.nbElements);
						month.result.nbElements += monthResult.nbElements;
						remainingMonthlyComputations.remove(month);
						found = true;
						break;
					}
				}
				if(!found) {
					Month month = new Month();
					month.month = monthResult.month;
					month.result.value = monthResult.value;
					month.result.nbElements = monthResult.nbElements;
					monthlyComputations.add(month);
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
