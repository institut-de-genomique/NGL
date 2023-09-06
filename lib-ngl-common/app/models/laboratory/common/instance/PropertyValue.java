package models.laboratory.common.instance;

import static fr.cea.ig.lfw.utils.Equality.objectEquals;
import static fr.cea.ig.lfw.utils.Equality.typedEquals;
import static fr.cea.ig.lfw.utils.Hashing.hash;

import java.util.Collection;
import java.util.function.Supplier;

import  com.fasterxml.jackson.annotation.JsonSubTypes;
import  com.fasterxml.jackson.annotation.JsonTypeInfo;
import  com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import  com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import models.laboratory.common.description.PropertyDefinition;
import validation.ContextValidation;
import validation.utils.ValidationHelper;

/**
 * Object used in Map of key/value to stored value with its unit
 * if unit is null, it will not stored in MongoDB
 * 
 * @author mhaquell
 *
 */
@JsonTypeInfo(use=Id.NAME, include=As.EXTERNAL_PROPERTY, property="_type", defaultImpl=models.laboratory.common.instance.property.PropertySingleValue.class, visible=true)
@JsonSubTypes({
	@JsonSubTypes.Type(value = models.laboratory.common.instance.property.PropertySingleValue.class,     name = PropertyValue.singleType),
	@JsonSubTypes.Type(value = models.laboratory.common.instance.property.PropertyListValue.class,       name = PropertyValue.listType),
	@JsonSubTypes.Type(value = models.laboratory.common.instance.property.PropertyFileValue.class,       name = PropertyValue.fileType),
	@JsonSubTypes.Type(value = models.laboratory.common.instance.property.PropertyImgValue.class,        name = PropertyValue.imgType),
	@JsonSubTypes.Type(value = models.laboratory.common.instance.property.PropertyObjectValue.class,     name = PropertyValue.objectType),
	@JsonSubTypes.Type(value = models.laboratory.common.instance.property.PropertyObjectListValue.class, name = PropertyValue.objectListType)
})
//public abstract class PropertyValue implements IValidation {
// The validation as IValidation cannot be applied to property values as property values
// cannot be validated without a collection of property definition, so property values do
// not implement IValidation anymore and provide a validate(ContextValidation,Collection<PropertyDefinition>)
// as should be expected.
public abstract class PropertyValue {
	
	public static final String singleType     = "single";
	public static final String listType       = "list";
	public static final String fileType       = "file";
	public static final String imgType        = "img";
	public static final String objectType     = "object";
	public static final String objectListType = "object_list";
	
	public String _type;
	
	// STRUCTURAL: use and abstract getValue accessor so the value field can be moved in subclasses and properly typed.
	// Could ignore this field and provide adequately typed serializers.
	public Object value;
	
	public PropertyValue(String _type) {
		this(_type, null);
	}

	public PropertyValue(String _type, Object value) {
		this._type = _type;
		this.value = value;
	}
	
	// This is supposed to be overloaded in subclasses so we have 
	// some covariance in the return type that helps with drools.
	public Object getValue() {
		return value;
	}
	
	// This is an obviously bad method that should be abstract 
	// and defined in subclasses so the proper value type is enforced.
	// This is a setter that is not named "setValue" so it does not interact with
	// json serialization.
	public void assignValue(Object value) {
		this.value = value;
	}
	
//	@Override
//	public void validate_(ContextValidation contextValidation) {
//		Collection<PropertyDefinition> propertyDefinitions = contextValidation.<Collection<PropertyDefinition>>getTypedObject("propertyDefinitions");
//		ValidationHelper.checkType(contextValidation, this, propertyDefinitions);
//	}
	
	public void validate(ContextValidation contextValidation, Collection<PropertyDefinition> propertyDefinitions) {
		// This does not generate any error (see implementation) and the return type 
		// is ignored so this does nothing.
		ValidationHelper.checkType(contextValidation, this, propertyDefinitions);
	}
	
	@Override
	public int hashCode() {
		return hash(1,_type,value);
	}
	
	@Override
	public boolean equals(Object obj) {
		return typedEquals(PropertyValue.class, this, obj, (x,y) -> objectEquals(x._type,y._type) && objectEquals(x.value,y.value));
	}
	
	
	/**
	 * Validate the core definition of a property (required and not empty) and
	 * executes the provided assertion if the property is required and not empty.
	 * @param validationContext  validation context
	 * @param propertyDefinition property definition
	 * @param assertion          assertion to run if the core requirements are met
	 * @return                   true if the validation succeeded, false otherwise
	 */
	protected boolean validateProperty(ContextValidation  validationContext, 
			                           PropertyDefinition propertyDefinition, 
			                           Supplier<Boolean>  assertion) {
		if (!propertyDefinition.required)
			return true;
		if (!ValidationHelper.validateNotEmpty(validationContext, this, propertyDefinition.code))
			return false;
		return assertion.get();
	}

}
