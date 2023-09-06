package fr.cea.ig.lfw.utils.iteration;

import java.util.Iterator;

import fr.cea.ig.lfw.utils.ZenIterable;

/**
 * Empty iterable (no elements).
 * 
 * @author vrd
 *
 * @param <A> element type
 */
public class EmptyIterable<A> implements ZenIterable<A> { 
	
	@Override
	public Iterator<A> iterator() {
		return new Iterator<A>() {

			@Override
			public boolean hasNext() {
				return false;
			}

			@Override
			public A next() {
				throw new RuntimeException("no next element (empty iterator)");
			}

		};
	}
	
}
