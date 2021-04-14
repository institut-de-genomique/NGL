package models.laboratory.experiment.instance;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fr.cea.ig.DBObject;
import fr.cea.ig.ngl.dao.experiments.ExperimentsDAO;
import fr.cea.ig.ngl.utils.GuiceSupplier;

import models.laboratory.common.instance.Comment;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.Valuation;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.instrument.instance.InstrumentUsed;
import models.laboratory.processes.instance.Process;
import models.laboratory.reagent.instance.ReagentUsed;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.IValidation;
import validation.common.instance.CommonValidationHelper;
import validation.experiment.instance.ExperimentValidationHelper;

/**
 * Step of a {@link Process}, has an {@link ExperimentType} and therefore a category and
 * provides a mapping from the input containers to the output containers.
 * 
 * <p>
 * Experiment instance are stored one collection for the moment.
 * For find the collection, see the value of attribute commoninfotype.collectionName in class experimentType.
 */
//@MongoCollection(name="Experiment")
public class Experiment extends DBObject implements IValidation {
	
	public static final Supplier<ExperimentsDAO> find = new GuiceSupplier<>(ExperimentsDAO.class);
	

	public String typeCode;
	
	public String categoryCode;
	
	public TraceInformation traceInformation = new TraceInformation();
	public Map<String, PropertyValue> experimentProperties;
	public Map<String, PropertyValue> instrumentProperties;
	
	public InstrumentUsed instrument;
	public String protocolCode;

	public State state = new State();
	public Valuation status = new Valuation();
	
	public List<AtomicTransfertMethod> atomicTransfertMethods; 
	
	public List<ReagentUsed> reagents;
	
	public List<Comment> comments;
	
	public Set<String> projectCodes;
	public Set<String> sampleCodes;
	
	public Set<String> inputContainerSupportCodes;
	public Set<String> inputContainerCodes;
	public Set<String> inputProcessCodes;
	public Set<String> inputProcessTypeCodes;
	public Set<String> inputFromTransformationTypeCodes;
	
	public Set<String> outputContainerCodes;
	public Set<String> outputContainerSupportCodes;
	
	public Experiment() {
		traceInformation = new TraceInformation();		
	}
	
	public Experiment(String code) {
		this.code = code;		
	}
	
	@JsonIgnore
	@Deprecated
	@Override
	public void validate(ContextValidation contextValidation) {
//		if (contextValidation.getObject(CommonValidationHelper.FIELD_STATE_CODE) == null) {
//			contextValidation.putObject(CommonValidationHelper.FIELD_STATE_CODE , state.code);			
//		}
//		contextValidation.putObject(CommonValidationHelper.FIELD_EXPERIMENT , this);
//		
//		CommonValidationHelper    .validateIdPrimary                     (contextValidation, this);
//		CommonValidationHelper    .validateCodePrimary                   (contextValidation, this, InstanceConstants.EXPERIMENT_COLL_NAME);
//		ExperimentValidationHelper.validateExperimentTypeRequired        (contextValidation, typeCode, experimentProperties);
//		ExperimentValidationHelper.validateExperimentCategoryCodeRequired(contextValidation, categoryCode);
////		ExperimentValidationHelper.validateState                   (typeCode, state, contextValidation);
//		CommonValidationHelper    .validateStateRequired                 (contextValidation, typeCode, state);
//		ExperimentValidationHelper.validateStatusRequired                (contextValidation, typeCode, status);
//		ExperimentValidationHelper.validateProtocolCode                  (contextValidation, typeCode, protocolCode);
//		ExperimentValidationHelper.validateInstrumentUsed          (contextValidation,instrument,instrumentProperties);
//		ExperimentValidationHelper.validateAtomicTransfertMethods  (categoryCode, typeCode, instrument, atomicTransfertMethods, contextValidation);
//		ExperimentValidationHelper.validateReagents                      (contextValidation, reagents); // TO DO: active reagents validation inside ReagentUsed
//		CommonValidationHelper    .validateTraceInformationRequired      (contextValidation, traceInformation);
//		ExperimentValidationHelper.validateComments                      (contextValidation, comments);
//		ExperimentValidationHelper.validateRules                         (contextValidation, this);
//		
//		contextValidation.removeObject(CommonValidationHelper.FIELD_EXPERIMENT);
		validate(contextValidation, contextValidation.getTypedObject(CommonValidationHelper.FIELD_STATE_CODE));
	}
	
	public void validate(ContextValidation contextValidation, String stateCode) {
		if (stateCode == null)
			stateCode = state.code;
		Experiment experiment = this;
		CommonValidationHelper    .validateIdPrimary                     (contextValidation, this);
		CommonValidationHelper    .validateCodePrimary                   (contextValidation, this, InstanceConstants.EXPERIMENT_COLL_NAME);
		ExperimentValidationHelper.validateExperimentTypeRequired        (contextValidation, typeCode, experimentProperties);
		ExperimentValidationHelper.validateExperimentCategoryCodeRequired(contextValidation, categoryCode);
		CommonValidationHelper    .validateStateRequired                 (contextValidation, typeCode, state);
		ExperimentValidationHelper.validateStatusRequired                (contextValidation, typeCode, status);
		ExperimentValidationHelper.validateProtocolCode                  (contextValidation, typeCode, protocolCode, stateCode);
		ExperimentValidationHelper.validateInstrumentUsed                (contextValidation, instrument, instrumentProperties, stateCode);
		ExperimentValidationHelper.validateAtomicTransfertMethods        (contextValidation, categoryCode, typeCode, instrument, atomicTransfertMethods, experiment, stateCode);
		ExperimentValidationHelper.validateReagents                      (contextValidation, reagents); // GA: active reagents validation inside ReagentUsed
		CommonValidationHelper    .validateTraceInformationRequired      (contextValidation, traceInformation);
		ExperimentValidationHelper.validateComments                      (contextValidation, comments);
		ExperimentValidationHelper.validateRules                         (contextValidation, this);
		
	}
	
}
