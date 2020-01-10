package fr.cea.ig.lfw.utils.iteration;

import java.util.Iterator;
import java.util.function.Function;

/**
 * Transforme un iterator de A en iterator de B.
 * 
 * @author sgas
 *
 * @param <A> type of wrapped iterator elements
 * @param <B> type of viewed elements
 */
public class MappingIterator <A,B> implements Iterator <B> {
	
	/**
	 * Wrapped iterator.
	 */
	private final Iterator <A> i;
	
	/**
	 * Function applied to view wrapped iterator elements.
	 */
	private final Function <A,B> f;
	
	/**
	 * Methode de transformation de A vers B.
	 * @param i iterateur
	 * @param f fonction a appliquer
	 */
	public MappingIterator(Iterator <A> i, Function <A,B> f) {
		this.i = i;
		this.f = f;
	}
	
	/**
	 * Indique si l'iterator peut encore fournir un element. 
	 */
	@Override
	public boolean hasNext() {
		return i.hasNext();
	}
	
	/**
	 * Renvoie le prochain B (prochain element de A tranformé en B).
	 */
	@Override
	public B next() {
//		A a = i.next();
//		B b = f.apply(a);
//		return b;
		return f.apply(i.next());
	}
	
}
