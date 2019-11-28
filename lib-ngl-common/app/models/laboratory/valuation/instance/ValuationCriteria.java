package models.laboratory.valuation.instance;

import java.util.List;

import fr.cea.ig.DBObject;
import models.laboratory.common.instance.TraceInformation;
import validation.ContextValidation;
import validation.IValidation;

public class ValuationCriteria extends DBObject implements IValidation {

	public String name;
	
	public String objectTypeCode;
	
	public List<String> typeCodes;
	
	public List<Property> properties;
	
	public Boolean active = Boolean.TRUE;
	
	public TraceInformation traceInformation;
	
	@Override
	public void validate(ContextValidation contextValidation) {
	}

}
