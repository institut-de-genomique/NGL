package fr.cea.ig.util.function;

public class T3<A,B,C> implements CT3<A,B,C> {
	
	public final A a;
	public final B b;
	public final C c;
	
	public T3(A a, B b, C c) {
		if (a == null)
			throw new IllegalArgumentException("a is null");
		if (b == null)
			throw new IllegalArgumentException("b is null");
		if (c == null)
			throw new IllegalArgumentException("c is null");
		this.a = a;
		this.b = b;
		this.c = c;
	}
	
	@Override
	public void accept(C1<T3<A, B, C>> c) throws Exception {
		c.accept(this);
	}
	
	public <R> R map(F3<A,B,C,R> f) throws Exception {
		return f.apply(a, b, c);
	}
	
	@Override
	public boolean equals(Object o)  {
		if (o == null)
			return false;
		if (o instanceof T3) {
			T3<?,?,?> t = (T3<?,?,?>)o;
			return a.equals(t.a)
			    && b.equals(t.b)
				&& c.equals(t.c);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return a.hashCode() 
			 + b.hashCode() >>> 1 
			 + c.hashCode() >>> 2;
	}
	
}
