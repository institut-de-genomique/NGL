package fr.cea.ig.lfw.utils.iteration;

import java.util.Iterator;

public class SkippingIterator<A> implements Iterator <A> {
	
	/**
	 * Wrapped iterator.
	 */
	private final Iterator <A> i;
	
	public SkippingIterator(Iterator<A> i, int cp) {
		this.i = i;
		for (int j=0; j<cp; j++ ) {
			if (i.hasNext()) {
				i.next();
			}
		}
	}
	@Override
	public boolean hasNext() {
		return i.hasNext();
	}

	@Override
	public A next() {
		return i.next();
	}
}
