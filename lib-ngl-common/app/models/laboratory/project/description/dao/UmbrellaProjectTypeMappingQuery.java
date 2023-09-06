package models.laboratory.project.description.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

import models.laboratory.common.description.CommonInfoType;
import models.laboratory.common.description.dao.CommonInfoTypeDAO;
import models.laboratory.project.description.UmbrellaProjectCategory;
import models.laboratory.project.description.UmbrellaProjectType;
import models.utils.dao.DAOException;
import models.utils.dao.MappingSqlQueryFactory;
import models.utils.dao.NGLMappingSqlQuery;
import play.api.modules.spring.Spring;

public class UmbrellaProjectTypeMappingQuery extends NGLMappingSqlQuery<UmbrellaProjectType> {

	public static final MappingSqlQueryFactory<UmbrellaProjectType> factory = UmbrellaProjectTypeMappingQuery::new;

	public UmbrellaProjectTypeMappingQuery(DataSource ds, String sql, SqlParameter... sqlParameters) {
		super(ds,sql,sqlParameters);
	}
	
	@Override
	protected UmbrellaProjectType mapRow(ResultSet rs, int rowNum) throws SQLException {
		try {
			UmbrellaProjectType projectType = new UmbrellaProjectType();
			projectType.id = rs.getLong("id");
			long idCommonInfoType = rs.getLong("fk_common_info_type");
			long idProjectCategory = rs.getLong("fk_umbrella_project_category");
			//Get commonInfoType
			CommonInfoTypeDAO commonInfoTypeDAO = Spring.getBeanOfType(CommonInfoTypeDAO.class);
			CommonInfoType commonInfoType = commonInfoTypeDAO.findById(idCommonInfoType);
			projectType.setCommonInfoType(commonInfoType);
			//Get category
			UmbrellaProjectCategoryDAO projectCategoryDAO = Spring.getBeanOfType(UmbrellaProjectCategoryDAO.class);
			UmbrellaProjectCategory projectCategory = null;
			try {
				projectCategory = projectCategoryDAO.findById(idProjectCategory);
			} catch (DAOException e) {
				throw new SQLException(e);
			}
			projectType.category = projectCategory;
			return projectType;
		} catch (DAOException e) {
			throw new SQLException(e);
		}
	}

}
