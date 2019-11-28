package fr.cea.ig.ngl.dao.permissions;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import fr.cea.ig.authorization.authorizators.UserDAOAuthorizator;
import fr.cea.ig.ngl.dao.api.APIException;
import models.administration.authorisation.Permission;
import models.administration.authorisation.description.dao.UserDAO;
import models.utils.dao.DAOException;
import play.api.modules.spring.Spring;

@Singleton
public class PermissionAPI {

	private static final play.Logger.ALogger logger = play.Logger.of(PermissionAPI.class);
	
	private final PermissionDAO dao;
	
	@Inject
	public PermissionAPI(PermissionDAO dao) {
		this.dao = dao;		
	}
	
	public List<Permission> byUserLogin(String login) throws DAOException, APIException {
		return dao.byUserLogin(login);
	}
	
	 /**
     * Does the user exist ?
     * @param login user login
     * @return      true is the user exists
     */
    public boolean isDeclaredUser(String login) {
        return getUserDAO().isDeclaredUser(login);
    }

    private UserDAO getUserDAO() {
//        return (UserDAO) Spring.getBeanOfType(UserDAO.class);
        return Spring.getBeanOfType(UserDAO.class);
    }

    /**
     * Is the user active ?
     * @param login user login
     * @return      true if the user is active 
     */
    public boolean isActiveUser(String login) {
        return getUserDAO().isUserActive(login);		
    }

    /**
     * Can the user access the given application by name .
     * @param login user login
     * @param app   application key
     * @return      tue if the user can access the application
     */
    public boolean canAccessApplication(String login, String app) {
        return getUserDAO().canAccessApplication(login, app);        
    }

    /**
     * Does the user have a given permission ?
     * @param login      user login
     * @param permission permission to check
     * @return           true if the user has the requested permission
     */
    public boolean hasPermission(String login, String permission) {
//        return PermissionHelper.checkPermission(login, permission);
    	try {
    		for (Permission p : byUserLogin(login))
    			if (p.code.equals(permission))
    				return true;
    		return false;
    	} catch (APIException e) {
    		logger.warn("error while reading permissions", e);
    		return false;
    	}
    }

    /**
     * Declare user in database.
     * @param login user login
     * @param role  user role
     */
    public void declareUser(String login, String role) {
        synchronized (UserDAOAuthorizator.class) {
            if (!isDeclaredUser(login))
                getUserDAO().declareUser(login, role);
        }
    }

    /**
     * Grant application access to a user.
     * @param login       user login
     * @param application application key
     */
    public void grantApplicationAccess(String login, String application) {
        getUserDAO().grantApplicationAccess(login, application);
    }

}
