package models.laboratory.resolutions.instance;


import models.utils.InstanceConstants;
import validation.ContextValidation;

import validation.IValidation;
import validation.common.instance.CommonValidationHelper;
import validation.utils.ValidationHelper;

public class Resolution implements IValidation, Comparable<Resolution> {
	
	public String             code;
    public String             name;
    public Short              displayOrder;
    public String             level        = "default";
    public ResolutionCategory category;
	
    public Resolution() {}
    
    public Resolution(String name, String code, ResolutionCategory rc, Short displayOrder, String level) {
    	// We allow null resolution to allow partial object initialization. 
//    	if (rc == null)
//    		throw new IllegalArgumentException("resolution category is null");
		this.code         = code;
		this.name         = name;
		this.displayOrder = displayOrder;
		if (rc != null)
			this.category     = new ResolutionCategory(rc.name, rc.displayOrder); 
		this.level        = level;
    }
    
    public Resolution(String name, String code, ResolutionCategory rc, Short displayOrder) {
		this(name, code, rc, displayOrder, null);
    }

    /**
     * Validate this resolution.
     */
	@Override
	public void validate(ContextValidation contextValidation) {
//    	contextValidation.putObject("resolutions", this);    	
    	CommonValidationHelper.validateUniqueFieldValue(contextValidation, "code", code, ResolutionConfiguration.class, InstanceConstants.RESOLUTION_COLL_NAME );
    	ValidationHelper      .validateNotEmpty        (contextValidation, name,                  "name");	
    	ValidationHelper      .validateNotEmpty        (contextValidation, category.name,         "category.name");
    	ValidationHelper      .validateNotEmpty        (contextValidation, displayOrder,          "displayOrder");
    	ValidationHelper      .validateNotEmpty        (contextValidation, category.displayOrder, "category.displayOrder");    	
//    	contextValidation.removeObject("resolutions");
	}
	
	@Override
	public int compareTo(Resolution r) {
		int result = category.displayOrder.compareTo(r.category.displayOrder);
	    if (result == 0) {
	        return displayOrder.compareTo(r.displayOrder);
	    } else {
	        return result;
	    }
	}
	
}
