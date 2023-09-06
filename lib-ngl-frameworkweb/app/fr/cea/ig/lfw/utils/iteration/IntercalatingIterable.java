package fr.cea.ig.lfw.utils.iteration;

import java.util.Iterator;

import fr.cea.ig.lfw.utils.ZenIterable;

class IntercalatingIterator<A> implements Iterator<A> {

	private Iterator<A> i;
	private A a;
	private boolean intercalating;
	
	public IntercalatingIterator(Iterator<A> i, A a) {
		this.i = i;
		this.a = a;
		intercalating = false;
	}
	
	@Override
	public boolean hasNext() {
		return intercalating || i.hasNext();
	}

	@Override
	public A next() {
		if (intercalating) {
			intercalating = false;
			return a;
		}
		A r = i.next();
		intercalating = i.hasNext();
		return r;
	}
	
}

public class IntercalatingIterable<A> implements ZenIterable<A> {

	private Iterable<A> i;
	private A a;
	
	public IntercalatingIterable(Iterable<A> i, A a) {
		this.i = i;
		this.a = a;
	}
	
	@Override
	public Iterator<A> iterator() {
		return new IntercalatingIterator<>(i.iterator(),a);
	}
	
}
