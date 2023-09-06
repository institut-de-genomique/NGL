package fr.cea.ig.authorization;

import java.util.HashSet;
import java.util.Set;

///**
// * Simple authorization check that validates that a given login 
// * has a set of permissions.
// *  
// * @author vrd
// *
// */
//public interface IAuthorizator {
//
//	/**
//	 * Has the login enough permissions to be authorized ?
//	 * @param  login login to check
//	 * @param  perms required set of permissions
//	 * @return true if the login has enough permissions
//	 */
//	boolean authorize(String login, String[] perms);
//
//}

/**
* Simple authorization check that validates that a given login 
* has a set of permissions.
* <p>
* Default implementation checks the requested permissions against permissions
* from {@link #getPermissions(String)}.
*   
* @author vrd
*
*/
//Actual implementations have to override one of the two methods. 
public interface IAuthorizator {

	/**
	 * Has the login enough permissions to be authorized ?
	 * <p>
	 * Default implementation relies on the getPermission implementation. 
	 * @param  login login to check
	 * @param  perms required set of permissions
	 * @return true if the login has enough permissions
	 */
	default boolean authorize(String login, String... perms) {
		Set<String> lp = getPermissions(login);
		for (String p : perms)
			if (!lp.contains(p))
				return false;
		return true;
	}

	/**
	 * Set of permissions for a given login. Default implementation
	 * returns an empty set.
	 * @param login login to get permissions of
	 * @return      set of permissions
	 */
	default Set<String> getPermissions(String login) {
		return new HashSet<>();
	}
	
}
