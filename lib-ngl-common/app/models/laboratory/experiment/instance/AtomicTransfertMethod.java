package models.laboratory.experiment.instance;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import models.laboratory.common.instance.Comment;
import models.laboratory.container.description.ContainerSupportCategory;
import models.laboratory.instrument.instance.InstrumentUsed;
import validation.ContextValidation;
import validation.IValidation;
import validation.experiment.instance.AtomicTransfertMethodValidationHelper;

/**
 * Looks like a containers to containers mapping. Classes names are referenced from the
 * description database.
 */
@JsonTypeInfo(use=Id.NAME, include=As.PROPERTY, property="class", defaultImpl= models.laboratory.experiment.instance.OneToOneContainer.class)
@JsonSubTypes({
	@JsonSubTypes.Type(value =  models.laboratory.experiment.instance.ManyToOneContainer.class, name = "ManyToOne"),
	@JsonSubTypes.Type(value =  models.laboratory.experiment.instance.OneToManyContainer.class, name = "OneToMany"),
	@JsonSubTypes.Type(value =  models.laboratory.experiment.instance.OneToOneContainer .class, name = "OneToOne"),
	@JsonSubTypes.Type(value =  models.laboratory.experiment.instance.OneToVoidContainer.class, name = "OneToVoid")
})
public abstract class AtomicTransfertMethod implements IValidation {
//public abstract class AtomicTransfertMethod {

	public Integer                   viewIndex;            // use in rules validation to have the position+1 in the list of ATM.	
	public List<InputContainerUsed>  inputContainerUseds;
	public List<OutputContainerUsed> outputContainerUseds;
	public String                    line;                 // is equal to outputSupportContainerLine
	public String                    column;               // is equal to outputSupportContainerColumn
	public Comment                   comment;
	
	public abstract void updateOutputCodeIfNeeded(ContainerSupportCategory outputCsc, String supportCode);
	
	// As a list is used to store the output containers used, there is no need to provide
	// this as an abstract method. A standard implementation is enough.
//	public void removeOutputContainerCode() {
//		outputContainerUseds.forEach(ocu -> ocu.code = null);
//	}
	public abstract void removeOutputContainerCode() ;
	
	@Deprecated
	@Override
	public void validate(ContextValidation contextValidation) {
		AtomicTransfertMethodValidationHelper.validateLineAndColumn(contextValidation, line, column);
		AtomicTransfertMethodValidationHelper.validateInputContainers(contextValidation, inputContainerUseds);
	}
	
	/**
	 * Validate this transfer method.
	 * @param contextValidation  validation context
	 * @param experimentTypeCode experiment type code 
	 * @param stateCode          state code
	 * @param instrumentUsed     instrument used
	 * @param importTypeCode     optional import type code
	 */
	public void validate(ContextValidation contextValidation, String experimentTypeCode, String stateCode, InstrumentUsed instrumentUsed, String importTypeCode) {
		AtomicTransfertMethodValidationHelper.validateLineAndColumn  (contextValidation, stateCode, line, column);
		AtomicTransfertMethodValidationHelper.validateInputContainers(contextValidation, inputContainerUseds, experimentTypeCode, stateCode, instrumentUsed);
	}
	
}
