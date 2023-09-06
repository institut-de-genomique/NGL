package fr.cea.ig.lfw.utils.iteration;

import java.util.Iterator;
import fr.cea.ig.lfw.utils.ZenIterable;

/**
 * Iterable that skips a given number of elements.
 * 
 * @author vrd
 *
 * @param <A> type of elements
 */
public class SkippingIterable<A> implements ZenIterable <A> {

	/**
	 * Wrapped Iterable.
	 */
	private final Iterable <A> i;
	
	/**
	 * Number of elements to skip.
	 */
	private final int toSkip;
	
	/**
	 * Skipping toSkip Elements.
	 * @param i      wrapped iterable
	 * @param toSkip number of wrapped iterable elements to skip 
	 */
	public SkippingIterable(Iterable<A> i, int toSkip) {
		this.i = i;
		this.toSkip = toSkip;
	}
	
	@Override
	public Iterator<A> iterator() {
		Iterator <A> j = i.iterator();
		Iterator <A> k = new SkippingIterator<>(j, toSkip);
		return k;
	}	
	
}

