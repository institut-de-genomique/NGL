package models.laboratory.parameter.map;

import java.util.Map;
import models.laboratory.parameter.Parameter;
import validation.ContextValidation;



public class MapParameter extends Parameter {

	public MapParameter() {
		super("map-parameter");		
	}

	public Map<String, String> map;
		
	public void validate(ContextValidation contextValidation) {
		super.validate(contextValidation);
	}
}
