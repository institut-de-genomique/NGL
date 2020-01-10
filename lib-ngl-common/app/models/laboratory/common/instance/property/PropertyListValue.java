package models.laboratory.common.instance.property;

import static fr.cea.ig.lfw.utils.Hashing.hash;
import static fr.cea.ig.lfw.utils.Equality.objectEquals;
import static fr.cea.ig.lfw.utils.Equality.typedEquals;
import static fr.cea.ig.lfw.utils.Iterables.first;
import static validation.utils.ValidationConstants.ERROR_BADTYPE_MSG;
import static validation.utils.ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG;

import java.util.Collection;
import java.util.List;

import fr.cea.ig.lfw.utils.Iterables;
import validation.ContextValidation;
import validation.utils.ValidationHelper;

import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.instance.PropertyValue;

/**
 * Property value with a value of type list ({@literal List<?>}) and a unit.
 */
public class PropertyListValue extends PropertyValue {
	
	private static final play.Logger.ALogger logger = play.Logger.of(PropertyListValue.class);
	
	public String unit;
	
	public PropertyListValue() {
		super(PropertyValue.listType);
	}
	
	public PropertyListValue(List<? extends Object> value) {
		super(PropertyValue.listType, value);
	}
	
	public PropertyListValue(List<? extends Object> value, String unit) {
		super(PropertyValue.listType, value);
		this.unit = unit;
	}

	@Override
	public String toString() {
		return "PropertyListValue [value=" + value + ", unit=" + unit + ", class=" + value.getClass().getName() + "]";
	}
	
	@Override
	public void validate(ContextValidation contextValidation, Collection<PropertyDefinition> propertyDefinitions) {
		super.validate(contextValidation, propertyDefinitions);
		PropertyDefinition propertyDefinition = first(propertyDefinitions).orElse(null);
		if (ValidationHelper.checkIfActive(contextValidation, propertyDefinition)
				    && validate(contextValidation, propertyDefinition)				
					&& convertValue(contextValidation, propertyDefinition)) {
			validateBounds(contextValidation, propertyDefinition);
			// GA: FORMAT AND UNIT
		}		
	}
	
	@SuppressWarnings("unchecked") // value cannot be properly typed unless the value field is moved out of PropertyValue 
	public List<Object> listValue() {
		return (List<Object>)value;
	}
	
	@Override
	public int hashCode() {
		return hash(super.hashCode(),unit);
	}
	
	@Override
	public boolean equals(Object obj) {
		return typedEquals(PropertyListValue.class, this, obj,
				           (x,y) -> super.equals(obj) && objectEquals(x.unit,y.unit));
	}

	/**
	 * Convert the values of this property list values to the expected property defined dynamic type.
	 * @param contextValidation  validation context
	 * @param propertyDefinition property definition
	 * @return                   true if the conversion was successful false otherwise
	 */
	private boolean convertValue(ContextValidation contextValidation, PropertyDefinition propertyDefinition) {
		try {
			String valueType = propertyDefinition.valueType;
			value = Iterables.map(listValue(), v -> ValidationHelper.dynamicCast(valueType,v))
					         .toList();
			if (propertyDefinition.saveMeasureValue != null && unit == null)
				unit = propertyDefinition.saveMeasureValue.value;
			return true;
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			contextValidation.addError(propertyDefinition.code, ERROR_BADTYPE_MSG, propertyDefinition.valueType, value);
			return false;			
		}
	}

	/**
	 * Validate that a property list value conforms to a property definition
	 * @param contextValidation  validation context
	 * @param propertyDefinition property definition
	 * @return true if the value conforms to the definition, false otherwise
	 */
	private boolean validate(ContextValidation contextValidation, PropertyDefinition propertyDefinition) {
		return validateProperty(contextValidation, propertyDefinition,
				() -> {
					if (!ValidationHelper.validateNotEmpty(contextValidation, listValue(), propertyDefinition.code + ".value"))
						return false;
					boolean isValid = true;
					int i = 0;
					for (Object value : listValue())
						isValid = ValidationHelper.validateNotEmpty(contextValidation, value, propertyDefinition.code + ".value[" + i++ + "]")
								  && isValid;
					return isValid;
				});
	}

	/**
	 * Validate that all the values in the list property are within the bounds of the property definition.
	 * @param contextValidation  validation context
	 * @param propertyDefinition property definition
	 * @return true if all the values are within the definition bounds, false otherwise
	 */
	private boolean validateBounds(ContextValidation contextValidation, PropertyDefinition propertyDefinition) {
		if (!propertyDefinition.choiceInList)
			return true;
		int i = 0;
		boolean isOk = true;
		for (Object value : listValue()) {
			if (!ValidationHelper.checkIfExistInTheList(propertyDefinition, value)) {
				contextValidation.addError(propertyDefinition.code+".value["+i+++"]", ERROR_VALUENOTAUTHORIZED_MSG, value);
				isOk = false;
			}
		}
		return isOk;
	}

}
