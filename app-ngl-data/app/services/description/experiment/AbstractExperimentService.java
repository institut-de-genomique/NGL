package services.description.experiment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.description.Value;
import models.laboratory.experiment.description.ExperimentCategory;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.instrument.description.InstrumentUsedType;
import models.laboratory.parameter.index.IlluminaIndex;
import models.laboratory.processes.description.ExperimentTypeNode;
import models.laboratory.processes.description.ProcessType;
import models.laboratory.processes.description.dao.ExperimentTypeNodeDAO;
import models.laboratory.sample.description.SampleType;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;
import models.utils.dao.DAOHelpers;
import play.api.modules.spring.Spring;
import play.data.validation.ValidationError;
import services.description.DescriptionFactory;

public abstract class AbstractExperimentService {


	public void main(Map<String, List<ValidationError>> errors)  throws DAOException{
		DAOHelpers.removeAll(ProcessType.class, ProcessType.find);
		//remove all previous before delete ExpTypeNode
		Spring.getBeanOfType(ExperimentTypeNodeDAO.class).removeAllPrevious();

		DAOHelpers.removeAll(ExperimentTypeNode.class, ExperimentTypeNode.find);

		DAOHelpers.removeAll(ExperimentType.class, ExperimentType.find);
		DAOHelpers.removeAll(ExperimentCategory.class, ExperimentCategory.find);

		saveProtocolCategories(errors);
		saveExperimentCategories(errors);
		saveExperimentTypes(errors);
		saveExperimentTypeNodes(errors);
	}

	public static List<InstrumentUsedType> getInstrumentUsedTypes(String...codes) throws DAOException {
		return DAOHelpers.getModelByCodes(InstrumentUsedType.class,InstrumentUsedType.find, codes);
	}

	public static List<SampleType> getSampleTypes(String...codes) throws DAOException {
		return DAOHelpers.getModelByCodes(SampleType.class,SampleType.find, codes);
	}

	public static List<ExperimentType> getExperimentTypes(String...codes) throws DAOException {
		return DAOHelpers.getModelByCodes(ExperimentType.class,ExperimentType.find, codes);
	}

	public static List<ExperimentTypeNode> getExperimentTypeNodes(String...codes) throws DAOException {
		return DAOHelpers.getModelByCodes(ExperimentTypeNode.class,ExperimentTypeNode.find, codes);
	}

	abstract void saveExperimentTypeNodes(Map<String, List<ValidationError>> errors) throws DAOException ;

	abstract void saveExperimentTypes(Map<String, List<ValidationError>> errors) throws DAOException;

	abstract void saveExperimentCategories(Map<String, List<ValidationError>> errors) throws DAOException;

	abstract void saveProtocolCategories(Map<String, List<ValidationError>> errors) throws DAOException;

	//GA 24/07/2015 ajout des TagCategories
	// FDS 02/03/2017 ajout POOL-INDEX.... pourquoi cette méthode existe aussi dans RunService ???
	protected static List<Value> getTagCategories(){
		List<Value> values = new ArrayList<>();
		values.add(DescriptionFactory.newValue("SINGLE-INDEX", "SINGLE-INDEX"));
		values.add(DescriptionFactory.newValue("DUAL-INDEX", "DUAL-INDEX"));
		values.add(DescriptionFactory.newValue("MID", "MID"));
		values.add(DescriptionFactory.newValue("POOL-INDEX", "POOL-INDEX"));
		return values;	
	}

	// FDS 06/03/2017 modifications: ajout methode getTagIlluminaCategory
	protected static List<Value> getTagIllumina() {
		return getTagIlluminaCategory(null) ;
	}

	protected static List<Value> getTagIllumina (List<String> categoryCodes) {
		return getTagIlluminaCategory(categoryCodes) ;
	}

	protected static List<Value> getTagIlluminaCategory (List<String> categoryCodes) {
		List<Value> values = new ArrayList<>();

		if (categoryCodes == null) {
			// ancien fonctionnement recuperer index "SINGLE-INDEX","DUAL-INDEX" et"MID"
			categoryCodes = new ArrayList<>( Arrays.asList("SINGLE-INDEX","DUAL-INDEX","MID","POOL-INDEX"));
		}

		//System.out.print("getTagIlluminaCategory getting indexes :"+ categoryCodes+"\n");

		List<IlluminaIndex> indexes = MongoDBDAO.find(InstanceConstants.PARAMETER_COLL_NAME, IlluminaIndex.class, 
				DBQuery.is("typeCode", "index-illumina-sequencing").and(DBQuery.in("categoryCode", categoryCodes))).sort("name").toList();
		indexes.forEach(index -> {
			values.add(DescriptionFactory.newValue(index.code, index.name));	
		});

		return values;
	}		
}
