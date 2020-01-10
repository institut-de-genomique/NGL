package fr.cea.ig.lfw.utils;

import java.util.Iterator;
import java.util.function.Function;

import fr.cea.ig.lfw.utils.iteration.MappingIterator;
import fr.cea.ig.lfw.utils.iteration.SkippingIterator;

/**
 * Pas encore utilisé.
 * 
 * @author sgas 
 *
 */
public class Iterators {
	
	public static <A, B> Iterator<B> map(Iterator<A> i, Function<A,B> function) {
		return new MappingIterator<>(i, function);
	}
	
	public static <A> Iterator<A> skip (Iterator <A> i, int cp) {
		return new SkippingIterator<>(i, cp);
	}
	
}
