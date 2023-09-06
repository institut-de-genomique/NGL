package validation.utils;


import static validation.utils.ValidationConstants.ERROR_NOTACTIVE;
import static validation.utils.ValidationConstants.ERROR_NOTDEFINED_MSG;
import static validation.utils.ValidationConstants.ERROR_REQUIRED_MSG;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import fr.cea.ig.lfw.utils.Iterables;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.description.State;
import models.laboratory.common.description.Value;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.TBoolean;
import play.data.validation.ValidationError;
import validation.ContextValidation;

public class ValidationHelper {
	
	public static final play.Logger.ALogger logger = play.Logger.of(ValidationHelper.class);
	
	
	/**
	 * Validate properties against definitions. The definitions are typically gathered from
	 * referenced types (sample type and import type for a sample) and define extra properties
	 * for the referencing type.  
	 * @param contextValidation   validation context
	 * @param properties          properties
	 * @param propertyDefinitions property definitions
	 * @param validateNotDefined  should property values that do not appear in the definitions handled as errors ? 
	 */
	public static void validateProperties(ContextValidation          contextValidation, 
			                              Map<String, PropertyValue> properties,
			                              List<PropertyDefinition>   propertyDefinitions, 
			                              boolean                    validateNotDefined) {
		validateProperties(contextValidation, properties, propertyDefinitions, validateNotDefined, true, null, null);
	}
	
	/**
	 * Validate properties against definitions using (validateNotDefined=true) and
	 * (testRequired=true).  
	 * @param contextValidation    validation context
	 * @param properties           properties
	 * @param propertyDefinitions  property definitions
	 */
	public static void validateProperties(ContextValidation          contextValidation, 
			                              Map<String, PropertyValue> properties,
			                              List<PropertyDefinition>   propertyDefinitions) {
		validateProperties(contextValidation, properties, propertyDefinitions, true, true, null, null);		
	}
	
	
	/**
	 * Validate properties against definitions.
	 * @param contextValidation    validation context
	 * @param properties           properties
	 * @param propertyDefinitions  property definitions
	 * @param validateNotDefined   should property values that do not appear in the definitions handled as errors ? 
	 * @param testRequired         should missing required properties be handled as errors ?  
	 * @param currentStateCode     current state code
	 * @param defaultRequiredState value to use as the minimum state if the property definition doesn't define one
	 */
	public static void validateProperties(ContextValidation          contextValidation, 
			                              Map<String, PropertyValue> properties,
			                              List<PropertyDefinition>   propertyDefinitions, 
			                              boolean                    validateNotDefined, 
			                              boolean                    testRequired, 
			                              String                     currentStateCode, 
			                              String                     defaultRequiredState) {
		Map<String, PropertyValue> inputProperties = new HashMap<>();
		if (!MapUtils.isEmpty(properties)) {
			removeNullValues(properties);
			inputProperties.putAll(properties);
		}
		
		logger.debug("validateProperties : validateNotDefined={}, testRequired={}", validateNotDefined, testRequired);
		Multimap<String, PropertyDefinition> multimap = getMultimap(propertyDefinitions);
		
		for (String key : multimap.keySet()) {
			Collection<PropertyDefinition> pdefs = multimap.get(key); 
			logger.debug("checking property '{}'", key);
			PropertyValue pv = inputProperties.get(key);
			logger.debug("property value {} : '{}' (org:'{}')", key, pv, properties == null ? null : properties.get(key));
//			PropertyDefinition propertyDefinition = (PropertyDefinition) pdefs.toArray()[0];
			// property definition is never null as the key is always associated to at least one value.
			PropertyDefinition propertyDefinition = Iterables.first(pdefs).orElse(null);			
			// if pv null and required
			if (pv == null && propertyDefinition.required 
					       && testRequired 
				           && isStateRequired(currentStateCode, propertyDefinition.requiredState, defaultRequiredState)) {
				logger.debug("missing required value for '{}'", key);
				contextValidation.addError(propertyDefinition.code + ".value", ERROR_REQUIRED_MSG, "");					
			} else if (pv != null) {
//				contextValidation.putObject("propertyDefinitions", pdefs);
//				logger.debug("calling validate(ContextValidation) on {} ({})", pv.getClass(), contextValidation.errorCount());
//				pv.validate(contextValidation);
//				logger.debug("called validate(ContextValidation) on {} ({})", pv.getClass(), contextValidation.errorCount());
				logger.debug("calling validate(ContextValidation) on {} ({})", pv.getClass(), contextValidation.errorCount());
				pv.validate(contextValidation, pdefs);
				logger.debug("called validate(ContextValidation) on {} ({})", pv.getClass(), contextValidation.errorCount());
			}
			// GA: REMOVE NOT ACTIVE IF NOT ACTIVE is a warning and not an error
//			if (inputProperties.containsKey(key)) {
//				inputProperties.remove(key);
//			}
			logger.info("removing tested property {}", key);
			inputProperties.remove(key);
		}		
		// treat other property not defined
		if (validateNotDefined) {
			for (String key : inputProperties.keySet()) {
				logger.debug("missing value for key {}", key);
				contextValidation.addError(key, ERROR_NOTDEFINED_MSG);
			}
		}
	}
	

	
	/**
	 * Purge properties that have a (roughly) null value.
	 * @param properties properties map to purge
	 */
	// could use isEmpty on the property value
	private static void removeNullValues(Map<String, PropertyValue> properties) {
		properties.entrySet().removeIf(entry -> entry.getValue() == null 
				                             || entry.getValue().value == null 
				                             || StringUtils.isBlank(entry.getValue().value.toString()));
	}
	
	
	/**
	 * Does the provided state code have a position greater or equal than either the required state if not
	 * null or else the required state (a null state code is valid, or no control (required state and default 
	 * are null)) ? 
	 * @param currentStateCode     state code to check
	 * @param requiredState        state to compare to
	 * @param defaultRequiredState state to compare to if the required state is null
	 * @return                     true if the above description holds, false otherwise
	 */
	private static boolean isStateRequired(String currentStateCode,	String requiredState, String defaultRequiredState) {
		if (currentStateCode == null) // nothing to check
			return true;
		if (defaultRequiredState == null && requiredState == null) // nothing to check against
			return true;
		State currentState    = State.find.get().findByCode(currentStateCode);
		State pdRequiredState = State.find.get().findByCode(requiredState == null ? defaultRequiredState : requiredState);
		return currentState.position >= pdRequiredState.position;						
	}

	/*
	 * transform the list of multimap where the key is the prefix of the code.
	 * 
	 * ex : code = prop.toto the key is prop.
	 * 
	 * Used to manage Object
	 * 
	 * @param propertyDefinitions
	 * @return
	 */
	private static Multimap<String, PropertyDefinition> getMultimap(List<PropertyDefinition> propertyDefinitions) {
		Multimap<String, PropertyDefinition> multimap = ArrayListMultimap.create();		
		for (PropertyDefinition pd : propertyDefinitions)
//			multimap.put(PropertyDefinition.splitCodePropertyDefinition(pd)[0], pd);			
			multimap.put(pd.splitCodePropertyDefinition()[0], pd);			
		return multimap;
	}

	/**
	 * Get a class object for a class name ({@link Class#forName(String)}) 
	 * that convert class not found exception to runtime exceptions. 
	 * @param className name of the class to resolve
	 * @return          resolved class object
	 */
	private static <T> Class<T> getClass(String className) {
		try {
			@SuppressWarnings("unchecked") // Reflection uncheckable
			Class<T> clazz = (Class<T>) Class.forName(className);
			return clazz;
		} catch (ClassNotFoundException e) {
			// ne doit pas arriver sauf si objet complexe
			throw new RuntimeException(e);
		}
	}	
	

	/**
	 * Converts a value to a value of a given type.
	 * @param type  name of the type to cast value to
	 * @param value value to cast
	 * @return      value of named type
	 * @deprecated use {@link #dynamicCast(String, Object)} 
	 */
	@Deprecated
	public static Object convertStringToType(String type, String value) {
		try {
			Class<?> valueClass = getClass(type);
			return convertValue(valueClass, value, null);
		} catch (Throwable e) {
			logger.error(e.getMessage(),e);			
		}
		return null;		
	}
	
	/**
	 * Converts elements of a collection to a given type. 
	 * @param type   new element types
	 * @param values values to cast
	 * @return       list of cast values
	 * @deprecated use {@link #dynamicCastElements(String, Collection)}
	 */
	@Deprecated
	public static List<Object> convertStringToType(String type, List<String> values) {
		try {
			Class<?> valueClass = getClass(type);
			List<Object> objects = new ArrayList<>(values.size());
			for (String value : values) {
				objects.add(convertValue(valueClass, value, null));
			}			
			return objects;
		} catch (Throwable e) {
			logger.error(e.getMessage(),e);			
		}
		return null;		
	}
	

	/*
	 * 
	 * @param type : final type
	 * @param value : the value
	 * @param format : format Date
	 * @return
	 */
	@Deprecated
	private static Object convertValue(Class<?> type, String value, String inputFormat) {
		Object o = null;
		if (String.class.equals(type)) {
			o = value;
		} else if (Integer.class.equals(type)) {
			o = Integer.valueOf(value);
		} else if (Double.class.equals(type)) {
			o = Double.valueOf(value);
		} else if (Float.class.equals(type)) {
			o = Float.valueOf(value);
		} else if (Boolean.class.equals(type)) {
			o = Boolean.valueOf(value);
		} else if (Long.class.equals(type)) {
			o = Long.valueOf(value);
		} else if (Date.class.equals(type)) {
			if(inputFormat==null){
				o = new Date(Long.valueOf(value));
			} else {  
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(inputFormat);
				try {
					o = simpleDateFormat.parse(value);
				} catch (ParseException e) {
					throw new RuntimeException(e.getMessage(),e);
				}
			}
		} else if (TBoolean.class.equals(type)) {
			o = TBoolean.valueOf(value);
		} else {
			logger.info("Erreur de type : {}", type);
			throw new RuntimeException("Type not managed: " + type);
		}
		return o;
	}

	/**
	 * Converters for dynamically typed objects.
	 */
	private static final HashMap<String,BiFunction<Object,String,Object>> converters;
	
	private static void register(Class<?> c, BiFunction<Object,String,Object> f) { 
		converters.put(c.getName(),f); 
	}
	
	static {
		converters = new HashMap<>();
		register(String  .class, (o,f) -> o.toString());
		register(Boolean .class, (o,f) -> Boolean.valueOf(o.toString()));
		register(Integer .class, (o,f) -> o instanceof Number ? ((Number)o).intValue()    : Integer.valueOf(o.toString()));
		register(Long    .class, (o,f) -> o instanceof Number ? ((Number)o).longValue()   : Long   .valueOf(o.toString()));
		register(Float   .class, (o,f) -> o instanceof Number ? ((Number)o).floatValue()  : Float  .valueOf(o.toString()));
		register(Double  .class, (o,f) -> o instanceof Number ? ((Number)o).doubleValue() : Double .valueOf(o.toString()));
		register(TBoolean.class, (o,f) -> TBoolean.valueOf(o.toString()));
		register(Date    .class, (o,f) -> {
			if (f == null) {
				if (o instanceof Number)
					return new Date(((Number)o).longValue());
				return new Date(Long.valueOf(o.toString()));
			} else {
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(f);
				try {
					return simpleDateFormat.parse(o.toString());
				} catch (ParseException e) {
					throw new RuntimeException(e.getMessage(),e);
				}
			}
		});
	}
	
	/**
	 * Converts a value to a value of a given type.
	 * @param typeName name of the type to cast value to
	 * @param value    value to cast
	 * @return         value of named type 
	 */
	public static Object dynamicCast(String typeName, Object value) {
		if (value == null)
			return null;
		if (value.getClass().getName().equals(typeName))
			return value;
		BiFunction<Object,String,Object> converter = converters.get(typeName);
		if (converter == null)
			throw new RuntimeException("dynamic cast to " + typeName + " not supported");
		return converter.apply(value, null);
	}
	
	/**
	 * Converts elements of a collection to a given type. 
	 * @param typeName new element types
	 * @param values   values to cast
	 * @return         list of cast values
	 */
	public static List<Object> dynamicCastElements(String typeName, Collection<? extends Object> values) {
		return Iterables.map(values, v -> dynamicCast(typeName, v)).toList();
	}
			
	/**
	 * Check if the property is not empty (null, "", " "; size = 0, etc).
	 * @param errors errors storage
	 * @param object object to test for emptiness
	 * @param key    error key
	 * @return       true if the object is not empty
	 * @deprecated use {@link #validateNotEmpty(ContextValidation, Object, String)}
	 */
	@Deprecated
	public static boolean required(Map<String, List<ValidationError>> errors, Object object, String key) {
		boolean isValid = true;
		if (object == null) {
			isValid =  false;
        }
        if (isValid && object instanceof String) {
        	isValid =  StringUtils.isNotBlank((String)object);
        }
        if (isValid && object instanceof Collection) {
        	isValid =  CollectionUtils.isNotEmpty((Collection<?>)object);
        }
        if (isValid && object instanceof Map) {
        	isValid =  MapUtils.isNotEmpty((Map<?,?>)object);
        }
        if (!isValid) {
        	addErrors(errors, key, ERROR_REQUIRED_MSG, object);
        }        
        return isValid;		
	}	
		
	/**
	 * Add an error message to the provided error map.
	 * @param errors    list of errors
	 * @param key       property key
	 * @param message   message key
	 * @param arguments message arguments
	 * @deprecated use ContextValidation.addErrors
	 */
	@Deprecated
	public static void addErrors(Map<String, List<ValidationError>> errors,	String key, String message, Object... arguments) {
		if (!errors.containsKey(key)) {
			errors.put(key, new ArrayList<ValidationError>());
		}		
		errors.get(key).add(new ValidationError(key, message,  java.util.Arrays.asList(arguments)));
	}

	
	/**
	 * Tests for emptiness of an object (null, StringUtils#isBlank, CollectionUtils#isEmpty, etc).
	 * @param object object to test for emptiness
	 * @return       true if the object is considered empty, false otherwise
	 */
	public static boolean isEmpty(Object object) {
		return object == null 
				|| (object instanceof String     && StringUtils.isBlank((String)object)) 
		        || (object instanceof Collection && CollectionUtils.isEmpty((Collection<?>)object)) 
		        || (object instanceof Map        && MapUtils.isEmpty((Map<?,?>)object)) 
		        || (object instanceof byte[]     && ((byte[])object).length==0);
	}
	
	/**
	 * Returns null if the argument is empty ({@link #isEmpty}), or the original object if not empty. 
	 * @param object object to test
	 * @return       null if the object is empty, the object otherwise 
	 */
	public static Object cleanValue(Object object) {
		if (isEmpty(object)) 
        	return null;
        return object;
	}
	
	
	/**
	 * Validate that an object is not null or empty (CTX_OK).
	 * @param contextValidation validation context
	 * @param object            object to test
	 * @param property          error key
	 * @return                  true if the object is not considered empty, false otherwise
	 */
	public static boolean validateNotEmpty(ContextValidation contextValidation, Object object, String property) {
		if (isEmpty(object)) {
			logger.debug("validateNotEmpty failed {} : {} {}", property, object == null ? null : object.getClass(), object);
			contextValidation.addError(property, ERROR_REQUIRED_MSG, object);
			return false;
		}
        return true;		
	}
	
	/*
	 * Check if the value is in the list
	 * @param contextValidation
	 * @param propertyValue
	 * @param propertyDefinition
	 * @return
	 */
//	public static boolean checkIfExistInTheList(ContextValidation contextValidation, PropertySingleValue propertyValue, PropertyDefinition propertyDefinition){
//		if (propertyDefinition.choiceInList && !checkIfExistInTheList(propertyDefinition, propertyValue.value)) {
//			contextValidation.addError(propertyDefinition.code+".value", ERROR_VALUENOTAUTHORIZED_MSG, propertyValue.value);
//			return false;
//		} else {
//			return true;
//		}
//	}
	
	// -----------------------------------------------------------------------------
	// renamed and argument reordered and moved to PropertySingleValue
	
//	/**
//	 * Validates that the property value is within the bounds of the property definition.
//	 * @param contextValidation  validation context
//	 * @param propertyValue      property value
//	 * @param propertyDefinition property definition
//	 * @return                   true if the property value is within the bounds of the definition, false otherwise
//	 * @deprecated use {@link PropertySingleValue#validatePropertyBounds(ContextValidation, PropertyDefinition, PropertySingleValue)}
//	 */
//	@Deprecated
//	public static boolean checkIfExistInTheList_(ContextValidation contextValidation, PropertySingleValue propertyValue, PropertyDefinition propertyDefinition) {
//		return PropertySingleValue.validatePropertyBounds(contextValidation, propertyDefinition, propertyValue);
//	}
	
	// -----------------------------------------------------------------------------

	/*
	 * Check if the value is in the list
	 * @param contextValidation
	 * @param propertyValue
	 * @param propertyDefinition
	 * @return
	 */
//	public static boolean checkIfExistInTheList(ContextValidation contextValidation, PropertyListValue propertyValue, PropertyDefinition propertyDefinition){
//		if (propertyDefinition.choiceInList) {
//			int i = 0;
//			boolean isOk = true;
////			for(Object value : propertyValue.value){
//			for (Object value : propertyValue.listValue()) {
//				if (!checkIfExistInTheList(propertyDefinition, value)) {
//					contextValidation.addError(propertyDefinition.code+".value["+i+++"]", ERROR_VALUENOTAUTHORIZED_MSG, value);
//					isOk = false;
//				}
//			}
//			return isOk;
//		} else {
//			return true;
//		}
//	}
	
	// -------------------------------------------------------------------------------
	// renamed and arguments reordered and moved to PropertyListValue
	
//	/**
//	 * Validate that all the values in the list property are within the bounds of the property definition.
//	 * @param contextValidation  validation context
//	 * @param propertyValue      property value
//	 * @param propertyDefinition property definition
//	 * @return true if all the values are within the definition bounds, false otherwise
//	 * @deprecated use {@link PropertyListValue#validatePropertyBounds(ContextValidation, PropertyDefinition, PropertyListValue)}
//	 */
//	@Deprecated
//	public static boolean checkIfExistInTheList_(ContextValidation contextValidation, PropertyListValue propertyValue, PropertyDefinition propertyDefinition) {
//		return PropertyListValue.validatePropertyBounds(contextValidation, propertyDefinition, propertyValue);
//	}
	
	// -------------------------------------------------------------------------------

	/*
	 * 
	 * @param contextValidation
	 * @param propertyValue
	 * @param propertyDefinition
	 */
//	public static boolean checkIfExistInTheList(ContextValidation contextValidation, PropertyObjectValue propertyValue, PropertyDefinition propertyDefinition) {
//		if (propertyDefinition.choiceInList) {
//			boolean isOk = true;
////			for (Entry<String, ?> entryValue : propertyValue.value.entrySet()){
//			for (Entry<String, ?> entryValue : propertyValue.mapValue().entrySet()){
//				Object value = entryValue.getValue();
//				if((propertyDefinition.code.endsWith(entryValue.getKey())) && !checkIfExistInTheList(propertyDefinition, value)){
//					contextValidation.addError(propertyDefinition.code+".value."+entryValue.getKey(), ERROR_VALUENOTAUTHORIZED_MSG, value);
//					isOk = false;
//				}
//			}			
//			return isOk;
//		} else {
//			return true;
//		}
//	}
	
	// ------------------------------------------------------------------------------------
	// renamed and argument reordered and moved to PropertyObjectValue
	
//	/**
//	 * Validate that all the values in the property map that matches the property definition
//	 * name are within the bounds of the definition. 
//	 * @param contextValidation  validation context
//	 * @param propertyValue      property value
//	 * @param propertyDefinition property definition
//	 * @return                   true if the matching values in the property map are within property definition bounds
//	 * @deprecated use {@link PropertyObjectValue#validatePropertyBounds(ContextValidation, PropertyDefinition, PropertyObjectValue)}
//	 */
//	public static boolean checkIfExistInTheList_(ContextValidation contextValidation, PropertyObjectValue propertyValue, PropertyDefinition propertyDefinition) {
//		return PropertyObjectValue.validatePropertyBounds(contextValidation, propertyDefinition, propertyValue);
//	}
	
	// ------------------------------------------------------------------------------------

	/*
	 * 
	 * @param contextValidation
	 * @param propertyValue
	 * @param propertyDefinition
	 * @return 
	 */
//	public static boolean checkIfExistInTheList(ContextValidation contextValidation, PropertyObjectListValue propertyValue, PropertyDefinition propertyDefinition) {
//		if (propertyDefinition.choiceInList) {
//			int i = 0;
//			boolean isOk = true;
////			for(Map<String, ?> map : propertyValue.value){
//			for (Map<String, ?> map : propertyValue.listMapValue()) {
//				for (Entry<String, ?> entryValue : map.entrySet()) {
//					Object value = entryValue.getValue();
//					if ((propertyDefinition.code.endsWith(entryValue.getKey())) && !checkIfExistInTheList(propertyDefinition, value)) {
//						contextValidation.addError(propertyDefinition.code+".value["+i+++"]."+entryValue.getKey(), ERROR_VALUENOTAUTHORIZED_MSG, value);
//						isOk = false;
//					}
//				}								
//			}
//			return isOk;	
//		} else {
//			return true;
//		}
//	}
	
	// ------------------------------------------------------------------------------------
	// renamed and arguments reordered and moved to PropertyObjectListValue
	
//	/**
//	 * Validate that values in each map of the property value whose key matches the property definition
//	 * are within the property definition bounds.
//	 * @param contextValidation  validation context
//	 * @param propertyValue      property value
//	 * @param propertyDefinition property definition
//	 * @return                   true if the matching values are within the property definition bounds, false otherwise
//	 * @deprecated use {@link PropertyObjectListValue#validatePropertyBounds(ContextValidation, PropertyDefinition, PropertyObjectListValue)} 
//	 */
//	@Deprecated
//	public static boolean checkIfExistInTheList_(ContextValidation contextValidation, PropertyObjectListValue propertyValue, PropertyDefinition propertyDefinition) {
//		return PropertyObjectListValue.validatePropertyBounds(contextValidation, propertyDefinition, propertyValue);
//	}
	
	// ------------------------------------------------------------------------------------

	/*
	 * 
	 * @param propertyDefinition
	 * @param value
	 * @return
	 */
	public static boolean checkIfExistInTheList(PropertyDefinition propertyDefinition, Object value) {
		Class<?> valueClass = getClass(propertyDefinition.valueType);
		for (Value possibleValue : propertyDefinition.possibleValues)
			if (value.equals(convertValue(valueClass, possibleValue.code, null)))
				return true;
		return false;
	}
	
	/*
	 * Check if the propertyDefinition is active
	 * @param contextValidation
	 * @param propertyDefinition
	 * @return
	 */
	public static boolean checkIfActive(ContextValidation contextValidation, PropertyDefinition propertyDefinition) {
		if (propertyDefinition.active)
			return true;
//		String[] codes = PropertyDefinition.splitCodePropertyDefinition(propertyDefinition);
		String[] codes = propertyDefinition.splitCodePropertyDefinition();
		if (codes.length == 1) { //simple case
			contextValidation.addError(propertyDefinition.code, ERROR_NOTACTIVE);
		} else { // object case
			contextValidation.addError(codes[0] + ".value." + codes[1], ERROR_NOTACTIVE);
		}
		return false;			
	}
	
	/**
	 * This does not generate an error but checks that a property value type matches all the provided
	 * definitions types.
	 * @param contextValidation   validation context
	 * @param propertyValue       property value
	 * @param propertyDefinitions collection of 0 or 1 definition
	 * @return                    true if the property collection is empty or all the property defined types match the property value type 
	 */
	public static boolean checkType(ContextValidation contextValidation, PropertyValue propertyValue, Collection<PropertyDefinition> propertyDefinitions) {
		boolean isSame = true;
		for (PropertyDefinition propDef : propertyDefinitions) {
			if (propertyValue._type == null || !propertyValue._type.equals(propDef.propertyValueType)) {
				logger.error("checkType - property type error {} : {}, expected {} got {}", propDef.code, propertyValue.value, propDef.propertyValueType, propertyValue._type);
				// GA: à activer si la prod se passe bien
				// contextValidation.addErrors(propDef.code, ERROR_PROPERTY_TYPE, propertyValue.value, propDef.propertyValueType,propertyValue._type);
				isSame = false;
			}
		}
		return isSame;
	}

}
