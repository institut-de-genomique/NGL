package models.laboratory.processes.instance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fr.cea.ig.DBObject;
import fr.cea.ig.ngl.dao.processes.ProcessesDAO;
import fr.cea.ig.ngl.utils.GuiceSupplier;
import models.laboratory.common.instance.Comment;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.experiment.instance.Experiment;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.ContextValidation.Mode;
import validation.common.instance.CommonValidationHelper;
import validation.processes.instance.ProcessValidationHelper;

/**
 * Process that executed some {@link Experiment}.
 */
public class Process extends DBObject {

	public static Supplier<ProcessesDAO> find = new GuiceSupplier<>(ProcessesDAO.class);
	
	public String typeCode;
	public String categoryCode;

	public State state;

	public TraceInformation traceInformation;
	public List<Comment> comments = new ArrayList<>(0);

	public Map<String,PropertyValue> properties;

	// Projects ref
	public Set<String> projectCodes;
	// Samples ref
	public Set<String> sampleCodes;

	public String currentExperimentTypeCode;
	public String inputContainerCode;
	public String inputContainerSupportCode;

	public Set<String> outputContainerSupportCodes;
	public Set<String> outputContainerCodes; 
	public Set<String> experimentCodes;

	public SampleOnInputContainer sampleOnInputContainer;

//	// Two creation validation modes and an everything validation mode.
//	/**
//	 * Validate this process (context parameter {@link #FIELD_STATE_CODE}, 
//	 * optional {@link CommonValidationHelper#FIELD_PROCESS_CREATION_CONTEXT}).
//	 * @param contextValidation validation context
//	 */
//	@JsonIgnore
////	@Override
//	@Deprecated
//	public void validate(ContextValidation contextValidation) {
//		// There is no state code provided for any call to this method so it is
//		// supposedly safe to defined a local variable if no call requires a hidden 
//		// parameter.
//		if (contextValidation.getObject(FIELD_STATE_CODE) == null && state != null) {
//			contextValidation.putObject(FIELD_STATE_CODE , state.code);			
//		}
//		String stateCode = state.code;
//		// We need some 
//		if (contextValidation.isCreationMode() 
//				&& contextValidation.getObject(CommonValidationHelper.FIELD_PROCESS_CREATION_CONTEXT).equals(CommonValidationHelper.VALUE_PROCESS_CREATION_CONTEXT_COMMON)) {
//			ProcessValidationHelper.validateProcessTypeRequired        (contextValidation, typeCode, properties);
//			ProcessValidationHelper.validateProcessCategoryCodeRequired(contextValidation, categoryCode);
//			ProcessValidationHelper.validateTraceInformationRequired   (contextValidation, traceInformation);
//			
//		} else if (contextValidation.isCreationMode() 
//				&& contextValidation.getObject(CommonValidationHelper.FIELD_PROCESS_CREATION_CONTEXT).equals(CommonValidationHelper.VALUE_PROCESS_CREATION_CONTEXT_SPECIFIC)) {
//			ProcessValidationHelper.validateIdPrimary                  (contextValidation, this);
//			ProcessValidationHelper.validateCodePrimary                (contextValidation, this, InstanceConstants.PROCESS_COLL_NAME);
//			
//			ProcessValidationHelper.validateStateRequired              (contextValidation, typeCode, state);
////			ProcessValidationHelper.validateContainerCodeRequired      (contextValidation, inputContainerCode, "inputContainerCode");
//			ProcessValidationHelper.validateContainerCodeRequired      (contextValidation, inputContainerCode, "inputContainerCode", stateCode);
////			ProcessValidationHelper.validateContainerSupportCode       (contextValidation, inputContainerSupportCode, "inputContainerSupportCode");
//			ProcessValidationHelper.validateContainerSupportCode       (contextValidation, inputContainerSupportCode, "inputContainerSupportCode", stateCode);
//			
//			ProcessValidationHelper.validateProjectCodes               (contextValidation, projectCodes);
//			ProcessValidationHelper.validateSampleCodes                (contextValidation, sampleCodes);
////			ProcessValidationHelper.validateSampleOnInputContainer     (contextValidation, sampleOnInputContainer);
//			ProcessValidationHelper.validateSampleOnInputContainer     (contextValidation, sampleOnInputContainer, stateCode);
//		} else {
//			ProcessValidationHelper.validateIdPrimary                        (contextValidation, this);
//			ProcessValidationHelper.validateCodePrimary                      (contextValidation, this, InstanceConstants.PROCESS_COLL_NAME);
//			
//			ProcessValidationHelper.validateProcessTypeRequired              (contextValidation, typeCode, properties);
//			ProcessValidationHelper.validateProcessCategoryCodeRequired      (contextValidation, categoryCode);
//			ProcessValidationHelper.validateStateRequired                    (contextValidation, typeCode, state);
//			ProcessValidationHelper.validateTraceInformationRequired         (contextValidation, traceInformation);
////			ProcessValidationHelper.validateContainerCodeRequired            (contextValidation, inputContainerCode, "inputContainerCode");
//			ProcessValidationHelper.validateContainerCodeRequired            (contextValidation, inputContainerCode, "inputContainerCode", stateCode);
////			ProcessValidationHelper.validateContainerSupportCode             (contextValidation, inputContainerSupportCode, "inputContainerSupportCode");
//			ProcessValidationHelper.validateContainerSupportCode             (contextValidation, inputContainerSupportCode, "inputContainerSupportCode", stateCode);
//			
//			ProcessValidationHelper.validateProjectCodes                     (contextValidation, projectCodes);
//			ProcessValidationHelper.validateSampleCodes                      (contextValidation, sampleCodes);
//			ProcessValidationHelper.validateCurrentExperimentTypeCodeOptional(contextValidation, currentExperimentTypeCode);		
////			ProcessValidationHelper.validateSampleOnInputContainer           (contextValidation, sampleOnInputContainer);
//			ProcessValidationHelper.validateSampleOnInputContainer           (contextValidation, sampleOnInputContainer, stateCode);
//		}
//		//ProcessValidationHelper.validateExperimentCodes(experimentCodes, contextValidation);
//	}
	
	/**
	 * Name is kept to match the original definition of the creation validation
	 * type as defined by {@link CommonValidationHelper#VALUE_PROCESS_CREATION_CONTEXT_COMMON}). 
	 * @param contextValidation validation context
	 */
	public void validateCreationCommon(ContextValidation contextValidation) {
		ContextValidation.assertMode(contextValidation, Mode.CREATION);
		ProcessValidationHelper.validateProcessTypeRequired        (contextValidation, typeCode, properties);
		ProcessValidationHelper.validateProcessCategoryCodeRequired(contextValidation, categoryCode);
		CommonValidationHelper .validateTraceInformationRequired   (contextValidation, traceInformation);		
	}
	
	/**
	 * Name is kept to match the original definition of the creation validation
	 * type as defined by {@link CommonValidationHelper#VALUE_PROCESS_CREATION_CONTEXT_SPECIFIC}). 
	 * @param contextValidation validation context
	 */
	public void validateCreationSpecific(ContextValidation contextValidation) {	
		String stateCode = state.code;
		ContextValidation.assertMode(contextValidation, Mode.CREATION);
		CommonValidationHelper .validateIdPrimary                  (contextValidation, this);
		CommonValidationHelper .validateCodePrimary                (contextValidation, this, InstanceConstants.PROCESS_COLL_NAME);
		CommonValidationHelper .validateStateRequired              (contextValidation, typeCode, state);
		ProcessValidationHelper.validateContainerCodeRequired      (contextValidation, inputContainerCode, "inputContainerCode", stateCode);
		ProcessValidationHelper.validateContainerSupportCode       (contextValidation, inputContainerSupportCode, "inputContainerSupportCode", stateCode);
		CommonValidationHelper .validateProjectCodes               (contextValidation, projectCodes);
		CommonValidationHelper .validateSampleCodes                (contextValidation, sampleCodes);
		ProcessValidationHelper.validateSampleOnInputContainer     (contextValidation, sampleOnInputContainer, stateCode);
	}
	
	public void validateUpdate(ContextValidation contextValidation) {
		String stateCode = state.code;
		ContextValidation.assertMode(contextValidation, Mode.UPDATE);	
		CommonValidationHelper .validateIdPrimary                        (contextValidation, this);
		CommonValidationHelper .validateCodePrimary                      (contextValidation, this, InstanceConstants.PROCESS_COLL_NAME);
		ProcessValidationHelper.validateProcessTypeRequired              (contextValidation, typeCode, properties);
		ProcessValidationHelper.validateProcessCategoryCodeRequired      (contextValidation, categoryCode);
		CommonValidationHelper .validateStateRequired                    (contextValidation, typeCode, state);
		CommonValidationHelper .validateTraceInformationRequired         (contextValidation, traceInformation);
		ProcessValidationHelper.validateContainerCodeRequired            (contextValidation, inputContainerCode, "inputContainerCode", stateCode);
		ProcessValidationHelper.validateContainerSupportCode             (contextValidation, inputContainerSupportCode, "inputContainerSupportCode", stateCode);
		CommonValidationHelper .validateProjectCodes                     (contextValidation, projectCodes);
		CommonValidationHelper .validateSampleCodes                      (contextValidation, sampleCodes);
		ProcessValidationHelper.validateCurrentExperimentTypeCodeOptional(contextValidation, currentExperimentTypeCode);		
		ProcessValidationHelper.validateSampleOnInputContainer           (contextValidation, sampleOnInputContainer, stateCode);
	}

	@JsonIgnore
	public Process cloneCommon() {
		Process p      = new Process();
		p.typeCode     = typeCode;
		p.categoryCode = categoryCode;
		if (properties != null && properties.size() > 0) {
			p.properties = new HashMap<>(properties);
		}
		p.sampleCodes               = sampleCodes;
		p.projectCodes              = projectCodes;
		p.traceInformation          = traceInformation;
		p.inputContainerSupportCode = inputContainerSupportCode;
		p.inputContainerCode        = inputContainerCode;
		p.state                     = state;
		p.comments                  = comments;
		return p;
	}

}
