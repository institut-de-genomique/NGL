package fr.cea.ig.lfw.utils.iteration;

import java.util.Iterator;

import fr.cea.ig.lfw.utils.ZenIterable;

class AppendingIterator<A> implements Iterator<A> {

	private Iterator<A> i;
	private A a;
	private boolean appended;
	
	public AppendingIterator(Iterator<A> i, A a) {
		this.i = i;
		this.a = a;
		appended = false;
	}
	
	@Override
	public boolean hasNext() {
		return i.hasNext() || !appended;
	}

	@Override
	public A next() {
		if (i.hasNext())
			return i.next();
		if (!appended) {
			appended = true;
			return a;
		}
		throw new RuntimeException("no next element");
	}
	
}

public class AppendingIterable<A> implements ZenIterable<A> {

	private Iterable<A> i;
	private A a;
	
	public AppendingIterable(Iterable<A> i, A a) {
		this.i = i;
		this.a = a;
	}
	
	@Override
	public Iterator<A> iterator() {
		return new AppendingIterator<>(i.iterator(),a);
	}

}
