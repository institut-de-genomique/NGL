package models.laboratory.experiment.instance;

import java.util.Set;

import models.laboratory.common.description.Level;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.common.instance.Valuation;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.instrument.instance.InstrumentUsed;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.container.instance.ContainerValidationHelper;
import validation.experiment.instance.ContainerUsedValidationHelper;

public class InputContainerUsed extends AbstractContainerUsed {
	
	public Double percentage; // percentage of input in the final output
	
	public Set<String> projectCodes; 
	public Set<String> sampleCodes; 
	public Set<String> fromTransformationTypeCodes;
	public Set<String> fromTransformationCodes;
	public Set<String> processTypeCodes;
	public Set<String> processCodes;
	
	public String fromPurificationTypeCode;
	public String fromTransfertTypeCode;
	
	/* used in QualityControl to copy new attribute into input container attribute */
	public PropertySingleValue newVolume;        
	public PropertySingleValue newConcentration; 
	public PropertySingleValue newQuantity; 	
	public PropertySingleValue newSize; 
	
	public Valuation valuation; // only on input because qc are to-void experiment !
	public TBoolean copyValuationToInput = TBoolean.UNSET;
	// keep for some HTML page pool or flowcell
	public State state;
	
	public InputContainerUsed() {
	}
	
	public InputContainerUsed(String code) {
		super(code);
	}

	/**
	 * Validate this input container used
	 * (context parameters {@link CommonValidationHelper#FIELD_TYPE_CODE} {@link CommonValidationHelper#FIELD_STATE_CODE}
	 * {@link CommonValidationHelper#FIELD_INST_USED}).
	 * @deprecated use {@link #validate(ContextValidation, String, String, InstrumentUsed)}
	 */
	@Deprecated
	@Override
	public void validate(ContextValidation contextValidation) {
//		@SuppressWarnings("deprecation")
//		Container container = ContainerUsedValidationHelper.validateExistInstanceCode(contextValidation, code, Container.class, InstanceConstants.CONTAINER_COLL_NAME, true);
		Container container = CommonValidationHelper.validateCodeForeignOptional(contextValidation, Container.find.get(), code, true);		
		ContainerUsedValidationHelper.validateInputContainerMatchesContainer    (contextValidation, this, container);
		ContainerUsedValidationHelper.validateInputContainerCategoryCodeRequired(contextValidation, categoryCode);
		ContainerValidationHelper.    validateVolumeOptional                    (contextValidation, volume);
		ContainerValidationHelper.    validateConcentrationOptional             (contextValidation, concentration);
		ContainerValidationHelper.    validateQuantityOptional                  (contextValidation, quantity);
		ContainerUsedValidationHelper.validatePercentage                        (contextValidation, percentage);		
		ContainerValidationHelper.    validateSizeOptional                      (contextValidation, size);
		ContainerUsedValidationHelper.validateExperimentProperties              (contextValidation, experimentProperties, Level.CODE.ContainerIn);
		ContainerUsedValidationHelper.validateInstrumentProperties              (contextValidation, instrumentProperties, Level.CODE.ContainerIn);
	}
	
	public void validate(ContextValidation contextValidation, String experimentTypeCode, String stateCode, InstrumentUsed instrumentUsed) {
//		@SuppressWarnings("deprecation")
//		Container container = ContainerUsedValidationHelper.validateExistInstanceCode(contextValidation, code, Container.class, InstanceConstants.CONTAINER_COLL_NAME, true);
		Container container = CommonValidationHelper.validateCodeForeignOptional(contextValidation, Container.find.get(), code, true);		
		ContainerUsedValidationHelper.validateInputContainerMatchesContainer    (contextValidation, this, container);
		ContainerUsedValidationHelper.validateInputContainerCategoryCodeRequired(contextValidation, categoryCode);
		ContainerValidationHelper.    validateVolumeOptional                    (contextValidation, volume);
		ContainerValidationHelper.    validateConcentrationOptional             (contextValidation, concentration);
		ContainerValidationHelper.    validateQuantityOptional                  (contextValidation, quantity);
		ContainerUsedValidationHelper.validatePercentage                        (contextValidation, percentage);		
		ContainerValidationHelper.    validateSizeOptional                      (contextValidation, size);
		ContainerUsedValidationHelper.validateExperimentProperties              (contextValidation, experimentProperties, Level.CODE.ContainerIn, experimentTypeCode, stateCode);
		ContainerUsedValidationHelper.validateInstrumentProperties              (contextValidation, instrumentProperties, Level.CODE.ContainerIn, instrumentUsed, stateCode);
	}
	
}
