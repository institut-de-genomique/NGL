package models.laboratory.run.description.dao;

import java.util.List;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Repository;

import models.laboratory.run.description.RunCategory;
import models.utils.dao.AbstractDAODefault;
import models.utils.dao.DAOException;

@Repository
public class RunCategoryDAO extends AbstractDAODefault<RunCategory> {

	public RunCategoryDAO() {
		super("run_category",RunCategory.class,true);
	}

	@SuppressWarnings("deprecation")
	public RunCategory findByTypeCode(String typeCode) throws DAOException {		
		String sql = getSqlCommon()+" inner join run_type r on r.fk_run_category = t.id "
				+" inner join common_info_type c on c.id = r.id"
				+" where c.code = ?";
		
		BeanPropertyRowMapper<RunCategory> mapper = new BeanPropertyRowMapper<>(entityClass);
		return jdbcTemplate.queryForObject(sql, mapper, typeCode);		
	}

	@Override
	protected List<String> getColumns() {
		return enumColumns;
	}

}



