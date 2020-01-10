package fr.cea.ig.lfw.utils;

import java.util.function.BiPredicate;

/**
 * Object equality implementation.
 * 
 * @author vrd
 *
 */
public interface Equality {

	/**
	 * Check if a equals b. 
	 * @param a a
	 * @param b b
	 * @return  true if a and b are null or of the same class and equals method return true 
	 */
	public static boolean objectEquals(Object a, Object b) {
		return typedEquals(Object.class, a, b, (x,y) -> x.equals(y));
	}
	
	/**
	 * Checks equality through a predicate. If == returns true this returns true
	 * and this means that even if the objects are not instances of T, the test returns true
	 * if == returns true.
	 * @param c expected class of objects, a and b must be instances of c or a subclass
	 * @param a first value to compare
	 * @param b second value to compare
	 * @param p predicate that does the comparison of properly typed non null objects 
	 * @param <T> expected type or super type of values to compare
	 * @return  equality result
	 */
	public static <T> boolean typedEquals(Class<T> c, Object a, Object b, BiPredicate<T,T> p) {
		if (a == b)
			return true;
		if (a == null) 
			return false;
		if (b == null)
			return false;
		if (a.getClass() != b.getClass())
			return false;
		T ta = c.cast(a);
		if (ta == null)
			return false;
		T tb = c.cast(b);
		if (tb == null)
			return false;
		return p.test(ta, tb);
	}
	
	public static boolean ea(Object a, Object b) { return objectEquals(a,b); }
	public static <T> boolean eq(Class<T> c, Object a, Object b, BiPredicate<T,T> p) { return typedEquals(c,a,b,p); }
	
//	default <T> boolean equals(Class<T> c, Object o, BiPredicate<T,T> p) {
//		if (o == null)
//			return false;
//		if (!super.equals(o))
//			return false;
//		return typedEquals(c,a,b,p);
//	}
	
	
}
