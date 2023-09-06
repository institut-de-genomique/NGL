package models.laboratory.instrument.description.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

import models.laboratory.instrument.description.Instrument;
import models.utils.dao.MappingSqlQueryFactory;
import models.utils.dao.NGLMappingSqlQuery;
import play.api.modules.spring.Spring;

//public class InstrumentMappingQuery extends MappingSqlQuery<Instrument> {
public class InstrumentMappingQuery extends NGLMappingSqlQuery<Instrument> {

	public static final MappingSqlQueryFactory<Instrument> factory = InstrumentMappingQuery::new;
	
//	public InstrumentMappingQuery() {
//		super();
//	}
	
//	public InstrumentMappingQuery(DataSource ds, String sql,SqlParameter sqlParameter) {
//		super(ds,sql);
//		if (sqlParameter != null)
////			super.declareParameter(sqlParameter);
//			declareParameter(sqlParameter);
//		compile();
//	}

	public InstrumentMappingQuery(DataSource ds, String sql, SqlParameter... sqlParameters) {
		super(ds,sql,sqlParameters);
	}
	
	@Override
	protected Instrument mapRow(ResultSet rs, int rowNum) throws SQLException {
		Instrument instrument = new Instrument();
		instrument.id        = rs.getLong("id");
		instrument.code      = rs.getString("code");
		instrument.name      = rs.getString("name");
		instrument.shortName = rs.getString("short_name");
		instrument.path      = rs.getString("path");
		instrument.active    = rs.getBoolean("active");
		
		long idType = rs.getLong("fk_instrument_used_type");
		if (idType != 0) {
			Map<String, Object> result = Spring.getBeanOfType(InstrumentUsedTypeDAO.class).findTypeCodeAndCatCode(idType);
			
			instrument.typeCode = (String)result.get("typeCode");
			instrument.categoryCode = (String)result.get("catCode");
		}
		return instrument;
	}

}
