package models.laboratory.common.description.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

import models.laboratory.common.description.State;
import models.laboratory.common.description.StateCategory;
import models.utils.dao.DAOException;
import models.utils.dao.MappingSqlQueryFactory;
import models.utils.dao.NGLMappingSqlQuery;

//public class StateMappingQuery extends MappingSqlQuery<State> {
public class StateMappingQuery extends NGLMappingSqlQuery<State> {

	public static final MappingSqlQueryFactory<State> factory = (d,s,ps) -> new StateMappingQuery(d,s,ps);
	
//	public StateMappingQuery() {
//		super();
//	}
	
//	public StateMappingQuery(DataSource ds, String sql,SqlParameter sqlParameter) {
//		super(ds,sql);
//		if (sqlParameter != null)
////			super.declareParameter(sqlParameter);
//			declareParameter(sqlParameter);
//		compile();
//	}

	public StateMappingQuery(DataSource ds, String sql, SqlParameter... sqlParameters) {
		super(ds,sql,sqlParameters);
	}
	
	@Override
	protected State mapRow(ResultSet rs, int rowNum) throws SQLException {
		State state = new State();
		state.id       = rs.getLong("id");
		state.code     = rs.getString("code");
		state.name     = rs.getString("name");
		state.active   = rs.getBoolean("active");
		state.position = rs.getInt("position");
		
		long idCategory = rs.getLong("fk_state_category");
		StateCategory category = null;
		try {
			category = StateCategory.find.get().findById(idCategory);
		} catch (DAOException e) {
			throw new SQLException(e);
		}
		state.category = category;
		state.display  = rs.getBoolean("display");
		state.functionnalGroup=rs.getString("functionnal_group");
		return state;
	}

}
