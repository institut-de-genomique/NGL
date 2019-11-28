package models.laboratory.processes.description.dao;

import static models.utils.dao.DAOException.daoAssertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.processes.description.ExperimentTypeNode;
import models.utils.dao.AbstractDAOMapping;
import models.utils.dao.DAOException;
//import play.Logger;
@Repository
public class ExperimentTypeNodeDAO  extends AbstractDAOMapping<ExperimentTypeNode> {
	
	private static final play.Logger.ALogger logger = play.Logger.of(ExperimentTypeNodeDAO.class);
	
//	public ExperimentTypeNodeDAO() {
//		super("experiment_type_node", ExperimentTypeNode.class, ExperimentTypeNodeMappingQuery.class,
//				"SELECT t.id, t.code, t.doPurification, t.mandatoryPurification, t.doQualityControl, t.mandatoryQualityControl,t.doTransfert, t.mandatoryTransfert, " +
//				"t.fk_experiment_type FROM experiment_type_node as t", true);
//	}
	public ExperimentTypeNodeDAO() {
		super("experiment_type_node", ExperimentTypeNode.class, ExperimentTypeNodeMappingQuery.factory,
				"SELECT t.id, t.code, t.doPurification, t.mandatoryPurification, t.doQualityControl, t.mandatoryQualityControl,t.doTransfert, t.mandatoryTransfert, " +
				"t.fk_experiment_type FROM experiment_type_node as t", true);
	}

	@Override
	public long save(ExperimentTypeNode value) throws DAOException {
//		if (value == null) {
//			throw new DAOException("ExperimentTypeNode is mandatory");
//		}
		daoAssertNotNull("ExperimentTypeNode",value);
//		if(null == value.experimentType || null == value.experimentType.id){
//			throw new DAOException("ExperimentType is mandatory");
//		}
		daoAssertNotNull("value.experimentType",    value.experimentType);
		daoAssertNotNull("value.experimentType.id", value.experimentType.id);
		
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("code",                    value.code);
		parameters.put("doPurification",          value.doPurification);
		parameters.put("mandatoryPurification",   value.mandatoryPurification);
		parameters.put("doQualityControl",        value.doQualityControl);
		parameters.put("mandatoryQualityControl", value.mandatoryQualityControl);
		parameters.put("fk_experiment_type",      value.experimentType.id);
		parameters.put("doTransfert",             value.doTransfert);
		parameters.put("mandatoryTransfert",      value.mandatoryTransfert);

		value.id = (Long) jdbcInsert.executeAndReturnKey(parameters);

		List<ExperimentType> experimentTypes = new ArrayList<>();
		if (value.possibleQualityControlTypes != null) {
			experimentTypes.addAll(value.possibleQualityControlTypes);
		}
		if (value.possiblePurificationTypes != null) {
			experimentTypes.addAll(value.possiblePurificationTypes);
		}
		if (value.possibleTransferts != null) {
			experimentTypes.addAll(value.possibleTransferts);
		}
		if (experimentTypes.size() > 0) {
			insertSatellites(experimentTypes, value.id, false);
		}
		if (value.previousExperimentTypeNodes != null && value.previousExperimentTypeNodes.size() > 0) {
			insertPrevious(value.previousExperimentTypeNodes, value.id, false);
		}
		logger.debug("saveExperimentTypeNode : "+ value.code);
		return value.id;
	}

	@Override
	public List<ExperimentTypeNode> findAll() throws DAOException {
		return initializeMapping(sqlCommon + " order by id DESC").execute();
	}

	@SuppressWarnings("deprecation")
	public void insertPrevious(List<ExperimentTypeNode> previousExperimentType, Long id, boolean deleteBefore) throws DAOException {
		if (deleteBefore) {
			removePrevious(id);
		}
		if (previousExperimentType != null && previousExperimentType.size() > 0) {
			String sql = "INSERT INTO previous_nodes(fk_node, fk_previous_node) VALUES(?,?)";
			for(ExperimentTypeNode experimentTypeNode:previousExperimentType){
//				if(experimentTypeNode == null || experimentTypeNode.id == null ){
//					throw new DAOException("experimentTypeNode is mandatory");
//				}
				daoAssertNotNull("experimentTypeNode",    experimentTypeNode);
				daoAssertNotNull("experimentTypeNode.id", experimentTypeNode.id);
				jdbcTemplate.update(sql, id, experimentTypeNode.id);
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void insertSatellites(List<ExperimentType> experimentTypes, Long id, boolean deleteBefore) throws DAOException {
		if (deleteBefore) {
			removeSatellites(id);
		}
		if (experimentTypes != null && experimentTypes.size() > 0) {
			String sql = "INSERT INTO satellite_experiment_type(fk_experiment_type_node, fk_experiment_type) VALUES(?,?)";
			for(ExperimentType experimentType:experimentTypes){
				if(experimentType == null || experimentType.id == null ){
					throw new DAOException("experimentType is mandatory");
				}
				jdbcTemplate.update(sql, id, experimentType.id);
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void removeSatellites(Long id) {
		String sql = "DELETE FROM satellite_experiment_type WHERE fk_experiment_type_node=?";
		jdbcTemplate.update(sql, id);
	}

	@SuppressWarnings("deprecation")
	private void removePrevious(Long id) {
		String sql = "DELETE FROM previous_nodes WHERE fk_node=?";
		jdbcTemplate.update(sql, id);
	}

	@SuppressWarnings("deprecation")
	public void removeAllPrevious(){
		String sql = "DELETE FROM previous_nodes";
		jdbcTemplate.update(sql);
	}
		
	 /**
	  * Update previousExperimentNode
	  */
	@Override
	public void update(ExperimentTypeNode etn) throws DAOException {
		insertPrevious(etn.previousExperimentTypeNodes,etn.id,false);
		//throw new UnsupportedOperationException();
	}

	@Override
	public void remove(ExperimentTypeNode value) throws DAOException {
		removeSatellites(value.id);
		removePrevious(value.id);
		super.remove(value);
	}

}
