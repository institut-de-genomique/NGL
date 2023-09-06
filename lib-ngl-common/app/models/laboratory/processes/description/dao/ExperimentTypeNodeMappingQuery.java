package models.laboratory.processes.description.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

import models.laboratory.experiment.description.ExperimentCategory;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.experiment.description.dao.ExperimentTypeDAO;
import models.laboratory.processes.description.ExperimentTypeNode;
import models.utils.dao.DAOException;
import models.utils.dao.MappingSqlQueryFactory;
import models.utils.dao.NGLMappingSqlQuery;
import play.api.modules.spring.Spring;

//public class ExperimentTypeNodeMappingQuery  extends MappingSqlQuery<ExperimentTypeNode> {
public class ExperimentTypeNodeMappingQuery extends NGLMappingSqlQuery<ExperimentTypeNode> {

	public static final MappingSqlQueryFactory<ExperimentTypeNode> factory = ExperimentTypeNodeMappingQuery::new;
	
//	public ExperimentTypeNodeMappingQuery() {
//		super();
//	}
	
//	public ExperimentTypeNodeMappingQuery(DataSource ds, String sql, SqlParameter sqlParameter) {
//		super(ds,sql);
//		if (sqlParameter != null)
////			super.declareParameter(sqlParameter);
//			declareParameter(sqlParameter);
//		compile();
//	}

	public ExperimentTypeNodeMappingQuery(DataSource ds, String sql, SqlParameter... sqlParameter) {
		super(ds,sql,sqlParameter);
	}

	@Override
	protected ExperimentTypeNode mapRow(ResultSet rs, int arg1) throws SQLException {
		try {
			ExperimentTypeNode node = new ExperimentTypeNode();
			node.id = rs.getLong("id");
			node.code = rs.getString("code");
			node.doPurification = rs.getBoolean("doPurification");
			node.doQualityControl = rs.getBoolean("doQualityControl");
			node.mandatoryPurification = rs.getBoolean("mandatoryPurification");
			node.mandatoryQualityControl = rs.getBoolean("mandatoryQualityControl");
			node.doTransfert=rs.getBoolean("doTransfert");
			node.mandatoryTransfert = rs.getBoolean("mandatoryTransfert");
			ExperimentTypeDAO expTypeDAO = Spring.getBeanOfType(ExperimentTypeDAO.class);
			
			node.experimentType = expTypeDAO.findById(rs.getLong("fk_experiment_type"));
			
			node.previousExperimentTypes = expTypeDAO.findPreviousExperimentTypeForAnExperimentTypeCode(node.experimentType.code);
			List<ExperimentType> experimentTypes = expTypeDAO.findSatelliteExperimentByNodeId(node.id);
			
			for(ExperimentType et: experimentTypes){
				if(ExperimentCategory.CODE.purification.equals(ExperimentCategory.CODE.valueOf(et.category.code))){
					node.possiblePurificationTypes.add(et);
				}else if(ExperimentCategory.CODE.qualitycontrol.equals(ExperimentCategory.CODE.valueOf(et.category.code))){
					node.possibleQualityControlTypes.add(et);
				}else if(ExperimentCategory.CODE.transfert.equals(ExperimentCategory.CODE.valueOf(et.category.code))){
					node.possibleTransferts.add(et);
				}else{
					throw new DAOException("Bad Satellite "+et.code);
				}
			}				
			return node;
		} catch (DAOException e) {
			throw new SQLException(e);
		}
	}

}
