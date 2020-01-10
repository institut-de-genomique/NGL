package models.laboratory.run.description.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

import models.laboratory.common.description.CommonInfoType;
import models.laboratory.common.description.dao.CommonInfoTypeDAO;
import models.laboratory.run.description.ReadSetType;
import models.utils.dao.DAOException;
import models.utils.dao.MappingSqlQueryFactory;
import models.utils.dao.NGLMappingSqlQuery;
import play.api.modules.spring.Spring;

//public class ReadSetTypeMappingQuery extends MappingSqlQuery<ReadSetType> {
public class ReadSetTypeMappingQuery extends NGLMappingSqlQuery<ReadSetType> {

	public static final MappingSqlQueryFactory<ReadSetType> factory = ReadSetTypeMappingQuery::new;
	
//	// Needed by reflection instanciation.
//	public ReadSetTypeMappingQuery() {
//		super();
//	}

//	public ReadSetTypeMappingQuery(DataSource ds, String sql, SqlParameter sqlParameter) {
//		super(ds,sql);
//		if (sqlParameter != null)
////			super.declareParameter(sqlParameter);
//			declareParameter(sqlParameter);
//		compile();
//	}
	public ReadSetTypeMappingQuery(DataSource ds, String sql, SqlParameter... sqlParameters) {
		super(ds,sql,sqlParameters);
	}
	
	@Override
	protected ReadSetType mapRow(ResultSet rs, int rowNum) throws SQLException {
		ReadSetType readSetType = new ReadSetType();

		readSetType.id = rs.getLong("id");

		long idCommonInfoType = rs.getLong("fk_common_info_type");
		//long idReadSetCategory = rs.getLong("fk_readset_category");

		//Get commonInfoType
		CommonInfoTypeDAO commonInfoTypeDAO = Spring.getBeanOfType(CommonInfoTypeDAO.class);
		CommonInfoType commonInfoType = null;
		try {
//			commonInfoType = (CommonInfoType) commonInfoTypeDAO.findById(idCommonInfoType);
			commonInfoType = commonInfoTypeDAO.findById(idCommonInfoType);
		} catch (DAOException e) {
			throw new SQLException(e);
		}
		//Set commonInfoType
		readSetType.setCommonInfoType(commonInfoType);
		//Get category
		/*
			ReadSetCategoryDAO readSetCategoryDAO = Spring.getBeanOfType(ReadSetCategoryDAO.class);
			ReadSetCategory readSetCategory=null;
			try {
				readSetCategory = (ReadSetCategory) readSetCategoryDAO.findById(idReadSetCategory);
			} catch (DAOException e) {
				throw new SQLException(e);
			}
			//Set category
			readSetType.category = readSetCategory;
		 */
		return readSetType;
	}

}
