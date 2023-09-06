package fr.cea.ig.lfw.utils.iteration;

import java.util.Iterator;

import fr.cea.ig.lfw.utils.ZenIterable;

class FlatteningIterator<A> implements Iterator<A> {

	private Iterator<? extends Iterable<A>> ii;
	private Iterator<A> i;
	
	public FlatteningIterator(Iterator<? extends Iterable<A>> ii) {
		this.ii = ii;
		if (ii.hasNext())
			i = ii.next().iterator();
		else
			i = null; // Could assign some empty iterator
	}
	
	@Override
	public boolean hasNext() {
		if (i == null)
			return false;
		if (i.hasNext())
			return true;
		if (ii.hasNext())
			i = ii.next().iterator();
		return i.hasNext();
	}

	@Override
	public A next() {
		if (i == null)
			throw new RuntimeException("no next element");
		return i.next();
	}
	
}

public class FlatteningIterable<A> implements ZenIterable<A> {

	private final Iterable<? extends Iterable<A>> ii;
	
	public FlatteningIterable(Iterable<? extends Iterable<A>> ii) {
		this.ii = ii;
	}
	
	@Override
	public Iterator<A> iterator() {
		return new FlatteningIterator<>(ii.iterator());
	}

}
