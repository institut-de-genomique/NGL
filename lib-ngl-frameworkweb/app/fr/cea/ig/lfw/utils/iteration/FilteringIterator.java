package fr.cea.ig.lfw.utils.iteration;

import java.util.Iterator;
import java.util.function.Predicate;

public class FilteringIterator<A> implements Iterator<A> {
	
	Iterator <A> i;
	Predicate<A> function;
	A tampon;
	boolean finished = false;
	
	public FilteringIterator(Iterator <A> i, Predicate<A> function) {
		this.i = i;
		this.function = function;
		computeNext();
	}
	
    private void computeNext() {
    	while (i.hasNext()) {
    		tampon = i.next();
//    		if (function.apply(tampon)) {
    		if (function.test(tampon)) {
    			return;
    		}
    	}
    	finished = true;
    }
    
	@Override
	public boolean hasNext() {
		return ! finished;
	}

	@Override
	public A next() {
		A tmp = tampon;
		computeNext();
		return tmp;
	}
	
}