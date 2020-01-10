package fr.cea.ig.authorization.authorizators;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import fr.cea.ig.authorization.IAuthorizator;
import fr.cea.ig.play.IGConfig;

/**
 * Uses a fixed set of permissions defined in the configuration file.
 * 
 * @author vrd
 *
 */
@Singleton
public class FixedAuthorizator implements IAuthorizator {

	/**
	 * Logger.
	 */
	private static final play.Logger.ALogger logger = play.Logger.of(FixedAuthorizator.class);
	
	/**
	 * Path to the permission array in the configuration.
	 */
	public static final String CONF_FIXED_PATH = "authorization.fixed";
	
	/**
	 * Set of permissions.
	 */
	private final Set<String> authorizations;
	
	/**
	 * DI constructor.
	 * @param config configuration to use
	 */
	@Inject
	public FixedAuthorizator(IGConfig config) {
		List<String> perms = config.getStringList(CONF_FIXED_PATH);
		authorizations = new HashSet<>();
		if (perms == null) {
			logger.error("no/bad premissions configured at {}",CONF_FIXED_PATH);
		} else {
			for (String perm : perms) {
				logger.debug("adding perm '{}'",perm);
				authorizations.add(perm);
			}
		}
	}
	
	/**
	 * Authorizes if all the requested permissions are in the permission set.
	 */
	@Override
	public boolean authorize(String login, String... perms) {
		boolean authorized = true; 
		for (String perm : perms)
			authorized = authorized && authorizations.contains(perm);
		return authorized;
	}

	@Override
	public Set<String> getPermissions(String login) {
		return authorizations;
	}
	
}
