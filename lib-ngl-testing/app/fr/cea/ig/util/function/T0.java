package fr.cea.ig.util.function;

/**
 * Tuple of size zero. This is kind of a Void equivalent
 * but with an instance instead of null.
 * 
 * @author vrd
 *
 */
public class T0 implements CT0 {

	@Override
	public void accept(C1<T0> c) throws Exception {
		c.accept(this);
	}
		
	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		return o instanceof T0;
	}
	
	@Override
	public int hashCode() {
		return 1;
	}
	
}