package services.instance.balancesheet;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mongojack.Aggregation;
import org.mongojack.Aggregation.Group.Accumulator;
import org.mongojack.DBQuery.Query;

import services.instance.balancesheet.BalanceSheetUtils.Collection;
import services.instance.balancesheet.BalanceSheetUtils.Method;

/**
 *
 * @author aprotat
 *
 */
public class ComputationRequest {

	private static final String VALUE_FIELD = "value";
	private static final String NB_FIELD = "nbElements";

	private final Collection collection;
	private final Method method;
	private final Match<?>[] matches;
	private final String property;

	public ComputationRequest(Collection collection, Match<?>... matches) {
		this(collection, Method.COUNT, null, matches);
	}

	public ComputationRequest(Collection collection, Method method, String property, Match<?>... matches) {
		this.collection = collection;
		this.method = method;
		this.property = property;
		this.matches = matches;
	}

	public Collection getCollection() {
		return collection;
	}

	public Method getMethod() {
		return method;
	}

	public Set<String> getMatches(){
		return Stream.of(matches).map(Object::toString).collect(Collectors.toSet());
	}

	public boolean sameMatches(java.util.Collection<String> matchesToTest) {
		if(matches.length != matchesToTest.size()) {
			return false;
		} else {
			for(Match<?> match:matches) {
				if(!matchesToTest.contains(match.toString())) {
					return false;
				}
			} return true;
		}
	}

	/**
	 *
	 * @return the property path
	 */
	public String getProperty() {
		return property;
	}

	public Query[] withAdditionalMatches(Query...queries) {
		if(matches.length == 0) {
			return queries;
		}
		Query[] merge = Arrays.copyOf(queries, queries.length + matches.length);
		Query[] additionalQueries = Stream.of(matches).map(Match::build).toArray(Query[]::new);
		System.arraycopy(additionalQueries, 0, merge, queries.length, matches.length);
		return merge;
	}

	public Map<String, Accumulator> addComputationAccumulators(Map<String, Accumulator> map) {
		switch(method) {
		case COUNT: map.put(NB_FIELD, Aggregation.Group.count());
		return map;
		case SUM: map.put(NB_FIELD, Aggregation.Group.count());
		map.put(VALUE_FIELD, Aggregation.Group.sum(property));
		return map;
		case MEAN: map.put(NB_FIELD, Aggregation.Group.count());
		map.put(VALUE_FIELD, Aggregation.Group.average(property));
		return map;
		default: return null;
		}
	}

	public Map<String, Accumulator> addGlobalComputationAccumulators(Map<String, Accumulator> map) {
		switch(method) {
		case COUNT: map.put(NB_FIELD, Aggregation.Group.sum(NB_FIELD));
		return map;
		case SUM: map.put(NB_FIELD, Aggregation.Group.sum(NB_FIELD));
		map.put(VALUE_FIELD, Aggregation.Group.sum(VALUE_FIELD));
		return map;
		case MEAN: map.put(NB_FIELD, Aggregation.Group.sum(NB_FIELD));
		map.put(VALUE_FIELD, Aggregation.Group.average(VALUE_FIELD));
		return map;
		default: return null;
		}
	}

	public Map<String, Aggregation.Expression<?>> keepComputationAccumulators(Map<String, Aggregation.Expression<?>> map) {
		switch(method) {
		case COUNT: map.put(NB_FIELD, Aggregation.Expression.path(NB_FIELD));
		return map;
		case SUM: map.put(NB_FIELD, Aggregation.Expression.path(NB_FIELD));
		map.put(VALUE_FIELD, Aggregation.Expression.path(VALUE_FIELD));
		return map;
		case MEAN: map.put(NB_FIELD, Aggregation.Expression.path(NB_FIELD));
		map.put(VALUE_FIELD, Aggregation.Expression.path(VALUE_FIELD));
		return map;
		default: return null;
		}
	}

	public ResultReducer resultReducer(){
		switch(method) {
		case COUNT: return (Double prevValue, Integer prevNbElement, Double newValue, Integer newNbElement) -> {
			return prevValue;
		};
		case SUM: return (Double prevValue, Integer prevNbElement, Double newValue, Integer newNbElement) -> {
			return prevValue == null ? newValue : prevValue + newValue;
		};
		case MEAN: return (Double prevValue, Integer prevNbElement, Double newValue, Integer newNbElement) -> {
			return prevValue == null ? newValue : ((prevValue * prevNbElement) + (newValue * newNbElement))/(prevNbElement + newNbElement);
		};
		default: return null;
		}
	}

	public static final class Match<T> {

		private final String path;
		private final BiFunction<String, T, Query> operation;
		private final T value;

		private Match(String path, BiFunction<String, T, Query> operation, T value) {
			this.path = path;
			this.operation = operation;
			this.value = value;
		}
		
		public static final <T> Match<T> create(String path, BiFunction<String, T, Query> operation, T value) {
			return new Match<T>(path, operation, value);
		}
		
		public Query build() {
			return this.operation.apply(path, value);
		}

		@Override
		public String toString() {
			return path + "=" + value;
		}

	}

}
