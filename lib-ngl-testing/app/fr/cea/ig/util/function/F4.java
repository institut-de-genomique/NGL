package fr.cea.ig.util.function;

/**
 * Four arguments function.
 * 
 * @author vrd
 *
 * @param <A> argument type
 * @param <B> argument type
 * @param <C> argument type
 * @param <D> argument type
 * @param <E> return type
 */
public interface F4<A,B,C,D,E> {
	
	/**
	 * Apply four argument to get the function result.
	 * @param a          argument
	 * @param b          argument
	 * @param c          argument
	 * @param d          argument
	 * @return           result
	 * @throws Exception error
	 */
	E apply(A a, B b, C c, D d) throws Exception;
	
	default F3<B,C,D,E> apply(A a) { return (b,c,d) -> apply(a,b,c,d); }
	default F2<C,D,E> apply(A a, B b) { return (c,d) -> apply(a,b,c,d); }
	default F1<D,E> apply(A a, B b, C c) { return d -> apply(a,b,c,d); }
	
}
