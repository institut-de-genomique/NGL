package fr.cea.ig.util.function;

/**
 * Six argument function.
 * 
 * @author vrd
 *
 * @param <A> argument type
 * @param <B> argument type
 * @param <C> argument type
 * @param <D> argument type
 * @param <E> argument type
 * @param <F> argument type
 * @param <G> result type
 */
public interface F6<A,B,C,D,E,F,G> {
	
	/**
	 * Apply six arguments to get the function result.
	 * @param a          argument
	 * @param b          argument
	 * @param c          argument
	 * @param d          argument
	 * @param e          argument
	 * @param f          argument
	 * @return           result
	 * @throws Exception error
	 */
	G apply(A a, B b, C c, D d, E e, F f) throws Exception;
	
	default F5<B,C,D,E,F,G> apply(A a) { return (b,c,d,e,f) -> apply(a,b,c,d,e,f); }
	default F4<C,D,E,F,G> apply(A a, B b) { return (c,d,e,f) -> apply(a,b,c,d,e,f); }
	default F3<D,E,F,G> apply(A a, B b, C c) { return (d,e,f) -> apply(a,b,c,d,e,f); }
	default F2<E,F,G> apply(A a, B b, C c, D d) { return (e,f) ->  apply(a,b,c,d,e,f); }
	default F1<F,G> apply(A a, B b, C c, D d, E e) { return f -> apply(a,b,c,d,e,f); } 
	
}
 