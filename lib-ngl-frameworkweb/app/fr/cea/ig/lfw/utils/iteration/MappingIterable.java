package fr.cea.ig.lfw.utils.iteration;

import java.util.Iterator;
import java.util.function.Function;

import fr.cea.ig.lfw.utils.ZenIterable;

/**
 * Transforme un iterable A en iterable B.
 * 
 * @author sgas
 *
 * @param <A> type of source elements
 * @param <B> type of viewed elements
 */
public class MappingIterable <A,B> implements ZenIterable <B> {
	
	/**
	 * Iterable to provide a view of.
	 */
	private final Iterable <A> i;
	
	/**
	 * Function to apply to wrapped iterable elements.
	 */
	private final Function <A, B> function;
	
	/**
	 * Constructeur.
	 * @param i        iterable dont on veut une vue
	 * @param function fonction a appliquer pour creer la vue
	 */
	public MappingIterable(Iterable <A> i, Function <A, B> function) {
		this.i = i;
		this.function = function;
	}

	/**
	 * Fournit un iterator de B, transformé de l'iterator A
	 */
	@Override
	public Iterator<B> iterator() {
		Iterator <A> j = i.iterator();
		Iterator <B> k = new MappingIterator <>(j, function);
		return k;
	}
	
}
