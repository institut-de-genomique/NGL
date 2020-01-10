package fr.cea.ig.authorization.authorizators;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import fr.cea.ig.authorization.IAuthorizator;
import fr.cea.ig.play.IGConfig;
import play.api.inject.Injector;

/**
 * Delegated authorization implementation that is configured 
 * through the application configuration.
 * 
 * @author vrd
 *
 */
@Singleton
public class ConfiguredAuthorizator implements IAuthorizator {

	/**
	 * Logger.
	 */
	private static final play.Logger.ALogger logger = play.Logger.of(ConfiguredAuthorizator.class);
	
	/**
	 * Configuration root path.
	 */
	public static final String CONF_ROOT_PATH = "authorization.configured";
	
	/**
	 * Key for the authorization mode that is a short name for well known classes.
	 */
	public static final String CONF_MODE_PATH = CONF_ROOT_PATH + ".mode";
	
	/**
	 * Key for an explicit authorizator class name. 
	 */
	public static final String CONF_CLASS_PATH = CONF_ROOT_PATH + ".class";
	
	/**
	 * Map of mode names to implementations classes.
	 */
	public static final Map<String,Class<?>> modeMap;
	
	/**
	 * Initialization of the mode name the implementation.
	 */
	static {
		modeMap = new HashMap<>();
		modeMap.put("always",   AlwaysAuthorizator.class);
		modeMap.put("never",    NeverAuthorizator.class);
		modeMap.put("fixed",    FixedAuthorizator.class);
		modeMap.put("user_dao", UserDAOAuthorizator.class);
	}
	
	/**
	 * Delegate implementation.
	 */
	private final IAuthorizator delegate;
	
	/**
	 * Construct the delegate implementation using the configuration.
	 * @param config   configuration
	 * @param injector injector used to create the delegate instance
	 */
	@Inject
	public ConfiguredAuthorizator(IGConfig config, Injector injector) {
		Class<?> delegateClass = null;
		String mode = config.getString(CONF_MODE_PATH, null);
		if (mode != null) {
			delegateClass = modeMap.get(mode);
			if (delegateClass == null)
				logger.error("unkown mode {}",mode);
		}
		String delegateClassName = config.getString(CONF_CLASS_PATH, null);
		if (delegateClassName != null) {
			if (delegateClass != null)
				logger.warn("definition of {} and {}, will keep {}",CONF_MODE_PATH,CONF_CLASS_PATH,CONF_CLASS_PATH);
			try {
				delegateClass = Class.forName(delegateClassName);
			} catch (ClassNotFoundException e) {
				
			}
		}
		if (delegateClass == null) {
			logger.error("no authorization mode configured at {} or {}",CONF_MODE_PATH,CONF_CLASS_PATH);
			delegate = new NeverAuthorizator(); 
		} else {
			Object delegateAuthorizator = injector.instanceOf(delegateClass);
			if (delegateAuthorizator instanceof IAuthorizator) {
				logger.debug("using delegate {}",delegateAuthorizator);
				delegate = (IAuthorizator)delegateAuthorizator;
			} else { 
				logger.error("{} does not implement {}",delegateClass.getName(),IAuthorizator.class.getName());
				delegate = new NeverAuthorizator(); 
			}
		}
	}

	/**
	 * Uses the delegate implementation.
	 */
	@Override
	public boolean authorize(String login, String... perms) {
		return delegate.authorize(login, perms);
	}

	@Override
	public Set<String> getPermissions(String login) {
		return delegate.getPermissions(login); 
	}
	
}
