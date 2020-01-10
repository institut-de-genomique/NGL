package fr.cea.ig.lfw.utils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import fr.cea.ig.lfw.utils.iteration.ZippingIterable;

/**
 * Interface qui permet d'utiliser un iterable de {@literal <A>} avec les methodes skip, map et filter
 * en les chainant (fluent java).
 * 
 * @param <A> element type
 * 
 * @author sgas
 *
 */
public interface ZenIterable <A> extends Iterable <A> {
	
	/**
	 * Transforme un ZenIterable de A en ZenIterable de B, grace à la methode indiquée
	 * <p>
	 * Attention la methode map devrait prendre une fonction pure : fonction qui retourne toujours le meme resultat
	 * si memes arguments utilisés. Si on utilise dans map une methode qui retourne un objet, alors si cette methode 
	 * est appelée 2 fois avec les memes arguments, elles retourna 2 objets distincts avec memes valeurs 
	 * mais avec des adresses differentes, d'ou probleme ensuite si utilisation de 2 each à suivre car  
	 * alors implicitement 2 appels de map. 
	 *
	 * @param   f Fonction de transformation de B vers A
	 * @param <B> view elements type
	 * @return    ZenIterable de B
	 */
	default <B> ZenIterable <B> map (Function <A,B> f) {
		return Iterables.map(this, f);
	}
	
	/**
	 * Ignore les cp premiers elements de l'iterable
	 * @param  cp nombre de premiers élements à ignorer
	 * @return    ZenIterable de B
	 */
	default ZenIterable <A> skip (int cp) {
		return Iterables.skip(this, cp);
	}
	
	/**
	 * Filtre un iterables, ne garde que les éléments pour qui le 
	 * prédicat est évalué a vrai.
	 * @param f prédicat de filtrage
	 * @return  ZenIterable filtré
	 */
//	default ZenIterable <A> filter (Function<A,Boolean> f) {
//		return Iterables.filter(this, f);
//	}
	default ZenIterable <A> filter (Predicate<A> f) {
		return Iterables.filter(this, f);
	}
	
	// default void each(Function<A, Void> f) { => oblige à return null dans f.
	/**
	 * Calls the consumer {@link Consumer#accept(Object)} method for each element.
	 * @param  f fonction appelé avec chaque element a
	 * @return   this pour pouvoir chainer les appels
	 */
	default ZenIterable <A> each(Consumer<A> f) {
		forEach (f);
		return this;
	}
	
	default Optional<A> first() {
		return Iterables.first(this);
	}
	
	default List<A> toList() {
		return Iterables.toList(this);
	}
	
	default Set<A> toSet() {
		return Iterables.toSet(this);
	}
	
	default <K,V> Map<K,V> toMap(Function<A,K> k, Function<A,V> v) {
		return Iterables.toMap(this, k, v);
	}
	
	default ZenIterable<A> prepend    (A a) { return Iterables.prepend    (this, a); }
	default ZenIterable<A> append     (A a) { return Iterables.append     (this, a); }
	default ZenIterable<A> intercalate(A a) { return Iterables.intercalate(this, a); }
	default ZenIterable<A> surround(A before, A between, A after) { return Iterables.surround(this, before, between, after); }
	default ZenIterable<A> countIn(Function<Integer,A> f) { return this.foldlIn(0, (a,e) -> a + 1, f); }
	default <B> ZenIterable<A> foldlIn(B start, BiFunction<B,A,B> f, Function<B,A> g) { return Iterables.foldlIn(this,start,f,g); }
	default ZenIterable<A> foldlIn(A start, BiFunction<A,A,A> f) { return Iterables.foldlIn(this,start,f,x -> x); }
	default <B> B          foldl(B b, BiFunction<B,A,B> f) { return Iterables.foldl(this,b,f); }
	default String asString() { return Iterables.asString(this); }
	//default String asString() {
	//	return Iterables.concat(map(Object::toString));
	//}
	default ZenIterable<A> take(int count) { return Iterables.take(this, count); }
//	default <B> ZippingIterable<ImmutablePair<A,B>> zip(Iterable<B> i) { return Iterables.zip(this, i); }
	default <B> ZippingIterable<A,B> zip(Iterable<B> i) { return Iterables.zip(this, i); }
	
	/**
	 * Maps the elements using the provided function and flattens the result.
	 * See {@link #map(Function)}, {@link Iterables#flatten(Iterable)}
	 * @param <B> result element type
	 * @param f   function to map elements
	 * @return    flattened iterable 
	 */
	default <B> ZenIterable<B> flatMap(Function<A, ? extends Iterable<B>> f) { 
		return Iterables.flatten(map(f)); 
	}
	
	default <B> ZenIterable<A> unique(Function<A,B> key) { return Iterables.unique(this, key); }
	default     ZenIterable<A> unique()                  { return Iterables.unique(this); }
	/**
	 * la methode stable fabrique un iterable 
	 * qui fait au maximum un appel à f(x), ce qui rend l'iterable stable.
	 * Voir {@link Iterables#stable(ZenIterable)}
	 * @return iterable stable 
	 */
	default     ZenIterable<A> stable()                  { return Iterables.stable(this); }
	
}


//// moins pratique que interface car 1! slot pour heritage alors que plusieurs pour interface.
//abstract class ZenIterable2 <A> implements Iterable <A> {
//	public <B> ZenIterable <B> map (Function <A,B> f) {
//		return Iterables.map(this, f);
//	}
//	public ZenIterable <A> skip (int cp) {
//		return Iterables.skip(this, cp);
//	}
//	public ZenIterable <A> filter (Function<A,Boolean> f) {
//		return Iterables.filter(this, f);
//	}
//}
