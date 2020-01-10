package fr.cea.ig.lfw.utils.iteration;

import java.util.Iterator;
import java.util.function.Predicate;

import fr.cea.ig.lfw.utils.ZenIterable;

public class FilteringIterable <A> implements ZenIterable <A> {
	
	Iterable <A> i;
	Predicate<A> function;
	
	public FilteringIterable(Iterable <A> i, Predicate<A> function) {
		this.i = i;
		this.function = function;
	}
	
	@Override
	public Iterator<A> iterator() {
		return new FilteringIterator<>(i.iterator(), function);
	}
	
}