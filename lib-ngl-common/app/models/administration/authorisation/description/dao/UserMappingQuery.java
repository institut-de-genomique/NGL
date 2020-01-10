package models.administration.authorisation.description.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

import models.administration.authorisation.Role;
import models.administration.authorisation.User;
import models.utils.dao.DAOException;
// import play.Logger;
import models.utils.dao.MappingSqlQueryFactory;
import models.utils.dao.NGLMappingSqlQuery;

/**
 * 
 * @author michieli
 *
 */
//public class UserMappingQuery extends MappingSqlQuery<User> {
public class UserMappingQuery extends NGLMappingSqlQuery<User> {

//	public static final MappingSqlQueryFactory<User> factory = (d,s,ps) -> new UserMappingQuery(d,s,ps);
	public static final MappingSqlQueryFactory<User> factory = UserMappingQuery::new;
	
//	public UserMappingQuery(){
//		super();
//	}
	
//	public UserMappingQuery(DataSource ds, String sql, SqlParameter sqlParameter){
//		super(ds,sql);
//		if (sqlParameter != null)
////			super.declareParameter(sqlParameter);
//			declareParameter(sqlParameter);
//		compile();
//	}

	public UserMappingQuery(DataSource ds, String sql, SqlParameter... sqlParameters) {
		super(ds,sql,sqlParameters);
	}
	
	@Override
	protected User mapRow(ResultSet rs, int rowNum) throws SQLException {
		User user = new User();
		user.id            = rs.getLong("id");
		user.login         = rs.getString("login");
		user.firstname     = rs.getString("firstname");
		user.lastname      = rs.getString("lastname");
		user.email         = rs.getString("email");
		user.technicaluser = rs.getInt("technicaluser");
		
		List<Long> someIds = new ArrayList<>();
		// List<String> someLabels = new ArrayList<>();
		try {
			List<Role> roles = Role.find.get().findByUserLogin(user.login);
			for(Role r:roles){
				someIds.add(r.id);
			}
			user.roleIds = someIds;
		} catch (DAOException e) {
			throw new SQLException(e);
		}
		return user;
	}
	
}
