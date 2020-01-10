package controllers.authorisation;

import fr.cea.ig.authorization.IAuthorizator;
import fr.cea.ig.play.IGGlobals;
import models.utils.dao.DAOException;

/**
 * Permission and team manager.
 * 
 * @author ydeshayes
 * @author michieli
 */
public class PermissionHelper {
	
	public static boolean checkPermission(String username, String codePermission) throws DAOException {
		if (username == null) 
			return false;
		IAuthorizator authorizator = IGGlobals.instanceOf(IAuthorizator.class);
		return authorizator.authorize(username, codePermission);
	}
		
//	/*
//	 * 
//	 * @param ses the user session
//	 * @param codePermission the code of the permission that you want to verify
//	 * @return true if the user can access to the resources
//	 * @throws DAOException 
//	 */
//	public static boolean checkPermission(String username, String codePermission) throws DAOException {
//		if (username != null) {
////			List<Permission> permissions = Permission.find.get().findByUserLogin(username);
////			for (Permission p:permissions) {
//			for (Permission p : Permission.find.get().findByUserLogin(username)) {
//				if (codePermission.equals(p.code))
//					return true;
//			}
//		}
//		return false;
//	}

//	public static User getUser(long id) throws DAOException {
//		return User.find.get().findById(id);
//	}
//	
//	public static boolean isTechnical(int id) throws DAOException {
//		return getUser(id).technicaluser == 1;
//	}
	
//	/*
//	 * 
//	 * @param ses the user session
//	 * @param varteam the name of the team you want to verify
//	 * @return true if the user is in the team
//	 * @throws DAOException 
//	 */
//	public static boolean checkTeam(String username, String varteam) throws DAOException {
//		User user = User.find.get().findByLogin(username);  
//		if (user != null) {
//			for (Team team:user.teams) {
//				if (team.nom.equals(varteam))
//					return true;
//			}
//		}
//		return false;
//	}
	
//	/*
//	 * 
//	 * @param ses the user session
//	 * @param teams the name of the teams you want to verify
//	 * @return  if the user is in one of these team
//	 * @throws DAOException 
//	 */
//	public static boolean checkTeam(String username, List<String> teams) throws DAOException {
//		//By default -> [""]
//		if(teams.size() < 2 && teams.get(0).equalsIgnoreCase(""))
//			return true;
//		
//		User user = User.find.get().findByLogin(username);  
//		if(user!=null) {
//			for(Team team:user.teams) {
//				for(String varteam:teams){
//					if(team.nom.equals(varteam))
//						return true;
//				}
//			}
//		}
//		return false;
//	}

//	/*
//	 * Method checkRole()
//	 */
//	public static boolean checkRole(String username, String labelRole) throws DAOException {
//		List<Role> roles = Role.find.get().findByUserLogin(username);
//		for (Role r:roles)
//			if (labelRole.equals(r.label))
//				return true;
//		return false;
//	}

	/*
	 * 
	 * @param ses the user session
	 * @param codePermission the code of the permissions that you want to verify
	 * @param allPermission if user need to have all the permission(true) or just one of these(false)
	 * @return true if the user can access to the resources
	 * @throws DAOException 
	 */
//	public static boolean checkPermission(Session ses,  List<String> codePermission, boolean allPermission) throws DAOException {
//		User user = User.find.findByLogin(ses.get(COOKIE_SESSION));  
//		Logger.debug("Le User lastname : " + user.lastname);
//		Logger.debug("check perm "+codePermission+" / "+user);
//		if(user!=null) {
//			if(!allPermission){
//				for(Role role:user.roles) {
//					Logger.debug("compare with role "+role.label);
//					for(models.administration.authorisation.Permission perm:role.permissions) {
//						Logger.debug("compare with perm "+perm.label);
//						for(String permissionAsk:codePermission){
//							if(perm.code.equals(permissionAsk))
//								return true;
//						}
//					}
//				}
//			}else{
//				int i=0;
//				for(Role role:user.roles) {
//					for(models.administration.authorisation.Permission perm:role.permissions) {
//						for(String permissionAsk:codePermission){	
//							if(perm.code.equals(permissionAsk))
//								i++;
//						}
//					}
//				
//			}
//			return i == codePermission.size();	
//		}
//		}
//		return false;
//	}

	/*
	public static boolean existPerm(int idPerm,String id) {
		models.administration.authorisation.Role role = models.administration.authorisation.Role.find.byId(idPerm);
		models.administration.authorisation.Permission permission = models.administration.authorisation.Permission.find.byId(Integer.parseInt(id));
		
		return role.permissions.contains(permission);
	}
	
	public static boolean existRole(int idUser,String id) {
		models.administration.authorisation.User user = models.administration.authorisation.User.find.byId(idUser);
		Role role = models.administration.authorisation.Role.find.byId(Integer.parseInt(id));
		
		return user.roles.contains(role);
	}
	
	public static boolean existSingleRole(int id) {
		Query<models.administration.authorisation.Role> role = models.administration.authorisation.Role.find.where("id="+id);
		role.findRowCount();
		return role.findRowCount()!=0;
	}
	
	public static boolean existSinglePerm(int id) {
		Query<models.administration.authorisation.Permission> perm = models.administration.authorisation.Permission.find.where("id="+id);
		perm.findRowCount();
		return perm.findRowCount()!=0;
	}
	
	public static boolean existUser(int idUser) {
		Query<models.administration.authorisation.User> user = models.administration.authorisation.User.find.where("id="+idUser);
		user.findRowCount();
		return user.findRowCount()!=0;
	}
	
	public static List<models.administration.authorisation.Permission> getAllPermission() {
		return models.administration.authorisation.Permission.find.all();
	}
	
	public static List<models.administration.authorisation.User> getAllUser() {
		return models.administration.authorisation.User.find.all();
	}
	
	public static List<models.administration.authorisation.Team> getAllTeam() {
		return models.administration.authorisation.Team.find.all();
	}
	
	public static List<models.administration.authorisation.Role> getAllRole() {
		return models.administration.authorisation.Role.find.all();
	}
	
	public static List<Application> getAllApplication() {
		return models.administration.authorisation.Application.find.all();
	}
	
	public static models.administration.authorisation.Role getRole(int id) {
		return models.administration.authorisation.Role.find.byId(id);
	}
	
	
	public static Application getApplication(int id) {
		return models.administration.authorisation.Application.find.byId(id);
	}
	
	public static models.administration.authorisation.Team getTeam(int id) {
		return models.administration.authorisation.Team.find.byId(id);
	}
	
	
	public static Permission getpermission(int id) {
		return models.administration.authorisation.Permission.find.byId(id);
	}
	
	public static Permission getpermission(String code) {
		return models.administration.authorisation.Permission.find.where("code LIKE "+code).findUnique();
	}
	
	public static Application getapplication(String code) {
		return models.administration.authorisation.Application.find.where("code LIKE "+code).findUnique();
	}
	
	
	
	
	public static Map<String, String> getMapRole() {
		  Map<String,String> map = new HashMap<String,String>();
		List<Role> liste = getAllRole();
		for(models.administration.authorisation.Role role: liste)
				map.put(String.valueOf(role.id), role.label);
		
		return map;
	}
	
	public static Map<String, String> getMapPerm() {
		Map<String,String> map = new HashMap<String,String>();
		List<models.administration.authorisation.Permission> liste = getAllPermission();
		for(models.administration.authorisation.Permission perm: liste)
				map.put(String.valueOf(perm.id), perm.label);
		
		return map;
   }
	
	public static Map<String,String> getMapTeam() {
		Map<String,String> map = new HashMap<String,String>();
		List<models.administration.authorisation.Team> liste = getAllTeam();
		for(models.administration.authorisation.Team team: liste)
				map.put(String.valueOf(team.id), team.nom);
		
		return map;
	}
	
	public static Map<String,String> getMapApplication() {
		Map<String,String> map = new HashMap<String,String>();
		List<models.administration.authorisation.Application> liste = getAllApplication();
		for(models.administration.authorisation.Application app: liste)
				map.put(String.valueOf(app.id), app.label);
		
		return map;
	}
	*/	
	
}