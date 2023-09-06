package models.administration.authorisation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fr.cea.ig.ngl.utils.SpringSupplier;
import models.administration.authorisation.description.dao.UserDAO;
import models.utils.Model;
import ngl.refactoring.MiniDAO;
//import play.Logger;
import play.data.validation.ValidationError;


//public class User extends Model<User> {
public class User extends Model {

//	@JsonIgnore
//	public static final UserFinder find = new UserFinder();
	@JsonIgnore
//	public static final UserDAO find = Spring.getBeanOfType(UserDAO.class);
	public static final Supplier<UserDAO> find = new SpringSupplier<>(UserDAO.class);
	public static final Supplier<MiniDAO<User>> miniFind = MiniDAO.createSupplier(find);

	public String login;
	public String firstname;
	public String lastname;
	public String email;

	/**
	 * 0 not technical, 1 technical
	 */
	// Looks like a boolean
	public int technicaluser;

	/**
	 * Only for technical users
	 */
	public String password;
	public String confirmpassword;

	// Attribute used to Lists the roles labels
	public List<Long> roleIds;
	public List<Team> teams;
	public List<Application> applications;
	public Boolean active;

//	@Override
//	public User self() { return this; }

//	public User() {
//		super(UserDAO.class.getName());
//	}
//
//	@Override
//	protected Class<? extends AbstractDAO<User>> daoClass() {
//		return UserDAO.class;
//	}

	@JsonIgnore
	public  Map<String,List<ValidationError>> validate() {
		if (!this.password.equals(this.confirmpassword)) {
			final Map<String,List<ValidationError>> map = new HashMap<>();
			final List<ValidationError> listeValidation = new ArrayList<>();
			listeValidation.add(new ValidationError("password","Password and confirmation are not the same.",null));
			map.put("password",listeValidation);
			map.put("confirmpassword",listeValidation);
			return map;
		}
		return null;
	}

	/**
	 *
	 * @author michieli
	 *
	 */

//	// Looks like a UserFinder is in fact a DAO<User>
//	public static class UserFinder extends Model.Finder<User,UserDAO> {
//
////		public UserFinder() {
////			super(UserDAO.class.getName());
////		}
//		public UserFinder() { super(UserDAO.class); }
//
//		@Override
//		public List<User> findAll() throws DAOException {
////			return ((UserDAO)getInstance()).findAll();
//			return getInstance().findAll();
//		}
//
//		public User findByLogin(String login) throws DAOException {
////			return ((UserDAO)getInstance()).findByLogin(login);
//			return getInstance().findByLogin(login);
//		}
//
//		public List<User> findByLikeLogin(String aLike) throws DAOException {
////			return ((UserDAO)getInstance()).findByLikeLogin(aLike);
//			return getInstance().findByLikeLogin(aLike);
//		}
//
//	}

}
