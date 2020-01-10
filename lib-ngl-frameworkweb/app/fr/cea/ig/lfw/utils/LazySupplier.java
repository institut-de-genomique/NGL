package fr.cea.ig.lfw.utils;

import java.util.function.Supplier;

/**
 * At most once computation of supplied value.
 * 
 * @author vrd
 *
 * @param <T> type of lazy value
 */
public abstract class LazySupplier<T> implements Supplier<T> {
	
	/**
	 * Value of the computation, null if the computation did not occur. 
	 */
	private T value = null;
	
	/**
	 * Computation that produces the value.
	 * @return value
	 */
	protected abstract T compute();
	
	/**
	 * Executes the computation at most once (for the first access).
	 */
	@Override
	public T get() {
		if (value == null)
			value = compute();
		return value;
	}
	
}
