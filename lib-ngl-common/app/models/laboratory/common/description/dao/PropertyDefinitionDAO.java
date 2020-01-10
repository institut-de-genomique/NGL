package models.laboratory.common.description.dao;

import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.asm.Type;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.stereotype.Repository;

import models.laboratory.common.description.Level;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.description.Value;
import models.utils.dao.AbstractDAOMapping;
import models.utils.dao.DAOException;
import models.utils.dao.DAOHelpers;
import play.api.modules.spring.Spring;

@Repository
public class PropertyDefinitionDAO extends AbstractDAOMapping<PropertyDefinition> {

	private static final play.Logger.ALogger logger = play.Logger.of(PropertyDefinitionDAO.class);
	
	protected PropertyDefinitionDAO() {
		super("property_definition", PropertyDefinition.class, PropertyDefinitionMappingQuery.factory,
				"SELECT id,code,name,required,required_state,editable,active,type,display_format,display_order,default_value,description,"
						+ "choice_in_list,fk_measure_category, property_value_type,fk_save_measure_unit,fk_display_measure_unit,fk_common_info_type "
						+" FROM property_definition as t",true);
	}

	public List<PropertyDefinition> findByCommonInfoType(long idCommonInfoType)	{
		String sql = sqlCommon+" WHERE fk_common_info_type = ? ";
		PropertyDefinitionMappingQuery propertyDefinitionMappingQuery=new PropertyDefinitionMappingQuery(dataSource, sql, new SqlParameter("fk_common_info_type",Type.LONG));
		return propertyDefinitionMappingQuery.execute(idCommonInfoType);
	}

	@Override
	public long save(PropertyDefinition value) throws DAOException {
		throw new DAOException("Must be inserted with commonInfoType id");
	}

	public PropertyDefinition findUnique(String code, Level.CODE levelCode) {
		String sql = 
				"select pd.code, pd.type, pd.property_value_type, "
						+ " case when count(distinct pd.choice_in_list) = 1 then pd.choice_in_list"
						+ " else 1 end as 'choice_in_list'" 
						+"	from  property_definition pd"
						+"	inner join property_definition_level pdf on pdf.fk_property_definition = pd.id"
						+"	inner join level l on l.id = pdf.fk_level and l.code = ?"
						+"	inner join common_info_type cit on cit.id = pd.fk_common_info_type "
						+DAOHelpers.getCommonInfoTypeSQLForInstitute("cit")
						+"	inner join object_type ot on ot.id = cit.fk_object_type"
						+" where pd.code = ?"
						+" group by pd.code, pd.type, pd.property_value_type";
		PropertyDefinitionMappingQuery propertyDefinitionMappingQuery=new PropertyDefinitionMappingQuery(dataSource, sql, true, new SqlParameter("l.code",Types.VARCHAR), new SqlParameter("pd.code",Types.VARCHAR));
		List<PropertyDefinition> l = propertyDefinitionMappingQuery.execute(levelCode.toString(), code);
		if (l.size() == 1) {
			return l.get(0);
		} else {
			logger.error("PropertyDefinition findUnique query return more than one result or zero result: "+sql+" / "+code+" / "+levelCode);
			return null;
		}		
	}

	public List<PropertyDefinition> findUnique(Level.CODE levelCode){
		String sql = 
				"select pd.code, pd.type, pd.property_value_type, "
						+ " case when count(distinct pd.choice_in_list) = 1 then pd.choice_in_list"
						+ " else 1 end as 'choice_in_list'" 
						+"	from  property_definition pd"
						+"	inner join property_definition_level pdf on pdf.fk_property_definition = pd.id"
						+"	inner join level l on l.id = pdf.fk_level and l.code = ?"
						+"	inner join common_info_type cit on cit.id = pd.fk_common_info_type "
						+DAOHelpers.getCommonInfoTypeSQLForInstitute("cit")
						+"	inner join object_type ot on ot.id = cit.fk_object_type "
						+" group by pd.code, pd.type, pd.property_value_type order by pd.code";

		PropertyDefinitionMappingQuery propertyDefinitionMappingQuery=new PropertyDefinitionMappingQuery(dataSource, sql, true, new SqlParameter("l.code",Types.VARCHAR));
		List<PropertyDefinition> l = propertyDefinitionMappingQuery.execute(levelCode.toString());
		return l;
	}
	
	public List<PropertyDefinition> findUnique() {
		String sql = 
				"select pd.code, pd.type, pd.property_value_type, "
						+ " case when count(distinct pd.choice_in_list) = 1 then pd.choice_in_list"
						+ " else 1 end as 'choice_in_list'" 
						+"	from  property_definition pd"
						+"	inner join common_info_type cit on cit.id = pd.fk_common_info_type "
						+"	inner join property_definition_level pdf on pdf.fk_property_definition = pd.id"
						+DAOHelpers.getCommonInfoTypeSQLForInstitute("cit")
						+"	inner join object_type ot on ot.id = cit.fk_object_type"
						+" group by pd.code, pd.type, pd.property_value_type  order by pd.code";

		PropertyDefinitionMappingQuery propertyDefinitionMappingQuery=new PropertyDefinitionMappingQuery(dataSource, sql, true);
		List<PropertyDefinition> l = propertyDefinitionMappingQuery.execute();
		return l;
	}

	public PropertyDefinition save(PropertyDefinition propertyDefinition, long idCommonInfoType) throws DAOException {
		if (propertyDefinition.levels == null || propertyDefinition.levels.size() == 0) {
			throw new DAOException("level does not exist or level.id is null) !! - " + propertyDefinition.code);
		}
		//Create propertyDefinition
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("code", propertyDefinition.code);
//		parameters.put("name", propertyDefinition.name);
		parameters.put("name", propertyDefinition.getName());
		parameters.put("description", propertyDefinition.description);
		parameters.put("required", propertyDefinition.required);
		parameters.put("editable", propertyDefinition.editable);
		parameters.put("active", propertyDefinition.active);
		parameters.put("choice_in_list", propertyDefinition.choiceInList);
		parameters.put("type", propertyDefinition.valueType);
		parameters.put("display_format", propertyDefinition.displayFormat);
		parameters.put("display_order", propertyDefinition.displayOrder);
		parameters.put("default_value", propertyDefinition.defaultValue);
		parameters.put("property_value_type", propertyDefinition.propertyValueType);
		parameters.put("fk_common_info_type", idCommonInfoType);
		parameters.put("required_state", propertyDefinition.requiredState);

		if (propertyDefinition.measureCategory != null) {
			if (propertyDefinition.measureCategory.id == null) {
				throw new DAOException("measureCategory does not exist (id is null) !!");
			}
			parameters.put("fk_measure_category", propertyDefinition.measureCategory.id);
		}

		if (propertyDefinition.saveMeasureValue != null) {
			if (propertyDefinition.saveMeasureValue.id == null) {
				throw new DAOException("saveMeasureValue does not exist (id is null) !!");
			}
			parameters.put("fk_save_measure_unit", propertyDefinition.saveMeasureValue.id);
		}

		if (propertyDefinition.displayMeasureValue != null) {
			if (propertyDefinition.displayMeasureValue.id == null) {
				throw new DAOException("displayMeasureValue does not exist (id is null) !!");
			}
			parameters.put("fk_display_measure_unit", propertyDefinition.displayMeasureValue.id);
		}

		Long newId = (Long) jdbcInsert.executeAndReturnKey(parameters);
		propertyDefinition.id = newId;

		insertPropertyDefinitionLevel(propertyDefinition.levels,propertyDefinition.id,false);
		insertValues(propertyDefinition.possibleValues, propertyDefinition.id, false);
		return propertyDefinition;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void update(PropertyDefinition propertyDefinition) throws DAOException {
		String sql = "UPDATE property_definition SET name=?, description=?, required=?, editable=?, " +
				"active=?,choice_in_list=?, type=?, display_format=?, " +
				"display_order=?, default_value=?,  property_value_type=?,  required_state=?" +
				" WHERE id=?";
//		jdbcTemplate.update(sql, propertyDefinition.name, propertyDefinition.description, propertyDefinition.required,propertyDefinition.editable,
		jdbcTemplate.update(sql, propertyDefinition.getName(), propertyDefinition.description, propertyDefinition.required,propertyDefinition.editable,
				propertyDefinition.active, propertyDefinition.choiceInList, propertyDefinition.valueType, propertyDefinition.displayFormat,
				propertyDefinition.displayOrder, propertyDefinition.defaultValue, propertyDefinition.propertyValueType,propertyDefinition.requiredState,
				propertyDefinition.id);

		//Update measure category
		String sqlCategory = "UPDATE property_definition SET fk_measure_category=? WHERE id=?";
		if (propertyDefinition.measureCategory != null) {
			jdbcTemplate.update(sqlCategory, propertyDefinition.measureCategory.id, propertyDefinition.id);
		} else {
			jdbcTemplate.update(sqlCategory, null, propertyDefinition.id);
		}

		String sqlMeasureValue = "UPDATE property_definition SET fk_save_measure_value=? WHERE id=?";
		if (propertyDefinition.saveMeasureValue != null) {
			//Update propertyDefinition
			jdbcTemplate.update(sqlMeasureValue, propertyDefinition.saveMeasureValue.id, propertyDefinition.id);
		} else {
			jdbcTemplate.update(sqlMeasureValue, null, propertyDefinition.id);
		}

		//Update displayMeasureValue
		String sqlValue = "UPDATE property_definition SET fk_display_measure_value=? WHERE id=?";
		if (propertyDefinition.displayMeasureValue != null) {
			//Update propertyDefinition
			jdbcTemplate.update(sqlValue, propertyDefinition.displayMeasureValue.id, propertyDefinition.id);
		} else {
			jdbcTemplate.update(sqlValue, null, propertyDefinition.id);
		}

		insertValues(propertyDefinition.possibleValues, propertyDefinition.id, true);
		insertPropertyDefinitionLevel(propertyDefinition.levels, propertyDefinition.id, true);
	}

	@SuppressWarnings("deprecation")
	private void insertValues(List<Value> values, Long id, boolean deleteBefore) {
		// Add values list
		if (deleteBefore) {
			String sqlState = "DELETE FROM value WHERE property_definition_id=?";
			jdbcTemplate.update(sqlState, id);
		}
		if (values != null && values.size() > 0) {
			ValueDAO valueDao = Spring.getBeanOfType(ValueDAO.class);
			for (Value value : values) {
				valueDao.save(value, id);
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void insertPropertyDefinitionLevel(List<Level> levels, Long id, boolean deleteBefore)  throws DAOException {
		if (deleteBefore) {
			removePropertyDefinitionLevel(id);
		}
		if (levels != null && levels.size() > 0) {
			String sql = "INSERT INTO property_definition_level (fk_property_definition, fk_level) VALUES(?,?)";
			for (Level level:levels) {
				if (level == null || level.id == null) {
					throw new DAOException("level is mandatory");
				}
				jdbcTemplate.update(sql, id,level.id);
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void removePropertyDefinitionLevel(Long id) {
		String sqlState = "DELETE FROM property_definition_level WHERE fk_property_definition_id=?";
		jdbcTemplate.update(sqlState, id);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void remove(PropertyDefinition propertyDefinition) throws DAOException {
		//Delete value
		String sqlState = "DELETE FROM value WHERE property_definition_id=?";
		jdbcTemplate.update(sqlState, propertyDefinition.id);
		//Delete levels
		logger.debug("Delete levels");
		removePropertyDefinitionLevel(propertyDefinition.id);
		//Delete property_definition
		super.remove(propertyDefinition);
	}

}
