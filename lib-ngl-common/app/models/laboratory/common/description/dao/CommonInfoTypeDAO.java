package models.laboratory.common.description.dao;

import static models.utils.dao.DAOException.daoAssertNotNull;

import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.stereotype.Repository;

import models.laboratory.common.description.CommonInfoType;
import models.laboratory.common.description.Institute;
import models.laboratory.common.description.ObjectType.CODE;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.description.State;
import models.utils.dao.AbstractDAOMapping;
import models.utils.dao.DAOException;
import models.utils.dao.DAOHelpers;
import play.api.modules.spring.Spring;

@Repository
public class CommonInfoTypeDAO extends AbstractDAOMapping<CommonInfoType> {

	// We hit a wall as the common info type may be defined but for no institute.
	// Hoped that this was done at construction with an empty list but the list
	// is emptied when the institute is removed. The selection of the common
	// info type for the institute comes out empty. If the cit does not exist,
	// create it, otherwise update it. 
	public CommonInfoTypeDAO() {
		super("common_info_type", CommonInfoType.class, CommonInfoTypeMappingQuery.factory, 
				"SELECT distinct t.id as cId, t.name, t.code as codeSearch, t.display_order as displayOrder, t.active as active, o.id as oId, o.code as codeObject, o.generic "+
				"FROM common_info_type as t "+
				"JOIN object_type as o ON o.id=t.fk_object_type " + DAOHelpers.getCommonInfoTypeSQLForInstitute("t"), true);			
	}

	@Override
	public long save(CommonInfoType cit) throws DAOException {
//		if (cit == null) 
//			throw new DAOException("CommonInfoType is mandatory");
//
//		//Check if objectType exist
//		if (cit.objectType == null || cit.objectType.id == null)
//			throw new DAOException("CommonInfoType.objectType is mandatory");
		daoAssertNotNull("cit",               cit); 
		daoAssertNotNull("cit.objectType",    cit.objectType);
		daoAssertNotNull("cit.objectType.id", cit.objectType.id);
		
		//Create new cit
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("name",           cit.name);
		parameters.put("code",           cit.code);
		parameters.put("fk_object_type", cit.objectType.id);
		parameters.put("display_order",  cit.displayOrder);
		parameters.put("active",         cit.active);
//		Long newId = (Long) jdbcInsert.executeAndReturnKey(parameters);
//		cit.id = newId;
		cit.id = jdbcInsert.executeAndReturnKey(parameters).longValue();
		insertState(cit.states, cit.id, false);
		insertProperties(cit.propertiesDefinitions, cit.id, false);
		insertInstitutes(cit.institutes, cit.id, false);
		
		return cit.id;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void update(CommonInfoType cit) throws DAOException {
//		if (cit == null || cit.id == null)
//			throw new DAOException("CommonInfoType is mandatory");
		daoAssertNotNull("cit",    cit);
		daoAssertNotNull("cti.id", cit.id);
		
		CommonInfoType citDB = findById(cit.id);
		if (citDB == null)
			throw new DAOException("CommonInfoType does not exist");

		String sql = "UPDATE common_info_type SET name=? WHERE id=?";
		jdbcTemplate.update(sql, cit.name, cit.id);
		insertState(cit.states, cit.id, true);
		insertProperties(cit.propertiesDefinitions, cit.id, true);
		insertInstitutes(cit.institutes, cit.id, true);
	}

	@Override
	public void remove(CommonInfoType commonInfoType) throws DAOException {
		//Delete state common_info_type_state
		removeStates(commonInfoType.id);
		//Delete property_definition
		removeProperties(commonInfoType.id);
		//Delete institutes
		removeInstitutes(commonInfoType.id);
		
		super.remove(commonInfoType);
	}

	@SuppressWarnings("deprecation")
	private void removeStates(Long citId) {
		String sqlState = "DELETE FROM common_info_type_state WHERE fk_common_info_type=?";
		jdbcTemplate.update(sqlState, citId);
	}
	
	@SuppressWarnings("deprecation")
	private void removeInstitutes( Long citId) {
		String sqlInstit = "DELETE FROM common_info_type_institute WHERE fk_common_info_type=?";
		jdbcTemplate.update(sqlInstit, citId);
	}

	@SuppressWarnings("deprecation")
	private void removeProperties(Long citId) throws DAOException {
		String sqlValues = "DELETE FROM value WHERE fk_property_definition in (select p.id from property_definition p "
	                                              +"where p.fk_common_info_type = ?)";
		jdbcTemplate.update(sqlValues, citId);

		String sqlLevels = "DELETE FROM property_definition_level WHERE fk_property_definition in (select p.id from property_definition p "
	                                              +"where p.fk_common_info_type = ?)";
		jdbcTemplate.update(sqlLevels, citId);
		
		String sqlProps = "DELETE FROM property_definition WHERE fk_common_info_type = ?";
		jdbcTemplate.update(sqlProps, citId);

		//Delete common_info_type
	}
	
		
	private void insertProperties(List<PropertyDefinition> propertyDefinitions, Long citId, boolean deleteBefore) throws DAOException {
		if (deleteBefore) {
			removeProperties(citId);
		}
		//Add PropertyDefinition
		if (propertyDefinitions != null && propertyDefinitions.size() > 0) {
			PropertyDefinitionDAO propertyDefinitionDAO = Spring.getBeanOfType(PropertyDefinitionDAO.class);
			for (PropertyDefinition propertyDefinition : propertyDefinitions) {
				propertyDefinitionDAO.save(propertyDefinition, citId);				
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	private void insertState(List<State> states, Long citId, boolean deleteBefore) throws DAOException {
		if (deleteBefore) {
			removeStates(citId);
		}
		//Add states list
		if (states != null && states.size() > 0) {
			String sql = "INSERT INTO common_info_type_state (fk_common_info_type,fk_state) VALUES(?,?)";
			for (State state : states) {
				if (state == null || state.id == null ) {
					throw new DAOException("state is mandatory");
				}
				jdbcTemplate.update(sql, citId,state.id);
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void insertInstitutes(List<Institute> institutes, Long citId, boolean deleteBefore) throws DAOException {
		if (deleteBefore) {
			removeInstitutes(citId);
		}
		//Add institutes list		
		if (institutes != null && institutes.size() > 0) {
			String sql = "INSERT INTO common_info_type_institute (fk_common_info_type, fk_institute) VALUES(?,?)";
			for (Institute institute : institutes) {
				if (institute == null || institute.id == null ) {
					throw new DAOException("institute is mandatory");
				}
				jdbcTemplate.update(sql, citId, institute.id);
			}
		}
	}
	
/*
	public List<CommonInfoType> findByName(String typeName) {
		String sql = sqlCommon+
				"WHERE t.name like \'%"+typeName+"%\' "+
				"ORDER by t.name asc";
		CommonInfoTypeMappingQuery commonInfoTypeMappingQuery = new CommonInfoTypeMappingQuery(dataSource, sql, null);
		return commonInfoTypeMappingQuery.execute();
	}

	public List<CommonInfoType> findByTypeNameAndType(String typeName, long idoObjectType) {
		String sql = sqlCommon+
				"WHERE t.name like \'%"+typeName+"%\' "+
				"AND fk_object_type=? "+
				"ORDER by t.name asc";
		CommonInfoTypeMappingQuery commonInfoTypeMappingQuery = new CommonInfoTypeMappingQuery(dataSource, sql, new SqlParameter("fk_object_type",Type.LONG));
		return commonInfoTypeMappingQuery.execute(idoObjectType);
	}
*/	
	
	/*
	 * Particular sql with two code must be implemented
	 */
	/*
	public CommonInfoType findByCode(String code) throws DAOException {
		String sql = sqlCommon+
				"WHERE t.code = ? ";
		CommonInfoTypeMappingQuery commonInfoTypeMappingQuery = new CommonInfoTypeMappingQuery(dataSource, sql, new SqlParameter("code",Types.VARCHAR));
		return commonInfoTypeMappingQuery.findObject(code);
	}
	 */
	public List<CommonInfoType> findByObjectTypeCode(CODE objectTypeCode) {
		String sql = sqlCommon +
				" WHERE o.code=? order by  t.display_order, t.name";		
		CommonInfoTypeMappingQuery commonInfoTypeMappingQuery = new CommonInfoTypeMappingQuery(dataSource, sql, new SqlParameter("code",Types.VARCHAR));
		return commonInfoTypeMappingQuery.execute(objectTypeCode.name());
	}
		
	public CommonInfoType findByExperimentTypeId(Long id){
		String sql = sqlCommon +
				" JOIN experiment_type et ON et.fk_common_info_type = t.id "+
				"WHERE et.id=? ";
		CommonInfoTypeMappingQuery commonInfoTypeMappingQuery = new CommonInfoTypeMappingQuery(dataSource, sql, new SqlParameter("id",Types.BIGINT));
		return commonInfoTypeMappingQuery.findObject(id);
	}

}
