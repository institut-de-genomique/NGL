package fr.cea.ig.util.function;

public class T2<A,B> implements CT2<A,B> {
	
	public final A a;
	public final B b;
	
	public T2(A a, B b) {
		if (a == null)
			throw new IllegalArgumentException("a is null");
		if (b == null)
			throw new IllegalArgumentException("b is null");
		this.a = a;
		this.b = b;
	}
	
	@Override
	public void accept(C1<T2<A, B>> c) throws Exception {
		c.accept(this);
	}
	
	public <R> R map(F2<A,B,R> f) throws Exception {
		return f.apply(a, b);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (o instanceof T2) {
			T2<?,?> t = (T2<?,?>)o;
			return a.equals(t.a) 
				&& b.equals(t.b);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return a.hashCode() 
			 ^ b.hashCode() >>> 1; 
	}
	
}
