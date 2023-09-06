package models.laboratory.common.description.dao;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Repository;

import models.laboratory.common.description.CodeLabel;

@SuppressWarnings("deprecation")
@Repository
public class CodeLabelDAO {

	protected DataSource dataSource;
	protected SimpleJdbcTemplate jdbcTemplate;
	
	@Autowired
	@Qualifier("ngl")
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		jdbcTemplate = new SimpleJdbcTemplate(dataSource);   		
	}
	
	public List<CodeLabel> findAll(){
		String sql = "select 'state' as table_name, code, name as label from state "+
						"union all "+
						"select 'type' as table_name, code, name as label from common_info_type "+
						"union all "+
						"select 'instrument' as table_name, code, name as label from instrument "+
						"union all "+
						"select 'instrument_cat' as table_name, code, name as label from instrument_category "+
						"union all "+
						"select 'container_support_cat' as table_name, code, name as label from container_support_category "+
						"union all "+
						"select 'umbrella_project_cat' as table_name, code, name as label from umbrella_project_category "+
						"union all "+
						"select 'project_cat' as table_name, code, name as label from project_category "+
						"union all "+
						"select 'process_cat' as table_name, code, name as label from process_category "+
						"union all "+						
						"select distinct 'value' as table_name, code, name as label from value "+
						"union all "+
						"select distinct concat('value','.',+pd.code) as table_name, v.code, v.name as label "
								+ "from value v inner join property_definition pd on pd.id = v.fk_property_definition "+
						"union all "+
						"select distinct 'property_definition' as table_name, code, name as label from property_definition "+
						"union all "+
						"select distinct 'sample_cat' as table_name, code, name as label from sample_category "+
						"union all "+
						"select distinct 'experiment_cat' as table_name, code, name as label from experiment_category "
						;
		BeanPropertyRowMapper<CodeLabel> mapper = new BeanPropertyRowMapper<>(CodeLabel.class);
		return jdbcTemplate.query(sql, mapper);
	}
	
}
