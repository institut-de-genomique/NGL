package fr.cea.ig.util.function;

/**
 * Two arguments function, same as {@link java.util.function.BiFunction}.
 * 
 * @author vrd
 *
 * @param <A> argument type
 * @param <B> argument type
 * @param <C> return type
 */
public interface F2<A,B,C> { 

	/**
	 * Apply 2 arguments to get the function result. 
	 * @param a          value
	 * @param b          value
	 * @return           function result
	 * @throws Exception error
	 */
	C apply(A a, B b) throws Exception; 

	/**
	 * Partial application of the first argument.
	 * @param a argument
	 * @return  partial application
	 */
	default F1<B,C> apply(final A a) {
		return b -> apply(a,b);
	}
	
}