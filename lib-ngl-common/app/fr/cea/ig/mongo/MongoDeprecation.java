package fr.cea.ig.mongo;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Those are methods that have been deprecated. The implementation
 * should be faithful to the original implementation.
 * <p> 
 * This is supposed to be a temporary fix.
 *  
 * @author vrd
 *
 */
// TODO: find a way to remove this class or keep it if no way around is found
public class MongoDeprecation {

	/**
	 * JDBC template replacement for same name method.
	 * @param t    JDBC template
	 * @param sql  SQL query
	 * @param args query arguments
	 * @return     0 if nothing is found, the found value otherwise
	 */
	public static long queryForLong(JdbcTemplate t, String sql, Object... args) {
		Long l = t.queryForObject(sql, Long.class, args);
		if (l == null)
			return 0;
		return l.longValue();
	}
		
	/**
	 * JDBC template replacement for same name method.
	 * @param t    JDBC template
	 * @param sql  SQL query
	 * @param args query arguments
	 * @return     0 if nothing is found, the found value otherwise
	 */
	public static int queryForInt(JdbcTemplate t, String sql, Object... args) {
		Integer l = t.queryForObject(sql, Integer.class, args);
		if (l == null)
			return 0;
		return l.intValue();
	}

}
