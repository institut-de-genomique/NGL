package fr.cea.ig.util.function;

public interface CT5<A,B,C,D,E> extends CC1<T5<A,B,C,D,E>> {
	default CC5<A,B,C,D,E> cc() {
		return nc -> accept(t -> nc.accept(t.a,t.b,t.c,t.d,t.e)); 
	}
//	default CC0 cc0() {
//		return nc -> accept(t -> nc.accept());
//	}
//	default <RA> CC1<RA> cc1(F1<T5<A,B,C,D,E>,RA> f) {
//		return nc -> accept(t -> nc.accept(f.apply(t)));
//	}
//	@Override
//	default <RA> CT1<RA> ct1(F1<T5<A,B,C,D,E>,T1<RA>> f) {
//		return nc -> accept(t -> nc.accept(f.apply(t)));
//	}
	default <RA,RB> CT2<RA,RB> ct2(F1<T5<A,B,C,D,E>,T2<RA,RB>> f) {
		return nc -> accept(t -> nc.accept(f.apply(t)));
	}
	default <RA,RB,RC> CT3<RA,RB,RC> ct3(F1<T5<A,B,C,D,E>,T3<RA,RB,RC>> f) {
		return nc -> accept(t -> nc.accept(f.apply(t)));
	}
}