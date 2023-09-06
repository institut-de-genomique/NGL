package models.administration.authorisation;

import java.util.List;
import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fr.cea.ig.ngl.utils.SpringSupplier;
import models.administration.authorisation.description.dao.RoleDAO;
import models.utils.Model;

/**
 * 
 * @author michieli
 *
 */
//public class Role extends Model<Role> {
public class Role extends Model {
	
//	@JsonIgnore
//	public static RoleFinder find = new RoleFinder();
	@JsonIgnore
//	public static RoleDAO find = Spring.getBeanOfType(RoleDAO.class);
	public static Supplier<RoleDAO> find = new SpringSupplier<>(RoleDAO.class);
	
	public String label;
	public List<Permission> permissions;
	
	// Missing contructor with DAO class name as super arg.
	
//	@Override
//	protected Class<? extends AbstractDAO<Role>> daoClass() {
//		return RoleDAO.class;
//	}

//	// Doc generation produces an error with the parent unqualified name.
//	// public static class RoleFinder extends Finder<Role> {
//	public static class RoleFinder extends Model.Finder<Role,RoleDAO> {
//		
////		public RoleFinder() {
////			super(RoleDAO.class.getName());
////		}
//		public RoleFinder() { super(RoleDAO.class); }
//		
//		@Override
//		public List<Role> findAll() throws DAOException {
////			return ((RoleDAO)getInstance()).findAll();
//			return getInstance().findAll();
//		}
//		
//		public List<Role> findByUserLogin(String aLogin) throws DAOException {
////			return ((RoleDAO)getInstance()).findByUserLogin(aLogin);
//			return getInstance().findByUserLogin(aLogin);
//		}
//		
//	}

}
