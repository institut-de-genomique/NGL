package models.laboratory.experiment.instance;

import java.util.List;
import java.util.Map;


import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.Content;
import models.laboratory.container.instance.LocationOnContainerSupport;
import validation.IValidation;

public abstract class AbstractContainerUsed implements IValidation {
	
	// could have been:  Container code, reference to {@link Container#code}.
	/**
	 * Code that uses some mangling (e.g: container#code + "_" + support#line).
	 */
	public String                     code;
	
	/**
	 * Container category code, duplicate from container {@link Container#categoryCode}.
	 */
	public String                     categoryCode;
	
	/**
	 * Container content, duplicate from container {@link Container#contents}.
	 */
	public List<Content>              contents;
	
	/**
	 * Location on container support, duplicate from container {@link Container#support}.
	 */
	public LocationOnContainerSupport locationOnContainerSupport;
	
	/**
	 * Could have been a simply typed volume value as a double (e.g: liter),
	 * duplicate from container {@link Container#volume}. 
	 */
	public PropertySingleValue        volume;
	public PropertySingleValue        concentration; 
	public PropertySingleValue        quantity; 	
	public PropertySingleValue        size;
	
	public Map<String,PropertyValue>  experimentProperties;
	public Map<String,PropertyValue>  instrumentProperties;
	
	public AbstractContainerUsed() {
	}
	
	public AbstractContainerUsed(String code) {
		this.code = code;
	}
	
}
