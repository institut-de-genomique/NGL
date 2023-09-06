package fr.cea.ig.authorization.authorizators;

import javax.inject.Singleton;

import fr.cea.ig.authorization.IAuthorizator;

/**
 * Never authorizes.
 * 
 * @author vrd
 *
 */
@Singleton
public class NeverAuthorizator implements IAuthorizator {

	// Implicit DI no arg constructor
	
//	/**
//	 * Never authorized.
//	 */
//	@Override
//	public boolean authorize(String login, String[] perms) {
//		return false;
//	}

}
