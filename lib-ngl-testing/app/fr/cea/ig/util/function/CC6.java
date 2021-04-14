package fr.cea.ig.util.function;

public interface CC6<A,B,C,D,E,F> extends C1<C6<A,B,C,D,E,F>> {
	
	// Currently the maximal parameter definition, no combination from here.
	
	default CT6<A,B,C,D,E,F> ct() {
		return nc -> accept((a,b,c,d,e,f) -> nc.accept(T.t6(a,b,c,d,e,f))); 
	}
	
	default <G> CC1<G> fmap(F6<A,B,C,D,E,F,G> ff) {
		return nc -> accept((a,b,c,d,e,f) -> nc.accept(ff.apply(a,b,c,d,e,f)));
	}

	default CC0 cc0() {
		return nc -> accept((a,b,c,d,e,f) -> nc.accept());
	}
	
	default <RA> CC1<RA> cc1(F6<A,B,C,D,E,F,RA> ff) {
		return nc -> accept((a,b,c,d,e,f) -> nc.accept(ff.apply(a,b,c,d,e,f)));
	}
	
	default <RA,RB> CT2<RA,RB> ct2(F6<A,B,C,D,E,F,T2<RA,RB>> ff) {
		return nc -> accept((a,b,c,d,e,f) -> nc.accept(ff.apply(a,b,c,d,e,f)));
	}
	
	default <RA,RB> CC2<RA,RB> cc2(F6<A,B,C,D,E,F,T2<RA,RB>> ff) {
		return ct2(ff).cc();
	}
	
	default <RA,RB,RC> CT3<RA,RB,RC> ct3(F6<A,B,C,D,E,F,T3<RA,RB,RC>> ff) {
		return nc -> accept((a,b,c,d,e,f) -> nc.accept(ff.apply(a,b,c,d,e,f)));
	}
	
	default <RA,RB,RC> CC3<RA,RB,RC> cc3(F6<A,B,C,D,E,F,T3<RA,RB,RC>> ff) {
		return ct3(ff).cc();
	}
	
	default <RA,RB,RC,RD> CT4<RA,RB,RC,RD> ct4(F6<A,B,C,D,E,F,T4<RA,RB,RC,RD>> ff) {
		return nc -> accept((a,b,c,d,e,f) -> nc.accept(ff.apply(a,b,c,d,e,f)));
	}
	
	default <RA,RB,RC,RD> CC4<RA,RB,RC,RD> cc4(F6<A,B,C,D,E,F,T4<RA,RB,RC,RD>> ff) {
		return ct4(ff).cc();
	}
	
	default <RA,RB,RC,RD,RE> CT5<RA,RB,RC,RD,RE> ct5(F6<A,B,C,D,E,F,T5<RA,RB,RC,RD,RE>> ff) {
		return nc -> accept((a,b,c,d,e,f) -> nc.accept(ff.apply(a,b,c,d,e,f)));
	}
	
	default <RA,RB,RC,RD,RE> CC5<RA,RB,RC,RD,RE> cc5(F6<A,B,C,D,E,F,T5<RA,RB,RC,RD,RE>> ff) {
		return ct5(ff).cc();
	}	
	
	default <RA, RB, RC, RD, RE, RF> CT6<RA, RB, RC, RD, RE, RF> ct6(F6<A, B, C, D, E, F, T6<RA, RB, RC, RD, RE, RF>> ff) {
		return nc -> accept((a, b, c, d, e, f) -> nc.accept(ff.apply(a, b, c, d, e, f)));
	}
	
	default <RA, RB, RC, RD, RE, RF> CC6<RA, RB, RC, RD, RE, RF> cc6(F6<A, B, C, D, E, F, T6<RA, RB, RC, RD, RE, RF>> ff) {
		return ct6(ff).cc();
	}
}