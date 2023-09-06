package models.laboratory.run.description.dao;

import static models.utils.dao.DAOException.daoAssertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

import models.laboratory.common.description.dao.CommonInfoTypeDAO;
import models.laboratory.run.description.AnalysisType;
import models.utils.dao.AbstractDAOCommonInfoType;
import models.utils.dao.DAOException;
import play.api.modules.spring.Spring;

@Repository
public class AnalysisTypeDAO extends AbstractDAOCommonInfoType<AnalysisType> {
	
	protected AnalysisTypeDAO() {
		super("analysis_type", AnalysisType.class, AnalysisTypeMappingQuery.factory, 
				"SELECT distinct c.id, c.fk_common_info_type ", 
						"FROM analysis_type as c " + sqlCommonInfoType, false);
	}
	
	@Override
	public long save(AnalysisType analysisType) throws DAOException {
//		if (analysisType == null)
//			throw new DAOException("AnalysisType is mandatory");
		daoAssertNotNull("analysisType", analysisType);
		//Add commonInfoType
		CommonInfoTypeDAO commonInfoTypeDAO = Spring.getBeanOfType(CommonInfoTypeDAO.class);
		analysisType.id = commonInfoTypeDAO.save(analysisType);
		
		//Create new runType
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("id", analysisType.id);
		parameters.put("fk_common_info_type", analysisType.id);
		
		jdbcInsert.execute(parameters);
		
		return analysisType.id;
	}

	@Override
	public void update(AnalysisType analysisType) throws DAOException {
		CommonInfoTypeDAO commonInfoTypeDAO = Spring.getBeanOfType(CommonInfoTypeDAO.class);
		commonInfoTypeDAO.update(analysisType);
	}

	@Override
	public void remove(AnalysisType analysisType) throws DAOException {
		//Remove readSetType
		super.remove(analysisType);
		//Remove commonInfoType
		CommonInfoTypeDAO commonInfoTypeDAO = Spring.getBeanOfType(CommonInfoTypeDAO.class);
		commonInfoTypeDAO.remove(analysisType);
	}
	
}

