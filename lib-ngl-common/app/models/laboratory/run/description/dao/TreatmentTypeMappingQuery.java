package models.laboratory.run.description.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

import models.laboratory.common.description.dao.CommonInfoTypeDAO;
import models.laboratory.run.description.TreatmentType;
import models.utils.dao.DAOException;
import models.utils.dao.MappingSqlQueryFactory;
import models.utils.dao.NGLMappingSqlQuery;
import play.api.modules.spring.Spring;

//public class TreatmentTypeMappingQuery extends MappingSqlQuery<TreatmentType> {
public class TreatmentTypeMappingQuery extends NGLMappingSqlQuery<TreatmentType> {

	public static final MappingSqlQueryFactory<TreatmentType> factory = TreatmentTypeMappingQuery::new;
	
//	// Needed by reflection instanciation.
//	public TreatmentTypeMappingQuery() {
////		super();
//	}
//
//	public TreatmentTypeMappingQuery(DataSource ds, String sql, SqlParameter sqlParameter) {
//		super(ds,sql);
//		if (sqlParameter != null)
////			super.declareParameter(sqlParameter);
//			declareParameter(sqlParameter);
//		compile();
//	}
	
	public TreatmentTypeMappingQuery(DataSource ds, String sql, SqlParameter... sqlParameters) {
		super(ds,sql,sqlParameters);
	}
	
	@Override
	protected TreatmentType mapRow(ResultSet rs, int rowNum) throws SQLException {
		try {	
			TreatmentType treatmentType = new TreatmentType();
			treatmentType.id = rs.getLong("id");
			treatmentType.names = rs.getString("names");  
			treatmentType.displayOrders = rs.getString("display_orders");  
						
			//Get commonInfoType
			long idCommonInfoType = rs.getLong("fk_common_info_type");			
			CommonInfoTypeDAO commonInfoTypeDAO = Spring.getBeanOfType(CommonInfoTypeDAO.class);
			treatmentType.setCommonInfoType(commonInfoTypeDAO.findById(idCommonInfoType));
			
			//Get category
			long idTreatmentCategory = rs.getLong("fk_treatment_category");			
			TreatmentCategoryDAO treatmentCategoryDAO = Spring.getBeanOfType(TreatmentCategoryDAO.class);
//			treatmentType.category = (TreatmentCategory) treatmentCategoryDAO.findById(idTreatmentCategory);
			treatmentType.category = treatmentCategoryDAO.findById(idTreatmentCategory);			
			TreatmentTypeContextDAO treatmentContextDAO =  Spring.getBeanOfType(TreatmentTypeContextDAO.class);
			treatmentType.contexts = treatmentContextDAO.findByTreatmentTypeId(treatmentType.id);
			
			return treatmentType;
		} catch (DAOException e) {
			throw new SQLException(e);
		}
	}

}
