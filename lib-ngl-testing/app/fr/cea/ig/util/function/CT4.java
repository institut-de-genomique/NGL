package fr.cea.ig.util.function;

public interface CT4<A,B,C,D> extends CC1<T4<A,B,C,D>> {
	// We should be equivalent to CC4
	default CC4<A,B,C,D> cc() {
		return nc -> accept(t -> nc.accept(t.a,t.b,t.c,t.d)); 
	}
//	default CC0 cc0() {
//		return nc -> accept(t -> nc.accept());
//	}
//	@Override
//	default <RA> CC1<RA> cc1(F1<T4<A,B,C,D>,T1<RA>> f) {
//		return ct1(f).cc();
//	}
//	@Override
//	default <RA> CT1<RA> ct1(F1<T4<A,B,C,D>,T1<RA>> f) {
//		return nc -> accept(t -> nc.accept(f.apply(t)));
//	}
	default <RA,RB> CT2<RA,RB> ct2(F1<T4<A,B,C,D>,T2<RA,RB>> f) {
		return nc -> accept(t -> nc.accept(f.apply(t)));
	}
	default <RA,RB,RC> CT3<RA,RB,RC> ct3(F1<T4<A,B,C,D>,T3<RA,RB,RC>> f) {
		return nc -> accept(t -> nc.accept(f.apply(t)));
	}
}
