package models.laboratory.run.description.dao;

import static models.utils.dao.DAOException.daoAssertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

import models.laboratory.common.description.dao.CommonInfoTypeDAO;
import models.laboratory.run.description.ReadSetType;
import models.utils.dao.AbstractDAOCommonInfoType;
import models.utils.dao.DAOException;
import play.api.modules.spring.Spring;

@Repository
public class ReadSetTypeDAO extends AbstractDAOCommonInfoType<ReadSetType>{

	protected ReadSetTypeDAO() {
		super("readset_type", ReadSetType.class, ReadSetTypeMappingQuery.factory, 
				"SELECT distinct c.id, c.fk_common_info_type ", 
						"FROM readset_type as c " + sqlCommonInfoType, false);
	}

	@Override
	public long save(ReadSetType readSetType) throws DAOException {
//		if (readSetType == null)
//			throw new DAOException("ReadSetType is mandatory");
		daoAssertNotNull("readSetType", readSetType);
		
		//Add commonInfoType
		CommonInfoTypeDAO commonInfoTypeDAO = Spring.getBeanOfType(CommonInfoTypeDAO.class);
		readSetType.id = commonInfoTypeDAO.save(readSetType);
		
		//Create new runType
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("id", readSetType.id);
		parameters.put("fk_common_info_type", readSetType.id);
		
		jdbcInsert.execute(parameters);
		
		return readSetType.id;
	}

	@Override
	public void update(ReadSetType readSetType) throws DAOException {
		CommonInfoTypeDAO commonInfoTypeDAO = Spring.getBeanOfType(CommonInfoTypeDAO.class);
		commonInfoTypeDAO.update(readSetType);
	}

	@Override
	public void remove(ReadSetType readSetType) throws DAOException {
		//Remove readSetType
		super.remove(readSetType);
		//Remove commonInfoType
		CommonInfoTypeDAO commonInfoTypeDAO = Spring.getBeanOfType(CommonInfoTypeDAO.class);
		commonInfoTypeDAO.remove(readSetType);
	}
	
}

