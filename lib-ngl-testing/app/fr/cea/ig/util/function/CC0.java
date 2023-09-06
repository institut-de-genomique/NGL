package fr.cea.ig.util.function;

/**
 * Consumer of runnable. This cannot be combined as the C0 accept method 
 * does not take any argument and thus nesting is not possible. This is not
 * equivalent to a CT0.
 *
 * @author vrd
 *
 */
public interface CC0 extends C1<C0> {
	
}
