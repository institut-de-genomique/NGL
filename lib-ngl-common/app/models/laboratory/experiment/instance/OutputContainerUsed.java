package models.laboratory.experiment.instance;

import models.laboratory.common.description.Level;
import models.laboratory.common.instance.Comment;
import models.laboratory.instrument.instance.InstrumentUsed;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.container.instance.ContainerValidationHelper;
import validation.experiment.instance.ContainerUsedValidationHelper;

public class OutputContainerUsed extends AbstractContainerUsed {
		
	public Comment comment;
	
	public OutputContainerUsed() {
	}
	
	public OutputContainerUsed(String code) {
		super(code);		
	}
	
	/**
	 * Validate this output container used (context parameters 
	 * {@link CommonValidationHelper#FIELD_TYPE_CODE} {@link CommonValidationHelper#FIELD_STATE_CODE}
	 * {@link CommonValidationHelper#FIELD_INST_USED} {@link CommonValidationHelper#FIELD_IMPORT_TYPE_CODE}).
	 */
	@Deprecated
	@Override
	public void validate(ContextValidation contextValidation) {
		ContainerUsedValidationHelper.validateOutputContainerCode         (contextValidation, code);		
		ContainerUsedValidationHelper.validateLocationOnSupportOnContainer(contextValidation, locationOnContainerSupport);		
		ContainerUsedValidationHelper.validateOutputContainerCategoryCode (contextValidation, categoryCode);		
		ContainerUsedValidationHelper.validateOutputContents              (contextValidation, contents);		
		ContainerValidationHelper    .validateVolumeOptional              (contextValidation, volume);		
		ContainerValidationHelper    .validateSizeOptional                (contextValidation, size);
		ContainerValidationHelper    .validateConcentrationOptional       (contextValidation, concentration);		
		ContainerValidationHelper    .validateQuantityOptional            (contextValidation, quantity);		
		ContainerUsedValidationHelper.validateExperimentProperties        (contextValidation, experimentProperties, Level.CODE.ContainerOut);		
		ContainerUsedValidationHelper.validateInstrumentProperties        (contextValidation, instrumentProperties, Level.CODE.ContainerOut);		
	}
	
	/**
	 * Validate this output container.
	 * @param contextValidation  validation context
	 * @param stateCode          state code
	 * @param instrumentUsed     instrument used
	 * @param importTypeCode     optional import type code
	 * @param experimentTypeCode experiment type code
	 */
	public void validate(ContextValidation contextValidation, String stateCode, InstrumentUsed instrumentUsed, String importTypeCode, String experimentTypeCode) {
		ContainerUsedValidationHelper.validateOutputContainerCode         (contextValidation, code, stateCode);
		ContainerUsedValidationHelper.validateLocationOnSupportOnContainer(contextValidation, locationOnContainerSupport, instrumentUsed, stateCode);
		ContainerUsedValidationHelper.validateOutputContainerCategoryCode (contextValidation, categoryCode, instrumentUsed);
		ContainerUsedValidationHelper.validateOutputContents              (contextValidation, contents, stateCode, importTypeCode); 
		ContainerValidationHelper    .validateVolumeOptional              (contextValidation, volume);
		ContainerValidationHelper    .validateSizeOptional                (contextValidation, size);
		ContainerValidationHelper    .validateConcentrationOptional       (contextValidation, concentration);
		ContainerValidationHelper    .validateQuantityOptional            (contextValidation, quantity);
		ContainerUsedValidationHelper.validateExperimentProperties        (contextValidation, experimentProperties, Level.CODE.ContainerOut, experimentTypeCode, stateCode);
		ContainerUsedValidationHelper.validateInstrumentProperties        (contextValidation, instrumentProperties, Level.CODE.ContainerOut, instrumentUsed, stateCode);
	}
}
