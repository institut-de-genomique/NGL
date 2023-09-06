package fr.cea.ig.util.function;

public interface CC3<A,B,C> extends C1<C3<A,B,C>> {

	default <D> CC4<A,B,C,D> nest(F3<A,B,C,CC1<D>> f) {
		return nc -> accept((a,b,c) -> f.apply(a,b,c).accept(d -> nc.accept(a,b,c,d)));
	}

	default <D> CC4<A,B,C,D> and(CC1<D> cc) {
		return nest((a,b,c) -> cc);
	}
	
	default <D> CC1<D> then(F3<A,B,C,CC1<D>> f) {
		return nest(f).cc1((a,b,c,d) -> d);
	}
	
	default <D,E> CC5<A,B,C,D,E> nest2(F3<A,B,C,CC2<D,E>> ff) {
		return nc -> accept((a,b,c) -> ff.apply(a,b,c).accept((d,e) -> nc.accept(a,b,c,d,e)));
	}

	default <D,E> CC5<A,B,C,D,E> and2(CC2<D,E> cc) {
		return nest2((a,b,c) -> cc);
	}
	
	default CT3<A,B,C> ct() {
		return nc -> accept((a,b,c) -> nc.accept(T.t3(a, b, c)));
	}
	
	default <D> CC1<D> fmap(F3<A,B,C,D> ff) {
		return nc -> accept((a,b,c) -> nc.accept(ff.apply(a,b,c)));
	}

	default CC0 cc0() {
		return nc -> accept((a,b,c) -> nc.accept());
	}
	
	default CC0 noArg() { 
		return cc0();
	}
	
	default <RA> CC1<RA> cc1(F3<A,B,C,RA> f) {
		return nc -> accept((a,b,c) -> nc.accept(f.apply(a,b,c)));
	}
	
	default <RA,RB> CT2<RA,RB> ct2(F3<A,B,C,T2<RA,RB>> f) {
		return nc -> accept((a,b,c) -> nc.accept(f.apply(a,b,c)));
	}

	default <RA,RB> CC2<RA,RB> cc2(F3<A,B,C,T2<RA,RB>> f) {
		return ct2(f).cc();
	}
	
	default <RA,RB,RC> CT3<RA,RB,RC> ct3(F3<A,B,C,T3<RA,RB,RC>> f) {
		return nc -> accept((a,b,c) -> nc.accept(f.apply(a,b,c)));
	}

	default <RA,RB,RC> CC3<RA,RB,RC> cc3(F3<A,B,C,T3<RA,RB,RC>> f) {
		return ct3(f).cc();
	}
	
	default <RA,RB,RC,RD> CT4<RA,RB,RC,RD> ct4(F3<A,B,C,T4<RA,RB,RC,RD>> f) {
		return nc -> accept((a,b,c) -> nc.accept(f.apply(a,b,c)));
	}

	default <RA,RB,RC,RD> CC4<RA,RB,RC,RD> cc4(F3<A,B,C,T4<RA,RB,RC,RD>> f) {
		return ct4(f).cc();
	}
	
	default <RA,RB,RC,RD,RE> CT5<RA,RB,RC,RD,RE> ct5(F3<A,B,C,T5<RA,RB,RC,RD,RE>> f) {
		return nc -> accept((a,b,c) -> nc.accept(f.apply(a,b,c)));
	}

	default <RA,RB,RC,RD,RE> CC5<RA,RB,RC,RD,RE> cc5(F3<A,B,C,T5<RA,RB,RC,RD,RE>> f) {
		return ct5(f).cc();
	}
	
}