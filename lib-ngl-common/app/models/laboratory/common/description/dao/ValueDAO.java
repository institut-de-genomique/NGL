package models.laboratory.common.description.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Repository;

import models.laboratory.common.description.Value;
import models.utils.dao.AbstractDAODefault;
import models.utils.dao.DAOException;
import models.utils.dao.DAOHelpers;

@Repository
public class ValueDAO extends AbstractDAODefault<Value> {

	private static final play.Logger.ALogger logger = play.Logger.of(ValueDAO.class);
	
	protected ValueDAO() {
		super("value", Value.class,true);
	}

	@SuppressWarnings("deprecation")
	public List<Value> findByPropertyDefinition(long idPropertyDefinition) {
		String sql = "SELECT id, value, code, name, default_value FROM value WHERE fk_property_definition=?";
		BeanPropertyRowMapper<Value> mapper = new BeanPropertyRowMapper<>(Value.class);
		return this.jdbcTemplate.query(sql, mapper, idPropertyDefinition);
	}

	public Value save(Value value, long idPropertyDefinition) {
		logger.debug("save - value:{}, code:{}, name:{}, default:{}", value.value, value.code, value.name, value.defaultValue);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("value", value.value); // GA: must be removed
		parameters.put("code",  value.code);
		parameters.put("name",  value.name);
        parameters.put("default_value", value.defaultValue);
        parameters.put("fk_property_definition", idPropertyDefinition);
        Long newId = (Long) jdbcInsert.executeAndReturnKey(parameters);
        value.id = newId;
		return value;
	}

	@SuppressWarnings("deprecation")
	public void update(Value value, long idPropertyDefinition) {
		String sql = "UPDATE value SET value=?, code=?, name=?, default_value=? WHERE id=? AND fk_property_definition=?";
		jdbcTemplate.update(sql,value.code, value.value, value.code, value.name, value.defaultValue, value.id, idPropertyDefinition);
	}

	@Override
	public Value findByCode(String code) throws DAOException {
		throw new UnsupportedOperationException("Value does not have a code");		
	}
	
	@Override
	public List<Value> findAll() throws DAOException {
		throw new UnsupportedOperationException("Value can be listed");
		
	}
	
	

	@SuppressWarnings("deprecation")
	public List<Value> findUnique(String propertyDefinitionCode) {
		String sql =  
				"select distinct v.value, v.code, v.name "
				+"from  value v "
			    +"inner join property_definition pd on pd.id = v.fk_property_definition "
				+"inner join common_info_type cit on cit.id = pd.fk_common_info_type "
				+DAOHelpers.getCommonInfoTypeSQLForInstitute("cit")
			    +"where pd.code = ? "
			    +"order by v.name";
		BeanPropertyRowMapper<Value> mapper = new BeanPropertyRowMapper<>(Value.class);
		return this.jdbcTemplate.query(sql, mapper, propertyDefinitionCode);		
	}

}
