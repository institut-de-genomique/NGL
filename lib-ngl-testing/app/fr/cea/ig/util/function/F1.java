package fr.cea.ig.util.function;

/**
 * One argument function, same as {@link java.util.function.Function}.
 * 
 * @author vrd
 *
 * @param <A> argument type
 * @param <B> return type
 */
public interface F1<A,B>   { 
	
	/**
	 * Apply an argument to get the result.
	 * @param a          argument
	 * @return           result
	 * @throws Exception error
	 */
	B apply(A a) throws Exception;
	
}