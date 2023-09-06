package models.administration.authorisation.description.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

import models.administration.authorisation.Role;
import models.utils.dao.MappingSqlQueryFactory;
import models.utils.dao.NGLMappingSqlQuery;

/**
 * 
 * @author michieli
 *
 */
//public class RoleMappingQuery extends MappingSqlQuery<Role>{
public class RoleMappingQuery extends NGLMappingSqlQuery<Role> {

	public static final MappingSqlQueryFactory<Role> factory = (d,s,ps) -> new RoleMappingQuery(d,s,ps);
	
//	public RoleMappingQuery(){
//		super();
//	}
	
//	public RoleMappingQuery(DataSource ds, String sql, SqlParameter sqlParameter) {
//		super(ds,sql);
//		if (sqlParameter != null)
//			super.declareParameter(sqlParameter);
//		compile();
//	}
	
	public RoleMappingQuery(DataSource ds, String sql, SqlParameter... sqlParameters) {
		super(ds,sql,sqlParameters);
	}

	@Override
	protected Role mapRow(ResultSet rs, int rowNum) throws SQLException {
		Role role = new Role();
		role.id    = rs.getLong("id");
		role.label = rs.getString("label");
		return role;
	}

}
