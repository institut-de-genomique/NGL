package models.laboratory.common.description.dao;

import java.util.List;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Repository;

import models.laboratory.common.description.Level;
import models.utils.dao.AbstractDAODefault;

@Repository
public class LevelDAO extends AbstractDAODefault<Level> {

	protected LevelDAO() {
		super("level", Level.class, true);
	}

	@SuppressWarnings("deprecation")
	public List<Level> findByPropertyDefinitionID(Long id) {
		
		String sql = "SELECT le.id, le.code, le.name "+
				"FROM level as le "+
				"INNER JOIN property_definition_level as p ON p.fk_level=le.id " +
				"WHERE p.fk_property_definition = ? ";
		
		BeanPropertyRowMapper<Level> mapper = new BeanPropertyRowMapper<>(Level.class);
		return jdbcTemplate.query(sql, mapper, id);		
	}
	
	@Override
	protected List<String> getColumns() {
		return enumColumns;
	}

}
