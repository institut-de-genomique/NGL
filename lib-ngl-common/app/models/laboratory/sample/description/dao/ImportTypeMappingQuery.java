package models.laboratory.sample.description.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

import models.laboratory.common.description.CommonInfoType;
import models.laboratory.common.description.dao.CommonInfoTypeDAO;
import models.laboratory.sample.description.ImportCategory;
import models.laboratory.sample.description.ImportType;
import models.utils.dao.DAOException;
import models.utils.dao.MappingSqlQueryFactory;
import models.utils.dao.NGLMappingSqlQuery;
import play.api.modules.spring.Spring;

//public class ImportTypeMappingQuery extends MappingSqlQuery<ImportType>{
public class ImportTypeMappingQuery extends NGLMappingSqlQuery<ImportType> {

	public static final MappingSqlQueryFactory<ImportType> factory = ImportTypeMappingQuery::new;
	
//	public ImportTypeMappingQuery()	{
//		super();
//	}
	
//	public ImportTypeMappingQuery(DataSource ds, String sql, SqlParameter sqlParameter)	{
//		super(ds,sql);
//		if (sqlParameter != null)
////			super.declareParameter(sqlParameter);
//			declareParameter(sqlParameter);
//		compile();
//	}

	public ImportTypeMappingQuery(DataSource ds, String sql, SqlParameter... sqlParameters)	{
		super(ds,sql,sqlParameters);
	}

	@Override
	protected ImportType mapRow(ResultSet rs, int rowNum) throws SQLException {
		try {
			ImportType importType = new ImportType();
			importType.id = rs.getLong("id");
			long idCommonInfoType = rs.getLong("fk_common_info_type");
			long idImportCategory = rs.getLong("fk_import_category");
			//Get commonInfoType
			CommonInfoTypeDAO commonInfoTypeDAO = Spring.getBeanOfType(CommonInfoTypeDAO.class);
			CommonInfoType commonInfoType = commonInfoTypeDAO.findById(idCommonInfoType);
			importType.setCommonInfoType(commonInfoType);
			//Get sampleCategory
			ImportCategoryDAO importCategoryDAO = Spring.getBeanOfType(ImportCategoryDAO.class);
			ImportCategory importCategory=null;
			try {
				importCategory =  importCategoryDAO.findById(idImportCategory);
			} catch (DAOException e) {
				throw new SQLException(e);
			}
			importType.category = importCategory;
			return importType;
		} catch (DAOException e) {
			throw new SQLException(e);
		}
	}

}
