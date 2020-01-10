package fr.cea.ig.util.function;

public interface CC4<A,B,C,D> extends C1<C4<A,B,C,D>> {
	
	default <E> CC5<A,B,C,D,E> nest(F4<A,B,C,D,CC1<E>> f) {
		return nc -> accept((a,b,c,d) -> f.apply(a,b,c,d).accept(e -> nc.accept(a,b,c,d,e)));
	}
	
	default <E> CC5<A,B,C,D,E> and(CC1<E> cc) {
		return nest((a,b,c,d) -> cc);
	}

	default <E> CC1<E> then(F4<A,B,C,D,CC1<E>> f) {
		return nest(f).cc1((a,b,c,d,e) -> e);
	}
	
	default <E,F> CC6<A,B,C,D,E,F> nest2(F4<A,B,C,D,CC2<E,F>> ff) {
		return nc -> accept((a,b,c,d) -> ff.apply(a,b,c,d).accept((e,f) -> nc.accept(a,b,c,d,e,f)));
	}

	default <E,F> CC6<A,B,C,D,E,F> and2(CC2<E,F> cc) {
		return nest2((a,b,c,d) -> cc);
	}
	
	default CT4<A,B,C,D> ct() {
		return nc -> accept((a,b,c,d) -> nc.accept(T.t4(a,b,c,d))); 
	}
	
	default <E> CC1<E> fmap(F4<A,B,C,D,E> ff) {
		return nc -> accept((a,b,c,d) -> nc.accept(ff.apply(a,b,c,d)));
	}
	
	default CC0 cc0() {
		return nc -> accept((a,b,c,d) -> nc.accept());
	}
	
	default <RA> CC1<RA> cc1(F4<A,B,C,D,RA> f) {
		return nc -> accept((a,b,c,d) -> nc.accept(f.apply(a,b,c,d)));
	}
	
	default <RA,RB> CT2<RA,RB> ct2(F4<A,B,C,D,T2<RA,RB>> f) {
		return nc -> accept((a,b,c,d) -> nc.accept(f.apply(a,b,c,d)));
	}
	
	default <RA,RB> CC2<RA,RB> cc2(F4<A,B,C,D,T2<RA,RB>> f) {
		return ct2(f).cc();
	}
	
	default <RA,RB,RC> CT3<RA,RB,RC> ct3(F4<A,B,C,D,T3<RA,RB,RC>> f) {
		return nc -> accept((a,b,c,d) -> nc.accept(f.apply(a,b,c,d)));
	}
	
	default <RA,RB,RC> CC3<RA,RB,RC> cc3(F4<A,B,C,D,T3<RA,RB,RC>> f) {
		return ct3(f).cc();
	}
	
	default <RA, RB, RC, RD> CT4<RA, RB, RC, RD> ct4(F4<A, B, C, D, T4<RA, RB, RC, RD>> f) {
		return nc -> accept((a, b, c, d) -> nc.accept(f.apply(a, b, c, d)));
	}
	
	default <RA, RB, RC, RD> CC4<RA, RB, RC, RD> cc4(F4<A, B, C, D, T4<RA, RB, RC, RD>> f) {
		return ct4(f).cc();
	}
}
