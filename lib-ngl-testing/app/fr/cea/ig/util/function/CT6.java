package fr.cea.ig.util.function;

public interface CT6<A,B,C,D,E,F> extends CC1<T6<A,B,C,D,E,F>> {
	
	default CC6<A,B,C,D,E,F> cc() {
		return nc -> accept(t -> nc.accept(t.a,t.b,t.c,t.d,t.e,t.f)); 
	}
	
//	@Override
//	default <RA> CT1<RA> ct1(F1<T6<A,B,C,D,E,F>,T1<RA>> f) {
//		return nc -> accept(t -> nc.accept(f.apply(t)));
//	}
	
	default <RA,RB> CT2<RA,RB> ct2(F1<T6<A,B,C,D,E,F>,T2<RA,RB>> f) {
		return nc -> accept(t -> nc.accept(f.apply(t)));
	}
	
	default <RA,RB,RC> CT3<RA,RB,RC> ct3(F1<T6<A,B,C,D,E,F>,T3<RA,RB,RC>> f) {
		return nc -> accept(t -> nc.accept(f.apply(t)));
	}
	
}