package fr.cea.ig.util.function;

/**
 * Five arguments function.
 * 
 * @author vrd
 *
 * @param <A> argument type
 * @param <B> argument type
 * @param <C> argument type
 * @param <D> argument type
 * @param <E> argument type
 * @param <F> return type
 */
public interface F5<A,B,C,D,E,F> {
	
	/**
	 * Apply five arguments to get the function result.
	 * @param a          argument
	 * @param b          argument
	 * @param c          argument
	 * @param d          argument
	 * @param e          argument
	 * @return           result
	 * @throws Exception error
	 */
	F apply(A a, B b, C c, D d, E e) throws Exception;
	
	default F4<B,C,D,E,F> apply(A a) { return (b,c,d,e) -> apply(a,b,c,d,e); }
	default F3<C,D,E,F> apply(A a, B b) { return (c,d,e) -> apply(a,b,c,d,e); }
	default F2<D,E,F> apply(A a, B b, C c) { return (d,e) -> apply(a,b,c,d,e); }
	default F1<E,F> apply(A a, B b, C c, D d) { return e -> apply(a,b,c,d,e); }
	
}
