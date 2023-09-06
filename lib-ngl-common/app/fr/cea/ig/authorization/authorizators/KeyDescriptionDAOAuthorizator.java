package fr.cea.ig.authorization.authorizators;

import java.util.Set;
import java.util.stream.Stream;

import javax.inject.Inject;

import com.google.inject.Provider;

import fr.cea.ig.authorization.IKeyDescriptionAuthorizator;
import fr.cea.ig.ngl.dao.keyDescriptions.KeyDescriptionAPI;
import fr.cea.ig.play.IGConfig;

public class KeyDescriptionDAOAuthorizator implements IKeyDescriptionAuthorizator {
	
	/**
     * Root configuration path.
     */
    public static final String CONF_ROOT_PATH = "authorization.user_dao";
	
	/**
     * Path in the configuration for the application name as defined in the UserDAO.
     */
    public static final String CONF_APPLICATION_NAME_PATH = CONF_ROOT_PATH + ".application";
	
	/**
     * Application name in the KeyDAO.
     */
    private final String applicationName;
    
    /**
     * Key API.
     */
    private final Provider<KeyDescriptionAPI> keyDescriptionAPI;
    
    /**
     * DI constructor.
     * 
     * @param config	configuration to use
     * @param keyAPI	key API
     */
    @Inject
    public KeyDescriptionDAOAuthorizator(IGConfig config, Provider<KeyDescriptionAPI> keyDescriptionAPI) {
        applicationName = config.getString(CONF_APPLICATION_NAME_PATH,null);
        this.keyDescriptionAPI = keyDescriptionAPI;
    }
	
	/**
	 * {@inheritDoc}
	 */
	public boolean authorize(String login, String... perms) {
		return Stream.of(perms).allMatch(this.getPermissions(login)::contains);
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<String> getPermissions(String login) {
		return keyDescriptionAPI
				.get()
				.getByUserName(login)
				.user
				.getPermissionsAliases(applicationName);
	}

}
