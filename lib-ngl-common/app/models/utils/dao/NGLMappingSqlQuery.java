package models.utils.dao;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.MappingSqlQuery;

public abstract class NGLMappingSqlQuery<T> extends MappingSqlQuery<T> {

	public NGLMappingSqlQuery(DataSource ds, String sql, SqlParameter... sqlParameters) {
		super(ds,sql);
		for (SqlParameter p : sqlParameters)
			if (p != null)
				declareParameter(p);
		compile();
	}

}
