package models.laboratory.common.instance.property;

import static fr.cea.ig.lfw.utils.Equality.objectEquals;
import static fr.cea.ig.lfw.utils.Equality.typedEquals;
import static fr.cea.ig.lfw.utils.Hashing.hash;
import static validation.utils.ValidationConstants.ERROR_BADTYPE_MSG;
import static validation.utils.ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.instance.PropertyValue;
import validation.ContextValidation;
import validation.utils.ValidationHelper;

/*
 * used to stock a list of complex object
 * an object is stock in Map with key = property and value = value of property
 */
/**
 * Property value whose value is a list of maps ({@literal List<Map<String,Object>>}), that is
 * considered a list of pseudo beans, could have been named PropertyMapListValue.
 */
public class PropertyObjectListValue extends PropertyValue {
	
	private static final play.Logger.ALogger logger = play.Logger.of(PropertyObjectListValue.class);
	
	public Map<String,String> unit;
	
	public PropertyObjectListValue() {
		super(PropertyValue.objectListType);
	}
	
	public PropertyObjectListValue(List<Map<String, Object>> value) {
		super(PropertyValue.objectListType, value);	
	}
	
	public PropertyObjectListValue(List<Map<String, Object>> value, Map<String,String> unit) {
		super(PropertyValue.objectListType, value);
		this.unit = unit;
	}

	@Override
	public List<Map<String, Object>> getValue() {
		return listMapValue();
	}
	
	@SuppressWarnings("unchecked") // uncheckable unless the value is defined in this class and not in PropertyValue. 
	public List<Map<String, Object>> listMapValue() {
		return (List<Map<String, Object>>)value;
	}
	
	@Override
	public String toString() {
		return "PropertyObjectListValue [value=" + value + ", unit=" + unit + ", class="+value.getClass().getName()+"]";
	}

	@Override
	public void validate(ContextValidation contextValidation, Collection<PropertyDefinition> propertyDefinitions) {
		super.validate(contextValidation, propertyDefinitions);
		for (PropertyDefinition propertyDefinition : propertyDefinitions) {
			if (ValidationHelper.checkIfActive(contextValidation, propertyDefinition)
					&& validate(contextValidation, propertyDefinition)				
					&& convertValue(contextValidation, propertyDefinition)) {
				validateBounds(contextValidation, propertyDefinition);
						// GA: FORMAT AND UNIT
			}
		}
	}
	
	@Override
	public int hashCode() {
		return hash(super.hashCode(), unit);
	}
	
	@Override
	public boolean equals(Object obj) {
		return typedEquals(PropertyObjectListValue.class, this, obj, 
				           (x,y) -> super.equals(obj) && objectEquals(x.unit,y.unit));
	}
	
	/**
	 * Converts the property definition designated property values of all the 'objects' in the object list. 
	 * @param contextValidation  validation context
	 * @param propertyDefinition property definition
	 * @return                   true if the conversion was successful, false otherwise
	 */
	private boolean convertValue(ContextValidation contextValidation, PropertyDefinition propertyDefinition) {
		String[] codes = propertyDefinition.splitCodePropertyDefinition();
		try {
			String valueType = propertyDefinition.valueType;
			String key       = codes[1];
			for (Map<String, Object> map : listMapValue()) 
				map.compute(key, (k,v) -> ValidationHelper.dynamicCast(valueType, v));
			return true;
		} catch(Throwable e) {
			logger.error(e.getMessage(),e);
			contextValidation.addError(codes[0]+".value."+codes[1], ERROR_BADTYPE_MSG, propertyDefinition.valueType, value);
			return false;
		}
	}
	
	/**
	 * Validate that a property value conforms to a property definition.
	 * @param contextValidation  validation context
	 * @param propertyDefinition property definition
	 * @return true if the value conforms to the definition, false otherwise
	 */
	private boolean validate(ContextValidation contextValidation, PropertyDefinition propertyDefinition) {
		return validateProperty(contextValidation, propertyDefinition,
				() -> {
					String[] codes = propertyDefinition.splitCodePropertyDefinition();
					int i = 0;
					boolean isValid = true;
					for (Map<String, Object> map : listMapValue()) {
						ValidationHelper.logger.debug("validateProperty(PropertyObjectListValue) validating {} : {}", i, map);
						Object value = map.get(codes[1]);
						isValid = ValidationHelper.validateNotEmpty(contextValidation, value, codes[0] + ".value[" + i++ + "]." + codes[1])
							    && isValid;
					}		
					return isValid;
				});
	}

	/**
	 * Validate that values in each map of the property value whose key matches the property definition
	 * are within the property definition bounds.
	 * @param contextValidation  validation context
	 * @param propertyDefinition property definition
	 * @return                   true if the matching values are within the property definition bounds, false otherwise 
	 */
	private boolean validateBounds(ContextValidation contextValidation, PropertyDefinition propertyDefinition) {
		if (!propertyDefinition.choiceInList) 
			return true;
		int i = 0;
		boolean isOk = true;
		for (Map<String, ?> map : listMapValue()) {
			for (Entry<String, ?> entryValue : map.entrySet()) {
				Object value = entryValue.getValue();
				if (propertyDefinition.code.endsWith(entryValue.getKey()) && !ValidationHelper.checkIfExistInTheList(propertyDefinition, value)) {
					contextValidation.addError(propertyDefinition.code+".value["+i+++"]."+entryValue.getKey(), ERROR_VALUENOTAUTHORIZED_MSG, value);
					isOk = false;
				}
			}								
		}
		return isOk;	
	}
	
}
