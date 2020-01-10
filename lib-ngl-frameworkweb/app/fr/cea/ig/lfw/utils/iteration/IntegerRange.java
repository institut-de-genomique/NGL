package fr.cea.ig.lfw.utils.iteration;

import java.util.Iterator;

import fr.cea.ig.lfw.utils.ZenIterable;

class IntegerRangeIterator implements Iterator<Integer> {

	private int current;
	private int to;
	private int step; 
	
	IntegerRangeIterator(int from, int to, int step) {
		current = from;
		this.to = to;
		this.step = step;
	}
	
	@Override
	public boolean hasNext() {
		if (step > 0)
			return current <= to;
		if (step < 0)
			return current >= to;
		return true;		
	}

	public boolean hasNext_() {
		return (step > 0 && current <= to)
			|| (step < 0 && current >= to)
			|| (step == 0);
	}	
	
	@Override
	public Integer next() {
		int value = current;
		current += step;
		return value;
	}
	
}

public class IntegerRange implements ZenIterable<Integer> {

	private int from;
	private int to;
	private int step;
	
	public IntegerRange(int from, int to, int step) {
		this.from = from;
		this.to   = to;
		this.step = step;
	}
	
	@Override
	public Iterator<Integer> iterator() {
		return new IntegerRangeIterator(from, to, step);
	}

}
