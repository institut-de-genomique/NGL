package fr.cea.ig.authorization.authorizators;

import java.util.HashSet;
import java.util.Set;

import fr.cea.ig.authorization.IAuthorizator;
import fr.cea.ig.authorization.Permission;

/**
 * Always authorize.
 * 
 * @author vrd
 *
 */
public class AlwaysAuthorizator implements IAuthorizator {
	
	/**
	 * Logger.
	 */
	private static final play.Logger.ALogger logger = play.Logger.of(AlwaysAuthorizator.class);

	private static Set<String> authorizations = null;
	// Implicit no arg DI constructor
	
	/**
	 * Always authorized.
	 */
	@Override
	public boolean authorize(String login, String... perms) {
		logger.debug("authorizing");
		return true;
	}

	/**
	 * This depends on the permissions being defined in the {@link fr.cea.ig.authorization.Permission}
	 * enumeration.
	 */
	@Override
	public Set<String> getPermissions(String login) {
		if (authorizations == null) {
			authorizations = new HashSet<>();
			for (Permission p : Permission.values())
				authorizations.add(p.getAlias());
		}
		return authorizations;
	}
	
}
