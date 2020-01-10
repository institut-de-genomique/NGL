package fr.cea.ig.lfw.utils.iteration;

import java.util.Iterator;

import fr.cea.ig.lfw.utils.ZenIterable;

class PrependingIterator<A> implements Iterator<A> {

	private Iterator<A> i;
	private A a;
	private boolean prepended;
	
	public PrependingIterator(Iterator<A> i, A a) {
		this.i = i;
		this.a = a;
		prepended = false;	
	}
	
	@Override
	public boolean hasNext() {
		return !prepended || i.hasNext();
	}

	@Override
	public A next() {
		if (!prepended) {
			prepended = true;
			return a;
		}
		return i.next();
	}
	
}

public class PrependingIterable<A> implements ZenIterable<A> {

	private Iterable<A> i;
	private A a;
	
	public PrependingIterable(Iterable<A> i, A a) {
		this.i = i;
		this.a = a;
	}

	@Override
	public Iterator<A> iterator() {
		return new PrependingIterator<>(i.iterator(),a);
	}
	
}
