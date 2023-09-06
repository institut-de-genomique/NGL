package models.laboratory.experiment.description.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

import models.laboratory.common.description.CommonInfoType;
import models.laboratory.common.description.dao.CommonInfoTypeDAO;
import models.laboratory.experiment.description.ExperimentCategory;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.instrument.description.InstrumentUsedType;
import models.laboratory.instrument.description.dao.InstrumentUsedTypeDAO;
import models.laboratory.sample.description.SampleType;
import models.laboratory.sample.description.dao.SampleTypeDAO;
import models.utils.dao.DAOException;
import models.utils.dao.MappingSqlQueryFactory;
import models.utils.dao.NGLMappingSqlQuery;
import play.api.modules.spring.Spring;

//public class ExperimentTypeMappingQuery extends MappingSqlQuery<ExperimentType> {
public class ExperimentTypeMappingQuery extends NGLMappingSqlQuery<ExperimentType> {

	public static final MappingSqlQueryFactory<ExperimentType> factory = ExperimentTypeMappingQuery::new;
	
	private Set<String> excludes;
	
	private Set<String> includes;
	
//	public ExperimentTypeMappingQuery()
//	{
//		super();
//	}
	
//	public ExperimentTypeMappingQuery(DataSource ds, String sql, SqlParameter sqlParameter)	{
//		super(ds,sql);
//		if (sqlParameter != null)
////			super.declareParameter(sqlParameter);
//			declareParameter(sqlParameter);
//		compile();
//	}
	
	public ExperimentTypeMappingQuery(DataSource ds, String sql, SqlParameter... sqlParameters)	{
		super(ds,sql,sqlParameters);
	}
	
	private boolean include(String key) {
		if(includes != null) return includes.contains(key);
		if(excludes != null) return !excludes.contains(key);
		return true;
	}
	
	@Override
	protected ExperimentType mapRow(ResultSet rs, int rowNumber) throws SQLException {
		ExperimentType experimentType = new ExperimentType();
		//play.Logger.debug("Experiment type "+experimentType);
		if(include("id")) experimentType.id = rs.getLong("id");
		if(include("atomicTransfertMethod")) experimentType.atomicTransfertMethod=rs.getString("atomic_transfert_method");
		if(include("shortCode")) experimentType.shortCode = rs.getString("short_code");
		if(include("newSample")) experimentType.newSample = rs.getBoolean("new_sample");
		long idExperimentCategory = rs.getLong("fk_experiment_category");
		long idCommonInfoType = rs.getLong("fk_common_info_type");
		//Get commonInfoType
		CommonInfoTypeDAO commonInfoTypeDAO = Spring.getBeanOfType(CommonInfoTypeDAO.class);
		CommonInfoType commonInfoType=null;
		try {
			commonInfoType = commonInfoTypeDAO.findById(idCommonInfoType);
		} catch (DAOException e1) {
			throw new SQLException(e1);
		}
		experimentType.setCommonInfoType(commonInfoType);
		
		//Get list instruments by common info type
		if(include("instrumentUsedTypes")) {
			InstrumentUsedTypeDAO instrumentUsedTypeDAO = Spring.getBeanOfType(InstrumentUsedTypeDAO.class);
			List<InstrumentUsedType> instrumentUsedTypes = instrumentUsedTypeDAO.findByExperimentId(idCommonInfoType);
			experimentType.instrumentUsedTypes=instrumentUsedTypes;
		}
		
		//Get list sample type by common info type
		if(include("sampleTypes")) {
			SampleTypeDAO sampleTypeDAO = Spring.getBeanOfType(SampleTypeDAO.class);
			List<SampleType> sampleTypes = sampleTypeDAO.findByExperimentId(idCommonInfoType);
			experimentType.sampleTypes=sampleTypes;
		}
				
		//Get Experiment category
		if(include("category")) {
			ExperimentCategoryDAO experimentCategoryDAO = Spring.getBeanOfType(ExperimentCategoryDAO.class);
			ExperimentCategory experimentCategory=null;
			try {
	//			experimentCategory = (ExperimentCategory) experimentCategoryDAO.findById(idExperimentCategory);
				experimentCategory = experimentCategoryDAO.findById(idExperimentCategory);
			} catch (DAOException e) {
				throw new SQLException(e);
			}
			experimentType.category = experimentCategory;
		}
		
		return experimentType;
	}
	
	public ExperimentTypeMappingQuery with(Set<String> excludes, Set<String> includes) {
		this.excludes = excludes;
		this.includes = includes;
		return this;
	}

}
