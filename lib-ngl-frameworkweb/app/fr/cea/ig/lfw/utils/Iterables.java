package fr.cea.ig.lfw.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import fr.cea.ig.lfw.utils.iteration.AppendingIterable;
import fr.cea.ig.lfw.utils.iteration.EmptyIterable;
import fr.cea.ig.lfw.utils.iteration.FilteringIterable;
import fr.cea.ig.lfw.utils.iteration.FlatteningIterable;
import fr.cea.ig.lfw.utils.iteration.IntegerRange;
import fr.cea.ig.lfw.utils.iteration.IntercalatingIterable;
import fr.cea.ig.lfw.utils.iteration.MappingIterable;
import fr.cea.ig.lfw.utils.iteration.PrependingIterable;
import fr.cea.ig.lfw.utils.iteration.SkippingIterable;
import fr.cea.ig.lfw.utils.iteration.ZippingIterable;


/**
 * Catalogue de raccourcis import static Iterables qui permet d'utiliser les methodes sans les prefixer.
 * Classe à utiliser pour transformer un Iterable (set, collection) en ZenIterable qui permet
 * de faire du chainage d'appels de methodes (fluent java : i.map(...).filter(...).each(...)).
 * @author sgas
 *
 */
// ZenIterableUtils
public class Iterables {
	
	/**
	 * Applies a function to elements of an iterable to produce
	 * an iterable of transformed values.
	 * @param <A>      source element type
	 * @param <B>      transformed element type
	 * @param i        iterable
	 * @param function function to apply elements to
	 * @return         iterable of transformed elements
	 */
	public static <A,B> ZenIterable<B> map(Iterable<A> i, Function<A,B> function) {
		return new MappingIterable<>(i, function);
	}
	
	/**
	 * Builds an iterable that skips a number of elements from a source iterable. 
	 * @param <A> element type
	 * @param i   source iterable
	 * @param cp  number of elements to skip
	 * @return    iterable that skips a number of elements
	 */
	public static <A> ZenIterable <A> skip(Iterable<A> i, int cp) {
		return new SkippingIterable<>(i, cp);
	}

	/**
	 * Builds an iterable that contains only the elements that matches the provided predicate.
	 * @param <A> element type 
	 * @param i   iterable to filter
	 * @param f   filter
	 * @return    filtered iterable
	 */
	public static <A> ZenIterable <A> filter(Iterable<A> i, Predicate<A> f) {
		return new FilteringIterable<>(i, f);
	}
	
	/**
	 * Creates a ZenITerable from an Iterable.
     * If the source iterable is null, an empty iterable is returned.
	 * @param <A> iterable element type
	 * @param i   source iterable
	 * @return    zen iterable
	 */
	public static <A> ZenIterable <A> zen(Iterable<A> i) {
		if (i == null)
			return new EmptyIterable<>();
		return skip(i,0);
	}
	
	public static <A> ZenIterable <A> zenIt(Iterator<A> i) {
		if (i == null)
			return new EmptyIterable<>();
		return () -> i;
	}
	
	/**
	 * Build a zen iterable from a vararg.
	 * @param <A> type des elemments de l'iterable
	 * @param as values
	 * @return   iterable
	 */
	// Method produces a javac warning
	@SafeVarargs
	public static final <A> ZenIterable<A> zenThem(A... as) {
		return zen(Arrays.asList(as));
	}
	
	public static <A> Optional<A> first(Iterable<A> i) {
		if (i == null)
			return Optional.empty();			
		for (A a : i)
			return Optional.of(a);
		return Optional.empty();
	}
	
	public static <A> List<A> toList(Iterable<A> i) {
		List<A> l = new ArrayList<>();
		if (i != null)
			for (A a : i)
				l.add(a);
		return l;
	}
	
	public static <A> Set<A> toSet(Iterable<A> i) {
		Set<A> l = new HashSet<>();
		if (i != null)
			for (A a : i)
				l.add(a);
		return l;
	}
	
	public static <A,K,V> Map<K,V> toMap(Iterable<A> i, Function<A,K> k, Function<A,V> v) {
		Map<K,V> m = new HashMap<>();
		for (A a : i)
			m.put(k.apply(a), v.apply(a));
		return m;
	}
	
	public static int sum(Iterable<Integer> l) {
		return foldl(l, 0, (sum,e) -> sum + e);
	}
	
	public static <A,B> B foldl(Iterable<A> i, B b, BiFunction<B,A,B> f) {
		if (i != null)
			for (A a : i)
				b = f.apply(b, a);
		return b;
	}
	
	// Insert the fold result in the stream itself
	public static <A,B> ZenIterable<A> foldlIn(Iterable<A> i, B b, BiFunction<B,A,B> f, Function<B,A> post) {
		ZenIterable<A> fi = new ZenIterable<A>() {

			@Override
			public Iterator<A> iterator() {
				return new Iterator<A>() {
					
					private B acc = b;
					private Iterator<A> it = i.iterator();
					private boolean append = true;
					
					@Override
					public boolean hasNext() {
						return it.hasNext() || append;
					}

					@Override
					public A next() {
						if (it.hasNext()) {
							A a = it.next();
							acc = f.apply(acc,a);
							return a;
						}
						if (append) {
							append = false;
							return post.apply(acc);
						}
						throw new RuntimeException("no more elements");
					}
					
				};
			}
			
		};
		return fi;
	}
	
	/**
	 * Builds a string that is the concatenation of the elements of an iterable of strings.
	 * @param i iterable to concatenate elements of
	 * @return  concatenation result
	 */
	public static String concat(Iterable<String> i) {
		return foldl(i, new StringBuilder(), (b,s) -> b.append(s)).toString();
	}
	
	/**
	 * Create an iterable that behaves like the original iterable with a given value
	 * inserted between the source iterable elements. 
	 * @param <A> element type
	 * @param i   source iterable
	 * @param a   value to intercalate
	 * @return    iterable that behaves like the source with the 'a' argument intercalated
	 */
	public static <A> ZenIterable<A> intercalate(Iterable<A> i, A a) {
		return new IntercalatingIterable<>(i,a);
	}
	
	/**
	 * Create an iterable that behaves like the provided iterable with an extra element added at the front.
	 * @param <A> element type
	 * @param i   source iterable
	 * @param a   element to prepend
	 * @return    new iterable that behaves like the input iterable with an extra element at the front
	 */
	public static <A> ZenIterable<A> prepend(Iterable<A> i, A a) {
		return new PrependingIterable<>(i, a);
	}
	
	/**
	 * Create an iterable that behaves like the provided iterable with an extra element added at the end.
	 * @param <A> element type
	 * @param i   source iterable
	 * @param a   element to append
	 * @return    new iterable that behaves like the input iterable with an extra element at the end
	 */
	public static <A> ZenIterable<A> append(Iterable<A> i, A a) {
		return new AppendingIterable<>(i, a);
	}
	
	public static <A> ZenIterable<A> surround(Iterable<A> i, A before, A between, A after) {
		return intercalate(i,between).prepend(before).append(after);
	}
	
	public static <A, B extends Iterable<A>> ZenIterable<A> flatten(Iterable<B> ii) {
		return new FlatteningIterable<>(ii);
	}
	
	public static ZenIterable<Integer> range(int from, int to) {
		if (from > to)
			return range(from, to, -1);
		if (from < to)
			return range(from, to, 1);
		return range(from, to, 0);
	}
	
	public static ZenIterable<Integer> range(int from, int to, int step) {
		return new IntegerRange(from, to, step);
	}

	public static ZenIterable<Integer> range(int from) {
		return range(from,Integer.MAX_VALUE);
	}
	
	public static ZenIterable<Integer> rangeFrom(int from, int step) {
		if (step < 0)
			return range(from, Integer.MIN_VALUE, step);
		if (step > 0)
			return range(from, Integer.MAX_VALUE, step);
		return range(from, from + 1, 0);
	}
	
	/**
	 * A null iterable yields an empty iterable, otherwise the
	 * argument iterable is returned.
	 * @param <A> iterable element type
	 * @param i   iterable to 'enforce' existence of 
	 * @return    a non null iterable
	 */
	public static <A> Iterable<A> nullAsEmpty(Iterable<A> i) {
		if (i == null)
			return new EmptyIterable<>();
		return i;
	}
	
	// Iteration over "indexed" elements would use 
	// for (Pair<Integer,T> p : zip(range(0),coll)) { ... }
	// Zip is as long as the shortest iterable.
//	public static <A,B> Iterable<ImmutablePair<A,B>> zip(Iterable<A> a, Iterable<B> b) {
	/**
	 * Zips two iterables, null iterables are considered empty.
	 * @param <A> first iterable element type
	 * @param <B> second iterable element type
	 * @param a first iterable
	 * @param b second iterable
	 * @return  zip
	 */
	public static <A,B> ZippingIterable<A,B> zip(Iterable<A> a, Iterable<B> b) {
		return new ZippingIterable<>(nullAsEmpty(a), nullAsEmpty(b));
	}
	
	/**
	 * Zip range(from), null iterable is considered empty ({@link #nullAsEmpty(Iterable)}.
	 * @param <A> element type 
	 * @param from start index
	 * @param i          iterable
	 * @return           'indexed' iterable 
	 */
	public static <A> ZippingIterable<Integer,A> index(int from, Iterable<A> i) {
		return new ZippingIterable<>(range(from), nullAsEmpty(i));
	}
	
	public static <A> boolean contains(Iterable<A> i, Predicate <A> p) {
		for (A a : nullAsEmpty(i)) 
			if (p.test(a))
				return true;
		return false;
	}
	
	public static <A> String asString(Iterable<A> i) {
		StringBuilder b = new StringBuilder();
		for (A a : i)
			b.append(a.toString());
		return b.toString();
	}
	
	public static <A> ZenIterable<A> repeat(A a, int count) {
		return repeat(a).take(count);
	}
	
	public static <A> ZenIterable<A> repeat(A a) {
		return new ZenIterable<A>() {

			@Override
			public Iterator<A> iterator() {
				return new Iterator<A>() {

					@Override
					public boolean hasNext() {
						return true;
					}

					@Override
					public A next() {
						return a;
					}
					
				};
			}
			
		};
	}

	public static <A> ZenIterable<A> take(Iterable<A> i, int count) {
		return new ZenIterable<A>() {

			@Override
			public Iterator<A> iterator() {
				return new Iterator<A>() {

					private int remains = count;
					private Iterator<A> ii = i.iterator();
					
					@Override
					public boolean hasNext() {
						return ii.hasNext() && remains > 0;
					}

					@Override
					public A next() {
						remains --;
						return ii.next();
					}
					
				};
			}
			
		};
	}
	
	// Could use fold but a direct count is enough
	public static int count(Iterable<?> i) {
		int count = 0;
		for (@SuppressWarnings("unused") Object o : i)
			count ++;
		return count;
	}
	
	/**
	 * Produces an iterable with no duplicates for the given key projection,
	 * uses an internal index that is proportional to the stream size.
	 * @param <A> iterable element type
	 * @param <B> projection key type
	 * @param i   source iterable
	 * @param key projection 
	 * @return    filtered iterable with no duplicates for the given key
	 */
	public static <A,B> ZenIterable<A> unique(Iterable<A> i, Function<A,B> key) {
		Set<B> index = new HashSet<>();
		// Side effect filter that is an impure but simple implementation
		return filter(i, a -> {
			B b = key.apply(a);
			if (index.contains(b))
				return false;
			index.add(b);
			return true;
		});
	}
	
	public static <A> ZenIterable<A> unique(ZenIterable<A> i) {
		return unique(i, a -> a); 
	}
	/**
	 * Si une operation sur l'iterable est une fonction instable, la methode stable fabrique un iterable 
	 * qui fait au maximum un appel à f(x), ce qui rend l'iterable stable.
	 * (si fonction instable f(x) ne retourne pas toujours le meme resultat; (ex : si retourne objet) 
	 * si fonction stable, f(x) renvoie toujours le meme resultat)
	 * @param <A>     iterable element type
	 * @param  i      iterable a stabiliser
	 * @return stable iterable
	 */
	public static <A> ZenIterable<A> stable(ZenIterable<A> i) {
		return zen(i.toList());
	}
	
	public static <A,B> ZenIterable<B> flatMap(Iterable<A> i, Function<A, ? extends Iterable<B>> f) { 
		return Iterables.flatten(Iterables.map(i, f)); 
	}

}
