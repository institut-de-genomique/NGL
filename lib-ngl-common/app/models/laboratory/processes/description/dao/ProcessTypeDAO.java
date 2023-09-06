package models.laboratory.processes.description.dao;

import static models.utils.dao.DAOException.daoAssertNotNull;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.stereotype.Repository;

import models.laboratory.common.description.dao.CommonInfoTypeDAO;
import models.laboratory.processes.description.ProcessExperimentType;
import models.laboratory.processes.description.ProcessType;
import models.utils.dao.AbstractDAOCommonInfoType;
import models.utils.dao.DAOException;
import play.api.modules.spring.Spring;

@Repository
public class ProcessTypeDAO extends AbstractDAOCommonInfoType<ProcessType> {

	private static final play.Logger.ALogger logger = play.Logger.of(ProcessTypeDAO.class);
	
//	protected ProcessTypeDAO() {
//		super("process_type", ProcessType.class, ProcessTypeMappingQuery.class, 
//				"SELECT distinct c.id, c.fk_common_info_type, c.fk_process_category, c.fk_void_experiment_type, c.fk_first_experiment_type, c.fk_last_experiment_type, t.active as active ",
//				"FROM process_type as c  "+sqlCommonInfoType, false);
//	}
	protected ProcessTypeDAO() {
		super("process_type", ProcessType.class, ProcessTypeMappingQuery.factory, 
				"SELECT distinct c.id, c.fk_common_info_type, c.fk_process_category, c.fk_void_experiment_type, c.fk_first_experiment_type, c.fk_last_experiment_type, t.active as active ",
				"FROM process_type as c  "+sqlCommonInfoType, false);
	}

	public List<ProcessType> findByProcessCategoryCodes(String...processCategoryCodes) {	
		try {
			String sql = sqlCommonSelect + ",t.name, t.code " + sqlCommonFrom + ", process_category as pc WHERE c.fk_process_category=pc.id "
						+"AND pc.code in ("+listToParameters(Arrays.asList(processCategoryCodes))+") order by display_order";
			
			return initializeMapping(sql, listToSqlParameters(Arrays.asList(processCategoryCodes) ,"pc.code", Types.VARCHAR)).execute(processCategoryCodes);
		} catch (DataAccessException e) {
			logger.warn(e.getMessage());
			return null;
		}
	}
	
	public List<ProcessType> findByPropertyDefinitions(List<String> propertyDefinitionCodes, List<String> processCategoryCodes, List<String> codes) {	
		try {
			StringBuilder sql = new StringBuilder(sqlCommonSelect)
					.append(",t.name, t.code ")
					.append(sqlCommonFrom)
					.append("INNER JOIN property_definition as pd ON t.id=pd.fk_common_info_type ");
			StringBuilder sqlSelect = new StringBuilder("WHERE pd.code in (").append(listToParameters(propertyDefinitionCodes)).append(") ");
			List<SqlParameter> sqlParameters = new ArrayList<>(propertyDefinitionCodes.size());
			Collections.addAll(sqlParameters, listToSqlParameters(propertyDefinitionCodes ,"pd.code", Types.VARCHAR));
			List<Object> objectCodes = new ArrayList<>(propertyDefinitionCodes);
			
			if(CollectionUtils.isNotEmpty(codes)) {
				sqlSelect.append("AND t.code in (").append(listToParameters(codes)).append(") ");
				Collections.addAll(sqlParameters, listToSqlParameters(codes ,"t.code", Types.VARCHAR));
				objectCodes.addAll(codes);
			} else if(CollectionUtils.isNotEmpty(processCategoryCodes)) {
				sql.append("INNER JOIN process_category as pc ON c.fk_process_category=pc.id ");
				sqlSelect.append("AND pc.code in (").append(listToParameters(processCategoryCodes)).append(") ");
				Collections.addAll(sqlParameters, listToSqlParameters(processCategoryCodes ,"pc.code", Types.VARCHAR));
				objectCodes.addAll(processCategoryCodes);
			}
			String sqlMerged = sql.append(sqlSelect).append("order by t.display_order").toString();
			
			return initializeMapping(sqlMerged, sqlParameters.toArray(new SqlParameter[sqlParameters.size()])).execute(objectCodes.toArray());
		} catch (DataAccessException e) {
			logger.warn(e.getMessage());
			return null;
		}
	}

	@Override
	public long save(ProcessType processType) throws DAOException {
//		if(null == processType){
//			throw new DAOException("ProcessType is mandatory");
//		}
		daoAssertNotNull("processType",processType);
		//Check if category exist
//		if(processType.category == null || processType.category.id == null){
//			throw new DAOException("ProcessCategory is not present !!");
//		}
		daoAssertNotNull("processType.category",    processType.category);
		daoAssertNotNull("processType.category.id", processType.category.id);
		// Add commonInfoType
		CommonInfoTypeDAO commonInfoTypeDAO = Spring.getBeanOfType(CommonInfoTypeDAO.class);
		processType.id = commonInfoTypeDAO.save(processType);		
		//Create new processType
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("id", processType.id);
		parameters.put("fk_common_info_type", processType.id);
		parameters.put("fk_process_category", processType.category.id);


		if(processType.voidExperimentType == null || processType.voidExperimentType.id == null ){
			throw new DAOException("VoidExperimentType is not present !!");
		}

		parameters.put("fk_void_experiment_type", processType.voidExperimentType.id);

		if(processType.firstExperimentType == null || processType.firstExperimentType.id == null ){
			throw new DAOException("FirstExperimentType is not present !!");
		}

		parameters.put("fk_first_experiment_type", processType.firstExperimentType.id);

		if(processType.lastExperimentType == null || processType.lastExperimentType.id == null ){
			throw new DAOException("LastExperimentType is not present !!");
		}

		parameters.put("fk_last_experiment_type", processType.lastExperimentType.id);

		jdbcInsert.execute(parameters);

		if(processType.experimentTypes == null || processType.experimentTypes.size() == 0 ){
			throw new DAOException("ExperimentTypes is not present !!");
		}

		//Add list experimentType
		insertExperimentTypes(processType.experimentTypes, processType.id, false);
		return processType.id;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void update(ProcessType processType) throws DAOException
	{
		CommonInfoTypeDAO commonInfoTypeDAO = Spring.getBeanOfType(CommonInfoTypeDAO.class);
		commonInfoTypeDAO.update(processType);
		if(processType.voidExperimentType == null || processType.voidExperimentType.id == null ){
			throw new DAOException("VoidExperimentType is not present !!");
		}

		if(processType.firstExperimentType == null || processType.firstExperimentType.id == null ){
			throw new DAOException("FirstExperimentType is not present !!");
		}

		if(processType.lastExperimentType == null || processType.lastExperimentType.id == null ){
			throw new DAOException("LastExperimentType is not present !!");
		}


		if(processType.experimentTypes == null || processType.experimentTypes.size() == 0 ){
			throw new DAOException("ExperimentTypes is not present !!");
		}

		String sql = "update process_experiment_type set fk_first_experiment_type = ?, fk_last_experiment_type = ?, fk_void_experiment_type = ? where id = ?";
		jdbcTemplate.update(sql, processType.firstExperimentType.id, processType.lastExperimentType.id, processType.voidExperimentType.id, processType.id);
		insertExperimentTypes(processType.experimentTypes, processType.id, true);
	}

	@SuppressWarnings("deprecation")
	private void insertExperimentTypes(List<ProcessExperimentType> experimentTypes, Long id, boolean deleteBefore) throws DAOException {
		if (deleteBefore) {
			removeExperimentTypes(id);
		}
		if (experimentTypes != null && experimentTypes.size() > 0) {
			// GA: add order of experiment
			String sql = "INSERT INTO process_experiment_type(fk_process_type, fk_experiment_type, position_in_process) VALUES(?,?,?)";
			for(ProcessExperimentType experimentType:experimentTypes){
				if(experimentType == null || experimentType.experimentType == null || experimentType.experimentType.id == null ){
					throw new DAOException("experimentType is mandatory");
				}
				jdbcTemplate.update(sql, id, experimentType.experimentType.id, experimentType.positionInProcess);
			}
		}		
	}

	@SuppressWarnings("deprecation")
	private void removeExperimentTypes(Long id) {
		String sql = "DELETE FROM process_experiment_type WHERE fk_process_type=?";
		jdbcTemplate.update(sql, id);
	}
	
	@SuppressWarnings("deprecation")
	public List<ProcessExperimentType> getProcessExperimentType(Long processId){
		String sql = "SELECT pet.position_in_process, et.code as experimentTypeCode "
	               + "FROM process_experiment_type as pet inner join common_info_type as et on et.id = pet.fk_experiment_type "
				   + "WHERE pet.fk_process_type = ? ORDER BY pet.position_in_process, et.code";
		BeanPropertyRowMapper<ProcessExperimentType> mapper = new BeanPropertyRowMapper<>(ProcessExperimentType.class);
		return this.jdbcTemplate.query(sql, mapper, processId);		
	}
	
	@Override
	public void remove(ProcessType processType) throws DAOException {
		//Remove process_experiment_type
		removeExperimentTypes(processType.id);
		//Remove processType
		super.remove(processType);

		//Remove CommonInfoType
		CommonInfoTypeDAO commonInfoTypeDAO = Spring.getBeanOfType(CommonInfoTypeDAO.class);
		commonInfoTypeDAO.remove(processType);
	}

	@SuppressWarnings("deprecation")
	public List<ProcessType> findByExperimentCode(String experimentTypeCode) {
		try {
			String sql = sqlCommonSelect + ",t.name, t.code " + sqlCommonFrom + ", process_experiment_type as pe, common_info_type ce WHERE pe.fk_process_type=t.id and pe.fk_experiment_type=ce.id AND ce.code=?";
			BeanPropertyRowMapper<ProcessType> mapper = new BeanPropertyRowMapper<>(entityClass);
			return this.jdbcTemplate.query(sql, mapper, experimentTypeCode);
		} catch (DataAccessException e) {
			logger.warn(e.getMessage());
			return null;
		}
	}
	
	public List<ProcessType> findAll(boolean light) throws DAOException {
		ProcessTypeMappingQuery mapping = (ProcessTypeMappingQuery)initializeMapping(sqlCommon+" order by display_order");
		mapping.lightVersion = light;
		return mapping.execute();
	}

	// Finder implementation
//	public List<ProcessType> findByExperimentTypeCode(String experimentTypeCode) throws DAOException {
////return ((ProcessTypeDAO)getInstance()).findByExperimentCode(experimentTypeCode);
//return getInstance().findByExperimentCode(experimentTypeCode);
//}
	public List<ProcessType> findByExperimentTypeCode(String experimentTypeCode) throws DAOException {
		return findByExperimentCode(experimentTypeCode);
	}
}
