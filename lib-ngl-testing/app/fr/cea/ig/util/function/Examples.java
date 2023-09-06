package fr.cea.ig.util.function;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// Barely usable i think.
abstract class FList<A> {
	public static class Cons<A> extends FList<A> { 
		public final A head; public final FList<A> tail;
		public Cons(A head, FList<A> tail) {
			this.head = head; this.tail = tail;
		}
		@Override
		public <I,O> O destructi(I i, F2<I,Cons<A>,O> cc, F2<I,Nil<A>,O> cn) throws Exception {
			return cc.apply(i,this);
		}
		@Override
		public <O> O destruct(F1<Cons<A>, O> cc, F1<fr.cea.ig.util.function.FList.Nil<A>, O> cn) throws Exception {
			return cc.apply(this);
		}

	}
	public static class Nil<A>  extends FList<A> {

		@Override
		public <I,O> O destructi(I i, F2<I,Cons<A>,O> cc, F2<I,Nil<A>,O> cn) throws Exception {
			return cn.apply(i, this);
		}

		@Override
		public <O> O destruct(F1<fr.cea.ig.util.function.FList.Cons<A>, O> cc, F1<Nil<A>, O> cn) throws Exception {
			return cn.apply(this);
		} 

	}
	public abstract <I,O> O destructi(I i, F2<I,Cons<A>,O> cc, F2<I,Nil<A>,O> cn) throws Exception;
	public abstract <O> O destruct(F1<Cons<A>,O> cc, F1<Nil<A>,O> cn) throws Exception;
	
	// Stack heavy implementation.
	public static <A> FList<A> flist(Iterable<A> i) {
		return flist(i.iterator());
	}
	public static <A> FList<A> flist(Iterator<A> i) {
		if (i.hasNext())
			return new Cons<>(i.next(), flist(i));
		return new Nil<>();			
	}
	public static <A> List<A> list(FList<A> l) throws Exception {
		return list(l, new ArrayList<>());
	}
	public static <A> List<A> list(FList<A> l, List<A> r) throws Exception {
		return l.destruct(cc -> { r.add(cc.head); return list(cc.tail,r); }, cn -> r);
	}
}

/**
 * Example code.
 * 
 * @author vrd
 *
 */
public class Examples {

	/**
	 * Implementation of (roughly) try with resources. 
	 * @param file file to use to provide the input stream 
	 * @return     input stream action
	 */
	public static CC1<FileInputStream> inputStream(File file) {
		return nc -> {
			// Sample code that manages the resource itself
			FileInputStream i = null;
			try {
				i = new FileInputStream(file);
				nc.accept(i);
			} finally {
				if (i != null)
					i.close();
			}
		};
	}
	
//	// Iterator is a close match to FList, we use an accumulator.
	// Using a mutable list inside chained called could be disaster.
//	public static <A> CC1<List<A>> unwrap(Iterable<CC1<A>> i) {
//		return unwrap(i.iterator(),new T1<List<A>>(new ArrayList<>()).cc());
//	}
//	public static <A> CC1<List<A>> unwrap(Iterator<CC1<A>> i, CC1<List<A>> acc) {
//		if (!i.hasNext())
//			return acc;
//		else
//			return nc -> acc.accept(l -> { l.add(e)); 
//	}
	
	
//	public static <A> FList<A> cons(A head, FList<A> tail) { return new FList.Cons<>(head,tail); }
//	public static <A> FList<A> nil()                       { return new FList.Nil<>(); }
//	public static <A> CC1<FList<A>> unwrap(FList<CC1<A>> l) throws Exception {
//		return l.destruct(cc -> (nc -> unwrap(cc.tail).accept(tail -> cc.head.accept(head -> nc.accept(cons(head,tail))))), 
//				          cn -> (nc -> nc.accept(nil())));
//	}
//	public static <A> CC1<List<A>> unwrap(List<CC1<A>> l) throws Exception {
//		return unwrap(FList.flist(l)).cc1(fl -> FList.list(fl));
//	}
	
//	// Some parts are not efficient, would need some specialized types.
//	public static <A> A       head(List<A> l) { return l.get(0); }
//	public static <A> List<A> tail(List<A> l) { return l.subList(1, l.size()-1); }
//	public static <A> List<A> pre(A a, List<A> l) { List<A> r = new ArrayList<>(); r.add(a); r.addAll(l); return r; }
////	public static <A> List<A> app(List<A> l, A a) { List<A> r = new ArrayList<>(l); r.add(a); return r; }
//	
//	// No accumulator version, relies on list clones that will be inefficient on
//	// large lists. Consing is probably appropriate here. List -> Cons is linear and conversely.
//	// Could build some lazy consing over iterables.
//	public static <A> CC1<List<A>> unwrap(List<CC1<A>> l) { 
//		if (l.size() == 0) 
//			return new T1<List<A>>(new ArrayList<>()).cc();
//		else
//			return nc -> unwrap(tail(l)).accept(rl -> head(l).accept(a -> nc.accept(pre(a,rl))));
//	}

	
//	public static <A> CC1<List<A>> unwrap(List<CC1<A>> l) { 
//		return unwrap(l,new T1<List<A>>(new ArrayList<>()).cc());
//	}
//	
//	// Try to use "immutable" lists (do not alter input list and intermediate lists).
//	public static <A> CC1<List<A>> unwrap(List<CC1<A>> l, CC1<List<A>> r) {
//		if (l.size() == 0) { 
//			return r;
//		} else {
//			CC1<A>       head = l.get(0);
//			List<CC1<A>> tail = l.subList(1, l.size()-1);
//			return unwrap(tail, nc -> r.accept(cl -> head.accept(e -> {
//				List<A> nl = new ArrayList<>(cl);
//				nl.add(e); 
//				nc.accept(cl); 
//			})));
//		}
//	}
	
	private static <A> CC1<List<A>> append(CC1<List<A>> lc, CC1<A> cc) {
		return nc -> lc.accept(l -> cc.accept(e -> { l.add(e); nc.accept(l); }));
	}
	public static <A> CC1<List<A>> unwrap(List<CC1<A>> l) {
		CC1<List<A>> r = new T1<List<A>>(new ArrayList<>()).cc();
		for (CC1<A> a : l)
			r = append(r,a);
		return r;
	}
		
}
