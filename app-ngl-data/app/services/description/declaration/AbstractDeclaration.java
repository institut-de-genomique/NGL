package services.description.declaration;

import java.util.ArrayList;
import java.util.List;

import org.mongojack.DBQuery;

import com.typesafe.config.ConfigFactory;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.description.Value;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.instrument.description.InstrumentUsedType;
import models.laboratory.parameter.index.IlluminaIndex;
import models.laboratory.processes.description.ExperimentTypeNode;
import models.laboratory.processes.description.ProcessExperimentType;
import models.laboratory.processes.description.ProcessType;
import models.laboratory.sample.description.SampleType;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;
import models.utils.dao.DAOHelpers;
//import play.Logger;
//import play.Logger.ALogger;
import services.description.DescriptionFactory;

public abstract class AbstractDeclaration {
	
//	private static final play.Logger.ALogger logger = play.Logger.of(AbstractDeclaration.class);
	
	protected abstract List<ExperimentType> getExperimentTypeCommon();
	protected abstract List<ExperimentType> getExperimentTypeDEV();
	protected abstract List<ExperimentType> getExperimentTypePROD();
	protected abstract List<ExperimentType> getExperimentTypeUAT();

	public List<ExperimentType> getExperimentType() {
		List<ExperimentType> l = new ArrayList<>();
		//Logger.debug(this.getClass().getSimpleName()+" getExperimentType");
		if (getExperimentTypeCommon() != null) {
			l.addAll(getExperimentTypeCommon());
		}
		if (ConfigFactory.load().getString("ngl.env").equals("DEV")) {
			if (getExperimentTypeDEV() != null) {
				l.addAll(getExperimentTypeDEV());
			}
		} else if(ConfigFactory.load().getString("ngl.env").equals("UAT")) {
			if (getExperimentTypeUAT() != null) {
				l.addAll(getExperimentTypeUAT());
			}
		} else if(ConfigFactory.load().getString("ngl.env").equals("PROD")) {
			if (getExperimentTypePROD() != null) {
				l.addAll(getExperimentTypePROD());
			}
		} else {
			throw new RuntimeException("ngl.env value not implemented");
		}
		return l;
	}

	protected abstract List<ProcessType> getProcessTypeCommon();
	protected abstract List<ProcessType> getProcessTypeDEV();
	protected abstract List<ProcessType> getProcessTypePROD();
	protected abstract List<ProcessType> getProcessTypeUAT();

	public List<ProcessType> getProcessType() {
		List<ProcessType> l = new ArrayList<>();
		//Logger.debug(this.getClass().getSimpleName()+" getProcessType");
		if (getProcessTypeCommon()!=null) {
			l.addAll(getProcessTypeCommon());
		}
		if(ConfigFactory.load().getString("ngl.env").equals("DEV")){
			if(getProcessTypeDEV()!=null){
				l.addAll(getProcessTypeDEV());
			}
		}else if(ConfigFactory.load().getString("ngl.env").equals("UAT")){
			if(getProcessTypeUAT()!=null){
				l.addAll(getProcessTypeUAT());
			}
		}else if(ConfigFactory.load().getString("ngl.env").equals("PROD")) {
			if(getProcessTypePROD()!=null){
				l.addAll(getProcessTypePROD());
			} 		
		}else {
			throw new RuntimeException("ngl.env value not implemented");
		}
		
		return l;
	}
	
	protected abstract void getExperimentTypeNodeCommon();
	protected abstract void getExperimentTypeNodeDEV();
	protected abstract void getExperimentTypeNodePROD();
	protected abstract void getExperimentTypeNodeUAT();

	public void getExperimentTypeNode(){
		getExperimentTypeNodeCommon();
		//Logger.debug(this.getClass().getSimpleName()+" getExperimentTypeNode");
		if(ConfigFactory.load().getString("ngl.env").equals("DEV")){
			getExperimentTypeNodeDEV();
		}else if(ConfigFactory.load().getString("ngl.env").equals("UAT")){
			 getExperimentTypeNodeUAT();
		}else if(ConfigFactory.load().getString("ngl.env").equals("PROD")) {
			getExperimentTypeNodePROD();
		}else {
			throw new RuntimeException("ngl.env value not implemented");
		}
	}
	
	protected static ProcessExperimentType getPET(String expCode, Integer index) {
		return new ProcessExperimentType(getExperimentType(expCode), index);
	}
	
	protected static List<ExperimentType> getExperimentTypes(String...codes) throws DAOException {
		return DAOHelpers.getModelByCodes(ExperimentType.class,ExperimentType.find, codes);
	}
	
	protected static ExperimentType getExperimentType(String code) throws DAOException {
		return DAOHelpers.getModelByCode(ExperimentType.class,ExperimentType.find, code);
	}
	
	public static List<InstrumentUsedType> getInstrumentUsedTypes(String...codes) throws DAOException {
		return DAOHelpers.getModelByCodes(InstrumentUsedType.class,InstrumentUsedType.find, codes);
	}

	public static List<SampleType> getSampleTypes(String...codes) throws DAOException {
		return DAOHelpers.getModelByCodes(SampleType.class,SampleType.find, codes);
	}
	
	public static List<ExperimentTypeNode> getExperimentTypeNodes(String...codes) throws DAOException {
		return DAOHelpers.getModelByCodes(ExperimentTypeNode.class,ExperimentTypeNode.find, codes);
	}

	protected List<Value> getTagIllumina() {
		
		List<IlluminaIndex> indexes = MongoDBDAO.find(InstanceConstants.PARAMETER_COLL_NAME, IlluminaIndex.class, DBQuery.is("typeCode", "index-illumina-sequencing")).sort("name").toList();
		List<Value> values = new ArrayList<>();
		indexes.forEach(index -> {
			values.add(DescriptionFactory.newValue(index.code, index.code));	
		});
		
		return values;
	}
	
	public List<Value> getTagCategoriesIllumina(){
		List<Value> values = new ArrayList<>();
		values.add(DescriptionFactory.newValue("SINGLE-INDEX", "SINGLE-INDEX"));
		values.add(DescriptionFactory.newValue("MID", "MID"));
		values.add(DescriptionFactory.newValue("DUAL-INDEX", "DUAL-INDEX"));
		values.add(DescriptionFactory.newValue("POOL-INDEX", "POOL-INDEX"));
		return values;	
	}
}
