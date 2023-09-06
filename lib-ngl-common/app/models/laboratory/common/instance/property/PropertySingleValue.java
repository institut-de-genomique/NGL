package models.laboratory.common.instance.property;

import static fr.cea.ig.lfw.utils.Hashing.hash;
import static fr.cea.ig.lfw.utils.Equality.objectEquals;
import static fr.cea.ig.lfw.utils.Equality.typedEquals;
import static fr.cea.ig.lfw.utils.Iterables.first;
import static validation.utils.ValidationConstants.ERROR_BADTYPE_MSG;
import static validation.utils.ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG;

import java.util.Collection;

import validation.ContextValidation;
import validation.utils.ValidationHelper;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.instance.PropertyValue;

/*
 * Object used in Map of key/value to stored value with its unit
 * if unit is null, it will not stored in MongoDB.
 */
/**
 * Property with an object as value and a unit (e.g: PropertySingleValue { value=1, unit="m/s"}).
 * 
 * @author mhaquell
 *
 */
public class PropertySingleValue extends PropertyValue {

	private static final play.Logger.ALogger logger = play.Logger.of(PropertySingleValue.class);
	
	public String unit;
	
	public PropertySingleValue() {
		super(PropertyValue.singleType);
	}
	
	public PropertySingleValue(Object value) {
		super(PropertyValue.singleType, value);	
	}
	
	public PropertySingleValue(Object value, String unit) {
		super(PropertyValue.singleType, value);
		this.unit = unit;
	}
	
	@Override
	public String toString() {
		return "PropertySingleValue[value=" + value + ", unit=" + unit +  ", class=" + value.getClass().getName() + "]";
	}
	
	/**
	 * Validates against the first definition of the collection.
	 */
	@Override
	public void validate(ContextValidation contextValidation, Collection<PropertyDefinition> propertyDefinitions) {
		logger.debug("before super validate {} {}", contextValidation.errorCount(), this);
		super.validate(contextValidation, propertyDefinitions);
		logger.debug("after super validate {} {}", contextValidation.errorCount(), this);
		PropertyDefinition propertyDefinition = first(propertyDefinitions).orElse(null);
		if (ValidationHelper.checkIfActive(contextValidation, propertyDefinition)
				          && validate     (contextValidation, propertyDefinition)
				          && convertValue (contextValidation, propertyDefinition)) {
			validateBounds(contextValidation, propertyDefinition);
			// GA: FORMAT AND UNIT
		}
	}
	
	@Override
	public int hashCode() {
		return hash(super.hashCode(), unit);
	}
	
	@Override
	public boolean equals(Object obj) {
		return typedEquals(PropertySingleValue.class, this, obj,
				           (x,y) -> super.equals(obj) && objectEquals(x.unit,y.unit));
	}

	/**
	 * Converts the property value to the property definition defined type (and unit).
	 * @param contextValidation  validation context
	 * @param propertyDefinition property definition
	 * @return                   true of the conversion was successful, false otherwise
	 */
	private boolean convertValue(ContextValidation contextValidation, PropertyDefinition propertyDefinition) {
		try {
			value = ValidationHelper.dynamicCast(propertyDefinition.valueType, ValidationHelper.cleanValue(value));
			if (propertyDefinition.saveMeasureValue != null && unit == null)
				unit = propertyDefinition.saveMeasureValue.value; 
			return true;
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			contextValidation.addError(propertyDefinition.code+".value", ERROR_BADTYPE_MSG, propertyDefinition.valueType, value);
			return false;
		}
	}

	/**
	 * Validate that a property single value conforms to a property definition.
	 * @param contextValidation  validation context
	 * @param propertyDefinition property definition
	 * @return true of the value conforms to the definition, false otherwise
	 */
	private boolean validate(ContextValidation contextValidation, PropertyDefinition propertyDefinition) {
		return validateProperty(contextValidation, propertyDefinition,
				() -> ValidationHelper.validateNotEmpty(contextValidation, value, propertyDefinition.code + ".value"));
	}

	/**
	 * Validates that the property value is within the bounds of the property definition.
	 * @param contextValidation  validation context
	 * @param propertyDefinition property definition
	 * @return                   true if the property value is within the bounds of the definition, false otherwise
	 */
	private boolean validateBounds(ContextValidation contextValidation, PropertyDefinition propertyDefinition) {
		if (!propertyDefinition.choiceInList)
			return true;
		if (ValidationHelper.checkIfExistInTheList(propertyDefinition, value))
			return true;
		contextValidation.addError(propertyDefinition.code + ".value", ERROR_VALUENOTAUTHORIZED_MSG, value);
		return false;
	}
	
}
