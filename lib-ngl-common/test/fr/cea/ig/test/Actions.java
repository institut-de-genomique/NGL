package fr.cea.ig.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import fr.cea.ig.DBObject;
import fr.cea.ig.lfw.utils.Iterables;
import fr.cea.ig.play.IGGlobals;
import fr.cea.ig.util.function.CC1;
import fr.cea.ig.util.function.CC2;
import fr.cea.ig.util.function.CCActions;
import fr.cea.ig.util.function.F0;
import fr.cea.ig.util.function.T;
import fr.cea.ig.util.function.T2;

/**
 * Actions that are defined for the tests.
 * 
 * @author vrd
 *
 */
public class Actions {
	
	private static final play.Logger.ALogger logger = play.Logger.of(Actions.class);
	
	/**
	 * Use the given object and persist it using the API and removing
	 * it from the database when done.
	 * <p>
	 * This kind of action cannot really be reused in the sense that
	 * the instance is fixed so 2 instances cannot be used, the safe way
	 * is to use the factory version. 
	 * @param user user for the API action
	 * @param a    object to manage life cycle of
	 * @return     a persistence action
	 */
	public static <A extends DBObject> CC1<A> use(String user, A a) {
		return using(user, () -> a);
	}
	
	/**
	 * Use the given factory and persist a new instance using the API and removing
	 * it from the database when done. 
	 * @param user user for the API action
	 * @param a    factory of objects to manage life cycle of
	 * @return     a persistence action
	 */	
	public static <A extends DBObject> CC1<A> using(String user, F0<A> a) {
		return new Using<>(user,a);
	}	
	
	/**
	 * Use a factory as an instance generator action without persisting the instance.
	 * @param f factory
	 * @return  instance generation action
	 */
	public static <A> CC1<A> gen(F0<A> f) {
		return nc -> nc.accept(f.apply());
	}

	/**
	 * Use a factory to generate an object that is persisted and provide the
	 * created and the persisted instances as arguments. 
	 * @param user user
	 * @param ff   factory
	 * @return     creation and persistence action
	 */
	public static <A extends DBObject> CC2<A,A> using2(String user, F0<A> ff) {
		return gen(ff).nest(a -> use(user,a));
	}
	
	/**
	 * Generate a list of managed objects using the same factory count times.
	 * @param count number of instances to generate and manage
	 * @param user  persistence user
	 * @param sa    factory
	 * @return      CC1 of instance list
	 */
	public static <A extends DBObject> CC1<List<A>> repeat(int count, String user, F0<A> sa) {
//		return nc -> {
//			Stack<T2<Class<?>,String>> objects = new Stack<>();
//			List<A> as = new ArrayList<>();
//			AKindOfMagic m = IGGlobals.instanceOf(AKindOfMagic.class);		
//			try {
//				for (int i=0; i<count; i++) {
//					A        a     = sa.apply();
//					A        na    = m.create(a, user);
//					Class<?> clazz = na.getClass();
//					String   id    = na._id;
//					as.add(na);
//					objects.push(T.t2(clazz, id));
//				}
//				nc.accept(as);
//			} finally {
//				while (objects.size() > 0) {
//					T2<Class<?>,String> t = objects.pop();
//					try {
//						m.destroy(t.a,t.b);
//					} catch (Exception e) {
//						logger.debug("ignoring deletion error of " + t.a + " " + t.b);
//					}
//				}
//			}
//		};
		return usings(user, Iterables.repeat(sa, count));
	}
	
	// Some parts are not efficient, would need some specialized types.
    public static <A> A       head(List<A> l) { return l.get(0); }
    public static <A> List<A> tail(List<A> l) { return l.subList(1, l.size()-1); }
    public static <A> List<A> pre(A a, List<A> l) { List<A> r = new ArrayList<>(); r.add(a); r.addAll(l); return r; }
    public static <A> List<A> app(List<A> l, A a) { List<A> r = new ArrayList<>(l); r.add(a); return r; }
	
	
	// --------------------------------------------------------
	// Sample code
	
	/**
	 * Example code of using implementation, lacks logging. 
	 * @param user user identity to use
	 * @param sa   object supplier
	 * @return     creation and persistence action
	 */
	@SuppressWarnings("unused")
	private static <A extends DBObject> CC1<A> using_(String user, F0<A> sa) {
		return nc -> {
			AKindOfMagic m = IGGlobals.instanceOf(AKindOfMagic.class);		
			Class<?> clazz = null;
			String id = null;
			try {
				A a   = sa.apply();
				A na  = m.create(a,user);
				clazz = na.getClass();
				id    = na._id;
				nc.accept(na);
			} finally {
				if (clazz != null && id != null)
					m.destroy(clazz,id);
			}
		};
	}
	
	/**
	 * Manages the cleanup of a given DB object.
     * @param sa object supplier
     * @return   destroying action
	 */
	public static <A extends DBObject> CC1<A> cleaningOne(F0<A> sa) {
        return nc -> {
            AKindOfMagic m = IGGlobals.instanceOf(AKindOfMagic.class);      
            A a = sa.apply();
            try {
                nc.accept(a);
            } finally {
                m.destroy(a.getClass(), a._id);
            }
        };
    }

	/**
	 * Manages the cleanup of a list of DB objects.
	 * @param sa         object supplier
     * @return           destroying action
	 * @throws Exception when something went wrong
	 */
	public static <A extends DBObject> CC1<List<A>> cleaning(F0<List<A>> sa) throws Exception {
	    return CCActions.unwrap(sa.apply().stream().map(a -> cleaningOne(() -> a)).collect(Collectors.toList()));    
    }

	/**
	 * Use a collection of factories to build a CC of list of managed objects. This is a 
	 * kind of specialization of {@link CCActions#unwrap} in the context of NGL object life cycle.
	 * @param <A>  managed object type
	 * @param user persister identity
	 * @param sas  factories
	 * @return     CC of list
	 */
	public static <A extends DBObject> CC1<List<A>> usings(String user, Iterable<F0<A>> sas) {
		return nc -> {
			Stack<T2<Class<?>,String>> objects = new Stack<>();
			List<A> as = new ArrayList<>();
			AKindOfMagic m = IGGlobals.instanceOf(AKindOfMagic.class);		
			try {
				for (F0<A> sa : sas) {
					A        a     = sa.apply();
					A        na    = m.create(a, user);
					Class<?> clazz = na.getClass();
					String   id    = na._id;
					as.add(na);
					objects.push(T.t2(clazz, id));
				}
				nc.accept(as);
			} finally {
				while (objects.size() > 0) {
					T2<Class<?>,String> t = objects.pop();
					try {
						m.destroy(t.a,t.b);
					} catch (Exception e) {
						logger.debug("ignoring deletion error of " + t.a + " " + t.b);
					}
				}
			}
		};
	}

	public static <A extends DBObject> CC1<List<A>> uses(String user, Iterable<A> as) {
		return usings(user, Iterables.map(as, a -> () -> a));
	}
	
}


