package fr.cea.ig.util.function;

public interface CC5<A,B,C,D,E> extends C1<C5<A,B,C,D,E>> {
	
	default <F> CC6<A,B,C,D,E,F> nest(F5<A,B,C,D,E,CC1<F>> ff) {
		return nc -> accept((a,b,c,d,e) -> ff.apply(a,b,c,d,e).accept(f -> nc.accept(a,b,c,d,e,f)));
	}

	default <F> CC6<A,B,C,D,E,F> and(CC1<F> ff) {
		return nest((a,b,c,d,e) -> ff);
	}
	
	default <F> CC1<F> then(F5<A,B,C,D,E,CC1<F>> ff) {
		return nest(ff).cc1((a,b,c,d,e,f) -> f);
	}
	
	default CT5<A,B,C,D,E> ct() {
		return nc -> accept((a,b,c,d,e) -> nc.accept(T.t5(a,b,c,d,e))); 
	}
	
	default <F> CC1<F> fmap(F5<A,B,C,D,E,F> ff) {
		return nc -> accept((a,b,c,d,e) -> nc.accept(ff.apply(a,b,c,d,e)));
	}

	default CC0 cc0() {
		return nc -> accept((a,b,c,d,e) -> nc.accept());
	}
	
	default <RA> CC1<RA> cc1(F5<A,B,C,D,E,RA> f) {
		return nc -> accept((a,b,c,d,e) -> nc.accept(f.apply(a,b,c,d,e)));
	}
	
	default <RA,RB> CT2<RA,RB> ct2(F5<A,B,C,D,E,T2<RA,RB>> f) {
		return nc -> accept((a,b,c,d,e) -> nc.accept(f.apply(a,b,c,d,e)));
	}
	
	default <RA,RB> CC2<RA,RB> cc2(F5<A,B,C,D,E,T2<RA,RB>> f) {
		return ct2(f).cc();
	}
	
	default <RA,RB,RC> CT3<RA,RB,RC> ct3(F5<A,B,C,D,E,T3<RA,RB,RC>> f) {
		return nc -> accept((a,b,c,d,e) -> nc.accept(f.apply(a,b,c,d,e)));
	}
	
	default <RA,RB,RC> CC3<RA,RB,RC> cc3(F5<A,B,C,D,E,T3<RA,RB,RC>> f) {
		return ct3(f).cc();
	}
	
	default <RA,RB,RC,RD> CT4<RA,RB,RC,RD> ct4(F5<A,B,C,D,E,T4<RA,RB,RC,RD>> f) {
		return nc -> accept((a,b,c,d,e) -> nc.accept(f.apply(a,b,c,d,e)));
	}
	
	default <RA,RB,RC,RD> CC4<RA,RB,RC,RD> cc4(F5<A,B,C,D,E,T4<RA,RB,RC,RD>> f) {
		return ct4(f).cc();
	}
	
	default <RA, RB, RC, RD, RE> CT5<RA, RB, RC, RD, RE> ct5(F5<A, B, C, D, E, T5<RA, RB, RC, RD, RE>> f) {
		return nc -> accept((a, b, c, d, e) -> nc.accept(f.apply(a, b, c, d, e)));
	}
	
	default <RA, RB, RC, RD, RE> CC5<RA, RB, RC, RD, RE> cc5(F5<A, B, C, D, E, T5<RA, RB, RC, RD, RE>> f) {
		return ct5(f).cc();
	}
	
}