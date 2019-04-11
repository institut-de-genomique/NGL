package fr.cea.ig.lfw.utils.iteration;

import java.util.Iterator;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.tuple.ImmutablePair;

import fr.cea.ig.lfw.utils.ZenIterable;

class ZippingIterator<A,B> implements Iterator<ImmutablePair<A,B>> {
	private Iterator<A> a;
	private Iterator<B> b;
	public ZippingIterator(Iterator<A> a, Iterator<B> b) {
		this.a = a;
		this.b = b;
	}
	@Override
	public boolean hasNext() {
		return a.hasNext() && b.hasNext();
	}
	@Override
	public ImmutablePair<A, B> next() {
		return new ImmutablePair<>(a.next(),b.next());
	}
}

public class ZippingIterable<A,B> implements ZenIterable<ImmutablePair<A,B>> {
	
	private Iterable<A> a;
	private Iterable<B> b;
	
	public ZippingIterable(Iterable<A> a, Iterable<B> b) {
		this.a = a;
		this.b = b;
	}
	
	@Override
	public Iterator<ImmutablePair<A, B>> iterator() {
		return new ZippingIterator<>(a.iterator(),b.iterator());
	}

	public ZippingIterable<A,B> doEach(BiConsumer<A,B> c) {
		each(x -> c.accept(x.left,x.right));
		return this;
	}
	
}
