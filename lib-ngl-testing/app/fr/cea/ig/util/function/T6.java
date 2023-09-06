package fr.cea.ig.util.function;

public class T6<A,B,C,D,E,F> implements CT6<A,B,C,D,E, F> {
	
	public final A a;
	public final B b;
	public final C c;
	public final D d;
	public final E e;
	public final F f;
	
	public T6(A a, B b, C c, D d, E e, F f) {
		if (a == null)
			throw new IllegalArgumentException("a is null");
		if (b == null)
			throw new IllegalArgumentException("b is null");
		if (c == null)
			throw new IllegalArgumentException("c is null");
		if (d == null)
			throw new IllegalArgumentException("d is null");
		if (e == null)
			throw new IllegalArgumentException("e is null");
		if (f == null)
			throw new IllegalArgumentException("f is null");
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
		this.e = e;
		this.f = f;
	}
	
	@Override
	public void accept(C1<T6<A, B, C, D, E, F>> c) throws Exception {
		c.accept(this);
	}
	
	public <R> R map(F6<A,B,C,D,E,F,R> ff) throws Exception {
		return ff.apply(a, b, c, d, e, f);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (o instanceof T6) {
			T6<?,?,?,?,?,?> t = (T6<?,?,?,?,?,?>)o;
			return a.equals(t.a)
				&& b.equals(t.b)
				&& c.equals(t.c)
				&& d.equals(t.d)
				&& e.equals(t.e)
				&& f.equals(t.f);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return a.hashCode() 
			 + b.hashCode() >>> 1 
			 + c.hashCode() >>> 2 
			 + d.hashCode() >>> 3
			 + e.hashCode() >>> 4
			 + f.hashCode() >>> 5;
	}
	
}
