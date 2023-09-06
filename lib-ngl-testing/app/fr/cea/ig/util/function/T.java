package fr.cea.ig.util.function;

// Tuples
// We have an equivalence between CCN and CC1<TN>.
public class T {
	
	// Could use a single overloaded 't' method.
	public static T0 t0() { return new T0(); }
	public static <A> T1<A> t1(A a) { return new T1<>(a); }
	public static <A,B> T2<A,B> t2(A a, B b) { return new T2<>(a,b); }
	public static <A,B,C> T3<A,B,C> t3(A a, B b, C c) { return new T3<>(a,b,c); }
	public static <A,B,C,D> T4<A,B,C,D> t4(A a, B b, C c, D d) { return new T4<>(a,b,c,d); }
	public static <A,B,C,D,E> T5<A,B,C,D,E> t5(A a, B b, C c, D d, E e) { return new T5<>(a,b,c,d,e); }
	public static <A,B,C,D,E,F> T6<A,B,C,D,E,F> t6(A a, B b, C c, D d, E e, F f) { return new T6<>(a,b,c,d,e,f); }
	
	public static <A> CC1<A> cc1(A a) { return t1(a).cc(); }
	public static <A,B> CC2<A,B> cc2(A a, B b) { return t2(a, b).cc(); }
	public static <A,B,C> CC3<A,B,C> cc3(A a, B b, C c) { return t3(a, b, c).cc(); }
	public static <A,B,C,D> CC4<A,B,C,D> cc4(A a, B b, C c, D d) { return t4(a, b, c, d).cc(); }
	public static <A,B,C,D,E> CC5<A,B,C,D,E> cc5(A a, B b, C c, D d, E e) { return t5(a, b, c, d, e).cc(); }
	public static <A,B,C,D,E,F> CC6<A,B,C,D,E,F> cc6(A a, B b, C c, D d, E e, F f) { return t6(a, b, c, d, e, f).cc(); }
	
}

