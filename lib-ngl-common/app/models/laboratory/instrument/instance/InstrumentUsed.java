package models.laboratory.instrument.instance;


import validation.ContextValidation;
import validation.IValidation;
import validation.experiment.instance.InstrumentUsedValidationHelper;

import com.fasterxml.jackson.annotation.JsonIgnore;

import models.laboratory.container.description.ContainerSupportCategory;
import models.laboratory.instrument.description.Instrument;
import models.laboratory.instrument.description.InstrumentUsedType;
import models.laboratory.instrument.description.InstrumentCategory;

public class InstrumentUsed implements IValidation {

	/**
	 * Required {@link Instrument} reference.
	 */
	public String code;

	/**
	 * Required {@link InstrumentUsedType} reference.
	 */
	public String typeCode;
	
	/**
	 * Required {@link InstrumentCategory} reference.
	 */
	public String categoryCode;
	
	/**
	 * Required input {@link ContainerSupportCategory} reference.
	 */
	public String inContainerSupportCategoryCode;
	
	/**
	 * Required output {@link ContainerSupportCategory} reference.
	 */
	public String outContainerSupportCategoryCode;
	
	/**
	 * Validate this instance (all fields are required).
	 */
	@JsonIgnore
	@Override
	public void validate(ContextValidation contextValidation) {
		InstrumentUsedValidationHelper.validateInstrumentUsedTypeCodeRequired         (contextValidation, typeCode);
		InstrumentUsedValidationHelper.validateInstrumentCodeRequired                 (contextValidation, code);
		InstrumentUsedValidationHelper.validateInstrumentCategoryCodeRequired         (contextValidation, categoryCode);
		InstrumentUsedValidationHelper.validateInContainerSupportCategoryCodeRequired (contextValidation, inContainerSupportCategoryCode);
		InstrumentUsedValidationHelper.validateOutContainerSupportCategoryCodeRequired(contextValidation, outContainerSupportCategoryCode);		
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((categoryCode == null) ? 0 : categoryCode.hashCode());
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result
				+ ((inContainerSupportCategoryCode == null) ? 0 : inContainerSupportCategoryCode.hashCode());
		result = prime * result
				+ ((outContainerSupportCategoryCode == null) ? 0 : outContainerSupportCategoryCode.hashCode());
		result = prime * result + ((typeCode == null) ? 0 : typeCode.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InstrumentUsed other = (InstrumentUsed) obj;
		if (categoryCode == null) {
			if (other.categoryCode != null)
				return false;
		} else if (!categoryCode.equals(other.categoryCode))
			return false;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		if (inContainerSupportCategoryCode == null) {
			if (other.inContainerSupportCategoryCode != null)
				return false;
		} else if (!inContainerSupportCategoryCode.equals(other.inContainerSupportCategoryCode))
			return false;
		if (outContainerSupportCategoryCode == null) {
			if (other.outContainerSupportCategoryCode != null)
				return false;
		} else if (!outContainerSupportCategoryCode.equals(other.outContainerSupportCategoryCode))
			return false;
		if (typeCode == null) {
			if (other.typeCode != null)
				return false;
		} else if (!typeCode.equals(other.typeCode))
			return false;
		return true;
	}

}
