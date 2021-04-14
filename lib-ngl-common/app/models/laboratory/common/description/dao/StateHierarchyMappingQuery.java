package models.laboratory.common.description.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

import models.laboratory.common.description.ObjectType;
import models.laboratory.common.description.State;
import models.laboratory.common.description.StateHierarchy;
import models.utils.dao.DAOException;
import models.utils.dao.MappingSqlQueryFactory;
import models.utils.dao.NGLMappingSqlQuery;

//public class StateHierarchyMappingQuery extends MappingSqlQuery<StateHierarchy> {
public class StateHierarchyMappingQuery extends NGLMappingSqlQuery<StateHierarchy> {

	public static final MappingSqlQueryFactory<StateHierarchy> factory = (d,s,ps) -> new StateHierarchyMappingQuery(d,s,ps);
	
//	public StateHierarchyMappingQuery() {
//		super();
//	}
	
//	public StateHierarchyMappingQuery(DataSource ds, String sql,SqlParameter sqlParameter) {
//		super(ds,sql);
//		if (sqlParameter != null)
////			super.declareParameter(sqlParameter);
//			declareParameter(sqlParameter);
//		compile();
//	}

	public StateHierarchyMappingQuery(DataSource ds, String sql, SqlParameter... sqlParameters) {
		super(ds,sql,sqlParameters);
	}
	
	@Override
	protected StateHierarchy mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		StateHierarchy stateHierarchy = new StateHierarchy();
		
		stateHierarchy.id=rs.getLong("id");
		stateHierarchy.code=rs.getString("code");
		
		long id = rs.getLong("fk_child_state");
		State state = null;
		try {
			state = State.find.get().findById(id);
		} catch (DAOException e) {
			throw new SQLException(e);
		}
		stateHierarchy.childStateCode=state.code;
		
		stateHierarchy.childStateName = state.name;
		stateHierarchy.position = state.position;
		stateHierarchy.functionnalGroup = state.functionnalGroup;
		
		id = rs.getLong("fk_parent_state");
		state = null;
		try {
			state = State.find.get().findById(id);
		} catch (DAOException e) {
			throw new SQLException(e);
		}
		stateHierarchy.parentStateCode=state.code;

		id = rs.getLong("fk_object_type");
		ObjectType ot = null;
		try {
			ot = ObjectType.find.get().findById(id);
		} catch (DAOException e) {
			throw new SQLException(e);
		}
		stateHierarchy.objectTypeCode = ot.code;
		return stateHierarchy;
	}

}
