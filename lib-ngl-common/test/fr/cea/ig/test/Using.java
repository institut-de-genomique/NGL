package fr.cea.ig.test;

import fr.cea.ig.DBObject;
import fr.cea.ig.play.IGGlobals;
import fr.cea.ig.util.function.C1;
import fr.cea.ig.util.function.CC1;
import fr.cea.ig.util.function.F0;

/**
 * Implements the using action. This could have been implemented directly in the
 * Actions class using a lambda but this version allows proper logging and possibly
 * a more readable implementation.
 * 
 * @author vrd
 *
 * @param <A> type of object to persist
 */
public class Using<A extends DBObject> implements CC1<A> {
	
	/**
	 * Logger.
	 */
	private static final play.Logger.ALogger logger = play.Logger.of(Using.class);
	
	/**
	 * User that persists the object.
	 */
	private final String user;
	
	/**
	 * Instance factory.
	 */
	private final F0<A> fa;
	
	/**
	 * Constructor.
	 * @param user user that creates the object
	 * @param fa   factory to produce objects to persist
	 */
	public Using(String user, F0<A> fa) {
		this.user = user;
		this.fa = fa;
	}
	
	@Override
	public void accept(C1<A> cr) throws Exception {
		exec(user, fa, cr);
	}
	
	/**
	 * Persists an object fetched from a factory, calls some dependent code and finally
	 * removes the created instance from the data base. 
	 * @param user identity of the persisting user
	 * @param sa   instance factory
	 * @param cr   dependent code
	 * @throws Exception when something went wrong
	 */
	public static <A extends DBObject> void exec(String user, F0<A> sa, C1<A> cr) throws Exception {
		AKindOfMagic m = IGGlobals.instanceOf(AKindOfMagic.class);		
		A a = null;
		Class<?> clazz = null;
		String id = null;
		try {
			logger.debug("getting instance to create");
			a = sa.apply();
			logger.debug("creating instance through API {}", a);
			A na = m.create(a,user);
			clazz = na.getClass();
			id = na._id;
			logger.debug("calling user code");
			cr.accept(na);
			logger.debug("user code done");
		} finally {
			if (clazz != null && id != null) {
				logger.debug("cleaning {}", id);
				m.destroy(clazz,id);
			} else {
				if (a == null)
					logger.debug("no instance was provided");
				else
					logger.debug("API failed, no cleaning for {} {}", a.getClass(), a.getCode());
			}
		}
	}

}
