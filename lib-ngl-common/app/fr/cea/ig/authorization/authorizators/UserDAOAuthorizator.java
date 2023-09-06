package fr.cea.ig.authorization.authorizators;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.HashSet;
import java.util.Set;

//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.CompletionStage;
//import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.Provider;

import fr.cea.ig.authorization.IAuthorizator;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.permissions.PermissionAPI;
import fr.cea.ig.play.IGConfig;
import models.administration.authorisation.Permission;
import models.utils.dao.DAOException;

/**
 * IAuhtorization implementation on top of UserDAO.
 * <p>
 * Application name that is checked against the database is set using:
 * <br>
 * <code>
 * authorization.user_dao.application = "ngl-sq"
 * </code>
 * <p>
 * Automatic user creation is specified using a boolean flag, it defaults
 * to false:
 * <br>
 * <code>
 * authorization.user_dao.user_creation
 * </code>
 * <p>
 * Created user role name is not needed if the user_creation is set to false and
 * is set using:
 * <br>
 * <code>
 * authorization.user_dao.role_at_creation = "reader"
 * </code>
 * 
 * @author vrd
 *
 */
@Singleton
public class UserDAOAuthorizator implements IAuthorizator {

    /**
     * Logger.
     */
    private static final play.Logger.ALogger logger = play.Logger.of(UserDAOAuthorizator.class);

    /**
     * Root configuration path.
     */
    public static final String CONF_ROOT_PATH = "authorization.user_dao";

    /**
     * Path in the configuration for the application name as defined in the UserDAO.
     */
    public static final String CONF_APPLICATION_NAME_PATH = CONF_ROOT_PATH + ".application";

    /**
     * Path in the configuration for the user creation flag.
     */
    public static final String CONF_USER_CREATION_PATH    = CONF_ROOT_PATH + ".user_creation";

    /**
     * Path in the configuration for the user role at creation.
     */
    public static final String CONF_USER_ROLE_PATH        = CONF_ROOT_PATH + ".role_at_creation";

    /**
     * Should the user be created if it does not exist ?
     */
    private final boolean userCreation;

    /**
     * Role the user is granted when automatically created. 
     */
    private final String roleAtCreation;

    /**
     * Application name in the UserDAO.
     */
    private final String applicationName;

    /**
     * User DAO.
     */
    //private final UserDAO userDAO;

    /**
     * Permission API.
     */
    private final Provider<PermissionAPI> permissionAPI;

    // At this point the system may not support application
    // checks. Implementation in the UserDOA is incorrect.
    private static final boolean enableApplicationCheck = false; 

    /**
     * DI constructor.
     * @param config        configuration to use
     * @param permissionAPI permission API
     */
    @Inject
    public UserDAOAuthorizator(IGConfig config, Provider<PermissionAPI> permissionAPI) {
        userCreation = config.getBoolean(CONF_USER_CREATION_PATH, false);
        if (userCreation)
            roleAtCreation = config.getString(CONF_USER_ROLE_PATH,null);
        else
            roleAtCreation = null;
        applicationName = config.getString(CONF_APPLICATION_NAME_PATH,null);
        this.permissionAPI = permissionAPI;
    }

    /**
     * Authorize user based on the UserDAO information, may create 
     * the user if configured to do so.
     */
    @Override
    public boolean authorize(String login, String... perms) {
        if (applicationName == null) {
            logger.error("application name is not defined");
            return false;
        }
        if (isBlank(login)) {
            logger.warn("blank login {}",login);
            return false;
        }
        PermissionAPI permissionAPI = this.permissionAPI.get();
        if (!permissionAPI.isDeclaredUser(login)) {
            if (userCreation) {
            	permissionAPI.declareUser(login,roleAtCreation);
            } else {
                return false;
            }
        }
        if (!permissionAPI.isActiveUser(login))
            return false;
        if (!canAccessApplication(applicationName,login)) {
            if (userCreation) {
            	grantApplicationAccess(login,applicationName);
            } else {
                return false;
            }
        }
        /*boolean authorized = true;
		for (String perm : perms) {
			if (perm == null)
				throw new RuntimeException("badly configured null permission");
			authorized = authorized && hasPermission(login,perm); 
		}
		return authorized;*/
        for (String perm : perms) {
            if (perm == null)
                throw new RuntimeException("badly configured null permission");
            if (!permissionAPI.hasPermission(login,perm))
                return false; 
        }
        return true;
    }

    @Override
    public Set<String> getPermissions(String login) {
        Set<String> permissions = new HashSet<>();
        PermissionAPI permissionAPI = this.permissionAPI.get();
        if (isBlank(login))                               return permissions;
        if (!permissionAPI.isDeclaredUser(login))         return permissions;
        if (!permissionAPI.isActiveUser(login))           return permissions;
        // never used
        if (!canAccessApplication(applicationName,login)) return permissions;
        try {
            for (Permission p : permissionAPI.byUserLogin(login)) 
                permissions.add(p.code);
        } catch (DAOException | APIException e) {
            logger.error("permission access failed for " + login,e);
        } 
        return permissions;
    }
    
    /**
     * Can the user access the given application by name .
     * @param login user login
     * @param app   application key
     * @return      tue if the user can access the application
     */
    public boolean canAccessApplication(String login, String app) {
        if (enableApplicationCheck)
            return permissionAPI.get().canAccessApplication(login, app);
        return true;
    }
    /**
     * Grant application access to a user.
     * @param login       user login
     * @param application application key
     */
    public void grantApplicationAccess(String login, String application) {
        if (enableApplicationCheck)
        	permissionAPI.get().grantApplicationAccess(login, application);
    }
   
}

