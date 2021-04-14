package models.administration.authorisation.description.dao;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.stereotype.Repository;

import models.administration.authorisation.Role;
import models.administration.authorisation.User;
import models.utils.dao.AbstractDAOMapping;
import models.utils.dao.DAOException;

@Repository
public class UserDAO extends AbstractDAOMapping<User> { 

	private static final play.Logger.ALogger logger = play.Logger.of(UserDAO.class);
	
	protected UserDAO() {
		super("user", User.class, UserMappingQuery.factory,
				"SELECT t.id, t.login, t.firstname, t.lastname, t.email, t.technicaluser " +
				"FROM user as t ", true);
	}
	
	/*
	 * findAll() remade
	 */
	@Override
	public List<User> findAll() throws DAOException	{
		String sql = sqlCommon + "ORDER by t.login";
		logger.debug(sql);
		return initializeMapping(sql).execute();
	}
	
	@SuppressWarnings("deprecation")
	public boolean isExistUserWithLoginAndPassword(String login, String password) {
		String sql = "SELECT login " +
				"FROM user WHERE login='" + login + "' AND password='" + password + "'";
		
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("login",    login);
		parameters.put("password", password);
		
		BeanPropertyRowMapper<User> mapper = new BeanPropertyRowMapper<>(User.class);
		// Would have expected == 1
		return this.jdbcTemplate.query(sql, mapper).size() > 0;
	}

	public String  getUserPassword(String login) throws DAOException {
		String sql = "SELECT login , password FROM user WHERE login='" + login + "'";
		BeanPropertyRowMapper<User> mapper = new BeanPropertyRowMapper<>(User.class);
		@SuppressWarnings("deprecation")
		List<User> users = jdbcTemplate.query(sql, mapper);
		if (users == null || users.size() != 1)
			return null;
		return users.get(0).password;
	}	
	
	public boolean isUserActive(String login) throws DAOException {
		String sql = "SELECT active " + "FROM user WHERE login = '" + login + "' ";
		BeanPropertyRowMapper<User> mapper = new BeanPropertyRowMapper<>(User.class);
		logger.debug("UserDAO - isUserActive : requête SQL : " + sql + " for " + login);
		@SuppressWarnings("deprecation")
		List<User> users = jdbcTemplate.query(sql, mapper);
		return users != null && users.size() == 1 && users.get(0).active;
	}
	
	@SuppressWarnings("deprecation")
	public boolean isUserAccessApplication(String login, String application) {
		if (!login.equals("") && getUserId(login) != 0) {
			applicationAccess(login, application);
			String sql = "SELECT a.label "+
					"FROM user u, application a, user_application ua WHERE u.login='"+login+"' AND a.code='"+application+"' AND ua.user_id=u.id and ua.application_id=a.id";
			BeanPropertyRowMapper<User> mapper = new BeanPropertyRowMapper<>(User.class);
			return this.jdbcTemplate.query(sql, mapper).size() > 0;
		} else if (!login.equals("")) {
			createUser(login);
			applicationAccess(login, application);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public void setDefaultRole(String login, String role) {
		String sql = "SELECT u.id FROM user u, user_role ur WHERE u.login=? and u.id=ur.user_id";
		BeanPropertyRowMapper<User> mapper = new BeanPropertyRowMapper<>(User.class);
		if (this.jdbcTemplate.query(sql, mapper,login).size() == 0) {
			sql = "INSERT INTO user_role (user_id,role_id) VALUE (?,?)";
			this.jdbcTemplate.update(sql, getUserId(login), getRoleId(role));
		}
	}
	
	@SuppressWarnings("deprecation")
	private long getRoleId(String label) {
		String sql = "SELECT r.id FROM role r WHERE r.label=?";
		BeanPropertyRowMapper<Role> mapper = new BeanPropertyRowMapper<>(Role.class);
		return this.jdbcTemplate.query(sql, mapper,label).get(0).id;
	}
	
	@SuppressWarnings("deprecation")
	private long getApplicationId(String code) {
		String sql = "SELECT a.id FROM application a WHERE a.code=?";
		BeanPropertyRowMapper<Role> mapper = new BeanPropertyRowMapper<>(Role.class);
		return this.jdbcTemplate.query(sql, mapper,code).get(0).id;
	}
	
	private long getUserId(String login) {
		String sql = "SELECT u.id as id FROM user u WHERE u.login=?";
		BeanPropertyRowMapper<User> mapper = new BeanPropertyRowMapper<>(User.class);
		@SuppressWarnings("deprecation")
		List<User> users = this.jdbcTemplate.query(sql, mapper,login);
		if (users != null && users.size() > 0) {
			return users.get(0).id;
		} else {
			return 0;
		}
	}
	
	@SuppressWarnings("deprecation")
	private void applicationAccess(String login,String application) {
		String sql = "SELECT u.id FROM user u, user_application ua, application a WHERE u.login=? and u.id=ua.user_id and a.code = ? and a.id=ua.application_id";
		BeanPropertyRowMapper<User> mapper = new BeanPropertyRowMapper<>(User.class);
		if (this.jdbcTemplate.query(sql, mapper,login, application).size() == 0) {
			sql = "INSERT INTO user_application (user_id,application_id) VALUE (?,?)";
			this.jdbcTemplate.update(sql, getUserId(login), getApplicationId(application));
		}
	}
	
	@SuppressWarnings("deprecation")
	private void createUser(String login) {
		String sql = "INSERT INTO user (login) VALUE (?)";
		this.jdbcTemplate.update(sql, login);
	}
	
	/*
	 * findByLogin()
	 */
	public User findByLogin(String aLogin) throws DAOException {
		if (null == aLogin) {
			throw new DAOException("login is mandatory");
		}
		String sql = sqlCommon + "WHERE t.login=?";
		UserMappingQuery userMappingQuery = new UserMappingQuery(dataSource, sql, new SqlParameter("login", Types.VARCHAR));
		return userMappingQuery.findObject(aLogin);
	}
	
	/*
	 * findByLikeLogin()
	 */
	public List<User> findByLikeLogin(String aLike) throws DAOException {
		if (null == aLike) {
			throw new DAOException("login is mandatory");
		}
		String sql = sqlCommon + "WHERE t.login LIKE ?";
		return initializeMapping(sql, new SqlParameter("like", Types.VARCHAR)).execute(aLike);
	}

	/*
	 * insertUserRoles
	 */
	@SuppressWarnings("deprecation")
	public void insertUserRoles(Long userId, List<Long> roleIds, boolean deleteBefore) throws DAOException {		
		if (deleteBefore)
			removeUserRoles(userId);
		// Add Roles to User
		if (roleIds != null && roleIds.size() > 0) {
			String sql = "INSERT INTO user_role (user_id,role_id) VALUES (?,?)";
			for (Long l:roleIds) {
				if (l == null)
					throw new DAOException();
				jdbcTemplate.update(sql, userId, l);
				logger.debug("Insertion de donnees : \n\tUser id : " + userId + "\n\tRoleId : " + l);
			}
		}
	}
	
	/*
	 * removeUserRoles()
	 */
	@SuppressWarnings("deprecation")
	public void removeUserRoles(Long userId) throws DAOException {
		String sql = "DELETE FROM user_role WHERE user_id=?";	
		jdbcTemplate.update(sql, userId);
		logger.debug("--> Suppression des donnees concernant le UserID : " + userId);
	}
	
	@Override
	public long save(User user) throws NotImplementedException {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("login",         user.code);
		parameters.put("firstname",     user.firstname);
		parameters.put("lastname",      user.lastname);
		parameters.put("email",         user.email);
		parameters.put("password",      user.password);
		parameters.put("technicaluser", user.technicaluser);
		parameters.put("active",        user.active);
		Long newId = (Long) jdbcInsert.executeAndReturnKey(parameters);
		user.id = newId;
		return user.id;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void update(User user) throws NotImplementedException {
		String sql = "UPDATE user SET firstname=?, lastname=?, email=?, password = ?, technicaluser = ?, active=? WHERE id=?";
		jdbcTemplate.update(sql, user.firstname, user.lastname, user.email, user.password,user.technicaluser,user.active, user.id);
	}
	
	// -- Authorization implementation
	
	public boolean isDeclaredUser(String login) {
		return !isBlank(login) && getUserId(login) != 0;
	}
	
	public void grantApplicationAccess(String login, String application) {
		applicationAccess(login, application);
	}
	
	public void declareUser(String login, String role) {
		createUser(login);
		setDefaultRole(login,role);
	}
	
	@SuppressWarnings("deprecation")
	public boolean canAccessApplication(String login, String application) {
		String sql = "SELECT u.id FROM user u, user_application ua, application a WHERE u.login=? and u.id=ua.user_id and a.code = ? and a.id=ua.application_id";
		BeanPropertyRowMapper<User> mapper = new BeanPropertyRowMapper<>(User.class);
		return jdbcTemplate.query(sql, mapper,login, application).size() > 0;
	}
	
}
