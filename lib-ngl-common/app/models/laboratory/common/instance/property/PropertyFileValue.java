package models.laboratory.common.instance.property;

import static fr.cea.ig.lfw.utils.Hashing.hash;
import static fr.cea.ig.lfw.utils.Equality.objectEquals;
import static fr.cea.ig.lfw.utils.Equality.typedEquals;
import static fr.cea.ig.lfw.utils.Iterables.first;

import java.io.IOException;
import java.util.Collection;

import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.instance.PropertyValue;
import validation.ContextValidation;
import validation.utils.ValidationHelper;

import com.google.common.io.Files;

/**
 * Used to stock a File
 * 
 * 
 */
public class PropertyFileValue extends PropertyByteValue {
	
	public String fullname;
	public String extension;
	
	public PropertyFileValue() {
		super(PropertyValue.fileType);
	}
	
	public PropertyFileValue(java.io.File value) throws IOException {
		super(PropertyValue.fileType, Files.toByteArray(value));
		this.extension = Files.getFileExtension(value.getName());
		this.fullname  = value.getName();
	}
	
//	@Override
//	public void validate(ContextValidation contextValidation) {
//		PropertyDefinition propertyDefinition = first(contextValidation.<Collection<PropertyDefinition>>getTypedObject("propertyDefinitions")).orElse(null);
//		super.validate(contextValidation);
//		ValidationHelper.validateNotEmpty(contextValidation, this.fullname,  propertyDefinition.code + ".fullname");
//		ValidationHelper.validateNotEmpty(contextValidation, this.extension, propertyDefinition.code + ".extension");
//	}
	@Override
	public void validate(ContextValidation contextValidation, Collection<PropertyDefinition> propertyDefinitions) {
		super.validate(contextValidation, propertyDefinitions);
		PropertyDefinition propertyDefinition = first(propertyDefinitions).orElse(null);
		ValidationHelper.validateNotEmpty(contextValidation, this.fullname,  propertyDefinition.code + ".fullname");
		ValidationHelper.validateNotEmpty(contextValidation, this.extension, propertyDefinition.code + ".extension");
	}

	@Override
	public String toString() {
		return "PropertyFileValue [name=" + fullname + ", ext=" + extension + ", class=" + value.getClass().getName() + "]";
	}
		
	@Override
	public int hashCode() {
		return hash(super.hashCode(), extension, fullname);
	}

	@Override
	public boolean equals(Object obj) {
		return typedEquals(PropertyFileValue.class, this, obj,
				           (x,y) -> super.equals(obj) 
				                    && objectEquals(x.extension, y.extension) 
				                    && objectEquals(x.fullname,  y.fullname));
	}

}
