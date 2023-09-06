package fr.cea.ig.util.function;

public interface CT1<A> extends CC1<T1<A>> {
	
	default CC1<A> cc() {
		return nc -> accept(t -> nc.accept(t.a)); 
	}
	
}