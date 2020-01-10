package fr.cea.ig.util.function;

/**
 * Tuple of size one. This is a wrapper around a single value
 * that is then equivalent to the value itself. The wrapper is
 * defined to complete the definition of all tuple types. 
 * 
 * @author vrd
 *
 * @param <A> value type
 */
public class T1<A> implements CT1<A> {
	
	/**
	 * First and only value.
	 */
	public final A a;
	
	public T1(A a) {
		if (a == null)
			throw new IllegalArgumentException("a is null");
		this.a = a;
	}
	
	@Override
	public void accept(C1<T1<A>> c) throws Exception {
		c.accept(this);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (o instanceof T1)
			return a.equals(((T1<?>)o).a);
		return false;
	}
	
	@Override
	public int hashCode() {
		return a.hashCode();
	}
	
}
