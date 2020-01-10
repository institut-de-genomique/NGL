package models.laboratory.common.description.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

import models.laboratory.common.description.MeasureCategory;
import models.laboratory.common.description.MeasureUnit;
import models.utils.dao.DAOException;
import models.utils.dao.MappingSqlQueryFactory;
import models.utils.dao.NGLMappingSqlQuery;
import play.api.modules.spring.Spring;

//public class MeasureUnitMappingQuery extends MappingSqlQuery<MeasureUnit> {
public class MeasureUnitMappingQuery extends NGLMappingSqlQuery<MeasureUnit> {

	public static final MappingSqlQueryFactory<MeasureUnit> factory = (d,s,ps) -> new MeasureUnitMappingQuery(d,s,ps);
	
//	public MeasureUnitMappingQuery()
//	{
//		super();
//	}

//	public MeasureUnitMappingQuery(DataSource ds, String sql,SqlParameter sqlParameter) {
//		super(ds,sql);
//		if (sqlParameter != null)
////			super.declareParameter(sqlParameter);
//			declareParameter(sqlParameter);
//		compile();
//	}

	public MeasureUnitMappingQuery(DataSource ds, String sql, SqlParameter... sqlParameters) {
		super(ds,sql,sqlParameters);
	}
	
	@Override
	protected MeasureUnit mapRow(ResultSet rs, int rowNum) throws SQLException {
		MeasureUnit measureValue = new MeasureUnit();
		measureValue.id          = rs.getLong("id");
		measureValue.value       = rs.getString("value");
		measureValue.defaultUnit = rs.getBoolean("default_unit");
		measureValue.code        = rs.getString("code");
		long idCategory = rs.getLong("fk_measure_category");
		
		MeasureCategoryDAO measureCategoryDAO = Spring.getBeanOfType(MeasureCategoryDAO.class);
		MeasureCategory measureCategory=null;
		try {
			measureCategory = measureCategoryDAO.findById(idCategory);
		} catch (DAOException e) {
			throw new SQLException(e);
		}
		measureValue.category=measureCategory;
		return measureValue;
	}

}
