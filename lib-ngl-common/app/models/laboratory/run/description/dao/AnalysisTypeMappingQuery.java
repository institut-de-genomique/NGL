package models.laboratory.run.description.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

import models.laboratory.common.description.CommonInfoType;
import models.laboratory.common.description.dao.CommonInfoTypeDAO;
import models.laboratory.run.description.AnalysisType;
import models.utils.dao.DAOException;
import models.utils.dao.MappingSqlQueryFactory;
import models.utils.dao.NGLMappingSqlQuery;
import play.api.modules.spring.Spring;

//public class AnalysisTypeMappingQuery extends MappingSqlQuery<AnalysisType> {
public class AnalysisTypeMappingQuery extends NGLMappingSqlQuery<AnalysisType> {

	public static final MappingSqlQueryFactory<AnalysisType> factory = AnalysisTypeMappingQuery::new;
	
//	// This is needed by reflection instanciation.  
//	public AnalysisTypeMappingQuery() {
////		super();
//	}

//	public AnalysisTypeMappingQuery(DataSource ds, String sql, SqlParameter sqlParameter) {
//	private AnalysisTypeMappingQuery(DataSource ds, String sql, SqlParameter sqlParameter) {
//		super(ds,sql);
//		if (sqlParameter != null)
////			super.declareParameter(sqlParameter);
//			declareParameter(sqlParameter);
//		compile();
//	}

	public AnalysisTypeMappingQuery(DataSource ds, String sql, SqlParameter... sqlParameters) {
		super(ds,sql,sqlParameters);
	}
	
	@Override
	protected AnalysisType mapRow(ResultSet rs, int rowNum) throws SQLException {
		AnalysisType analysisType = new AnalysisType();

		analysisType.id = rs.getLong("id");

		long idCommonInfoType = rs.getLong("fk_common_info_type");
		//long idReadSetCategory = rs.getLong("fk_readset_category");

		//Get commonInfoType
		CommonInfoTypeDAO commonInfoTypeDAO = Spring.getBeanOfType(CommonInfoTypeDAO.class);
		CommonInfoType commonInfoType = null;
		try {
//				 commonInfoType = (CommonInfoType) commonInfoTypeDAO.findById(idCommonInfoType);
			commonInfoType = commonInfoTypeDAO.findById(idCommonInfoType);
		} catch (DAOException e) {
			throw new SQLException(e);
		}
		//Set commonInfoType
		analysisType.setCommonInfoType(commonInfoType);
		//Get category
		return analysisType;
	}

}
