package fr.cea.ig.util.function;

public class T5<A,B,C,D,E> implements CT5<A,B,C,D,E> {
	
	public final A a;
	public final B b;
	public final C c;
	public final D d;
	public final E e;
	
	public T5(A a, B b, C c, D d, E e) {
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
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
		this.e = e;
	}
	
	@Override
	public void accept(C1<T5<A, B, C, D, E>> c) throws Exception {
		c.accept(this);
	}
	
	public <R> R map(F5<A,B,C,D,E,R> f) throws Exception {
		return f.apply(a, b, c, d, e);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (o instanceof T5) {
			T5<?,?,?,?,?> t = (T5<?,?,?,?,?>)o;
			return a.equals(t.a)
				&& b.equals(t.b)
				&& c.equals(t.c)
				&& d.equals(t.d)
				&& e.equals(t.e);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return a.hashCode() 
			 + b.hashCode() >>> 1 
			 + c.hashCode() >>> 2 
			 + d.hashCode() >>> 3
			 + e.hashCode() >>> 4;

	}
	
}
