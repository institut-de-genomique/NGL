package fr.cea.ig.lfw.utils;

/**
 * Hashing functions.
 * 
 * @author vrd
 *
 */
public interface Hashing {
	
	/**
	 * Simple hashing using factor multiplication.
	 * @param factor  used to multiply hash codes, typically a prime number.  
	 * @param start   hash start value
	 * @param objects objects to add to hashed value
	 * @return        computed hashed value
	 */
	public static int hashF(int factor, int start, Object... objects) {
		int h = start;
		for (Object o : objects) {
			h = h * factor;
			if (o != null)
				h += o.hashCode();
		} 
		return h;				
	}
	
	/**
	 * Simple hashing. This is NGL code factoring, uses 31 as the hash factor.
	 * @param start   start value, typically super hash code.
	 * @param objects objects to add to hash 
	 * @return        computed hashed value
	 */
	public static int hash(int start, Object... objects) {
		return hashF(31,start,objects);
	}

}
