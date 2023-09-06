package models.laboratory.common.description.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Repository;

import models.laboratory.common.description.Institute;
import models.utils.dao.AbstractDAOMapping;
import models.utils.dao.DAOException;

@Repository
public class InstituteDAO extends AbstractDAOMapping<Institute> {

//	private static final play.Logger.ALogger logger = play.Logger.of(InstituteDAO.class);
	
	protected InstituteDAO() {
		super("institute", Institute.class, InstituteMappingQuery.factory,
				"SELECT t.id, t.name, t.code FROM institute as t ",
				true);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void remove(Institute institute) throws DAOException {
		// Remove list institute for common_info_type
		String sql = "DELETE FROM common_info_type_institute WHERE fk_institute=?";
		jdbcTemplate.update(sql, institute.id);
		
		sql = "DELETE FROM instrument_institute WHERE fk_institute=?";
		jdbcTemplate.update(sql, institute.id);
			
		//remove institute itself
		super.remove(institute);
	}

	@Override
	public long save(Institute institute) throws DAOException {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("code", institute.code);
		parameters.put("name", institute.name);

//		Long newId = (Long) jdbcInsert.executeAndReturnKey(parameters);
		Long newId = jdbcInsert.executeAndReturnKey(parameters).longValue();
		institute.id = newId;
		return institute.id;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void update(Institute institute) throws DAOException {
		String sql = "UPDATE institute SET code=?, name=? WHERE id=?";
		jdbcTemplate.update(sql, institute.code, institute.name);
	}
	
	@SuppressWarnings("deprecation")
	public List<Institute> findByCommonInfoType(long idCommonInfoType) {
		String sql = "SELECT i.id, i.name, i.code "+
				"FROM institute i "+
				"JOIN common_info_type_institute ci ON ci.fk_institute= i.id "+
				"WHERE ci.fk_common_info_type=?";
		BeanPropertyRowMapper<Institute> mapper = new BeanPropertyRowMapper<>(Institute.class);
		return jdbcTemplate.query(sql, mapper, idCommonInfoType);
	}

}

