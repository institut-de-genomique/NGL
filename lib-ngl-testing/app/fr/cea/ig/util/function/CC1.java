package fr.cea.ig.util.function;

import java.util.function.Supplier;

public interface CC1<A> extends C1<C1<A>> {
	
	// Core combination of CCNs.
	default <B> CC2<A,B> nest(F1<A,CC1<B>> f) {
		return nc -> accept(a -> f.apply(a).accept(b -> nc.accept(a,b)));
	}

	// Combine into a CC2 without argument dependence.
	default <B> CC2<A,B> and(CC1<B> cc) {
		return nest(a -> cc);
	}
	
	default CC1<A> insert(C0 cc) {
		return nc -> accept(a -> { cc.accept(); nc.accept(a); });
	}
	
	// Combine into a CC1 that matches the argument.
	default <B> CC1<B> then(F1<A,CC1<B>> cc) {
		return nest(cc).cc1((a,b) -> b);
	}
	
	default <B,C> CC3<A,B,C> nest2(F1<A,CC2<B,C>> ff) {
		return nc -> accept(a -> ff.apply(a).accept((b,c) -> nc.accept(a,b,c)));
	}
	
	default <B,C> CC3<A,B,C> and2(CC2<B,C> cc) {
		return nest2(a -> cc);
	}
	
	default <B,C> CC2<B,C> then2(F1<A, CC2<B,C>> f) {
		return nest2(f).cc2((a,b,c) -> T.t2(b,c));
	}
	
	default <B,C,D> CC4<A,B,C,D> nest3(F1<A,CC3<B,C,D>> ff) {
		return nc -> accept(a -> ff.apply(a).accept((b,c,d) -> nc.accept(a,b,c,d)));
	}
	
	default <B,C,D> CC4<A,B,C,D> and3(CC3<B,C,D> ff) {
		return nest3(a -> ff);
	}
	
	default <B,C,D,E> CC5<A,B,C,D,E> nest4(F1<A,CC4<B,C,D,E>> ff) {
		return nc -> accept(a -> ff.apply(a).accept((b,c,d,e) -> nc.accept(a,b,c,d,e)));
	}	
	
	default <B,C,D,E,F> CC6<A,B,C,D,E,F> nest5(F1<A,CC5<B,C,D,E,F>> ff) {
		return nc -> accept(a -> ff.apply(a).accept((b,c,d,e,f) -> nc.accept(a,b,c,d,e,f)));
	}	

	default CT1<A> ct() {
		return nc -> accept(a -> nc.accept(T.t1(a)));
	}

	default <B> CC1<B> fmap(F1<A,B> ff) {
		return nc -> accept(a -> nc.accept(ff.apply(a)));
	}
	
	// Turn this into a CC0
	default CC0 cc0() {
		return nc -> accept(a -> nc.accept());
	}
	
	// standard naming of cc0
	default CC0 noArg() {
		return cc0();
	}
	
	default <RA> CT1<RA> ct1(F1<A, T1<RA>> f) {
		return nc -> accept((a) -> nc.accept(f.apply(a)));
	}

	default <RA> CC1<RA> cc1(F1<A, T1<RA>> f) {
		return ct1(f).cc();
	}
	
	default <RA,RB> CC2<RA,RB> cc2(F1<A,T2<RA,RB>> f) {
		return nc -> accept(a -> f.apply(a).accept(t -> nc.accept(t.a, t.b)));
	}
	
	public static <A> CC1<A> from(Supplier<A> s) {
		return nc -> nc.accept(s.get());
	}
	
	public static <A> CC1<A> from(F0<A> s) {
		return nc -> nc.accept(s.apply());
	}
	
}
