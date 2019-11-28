package fr.cea.ig.ngl.test.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import fr.cea.ig.lfw.utils.Iterables;
import fr.cea.ig.util.function.F1;

/**
 * Partial implementation of functional stuff. This differs from the
 * {@link fr.cea.ig.lfw.utils.Iterables} implementation in that it exceptions
 * appear in the signatures and that it's a more direct (list to list implementation).
 * 
 * @author vrd
 *
 */
public class FIterables {

	public static final <A,B> List<B> map(Iterable<A> i, F1<A,B> f) throws Exception {
		List<B> r = new ArrayList<>();
		for (A a : i)
			r.add(f.apply(a));
		return r;
	}

	public static final <A,B> List<B> flatMap(Iterable<A> i, F1<A, ? extends Iterable<B>> f) throws Exception {
		List<B> r = new ArrayList<>();
		for (A a : i)
			for (B b : f.apply(a))
				r.add(b);
		return r;
	}

	public static final <A,B> Set<B> flatSet(Iterable<A> i, Function<A,? extends Iterable<B>> f) {
		return Iterables.flatMap(i, f).toSet();
	}
	
	public static final <A> List<A> concat(Iterable<A> i, A a) {
		List<A> r = new ArrayList<>();
		for (A x : i)
			r.add(x);
		r.add(a);
		return r;
	}
	
}
