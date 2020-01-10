package fr.cea.ig.lfw.utils;

import java.util.function.Supplier;

/**
 * Lazy supplier implementation by lambda expression.
 *  
 * @author vrd
 *
 * @param <T> type of value
 */
public class LazyLambdaSupplier<T> extends LazySupplier<T> {
		
	/**
	 * Value computation as a lambda.
	 */
	private Supplier<T> computation;
	
	/**
	 * Constructor that takes the value computation as a lambda. 
	 * @param computation computation of the value
	 */
	public LazyLambdaSupplier(Supplier<T> computation) {
		this.computation = computation;
	}

	/**
	 * Run the lambda provide at construction time.
	 */
	@Override
	protected T compute() {
		return computation.get();
	}

}
