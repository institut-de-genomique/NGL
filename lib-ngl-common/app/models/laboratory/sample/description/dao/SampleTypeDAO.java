package models.laboratory.sample.description.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.asm.Type;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.stereotype.Repository;

import models.laboratory.common.description.dao.CommonInfoTypeDAO;
import models.laboratory.sample.description.SampleType;
import models.utils.dao.AbstractDAOCommonInfoType;
import models.utils.dao.DAOException;
import play.api.modules.spring.Spring;

@Repository
public class SampleTypeDAO extends AbstractDAOCommonInfoType<SampleType> {

//	protected SampleTypeDAO() {
//		super("sample_type", SampleType.class, SampleTypeMappingQuery.class, 
//				"SELECT distinct c.id, c.fk_common_info_type, c.fk_sample_category ",
//				"FROM sample_type as c "+sqlCommonInfoType, false);
//	}
	protected SampleTypeDAO() {
		super("sample_type", SampleType.class, SampleTypeMappingQuery.factory, 
				"SELECT distinct c.id, c.fk_common_info_type, c.fk_sample_category ",
				"FROM sample_type as c "+sqlCommonInfoType, false);
	}

	@Override
	public long save(SampleType sampleType) throws DAOException	{
		if (sampleType == null)
			throw new DAOException("sampleType is mandatory");
		//Check if category exist
		if (sampleType.category == null || sampleType.category.id == null)
			throw new DAOException("SampleCategory is not present !!");
		
		//Add commonInfoType
		CommonInfoTypeDAO commonInfoTypeDAO = Spring.getBeanOfType(CommonInfoTypeDAO.class);
		sampleType.id = commonInfoTypeDAO.save(sampleType);
		//Create sampleType 
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("id", sampleType.id);
		parameters.put("fk_common_info_type", sampleType.id);
		parameters.put("fk_sample_category", sampleType.category.id);
		jdbcInsert.execute(parameters);
		return sampleType.id;
	}

	@Override
	public void update(SampleType sampleType) throws DAOException
	{
		CommonInfoTypeDAO commonInfoTypeDAO = Spring.getBeanOfType(CommonInfoTypeDAO.class);
		commonInfoTypeDAO.update(sampleType);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void remove(SampleType sampleType) throws DAOException {
		//remove from abstractExperiment experiment_type_sample_type
		String sqlExp = "DELETE FROM experiment_type_sample_type WHERE fk_sample_type=?";
		jdbcTemplate.update(sqlExp, sampleType.id);
		//Remove sampleType
		super.remove(sampleType);
		//Remove commonInfotype
		CommonInfoTypeDAO commonInfoTypeDAO = Spring.getBeanOfType(CommonInfoTypeDAO.class);
		commonInfoTypeDAO.remove(sampleType);
	}
	
	public List<SampleType> findByExperimentId(long id) {
		String sql=sqlCommon+
				"JOIN experiment_type_sample_type as cit ON fk_sample_type=c.id " +
				"WHERE cit.fk_experiment_type = ?";
		SampleTypeMappingQuery sampleTypeMappingQuery = new SampleTypeMappingQuery(dataSource, sql,new SqlParameter("id", Type.LONG));
		return sampleTypeMappingQuery.execute(id);
	}
	
}
