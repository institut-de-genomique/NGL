package models.administration.authorisation;

import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fr.cea.ig.ngl.utils.SpringSupplier;
import models.administration.authorisation.description.dao.PermissionDAO;
import models.utils.Model;

// TODO: comment

/**
 * 
 * @author michieli
 *
 */
//public class Permission extends Model<Permission> {
public class Permission extends Model {

//	@JsonIgnore
//	public static final PermissionFinder find = new PermissionFinder();
//	@JsonIgnore
//	public static final PermissionDAO find = Spring.getBeanOfType(PermissionDAO.class);
	@JsonIgnore
	public static final Supplier<PermissionDAO> find = new SpringSupplier<>(PermissionDAO.class);
	
	public String label;
//	public String code;

//	@Override
//	protected Class<? extends AbstractDAO<Permission>> daoClass() {
//		return PermissionDAO.class;
//	}
	
//	// Doc generation produces an error with the parent unqualified name.
//	// public static class PermissionFinder extends Finder<Permission> {
//	public static class PermissionFinder extends Finder<Permission,PermissionDAO> {
//
////		public PermissionFinder() {
////			super(PermissionDAO.class.getName());
////		}
//		public PermissionFinder() { super(PermissionDAO.class); }
//		
//		public List<Permission> findByUserLogin(String aLogin) throws DAOException {
////			return ((PermissionDAO)getInstance()).findByUserLogin(aLogin);
//			return getInstance().findByUserLogin(aLogin);
//		}
//		
//	}

}
