package models.laboratory.experiment.instance;

import models.laboratory.container.description.ContainerSupportCategory;
import models.laboratory.instrument.instance.InstrumentUsed;
import validation.ContextValidation;
import validation.experiment.instance.AtomicTransfertMethodValidationHelper;

/**
 * One input container and no output container. 
 */
public class OneToVoidContainer extends AtomicTransfertMethod {

	@Deprecated
	@Override
	public void validate(ContextValidation contextValidation) {
		super.validate(contextValidation);
		AtomicTransfertMethodValidationHelper.validateInputContainerOne  (contextValidation, inputContainerUseds);
		AtomicTransfertMethodValidationHelper.validateOutputContainerNone(contextValidation, outputContainerUseds);
	}
	
	/**
	 * Validate that there is a single input container and no output container.
	 */
	@Override
	public void validate(ContextValidation contextValidation, String experimentTypeCode, String stateCode, InstrumentUsed instrumentUsed, String importTypeCode) {
		super.validate(contextValidation, experimentTypeCode, stateCode, instrumentUsed, importTypeCode);
		AtomicTransfertMethodValidationHelper.validateInputContainerOne  (contextValidation, inputContainerUseds);
		AtomicTransfertMethodValidationHelper.validateOutputContainerNone(contextValidation, outputContainerUseds);
	}

	@Override
	public void updateOutputCodeIfNeeded(ContainerSupportCategory outputCsc, String supportCode) {
	}
	
	@Override
	public void removeOutputContainerCode() {
	}

}
