package fr.cea.ig.util.function;

public interface C3<A,B,C> { 

	void accept(A a, B b, C c) throws Exception;
	
	default C3<A,B,C> andThen(C3<A,B,C> other) {
		return (a,b,c) -> {
			accept(a,b,c);
			other.accept(a,b,c);
		};
	}
	
}