package fr.cea.ig.util.function;

public class T4<A,B,C,D> implements CT4<A,B,C,D> {
	
	public final A a;
	public final B b;
	public final C c;
	public final D d;
	
	public T4(A a, B b, C c, D d) {
		if (a == null)
			throw new IllegalArgumentException("a is null");
		if (b == null)
			throw new IllegalArgumentException("b is null");
		if (c == null)
			throw new IllegalArgumentException("c is null");
		if (d == null)
			throw new IllegalArgumentException("d is null");
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
	}
	
	@Override
	public void accept(C1<T4<A, B, C, D>> c) throws Exception {
		c.accept(this);
	}
	
	public <R> R map(F4<A,B,C,D,R> f) throws Exception {
		return f.apply(a, b, c, d);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (o instanceof T4) {
			T4<?,?,?,?> t = (T4<?,?,?,?>)o;
			return a.equals(t.a)
				&& b.equals(t.b)
				&& c.equals(t.c)
				&& d.equals(t.d);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return a.hashCode() 
			 + b.hashCode() >>> 1 
			 + c.hashCode() >>> 2 
			 + d.hashCode() >>> 3;
	}
	
}
