package fr.cea.ig.ngl.utils;

import fr.cea.ig.util.function.C0;

/**
 * Simple timing method.
 * 
 * @author vrd
 *
 */
public class UserExecutionTime {
	
	private static final play.Logger.ALogger logger = play.Logger.of(UserExecutionTime.class);
	
	public static long run(String name, C0 c) throws Exception {
		long start = System.currentTimeMillis();
		logger.debug("starting {} ({})", name, c);
		c.accept();
		long duration = System.currentTimeMillis() - start;
		logger.debug("ran {} in {}ms", name, duration);
		return duration;
	}
	
}