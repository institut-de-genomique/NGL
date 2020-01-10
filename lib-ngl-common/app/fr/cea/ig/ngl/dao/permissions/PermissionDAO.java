package fr.cea.ig.ngl.dao.permissions;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import models.administration.authorisation.Permission;
import models.utils.dao.DAOException;

@Singleton
public class PermissionDAO {

	// Not needed, placehodler
	@Inject
	public PermissionDAO() {
	}
	
	public List<Permission> byUserLogin(String login) throws DAOException {
		return Permission.find.get().findByUserLogin(login);
	}
	
}
