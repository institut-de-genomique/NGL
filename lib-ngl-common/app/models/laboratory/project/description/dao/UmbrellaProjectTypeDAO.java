package models.laboratory.project.description.dao;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

import models.laboratory.common.description.dao.CommonInfoTypeDAO;
import models.laboratory.project.description.UmbrellaProjectType;
import models.utils.dao.AbstractDAOCommonInfoType;
import models.utils.dao.DAOException;
import play.api.modules.spring.Spring;

@Repository
public class UmbrellaProjectTypeDAO extends AbstractDAOCommonInfoType<UmbrellaProjectType> {

	protected UmbrellaProjectTypeDAO() {
		super("umbrella_project_type", UmbrellaProjectType.class, UmbrellaProjectTypeMappingQuery.factory, 
				"SELECT distinct c.id, c.fk_common_info_type, c.fk_umbrella_project_category ",
						"FROM umbrella_project_type as c "+sqlCommonInfoType, false);
	}

	@Override
	public long save(UmbrellaProjectType projectType) throws DAOException {
		
		if (projectType == null) {
			throw new DAOException("UmbrellaProjectType is mandatory");
		}
		//Check if category exist
		if(projectType.category == null || projectType.category.id == null){
			throw new DAOException("UmbrellaProjectCategory is not present !!");
		}
		
		//Add commonInfoType
		CommonInfoTypeDAO commonInfoTypeDAO = Spring.getBeanOfType(CommonInfoTypeDAO.class);
		projectType.id = commonInfoTypeDAO.save(projectType);
		
		//Create new projectType
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("id", projectType.id);
		parameters.put("fk_common_info_type", projectType.id);
		parameters.put("fk_umbrella_project_category", projectType.category.id);
		jdbcInsert.execute(parameters);
		return projectType.id;
	}

	@Override
	public void update(UmbrellaProjectType projectType) throws DAOException
	{
		CommonInfoTypeDAO commonInfoTypeDAO = Spring.getBeanOfType(CommonInfoTypeDAO.class);
		commonInfoTypeDAO.update(projectType);
	}

	@Override
	public void remove(UmbrellaProjectType projectType) throws DAOException {
		//Remove ProjectType
		super.remove(projectType);
		//Remove commonInfoType
		CommonInfoTypeDAO commonInfoTypeDAO = Spring.getBeanOfType(CommonInfoTypeDAO.class);
		commonInfoTypeDAO.remove(projectType);
	}
}
