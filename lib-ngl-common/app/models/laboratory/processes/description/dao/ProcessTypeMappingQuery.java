package models.laboratory.processes.description.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

import models.laboratory.common.description.CommonInfoType;
import models.laboratory.common.description.dao.CommonInfoTypeDAO;
import models.laboratory.experiment.description.dao.ExperimentTypeDAO;
import models.laboratory.processes.description.ProcessCategory;
import models.laboratory.processes.description.ProcessType;
import models.utils.dao.DAOException;
import models.utils.dao.MappingSqlQueryFactory;
import models.utils.dao.NGLMappingSqlQuery;
import play.api.modules.spring.Spring;

//public class ProcessTypeMappingQuery extends MappingSqlQuery<ProcessType> {
public class ProcessTypeMappingQuery extends NGLMappingSqlQuery<ProcessType> {

	public static final MappingSqlQueryFactory<ProcessType> factory = ProcessTypeMappingQuery::new;
	
	boolean lightVersion = false;
	
//	public ProcessTypeMappingQuery()
//	{
//		super();
//	}
	
//	public ProcessTypeMappingQuery(DataSource ds, String sql, SqlParameter sqlParameter) {
//		super(ds,sql);
//		if (sqlParameter != null)
////			super.declareParameter(sqlParameter);
//			declareParameter(sqlParameter);
//		compile();
//	}
	
	public ProcessTypeMappingQuery(DataSource ds, String sql, SqlParameter... sqlParameters) {
		super(ds,sql,sqlParameters);
	}

	@Override
	protected ProcessType mapRow(ResultSet rs, int rowNum) throws SQLException {
		try {
			ProcessType processType = new ProcessType();
			processType.id = rs.getLong("id");
			long idCommonInfoType = rs.getLong("fk_common_info_type");
			long idProjectCategory = rs.getLong("fk_process_category");
			long idVoidExpType = rs.getLong("fk_void_experiment_type");
			long idFirstExpType = rs.getLong("fk_first_experiment_type");
			long idLastExpType = rs.getLong("fk_last_experiment_type");
			//Get commonInfoType
			CommonInfoTypeDAO commonInfoTypeDAO = Spring.getBeanOfType(CommonInfoTypeDAO.class);
			CommonInfoType commonInfoType = commonInfoTypeDAO.findById(idCommonInfoType);
			processType.setCommonInfoType(commonInfoType);
			//Get category
			ProcessCategoryDAO processCategoryDAO = Spring.getBeanOfType(ProcessCategoryDAO.class);
//			ProcessCategory processCategory=(ProcessCategory) processCategoryDAO.findById(idProjectCategory);
			ProcessCategory processCategory = processCategoryDAO.findById(idProjectCategory);
			processType.category = processCategory;
			//Get list experimentType
			if(!lightVersion){
				ExperimentTypeDAO expTypeDAO = Spring.getBeanOfType(ExperimentTypeDAO.class);
				//List<ExperimentType> experimentTypes = expTypeDAO.findByProcessTypeId(processType.id);
				//processType.experimentTypes=experimentTypes;
				//Get voidExperimentType
				processType.voidExperimentType=expTypeDAO.findById(idVoidExpType);
				//Get voidExperimentType
				processType.firstExperimentType=expTypeDAO.findById(idFirstExpType);
				//Get voidExperimentType
				processType.lastExperimentType=expTypeDAO.findById(idLastExpType);
				
				processType.experimentTypes = Spring.getBeanOfType(ProcessTypeDAO.class).getProcessExperimentType(processType.id);
			}
			return processType;
		} catch (DAOException e) {
			throw new SQLException(e);
		}
	}

}
