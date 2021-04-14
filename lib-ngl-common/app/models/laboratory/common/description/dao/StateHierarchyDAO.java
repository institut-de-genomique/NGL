package models.laboratory.common.description.dao;


import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.stereotype.Repository;

import models.laboratory.common.description.ObjectType;
import models.laboratory.common.description.ObjectType.CODE;
import models.laboratory.common.description.State;
import models.laboratory.common.description.StateHierarchy;
import models.utils.dao.AbstractDAOMapping;
import models.utils.dao.DAOException;

@Repository
public class StateHierarchyDAO extends AbstractDAOMapping<StateHierarchy> {

	protected StateHierarchyDAO() {
		super("state_object_type_hierarchy", StateHierarchy.class, StateHierarchyMappingQuery.factory, 
				"SELECT t.id, t.code, t.fk_child_state, t.fk_parent_state, t.fk_object_type " +
				"FROM state_object_type_hierarchy t ", true);
	}

	@Override
	public long save(StateHierarchy stateHierarchy) throws DAOException {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("code", stateHierarchy.code);
		parameters.put("fk_child_state", State.find.get().findByCode(stateHierarchy.childStateCode).id);
		parameters.put("fk_object_type", ObjectType.find.get().findByCode(stateHierarchy.objectTypeCode).id);
		parameters.put("fk_parent_state", State.find.get().findByCode(stateHierarchy.parentStateCode).id);
		
		Long newId = (Long) jdbcInsert.executeAndReturnKey(parameters);
		stateHierarchy.id = newId;
		
		return stateHierarchy.id;
	}

	@Override
	public void update(StateHierarchy value) throws DAOException {
	}

	public List<StateHierarchy> findByObjectTypeCode(CODE objectTypeCode) throws DataAccessException, DAOException {
		String sql = sqlCommon + 
					"JOIN object_type o ON o.id = t.fk_object_type " +
					"WHERE o.code = ?";
		
		return initializeMapping(sql, new SqlParameter("o.code", Types.VARCHAR)).execute(objectTypeCode.name());	
	}

}
