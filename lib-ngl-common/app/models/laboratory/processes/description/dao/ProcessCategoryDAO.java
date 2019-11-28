package models.laboratory.processes.description.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Repository;

import models.laboratory.processes.description.ProcessCategory;
import models.utils.dao.AbstractDAODefault;
import models.utils.dao.DAOException;

@Repository
public class ProcessCategoryDAO extends AbstractDAODefault<ProcessCategory> {

	public ProcessCategoryDAO() {
		super("process_category", ProcessCategory.class, true);
	}

	//overload to delete "order by" in sql query
	@SuppressWarnings("deprecation")
	@Override
	public List<ProcessCategory> findAll() throws DAOException {
		try {
			String sql = getSqlCommon();
			//Logger.debug(sql);
			BeanPropertyRowMapper<ProcessCategory> mapper = new BeanPropertyRowMapper<>(entityClass);
			return this.jdbcTemplate.query(sql, mapper);
		} catch (DataAccessException e) {
			return new ArrayList<>();
		}
	}
	
	@Override
	protected List<String> getColumns() {
		return enumColumns;
	}

	public ProcessCategory getByCode(String code) throws DAOException {
		ProcessCategory pc = findByCode(code);
		if (pc == null)
			throw new DAOException("process category '" + code + "' does not exist");
		return pc;
	}
	
}
