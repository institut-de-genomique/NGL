package models.laboratory.experiment.description.dao;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Repository;

import models.laboratory.experiment.description.ExperimentCategory;
import models.utils.dao.AbstractDAODefault;

@Repository
public class ExperimentCategoryDAO extends AbstractDAODefault<ExperimentCategory> {

	public ExperimentCategoryDAO() {
		super("experiment_category",ExperimentCategory.class,true);
	}
	
	@SuppressWarnings("deprecation")
	public List<ExperimentCategory> findByProcessTypeCode(String processTypeCode) {
		try {
			String sql = "SELECT DISTINCT(ec.name), ec.code " +
					"FROM experiment_category ec, experiment_type et, process_type pt inner join common_info_type cit on fk_common_info_type=cit.id, process_experiment_type pet " +
					"WHERE pt.id=pet.fk_process_type AND et.id=pet.fk_experiment_type AND ec.id=et.fk_experiment_category AND cit.code=?";
			BeanPropertyRowMapper<ExperimentCategory> mapper = new BeanPropertyRowMapper<>(entityClass);
//			return this.jdbcTemplate.query(sql, mapper, processTypeCode);
			return jdbcTemplate.query(sql, mapper, processTypeCode);
		} catch (DataAccessException e) {
			return null;
		}
	}
	
	public boolean hasOutputContainerUsed() {
		return false;
	}

	
	@Override
	protected List<String> getColumns() {
		return enumColumns;
	}
}
