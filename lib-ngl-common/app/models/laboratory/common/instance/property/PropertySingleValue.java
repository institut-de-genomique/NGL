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
	
//	@Override
//	public void validate(ContextValidation contextValidation) {
//		logger.debug("before super validate {} {}", contextValidation.errorCount(), this);
//		super.validate(contextValidation);
//		logger.debug("after super validate {} {}", contextValidation.errorCount(), this);
//		PropertyDefinition propertyDefinition = first(contextValidation.<Collection<PropertyDefinition>>getTypedObject("propertyDefinitions")).orElse(null);
//		if (ValidationHelper.checkIfActive(contextValidation, propertyDefinition)
////				&& PropertySingleValue.validateProperty(contextValidation, propertyDefinition, this)
//				&& validate(contextValidation, propertyDefinition)
////				&& PropertySingleValue.convertPropertyValue(contextValidation, this, propertyDefinition)) {
//				&& convertValue(contextValidation, propertyDefinition)) {
////			PropertySingleValue.validatePropertyBounds(contextValidation, propertyDefinition, this);
//			validateBounds(contextValidation, propertyDefinition);
//			// TODO: FORMAT AND UNIT
//		}
//	}
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
			// TODO: FORMAT AND UNIT
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

//	public static boolean convertPropertyValue(ContextValidation contextValidation, PropertySingleValue propertyValue, PropertyDefinition propertyDefinition) {
//		try {
//			propertyValue.value = ValidationHelper.dynamicCast(propertyDefinition.valueType, ValidationHelper.cleanValue(propertyValue.value));
//			if (propertyDefinition.saveMeasureValue != null && propertyValue.unit == null)
//				propertyValue.unit = propertyDefinition.saveMeasureValue.value; 
//			return true;
//		} catch (Exception e) {
//			logger.error(e.getMessage(),e);
//			contextValidation.addError(propertyDefinition.code+".value", ERROR_BADTYPE_MSG, propertyDefinition.valueType, propertyValue.value);
//			return false;
//		}
//	}
	
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

//	/**
//	 * Validate that a property single value conforms to a property definition.
//	 * @param contextValidation  validation context
//	 * @param propertyDefinition property definition
//	 * @param propertyValue      property value
//	 * @return true of the value conforms to the definition, false otherwise
//	 */
//	public static boolean validateProperty(ContextValidation contextValidation, PropertyDefinition propertyDefinition, PropertySingleValue propertyValue) {
//		return ValidationHelper.validatePropertyCore(contextValidation, propertyDefinition, propertyValue,
//				() -> ValidationHelper.validateNotEmpty(contextValidation, propertyValue.value, propertyDefinition.code + ".value"));
//	}
	
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

//	/**
//	 * Validates that the property value is within the bounds of the property definition.
//	 * @param contextValidation  validation context
//	 * @param propertyDefinition property definition
//	 * @param propertyValue      property value
//	 * @return                   true if the property value is within the bounds of the definition, false otherwise
//	 */
//	public static boolean validatePropertyBounds(ContextValidation contextValidation, PropertyDefinition propertyDefinition, PropertySingleValue propertyValue){
//		if (!propertyDefinition.choiceInList)
//			return true;
//		if (!ValidationHelper.checkIfExistInTheList(propertyDefinition, propertyValue.value)) {
//			contextValidation.addError(propertyDefinition.code + ".value", ERROR_VALUENOTAUTHORIZED_MSG, propertyValue.value);
//			return false;
//		} else {
//			return true;
//		}
//	}
	
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
