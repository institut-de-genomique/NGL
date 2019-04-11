package models.laboratory.common.instance.property;

import static fr.cea.ig.lfw.utils.Equality.objectEquals;
import static fr.cea.ig.lfw.utils.Equality.typedEquals;
import static fr.cea.ig.lfw.utils.Hashing.hash;
import static validation.utils.ValidationConstants.ERROR_BADTYPE_MSG;
import static validation.utils.ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.instance.PropertyValue;
import validation.ContextValidation;
import validation.utils.ValidationHelper;

/*
 * used to stock a complex object
 * an object is stock in Map with key = property and value = value of property
 */
/**
 * Property value whose value is a map that is used as a java bean like structure
 * ({@literal Map<property name, property value>}), could have been named PropertyMapValue. 
 */
public class PropertyObjectValue extends PropertyValue {
	
	private static final play.Logger.ALogger logger = play.Logger.of(PropertyObjectValue.class);
	
	public Map<String,String> unit;
	
	public PropertyObjectValue() {
		super(PropertyValue.objectType);		
	}
	
	public PropertyObjectValue(Map<String, Object> value) {
		super(PropertyValue.objectType, value);		
	}
	
	public PropertyObjectValue(Map<String, Object> value, Map<String,String> unit) {
		super(PropertyValue.objectType, value);
		this.unit = unit;
	}
	
	@Override
	public Map<String,Object> getValue() {
		return mapValue(); 
	}
	
	@SuppressWarnings("unchecked") // value field could be well typed but this requires that the field is moved in subclasses of PropertyValue. 
	public Map<String, Object> mapValue() {
		return (Map<String, Object>)value;
	}
	
	@Override
	public String toString() {
		return "PropertyObjectValue [value=" + value + ", unit=" + unit + ", class="+value.getClass().getName()+"]";
	}
	
//	@Override
//	public void validate(ContextValidation contextValidation) {
//		super.validate(contextValidation);
//		for (PropertyDefinition propertyDefinition : contextValidation.<Collection<PropertyDefinition>>getTypedObject("propertyDefinitions")) {
//			if (ValidationHelper.checkIfActive(contextValidation, propertyDefinition) 
////						&& PropertyObjectValue.validateProperty(contextValidation, propertyDefinition, this)
//					    && validate(contextValidation, propertyDefinition)
////						&& convertPropertyValue(contextValidation, this, propertyDefinition)) {
//						&& convertValue(contextValidation, propertyDefinition)) {
////				PropertyObjectValue.validatePropertyBounds(contextValidation, propertyDefinition, this);
//				validateBounds(contextValidation, propertyDefinition);
//				// TODO: FORMAT AND UNIT
//			}
//		}
//	}
	
	@Override
	public void validate(ContextValidation contextValidation, Collection<PropertyDefinition> propertyDefinitions) {
		super.validate(contextValidation, propertyDefinitions);
		for (PropertyDefinition propertyDefinition : propertyDefinitions) {
			if (ValidationHelper.checkIfActive(contextValidation, propertyDefinition) 
					    && validate(contextValidation, propertyDefinition)
						&& convertValue(contextValidation, propertyDefinition)) {
				validateBounds(contextValidation, propertyDefinition);
				// TODO: FORMAT AND UNIT
			}
		}
	}

	@Override
	public int hashCode() {
		return hash(super.hashCode(),unit);
	}

	@Override
	public boolean equals(Object obj) {
		return typedEquals(PropertyObjectValue.class, this, obj,
				           (x,y) -> super.equals(obj) && objectEquals(x.unit,y.unit));
	}

//	/**
//	 * Convert the value designated by a property definition to a value of the property definition
//	 * defined type.
//	 * @param contextValidation  validation context
//	 * @param propertyValue      value that hold the map where to find the value to convert
//	 * @param propertyDefinition property definition
//	 * @return                   true if the conversion was successful, false otherwise
//	 */
//	private static boolean convertPropertyValue(ContextValidation contextValidation, PropertyObjectValue propertyValue, PropertyDefinition propertyDefinition) {
//		String[] codes = ValidationHelper.splitCodePropertyDefinition(propertyDefinition);
//		try {
//			String              valueType = propertyDefinition.valueType;
//			String              key       = codes[1];
//			Map<String, Object> map       = propertyValue.getValue();
//			map.put(key, ValidationHelper.dynamicCast(valueType, map.get(key)));
//		} catch (Exception e) {
//			logger.error(e.getMessage(),e);
//			contextValidation.addError(codes[0] + ".value." + codes[1], ERROR_BADTYPE_MSG, propertyDefinition.valueType, propertyValue.value);
//			return false;
//		}
//		return true;
//	}

	/**
	 * Convert the value designated by a property definition to a value of the property definition
	 * defined type.
	 * @param contextValidation  validation context
	 * @param propertyDefinition property definition
	 * @return                   true if the conversion was successful, false otherwise
	 */
	private boolean convertValue(ContextValidation contextValidation, PropertyDefinition propertyDefinition) {
		String[] codes = propertyDefinition.splitCodePropertyDefinition();
		try {
			getValue().compute(codes[1], (k,v) -> ValidationHelper.dynamicCast(propertyDefinition.valueType, v));
			return true;
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			contextValidation.addError(codes[0] + ".value." + codes[1], ERROR_BADTYPE_MSG, propertyDefinition.valueType, value);
			return false;
		}
	}

//	/**
//	 * Validate that a property object value conforms to a property definition.
//	 * @param contextValidation  validation context
//	 * @param propertyDefinition property definition
//	 * @param propertyValue      property value
//	 * @return true if the value conforms to the definition, false otherwise
//	 */
//	public static boolean validateProperty(ContextValidation contextValidation, PropertyDefinition propertyDefinition, PropertyObjectValue propertyValue) {
//		return ValidationHelper.validatePropertyCore(contextValidation, propertyDefinition, propertyValue,
//				() -> {
//					String[] codes = ValidationHelper.splitCodePropertyDefinition(propertyDefinition);
//					Object value = propertyValue.mapValue().get(codes[1]);
//					return ValidationHelper.validateNotEmpty(contextValidation, value, codes[0]+".value."+codes[1]);
//				});
//	}
	/**
	 * Validate that a property object value conforms to a property definition.
	 * @param contextValidation  validation context
	 * @param propertyDefinition property definition
	 * @return true if the value conforms to the definition, false otherwise
	 */
	private boolean validate(ContextValidation contextValidation, PropertyDefinition propertyDefinition) {
		return validateProperty(contextValidation, propertyDefinition,
				() -> {
					String[] codes = propertyDefinition.splitCodePropertyDefinition();
					Object value = mapValue().get(codes[1]);
					return ValidationHelper.validateNotEmpty(contextValidation, value, codes[0]+".value."+codes[1]);
				});
	}

//	/**
//	 * Validate that all the values in the property map that matches the property definition
//	 * name are within the bounds of the definition. 
//	 * @param contextValidation  validation context
//	 * @param propertyDefinition property definition
//	 * @param propertyValue      property value
//	 * @return                   true if the matching values in the property map are within property definition bounds
//	 */
//	public static boolean validatePropertyBounds(ContextValidation contextValidation, PropertyDefinition propertyDefinition, PropertyObjectValue propertyValue) {
//		if (!propertyDefinition.choiceInList) 
//			return true;
//		boolean isOk = true;
//		for (Entry<String, ?> entryValue : propertyValue.mapValue().entrySet()) {
//			Object value = entryValue.getValue();
//			if (propertyDefinition.code.endsWith(entryValue.getKey()) && !ValidationHelper.checkIfExistInTheList(propertyDefinition, value)) {
//				contextValidation.addError(propertyDefinition.code+".value."+entryValue.getKey(), ERROR_VALUENOTAUTHORIZED_MSG, value);
//				isOk = false;
//			}
//		}			
//		return isOk;
//	}
	/**
	 * Validate that all the values in the property map that matches the property definition
	 * name are within the bounds of the definition. 
	 * @param contextValidation  validation context
	 * @param propertyDefinition property definition
	 * @return                   true if the matching values in the property map are within property definition bounds
	 */
	private boolean validateBounds(ContextValidation contextValidation, PropertyDefinition propertyDefinition) {
		if (!propertyDefinition.choiceInList) 
			return true;
		boolean isOk = true;
		for (Entry<String, ?> entryValue : mapValue().entrySet()) {
			Object value = entryValue.getValue();
			if (propertyDefinition.code.endsWith(entryValue.getKey()) && !ValidationHelper.checkIfExistInTheList(propertyDefinition, value)) {
				contextValidation.addError(propertyDefinition.code+".value."+entryValue.getKey(), ERROR_VALUENOTAUTHORIZED_MSG, value);
				isOk = false;
			}
		}			
		return isOk;
	}

}
