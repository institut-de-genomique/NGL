package fr.cea.ig.util.function;

public interface CT2<A,B> extends CC1<T2<A,B>> {
	default CC2<A,B> cc() {
		return nc -> accept(t -> nc.accept(t.a,t.b)); 
	}
//	@Override
//	default <RA> CT1<RA> ct1(F1<T2<A,B>,T1<RA>> f) {
//		return nc -> accept(t -> nc.accept(f.apply(t)));
//	}
}