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
import models.utils.dao.DAOHelpers;    //FDS 24/10/2019 : NGL-2719: rollback refactoring
import play.api.modules.spring.Spring;
import play.data.validation.ValidationError;
import services.description.DescriptionFactory;

public abstract class AbstractExperimentService {

	private static final play.Logger.ALogger logger = play.Logger.of(AbstractExperimentService.class);
	
	public void main(Map<String, List<ValidationError>> errors)  throws DAOException {
//		DAOHelpers.removeAll(ProcessType.class, ProcessType.find.get());
		ProcessType.find.get().removeAll();
		//remove all previous before delete ExpTypeNode
		Spring.getBeanOfType(ExperimentTypeNodeDAO.class).removeAllPrevious();

//		DAOHelpers.removeAll(ExperimentTypeNode.class, ExperimentTypeNode.find.get());
//		DAOHelpers.removeAll(ExperimentType.class,     ExperimentType.find.get());
//		DAOHelpers.removeAll(ExperimentCategory.class, ExperimentCategory.find.get());
		ExperimentTypeNode.find.get().removeAll();
		ExperimentType    .find.get().removeAll();
		ExperimentCategory.find.get().removeAll();

		saveProtocolCategories(errors);
		saveExperimentCategories(errors);
		saveExperimentTypes(errors);
		saveExperimentTypeNodes(errors);
	}

	// FDS 24/10/2019 : NGL-2719: rollback refactoring
	public static List<InstrumentUsedType> getInstrumentUsedTypes(String...codes) throws DAOException {
		return DAOHelpers.getModelByCodes(InstrumentUsedType.class,InstrumentUsedType.find.get(), codes);
	}
	
	// FDS 24/10/2019 : NGL-2719: rollback refactoring
	public static List<SampleType> getSampleTypes(String...codes) throws DAOException {
		return DAOHelpers.getModelByCodes(SampleType.class,SampleType.find.get(), codes);
	}
	
	// FDS 24/10/2019 : NGL-2719: rollback refactoring
	public static List<ExperimentType> getExperimentTypes(String...codes) throws DAOException {
		return DAOHelpers.getModelByCodes(ExperimentType.class,ExperimentType.find.get(), codes);
	}

	// FDS 24/10/2019 : NGL-2719: rollback refactoring
	public static List<ExperimentTypeNode> getExperimentTypeNodes(String...codes) throws DAOException {
		return DAOHelpers.getModelByCodes(ExperimentTypeNode.class,ExperimentTypeNode.find.get(), codes);
	}

	abstract void saveExperimentTypeNodes(Map<String, List<ValidationError>> errors) throws DAOException ;

	abstract void saveExperimentTypes(Map<String, List<ValidationError>> errors) throws DAOException;

	abstract void saveExperimentCategories(Map<String, List<ValidationError>> errors) throws DAOException;

	abstract void saveProtocolCategories(Map<String, List<ValidationError>> errors) throws DAOException;

	// GA 24/07/2015 ajout des TagCategories
	// FDS  attention cette méthode existe aussi dans RunService, maintenir en cohérence
	protected static List<Value> getTagCategories(){
		List<Value> values = new ArrayList<>();
		values.add(DescriptionFactory.newValue("SINGLE-INDEX", "SINGLE-INDEX"));
		values.add(DescriptionFactory.newValue("DUAL-INDEX", "DUAL-INDEX"));
		values.add(DescriptionFactory.newValue("MID", "MID"));
		values.add(DescriptionFactory.newValue("POOL-INDEX", "POOL-INDEX")); // FDS 02/03/2017 ajout POOL-INDEX.
		return values;	
	}

	// FDS 06/03/2017 modifications: ajout methode getTagIlluminaCategory
	protected static List<Value> getTagIllumina() {
		return getTagIlluminaCategory(null);
	}

	protected static List<Value> getTagIllumina (List<String> categoryCodes) {
		return getTagIlluminaCategory(categoryCodes) ;
	}

	/**
	 * return tags for required categoryCodes; If categoryCodes is null then return all tags
	 * @param categoryCodes
	 * @return tag list
	 */
	protected static List<Value> getTagIlluminaCategory (List<String> categoryCodes) {
		List<Value> values = new ArrayList<>();

		if (categoryCodes == null) {
			// ancien fonctionnement: recuperer les tag de toutes les categories
			categoryCodes = new ArrayList<>( Arrays.asList("SINGLE-INDEX","DUAL-INDEX","MID","POOL-INDEX"));
		}

		//System.out.print("getTagIlluminaCategory getting indexes :"+ categoryCodes+"\n");

		List<IlluminaIndex> indexes = MongoDBDAO.find(InstanceConstants.PARAMETER_COLL_NAME, IlluminaIndex.class, 
				DBQuery.is("typeCode", "index-illumina-sequencing").and(DBQuery.in("categoryCode", categoryCodes))).sort("name").toList();
		indexes.forEach(index -> {
			if (index.name == null) {
				// throw new RuntimeException("bad illumina stuff (no name), code:'" + index.code + "'");
				logger.info("skip null name for {}",index.code);
			} else {
				values.add(DescriptionFactory.newValue(index.code, index.name));
			}
		});

		return values;
	}	
	
	public void save(ExperimentTypeNode n) throws DAOException {
		ExperimentTypeNode.find.get().save(n);
	}
		
}
