package models.administration.authorisation.description.dao;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.stereotype.Repository;

import models.administration.authorisation.Role;
import models.administration.authorisation.User;
import models.utils.dao.AbstractDAOMapping;
import models.utils.dao.DAOException;
import play.Logger;

@Repository
public class UserDAO extends AbstractDAOMapping<User> { 

	protected UserDAO() {
		super("user", User.class, UserMappingQuery.class,
				"SELECT t.id, t.login, t.firstname, t.lastname, t.email, t.technicaluser, t.active " +
				"FROM user as t ", true);
	}
	/*
	 * findAll() remade
	 */
	public List<User> findAll() throws DAOException
	{
		String sql = sqlCommon + "ORDER by t.login";
		Logger.debug(sql);
		return initializeMapping(sql).execute();
	}
	
	public boolean isExistUserWithLoginAndPassword(String login, String password){
		String sql = "SELECT login "+
				"FROM user WHERE login = :login AND password = :password";
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("login", login);
		parameters.put("password", password);
		
		BeanPropertyRowMapper<User> mapper = new BeanPropertyRowMapper<User>(User.class);
		List<User> us =  this.jdbcTemplate.query(sql, mapper, parameters);
		
		return this.jdbcTemplate.query(sql, mapper).size()>0;
	}

	public String  getUserPassword(String login) throws DAOException{
		String sql = "SELECT login , password "+
				"FROM user WHERE login = :login";
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("login", login);
		BeanPropertyRowMapper<User> mapper = new BeanPropertyRowMapper<User>(User.class);
		Logger.debug("UserDAO - getUserPassword : requête SQL : " + sql + "  for " + login);
		List<User> users =  this.jdbcTemplate.query(sql, mapper, parameters);
		
		if(users != null && users.size() == 1){
			return users.get(0).password.toString();
		}else{
			return null;
		}
		
		//return null ;
	}	
	public boolean isUserActive(String login) throws DAOException{
		String sql = "SELECT active "+
				"FROM user WHERE login = :login";
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("login", login);
		BeanPropertyRowMapper<User> mapper = new BeanPropertyRowMapper<User>(User.class);
		Logger.debug("UserDAO - isUserActive : requête SQL : " + sql + "  for " + login);
		List<User> users =  this.jdbcTemplate.query(sql, mapper, parameters);
		
		if(users != null && users.size() == 1){
			return users.get(0).active;
		}
		return false;
	}
	public boolean isUserAccessApplication(String login, String application){
		if(!login.equals("") && getUserId(login) != 0){
			applicationAccess(login, application);
			 
			String sql = "SELECT a.label "+
					"FROM user u, application a, user_application ua WHERE u.login= :login AND a.code= :application AND ua.user_id=u.id and ua.application_id=a.id";
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("login", login);
			parameters.put("application", application);
			BeanPropertyRowMapper<User> mapper = new BeanPropertyRowMapper<User>(User.class);
			return this.jdbcTemplate.query(sql, mapper, parameters).size()>0;
		}else if(!login.equals("")){
			createUser(login);
			applicationAccess(login, application);
			return true;
		}
		
		return false;
	}
	
	public void setDefaultRole(String login, String role){
		String sql = "SELECT u.id FROM user u, user_role ur WHERE u.login=? and u.id=ur.user_id";
		
		BeanPropertyRowMapper<User> mapper = new BeanPropertyRowMapper<User>(User.class);
		if(this.jdbcTemplate.query(sql, mapper,login).size() == 0){
			sql = "INSERT INTO user_role (user_id,role_id) VALUE (?,?)";
			this.jdbcTemplate.update(sql, getUserId(login), getRoleId(role));
		}
	}
	
	private long getRoleId(String label){
		String sql = "SELECT r.id FROM role r WHERE r.label=?";
		BeanPropertyRowMapper<Role> mapper = new BeanPropertyRowMapper<Role>(Role.class);
		return this.jdbcTemplate.query(sql, mapper,label).get(0).id;
	}
	
	private long getApplicationId(String code){
		String sql = "SELECT a.id FROM application a WHERE a.code=?";
		BeanPropertyRowMapper<Role> mapper = new BeanPropertyRowMapper<Role>(Role.class);
		return this.jdbcTemplate.query(sql, mapper,code).get(0).id;
	}
	
	private long getUserId(String login){
		String sql = "SELECT u.id as id FROM user u WHERE u.login=?";
		BeanPropertyRowMapper<User> mapper = new BeanPropertyRowMapper<User>(User.class);
		List<User> users = this.jdbcTemplate.query(sql, mapper,login);
		if(users != null && users.size() > 0){
			return users.get(0).id;
		}else{
			return 0;
		}
	}
	
	private void applicationAccess(String login,String application){
		String sql = "SELECT u.id FROM user u, user_application ua, application a WHERE u.login=? and u.id=ua.user_id and a.code = ? and a.id=ua.application_id";
		
		BeanPropertyRowMapper<User> mapper = new BeanPropertyRowMapper<User>(User.class);
		if(this.jdbcTemplate.query(sql, mapper,login, application).size() == 0){
			sql = "INSERT INTO user_application (user_id,application_id) VALUE (?,?)";
			this.jdbcTemplate.update(sql, getUserId(login), getApplicationId(application));
		}
	}
	
	private void createUser(String login){
		String sql = "INSERT INTO user (login) VALUE (?)";
		this.jdbcTemplate.update(sql, login);
	}
	
	
	/*
	 * findByLogin()
	 */
	public User findByLogin(String aLogin) throws DAOException{
		if(null == aLogin){
			throw new DAOException("login is mandatory");
		}
		String sql = sqlCommon + "WHERE t.login=?";
		UserMappingQuery userMappingQuery = new UserMappingQuery(dataSource, sql, new SqlParameter("login", Types.VARCHAR));
		return userMappingQuery.findObject(aLogin);
	}
	
	
	/*
	 * findByLikeLogin()
	 */
	public List<User> findByLikeLogin(String aLike) throws DAOException{
		if(null == aLike){
			throw new DAOException("login is mandatory");
		}
		String sql = sqlCommon + "WHERE t.login LIKE ?";
		return initializeMapping(sql, new SqlParameter("like", Types.VARCHAR)).execute(aLike);
	}

	
	/*
	 * insertUserRoles
	 */
	public void insertUserRoles(Long userId, List<Long> roleIds, boolean deleteBefore) throws DAOException{		
		if (deleteBefore)
			removeUserRoles(userId);
		
		// Add Roles to User
		if(roleIds!=null && roleIds.size()>0){
			String sql = "INSERT INTO user_role (user_id,role_id) VALUES (?,?)";
			for(Long l:roleIds){
				if(l == null)
					throw new DAOException();
				jdbcTemplate.update(sql, userId, l);
				Logger.debug("Insertion de donnees : \n\tUser id : " + userId + "\n\tRoleId : " + l);
			}
		}
	}
	
	
	/*
	 * removeUserRoles()
	 */
	public void removeUserRoles(Long userId) throws DAOException{
		String sql = "DELETE FROM user_role WHERE user_id=?";	
		jdbcTemplate.update(sql, userId);
		Logger.debug("--> Suppression des donnees concernant le UserID : " + userId);
	}
	
	

	@Override
	public long save(User 	user) throws DAOException{
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("login", user.code);
		parameters.put("firstname", user.firstname);
		parameters.put("lastname", user.lastname);
		parameters.put("email", user.email);
		parameters.put("password", user.password);
		parameters.put("technicaluser", user.technicaluser);
		parameters.put("active", user.active);
		Long newId = (Long) jdbcInsert.executeAndReturnKey(parameters);
		user.id = newId;
		return user.id;
	}

	@Override
	public void update(User user) throws DAOException{
		String sql = "UPDATE user SET firstname=?, lastname=?, email=?, password = ?, technicaluser = ?, active=? WHERE id=?";
		jdbcTemplate.update(sql, user.firstname, user.lastname, user.email, user.password,user.technicaluser,user.active, user.id);
	}
}
