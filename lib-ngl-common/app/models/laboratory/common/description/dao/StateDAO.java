package models.laboratory.common.description.dao;
import static models.utils.dao.DAOException.daoAssertNotNull;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.stereotype.Repository;

import models.laboratory.common.description.ObjectType;
import models.laboratory.common.description.ObjectType.CODE;
import models.laboratory.common.description.State;
import models.utils.ListObject;
import models.utils.dao.AbstractDAOMapping;
import models.utils.dao.DAOException;
import models.utils.dao.DAOHelpers;

@Repository
public class StateDAO extends AbstractDAOMapping<State> {

	private static final play.Logger.ALogger logger = play.Logger.of(StateDAO.class);
	
	protected StateDAO() {
		super("state", State.class, StateMappingQuery.factory, 
				"SELECT t.id, t.name, t.code, t.active, t.position, t.fk_state_category, t.display, t.functionnal_group " +
				"FROM state as t ", true);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void remove(State state) throws DAOException	{
	 	//Remove list state for common_info_type
		String sqlState = "DELETE FROM common_info_type_state WHERE fk_state=?";
		jdbcTemplate.update(sqlState, state.id);
		//Remove list state for object_type
		sqlState = "DELETE FROM state_object_type WHERE fk_state=?";
		jdbcTemplate.update(sqlState, state.id);
		//remove state
		super.remove(state);
	}

	@Override
	public long save(State state) throws DAOException {
		logger.debug("saving state '{}'", state.code);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("code",              state.code);
		parameters.put("name",              state.name);
		parameters.put("active",            state.active);
		parameters.put("position",          state.position);
		parameters.put("fk_state_category", state.category.id);
		parameters.put("display",           state.display);
		parameters.put("functionnal_group", state.functionnalGroup);
		
		Long newId = (Long) jdbcInsert.executeAndReturnKey(parameters);
		state.id = newId;
		
		insertObjectTypes(state.objectTypes, state.id, true);
		
		return state.id;
	}

	@SuppressWarnings("deprecation")
	private void insertObjectTypes(List<ObjectType> objectTypes, Long id, boolean deleteBefore) throws DAOException {
		if (deleteBefore) {
			removeObjectTypes(id);
		}
		if (objectTypes != null && objectTypes.size() > 0) {
			String sql = "INSERT INTO state_object_type (fk_state, fk_object_type) VALUES (?,?)";
			for (ObjectType objectType : objectTypes) {
				if (objectType == null || objectType.id == null ) {
					throw new DAOException("objectType is mandatory");
				}
				jdbcTemplate.update(sql, id, objectType.id);
			}
		}				
	}

	@SuppressWarnings("deprecation")
	private void removeObjectTypes(Long id) {
		String sql = "DELETE FROM state_object_type WHERE fk_state=?";
		jdbcTemplate.update(sql, id);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void update(State state) throws DAOException {
		String sql = "UPDATE state SET code=?, name=?, active=?, position=?, fk_state_category=?, display=?, functionnal_group=? WHERE id=?";
		jdbcTemplate.update(sql, state.code, state.name, state.active, state.position, state.id, state.category.id, state.display, state.functionnalGroup);
	}
	
	@SuppressWarnings("deprecation")
	public List<ListObject> findAllForContainerList() {
		String sql = "SELECT t.code, t.name FROM state t"
	               + " inner join state_object_type sot on sot.fk_state = t.id" 
	               + " inner join object_type o on o.id = sot.fk_object_type"
	               + " WHERE o.code = ? ";
		BeanPropertyRowMapper<ListObject> mapper = new BeanPropertyRowMapper<>(ListObject.class);
		return this.jdbcTemplate.query(sql, mapper, ObjectType.CODE.Container.name());
	}

	public List<State> findByCategoryCode(String code) throws DAOException {
//		if(null == code){
//			throw new DAOException("code is mandatory");
//		}
		daoAssertNotNull("code",code);
		String sql = sqlCommon
				   + " inner join state_category s on s.id = t.fk_state_category"
				   + " WHERE s.code = ? ";
		return initializeMapping(sql, new SqlParameter("code", Types.VARCHAR)).execute(code);		
	}
	
	public List<State> findByCommonInfoType(long idCommonInfoType) throws DAOException {
		String sql = sqlCommon
				   + "JOIN common_info_type_state cs ON cs.fk_state=t.id "
				   + "JOIN common_info_type c on c.id =cs.fk_common_info_type "
				   + DAOHelpers.getCommonInfoTypeSQLForInstitute("c")
				   + "WHERE cs.fk_common_info_type=?";		
		return initializeMapping(sql, new SqlParameter("fk_common_info_type", Types.BIGINT)).execute(idCommonInfoType);		
	}
	
	public List<State> findByObjectTypeId(Long id) throws DAOException {
//		if (null == id) {
//			throw new DAOException("id is mandatory");
//		}
		daoAssertNotNull("id",id);
		String sql = sqlCommon
				   + "JOIN state_object_type s ON s.fk_state=t.id "
				   + "WHERE s.fk_object_type=?";		
		return initializeMapping(sql, new SqlParameter("fk_object_type", Types.BIGINT)).execute(id);		
	}

	/**
	 * List of states allowed for a given object type ({@link CODE}).
	 * @param objectTypeCode object type
	 * @return               state list for the object type
	 * @throws DAOException  SQL error
	 */
	public List<State> findByObjectTypeCode(ObjectType.CODE objectTypeCode) throws DAOException {
//		if(null == objectTypeCode){
//			throw new DAOException("code is mandatory");
//		}
		daoAssertNotNull("objectTypeCode", objectTypeCode);
		String sql = sqlCommon
				   + "JOIN state_object_type so ON so.fk_state=t.id "
				   + "JOIN object_type o ON so.fk_object_type=o.id "
				   + "WHERE o.code=? order by position";		
		return initializeMapping(sql, new SqlParameter("o.code", Types.VARCHAR)).execute(objectTypeCode.name());		
	}
	
	public List<State> findByTypeCode(String typeCode)  throws DAOException {
		String sql = sqlCommon
				   + " JOIN common_info_type_state cs ON cs.fk_state=t.id "
				   + " JOIN common_info_type c on c.id=cs.fk_common_info_type "
				   + DAOHelpers.getCommonInfoTypeSQLForInstitute("c")
				   + " where c.code=?";
		return initializeMapping(sql, new SqlParameter("c.code", Types.VARCHAR)).execute(typeCode);	
	}
	
	public List<State> findByDisplayAndObjectTypeCode(Boolean display, ObjectType.CODE objectTypeCode) throws DAOException {
//		if (objectTypeCode == null || display == null) {
//			throw new DAOException("code is mandatory");
//		}
		daoAssertNotNull("objectTypeCode", objectTypeCode);
		daoAssertNotNull("display",        display);
		String sql = sqlCommon
				   + "JOIN state_object_type so ON so.fk_state=t.id "
				   + "JOIN object_type o ON so.fk_object_type=o.id "
				   + "WHERE t.display=? AND o.code=? "
				   + "ORDER BY position";		
//		Object[] sqlParameters = new SqlParameter[2];
//		sqlParameters[0] = new SqlParameter("t.display", Types.BOOLEAN);
//		sqlParameters[1] = new SqlParameter("o.code", Types.VARCHAR);
//		return initializeMapping(sql, (SqlParameter[])sqlParameters).execute(display, objectTypeCode.name());
		SqlParameter[] sqlParameters = {
				new SqlParameter("t.display", Types.BOOLEAN),
				new SqlParameter("o.code",    Types.VARCHAR)
		};
		return initializeMapping(sql, sqlParameters).execute(display, objectTypeCode.name());
	}

	// roughly: select * from State as t
	// 
	/**
	 * Is the state code allowed for the common info type with the given type code ?
	 * @param code          state code
	 * @param typeCode      common info type code 
	 * @return              true if the query returned something
	 * @throws DAOException SQL error
	 */
	public boolean isCodeExistForTypeCode(String code, String typeCode)  throws DAOException {
		String sql = sqlCommon
				   + "JOIN common_info_type_state cs ON cs.fk_state=t.id "
				   + "JOIN common_info_type c on c.id=cs.fk_common_info_type "
				   + DAOHelpers.getCommonInfoTypeSQLForInstitute("c")
				   + " where t.code=? and c.code=?";		
//		return( initializeMapping(sql, new SqlParameter("t.code", Types.VARCHAR),
//				 new SqlParameter("c.code", Types.VARCHAR)).findObject(code, typeCode) != null )? true : false;	
		return initializeMapping(sql, 
				                 new SqlParameter("t.code", Types.VARCHAR), 
				                 new SqlParameter("c.code", Types.VARCHAR)
				                 ).findObject(code, typeCode) != null;	
	}
	
	/**
	 * Is the state code allowed for the given object type ?
	 * @param code           state code
	 * @param objectTypeCode object type code
	 * @return               true if the query returned something 
	 * @throws DAOException  SQL error
	 */
	// This is the same as using the state static definition (no SQL needed).
	public boolean isCodeExistForObjectTypeCode(String code, CODE objectTypeCode) throws DAOException {
		String sql = sqlCommon 
				   + "JOIN state_object_type cs ON cs.fk_state=t.id "
				   + "JOIN object_type c on c.id=cs.fk_object_type "
				   + " where t.code=? and c.code=?";	
//		return( initializeMapping(sql, new SqlParameter("t.code", Types.VARCHAR),
//				 new SqlParameter("c.code", Types.VARCHAR)).findObject(code, objectTypeCode) != null )? true : false;	
		return initializeMapping(sql, 
				                 new SqlParameter("t.code", Types.VARCHAR),
				                 new SqlParameter("c.code", Types.VARCHAR)
				                 ).findObject(code, objectTypeCode) != null;	
	}

	/**
	 * Is the next state a backward state from the previous state 
	 * in the state order (not from previous to next) ?
	 * @param previousState previous state
	 * @param nextState     next state
	 * @return              true if the state transition is backward
	 */
	public boolean isBackward(String previousState, String nextState) {
		return findByCode(nextState).position < findByCode(previousState).position;
	}

}
