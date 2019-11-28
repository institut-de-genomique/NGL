package fr.cea.ig.util.function;

public interface CC2<A,B> extends C1<C2<A,B>> {
	
	default <C> CC3<A,B,C> nest(F2<A,B,CC1<C>> f) {
		return nc -> accept((a,b) -> f.apply(a,b).accept(c -> nc.accept(a,b,c)));
	}

	default <C> CC3<A,B,C> and(CC1<C> cc) {
		return nest((a,b) -> cc);
	}
	
	default <C> CC1<C> then(F2<A,B,CC1<C>> cc) {
		return nest(cc).cc1((a,b,c) -> c);
	}

	default <C,D> CC4<A,B,C,D> nest2(F2<A,B,CC2<C,D>> ff) {
		return nc -> accept((a,b) -> ff.apply(a,b).accept((c,d) -> nc.accept(a,b,c,d)));
	}
	
	default <C,D> CC4<A,B,C,D> and2(CC2<C,D> cc) {
		return nest2((a,b) -> cc);
	}
	
	default <C,D> CC2<C,D> then2(F2<A,B,CC2<C,D>> ff) {
		return nest2(ff).cc2((a,b,c,d) -> T.t2(c,d));
	}
	
	default CT2<A,B> ct() {
		return nc -> accept((a,b) -> nc.accept(T.t2(a, b))); 	
	}
	
	default <C> CC1<C> fmap(F2<A,B,C> ff) {
		return nc -> accept((a,b) -> nc.accept(ff.apply(a,b)));
	}

	default CC0 cc0() {
		return nc -> accept((a,b) -> nc.accept());
	}

	default CC0 noArg() {
		return cc0();
	}

	default <RA> CC1<RA> cc1(F2<A,B,RA> f) {
		return nc -> accept((a,b) -> nc.accept(f.apply(a,b)));
	}

	default <RA, RB> CT2<RA, RB> ct2(F2<A, B, T2<RA, RB>> f) {
		return nc -> accept((a, b) -> nc.accept(f.apply(a, b)));
	}
	
	default <RA, RB> CC2<RA, RB> cc2(F2<A, B, T2<RA, RB>> f) {
		return ct2(f).cc();
	}
}