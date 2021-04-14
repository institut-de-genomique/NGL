package models.laboratory.common.instance.property;

import static fr.cea.ig.lfw.utils.Iterables.first;

import java.util.Collection;

import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.instance.PropertyValue;
import validation.ContextValidation;
import validation.utils.ValidationHelper;


/**
 * Property value that is a byte array.
 */
public class PropertyByteValue extends PropertyValue {
	
	public PropertyByteValue(String _type) {
		super(_type);
	}
	
	public PropertyByteValue(String _type, byte[] value) {
		super(_type, value);		
	}

	@Override
	public byte[] getValue() {
		return byteValue();
	}
	
	// This overrides the Object definition in the parent class so the 
	// type is effectively a byte array when seen by jackson.
	public void setValue(byte[] b) {
		value = b;
	}
	
	public byte[] byteValue() {
		// If the value is a string we could assume that it is in fact a base64
		// encoded byte array.
		if (! (value instanceof byte[]))
			throw new RuntimeException("value in " + this + " is not a byte[] : " + value.getClass());
		return (byte[])value;
	}
	
	@Override
	public String toString() {
		return "PropertyByteValue [value=" + value + ", class=" + value.getClass().getName() + "]";
	}
	
//	@Override
//	public void validate(ContextValidation contextValidation) { 
//		super.validate(contextValidation);
//		PropertyDefinition propertyDefinition = first(contextValidation.<Collection<PropertyDefinition>>getTypedObject("propertyDefinitions")).orElse(null);
//		if (ValidationHelper.checkIfActive(contextValidation, propertyDefinition)) {
////			PropertyByteValue.validateProperty(contextValidation, propertyDefinition, this); 
//			validate(contextValidation, propertyDefinition); 
//		}		
//	}
	@Override
	public void validate(ContextValidation contextValidation, Collection<PropertyDefinition> propertyDefinitions) { 
		super.validate(contextValidation, propertyDefinitions);
		PropertyDefinition propertyDefinition = first(propertyDefinitions).orElse(null);
		if (ValidationHelper.checkIfActive(contextValidation, propertyDefinition)) 
			validate(contextValidation, propertyDefinition); 
	}

//	/**
//	 * Validate that a property byte value conforms to a property definition 
//	 * @param contextValidation  validation context
//	 * @param propertyDefinition property definition
//	 * @param propertyValue      property value
//	 * @return true if the value conforms to the definition, false otherwise
//	 */
//	private static boolean validateProperty(ContextValidation contextValidation, PropertyDefinition propertyDefinition, PropertyByteValue propertyValue) {
//		return ValidationHelper.validatePropertyCore(contextValidation, propertyDefinition, propertyValue,
//				() -> ValidationHelper.validateNotEmpty(contextValidation, propertyValue.value, propertyDefinition.code + ".value"));
//	}
	
	/**
	 * Validate that this property byte value conforms to a property definition 
	 * @param contextValidation  validation context
	 * @param propertyDefinition property definition
	 * @return true if the value conforms to the definition, false otherwise
	 */
	private boolean validate(ContextValidation contextValidation, PropertyDefinition propertyDefinition) {
		return validateProperty(contextValidation, propertyDefinition,
				() -> ValidationHelper.validateNotEmpty(contextValidation, value, propertyDefinition.code + ".value"));
	}
		
}
