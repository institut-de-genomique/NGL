package fr.cea.ig.util.function;

public interface CT3<A,B,C> extends CC1<T3<A,B,C>> {
	default CC3<A,B,C> cc() {
		return nc -> accept(t -> nc.accept(t.a,t.b,t.c)); 
	}
//	@Override
//	default <RA> CT1<RA> ct1(F1<T3<A,B,C>,T1<RA>> f) {
//		return nc -> accept(t -> nc.accept(f.apply(t)));
//	}
	default <RA,RB> CT2<RA,RB> ct2(F1<T3<A,B,C>,T2<RA,RB>> f) {
		return nc -> accept(t -> nc.accept(f.apply(t)));
	}
}