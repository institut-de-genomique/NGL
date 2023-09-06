package fr.cea.ig.lfw.utils;

import java.util.ArrayList;
// import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Collection of functional-ish static methods.
 * Should provide Iterable {@literal -> } Iterable stuff, not lists.
 *  
 * @author vrd
 *
 */
public class FunCollections {

	/*
	 * Return the first non null produced result. Exceptions are 
	 * handled as nulls.
	 * @param ps producers to check
	 * @return   
	 */
	@SafeVarargs
	public static <T> Optional<T> first(Supplier<Optional<T>>... suppliers) {
		for (Supplier<Optional<T>> s : suppliers) {
			Optional<T> value = s.get();
			if (value.isPresent())
				return value;
		}
		return Optional.empty();
	}

	/* *
	 * Build a constant function from a value. 
	 * <p>
	 * This is not a real improvement in readability:
	 * <code>
	 * lambda : x {@literal ->} value 
	 * method : fconst(value)
	 * </code> 
	 * @param value function value
	 * @return      constant function that returns the parameter value
	 */
	public static <A,B> Function<A,B> fconst(final B value) {
		return x -> value;
	}

	public static <A,B> List<B> map(Iterable<A> col, Function<A,B> map) {
		List<B> result = new ArrayList<>();
		for (A a : col)
			result.add(map.apply(a));
		return result;
	}
//
//	public static <A,B,C> List<C> flatMap(Iterable<A> col, Function<A,Iterable<B>> flat, Function<B,C> map) {
//		List<C> result = new ArrayList<>();
//		for (A a : col)
//			for (B b : flat.apply(a))
//				result.add(map.apply(b));	
//		return result;
//	}
//
//	// Iterable implementation could return some infinite list and
//	// we provide the take(i,count) that provide only count elements.
//
//	public static <A> List<A> repeat(A a, int count) {
//		List<A> r = new ArrayList<>(count);
//		for (int i = 0; i < count; i++)
//			r.add(a);
//		return r;
//	}
//
//	// public static <A> List<A> take(Iterable<A> i, int count) {}
//
//	// Functional definitions could use Iterable -> Iterable types instead
//	// of building eagerly the result list. This then runs into the problem
//	// of building a list if needed (kind of stream collector).
//
//	static class MappingIterator<A,B> implements Iterator<B> {
//
//		private final Iterator<A> src;
//		private final Function<A,B> map;
//
//		public MappingIterator(Iterator<A> src, Function<A,B> map) {
//			this.src = src;
//			this.map = map;
//		}
//
//		@Override
//		public boolean hasNext() {
//			return src.hasNext();
//		}
//
//		@Override
//		public B next() {
//			return map.apply(src.next());
//		}
//
//	}
//
//	static class IntercalatingIterator<A> implements Iterator<A> {
//
//		private final Iterator<A> src;
//		private final A element;
//		private boolean intercalate;
//
//		public IntercalatingIterator(Iterator<A> src, A element) {
//			this.src     = src;
//			this.element = element;
//			intercalate  = false;
//		}
//
//		@Override
//		public boolean hasNext() {
//			return src.hasNext();
//		}
//
//		@Override
//		public A next() {
//			if (intercalate) {
//				intercalate = false;
//				return element;
//			} else {
//				intercalate = true;
//				return src.next();
//			}
//		}
//
//	}
//
//	static class SurroundingIterator<A> implements Iterator<A> {
//
//		private final Iterator<A> src;
//		private final A before;
//		private final A after;
//		private boolean first,last;
//
//		public SurroundingIterator(Iterator<A> src, A before, A after) {
//			this.src    = src;
//			this.before = before;
//			this.after  = after;
//			first       = true; // insert first
//			last        = true; // insert last
//		}
//
//		@Override
//		public boolean hasNext() {
//			if (first) return true;
//			return src.hasNext() || last;
//		}
//
//		@Override
//		public A next() {
//			if (first) {
//				first = false;
//				return before;
//			}
//			if (src.hasNext())
//				return src.next();
//			last = false;
//			return after;
//		}
//
//	}
//
//	public static <A,B> Iterator<B> imap(Iterator<A> i, Function<A,B> f) {
//		return new MappingIterator<>(i,f);
//	}
//
//	public static <A> Iterator<A> intercalate(Iterator<A> i, A a) {
//		return new IntercalatingIterator<>(i,a);
//	}

}
