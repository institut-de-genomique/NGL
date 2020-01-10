package sra.scripts.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Tools {

	/**
	 * Fait la difference entre 2 collections.
	 * @param plus collection de depart
	 * @param min  collection à soustraire
	 * @return     ensemble (set) correspondant à collection plus - collection min
	 * @param <A> type des elements des ensembles
	 */
	public static <A> Set<A> subtract(Collection<A> plus, Collection<A> min) {
		Set<A> diff = new HashSet<>(plus);
		diff.removeAll(min);
		return diff;
	}
	
}
