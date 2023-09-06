package fr.cea.ig.util.function;

/**
 * Three argument function.
 * 
 * @author vrd
 *
 * @param <A> argument type
 * @param <B> argument type
 * @param <C> argument type
 * @param <D> return type
 */
public interface F3<A,B,C,D> {
	
	/**
	 * Apply three arguments to get the function result.
	 * @param a          argument
	 * @param b          argument
	 * @param c          argument
	 * @return           result
	 * @throws Exception error
	 */
	D apply(A a, B b, C c) throws Exception; 

	/**
	 * Partial application of one argument.
	 * @param a argument
	 * @return  partial application
	 */
	default F2<B,C,D> apply(A a) {
		return (b,c) -> apply(a,b,c);
	}

	/**
	 * Partial application of 2 arguments. Equivalent
	 * to applying the arguments one at a time:
	 * <code>
	 * f.apply(a).apply(b);
	 * </code>
	 * @param a argument
	 * @param b argument
	 * @return  partial application
	 */
//	default F1<C,D> apply(A a, B b) {
//	return apply(a).apply(b);
//}
	default F1<C,D> apply(A a, B b) {
		return c -> apply(a,b,c);
	}
		
}

