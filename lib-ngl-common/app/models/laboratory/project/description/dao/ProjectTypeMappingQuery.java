package models.laboratory.project.description.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

import models.laboratory.common.description.CommonInfoType;
import models.laboratory.common.description.dao.CommonInfoTypeDAO;
import models.laboratory.project.description.ProjectCategory;
import models.laboratory.project.description.ProjectType;
import models.utils.dao.DAOException;
import models.utils.dao.MappingSqlQueryFactory;
import models.utils.dao.NGLMappingSqlQuery;
import play.api.modules.spring.Spring;

//public class ProjectTypeMappingQuery extends MappingSqlQuery<ProjectType> {
public class ProjectTypeMappingQuery extends NGLMappingSqlQuery<ProjectType> {

	public static final MappingSqlQueryFactory<ProjectType> factory = ProjectTypeMappingQuery::new;
	
//	public ProjectTypeMappingQuery()
//	{
//		super();
//	}

//	public ProjectTypeMappingQuery(DataSource ds, String sql, SqlParameter sqlParameter) {
//		super(ds,sql);
//		if (sqlParameter != null)
////			super.declareParameter(sqlParameter);
//			declareParameter(sqlParameter);
//		compile();
//	}

	public ProjectTypeMappingQuery(DataSource ds, String sql, SqlParameter... sqlParameters) {
		super(ds,sql,sqlParameters);
	}
	
	@Override
	protected ProjectType mapRow(ResultSet rs, int rowNum) throws SQLException {
		try {
			ProjectType projectType = new ProjectType();
			projectType.id = rs.getLong("id");
			long idCommonInfoType = rs.getLong("fk_common_info_type");
			long idProjectCategory = rs.getLong("fk_project_category");
			//Get commonInfoType
			CommonInfoTypeDAO commonInfoTypeDAO = Spring.getBeanOfType(CommonInfoTypeDAO.class);
			CommonInfoType commonInfoType = commonInfoTypeDAO.findById(idCommonInfoType);
			projectType.setCommonInfoType(commonInfoType);
			//Get category
			ProjectCategoryDAO projectCategoryDAO = Spring.getBeanOfType(ProjectCategoryDAO.class);
			ProjectCategory projectCategory = null;
			try {
//				projectCategory = (ProjectCategory) projectCategoryDAO.findById(idProjectCategory);
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
