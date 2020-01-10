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

/**
 * Used to stock a file type image 2D.
 * 
 * @author dnoisett
 * 
 */
public class PropertyImgValue extends PropertyFileValue {

	public Integer width;
	public Integer height;
	public String  path; // for information

	public PropertyImgValue() {
		super._type = PropertyValue.imgType;
	}

	public PropertyImgValue(java.io.File value, Integer width, Integer height) throws IOException {
		super(value);
		super._type = PropertyValue.imgType;
		this.width  = width;
		this.height = height;
	}

	@Override
	public String toString() {
		return "PropertyImgValue [fullname=" + fullname + ", ext=" + extension + ", width=" + width + ", height=" + height  +", path=" + path + ", class=" +value.getClass().getName()+"]";
	}

	@Override
	public void validate(ContextValidation contextValidation, Collection<PropertyDefinition> propertyDefinitions) {
		super.validate(contextValidation, propertyDefinitions); 
		PropertyDefinition propertyDefinition = first(propertyDefinitions).orElse(null);
		ValidationHelper.validateNotEmpty(contextValidation, width,  propertyDefinition.code + ".width");
		ValidationHelper.validateNotEmpty(contextValidation, height, propertyDefinition.code + ".height");
	}

	@Override
	public int hashCode() {
		return hash(super.hashCode(), height, path, width);
	}

	@Override
	public boolean equals(Object obj) {
		return typedEquals(PropertyImgValue.class, this, obj,
				           (x,y) -> super.equals(obj)
				                    && objectEquals(x.height, y.height)
				                    && objectEquals(x.path,   y.path)
				                    && objectEquals(x.width,  y.width));
	}

}
