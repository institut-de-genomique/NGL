package fr.cea.ig.util.function;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class CCActions {
	
	static class CC1ManagedLifeCycle<A extends CC1Managed> implements CC1<A> {
		
		private final Supplier<A> supplier;
		
		public CC1ManagedLifeCycle(Supplier<A> supplier) {
			this.supplier = supplier;
		}
		
		@Override
		public void accept(C1<A> c) throws Exception {
			A a = supplier.get();
			try {
				a.setUp();
				c.accept(a);
			} finally {
				a.tearDown();
			}
		}
		
	}

	/**
	 * Builds a CC1 for a CC1Managed implementor.
	 * @param <A>      type of managed object
	 * @param supplier CC1Managed factory
	 * @return         CC1
	 */
	public static <A extends CC1Managed> CC1<A> managed(Supplier<A> supplier) {
		return new CC1ManagedLifeCycle<>(supplier);
	}

	/**
	 * Action built from a producer (F0).
	 * @param <A> type of managed object
	 * @param f   producer
	 * @return    action that supplies objects from the producer
	 */
	public static <A> CC1<A> f0asCC1(F0<A> f) {
		return nc -> nc.accept(f.apply());
	}
	
	// --------------------------------------------------------
	// Timing
	
	/**
	 * Provide timing of before and after CC actions.
	 * @param <A>   type of managed object
	 * @param title logging title
	 * @param cc    CC to provide timing for
	 * @return      user execution time logging 
	 */
	public static <A> CC1<A> timeCC1(String title, CC1<A> cc) {
		return new Timing<>(title,cc);		
	}

	/**
	 * Provide timing of before and after CC actions.
	 * @param <A>   type of managed object
	 * @param <B>   type of managed object
	 * @param title logging title
	 * @param cc    CC to provide timing for
	 * @return      user execution time logging 
	 */
	public static <A,B> CC2<A,B> timeCC2(String title, CC2<A,B> cc) {
		return cct2(new Timing<>(title,cc.ct()));
	}
	
	/**
	 * Provide timing of before and after CC actions.
	 * @param <A> type of managed object
	 * @param <B> type of managed object
	 * @param <C> type of managed object
	 * @param title logging title
	 * @param cc    CC to provide timing for
	 * @return      user execution time logging 
	 */
	public static <A,B,C> CC3<A,B,C> timeCC3(String title, CC3<A,B,C> cc) {
		return cct3(new Timing<>(title,cc.ct()));
	}

	/**
	 * Build a CC2 from a {@literal CC1<T2>} which is almost a CT2.
	 * @param <A> type of managed object
	 * @param <B> type of managed object
	 * @param cc CC to convert
	 * @return   CC2
	 */
	public static <A,B> CC2<A,B> cct2(CC1<T2<A,B>> cc) {
		return nc -> cc.accept(t -> nc.accept(t.a,t.b));
	}
	
	/**
	 * Build a CC3 from a {@literal CC1<T3>} which is almost a CT3.
	 * @param <A> type of managed object
	 * @param <B> type of managed object
	 * @param <C> type of managed object
	 * @param cc CC to convert
	 * @return   CC3
	 */
	public static <A,B,C> CC3<A,B,C> cct3(CC1<T3<A,B,C>> cc) {
		return nc -> cc.accept(t -> nc.accept(t.a,t.b,t.c));
	}

	/**
	 * Internal append utility.
	 * @param lc list to append to
	 * @param cc action to append
	 * @return   new list
	 */
	private static <A> CC1<List<A>> append(CC1<List<A>> lc, CC1<A> cc) {
	    return nc -> lc.accept(l -> cc.accept(e -> { l.add(e); nc.accept(l); }));
	}

	/**
	 * Transforms a list of CC1 into a CC1 of a list.
	 * @param <A> type of managed object
	 * @param l list of CC1 to transform
	 * @return  transformed list
	 */
	public static <A> CC1<List<A>> unwrap(List<CC1<A>> l) {
	    CC1<List<A>> r = new T1<List<A>>(new ArrayList<>()).cc();
	    for (CC1<A> a : l)
	        r = append(r,a);
	    return r;
	}
	
	
}

//
// Not resistant to failure, but not important either.
//
class Timing<A> implements CC1<A> {
	
	private static final play.Logger.ALogger timeLogger = play.Logger.of(Timing.class);

	private String message;
	private CC1<A> cc;
	private long preStartTime;
	private long preEndTime;
	private long postStartTime;
	private long postEndTime;
	
	public Timing(String message, CC1<A> cc) {
		this.message = message;
		this.cc      = cc;
	}

	@Override
	public void accept(C1<A> c) throws Exception {
		preStartTime = System.currentTimeMillis();
		timeLogger.debug("starting {}", message);
		cc.accept(new C1<A>() {
			
			@Override
			public void accept(A a) throws Exception {
				preEndTime = System.currentTimeMillis();
				timeLogger.debug("done pre {} in {}ms", message, (preEndTime - preStartTime));
				c.accept(a);
				postStartTime = System.currentTimeMillis();
			}
			
		});
		postEndTime = System.currentTimeMillis();
		timeLogger.debug("done post {} in {}ms", message, (postEndTime - postStartTime));
	}
	
}



