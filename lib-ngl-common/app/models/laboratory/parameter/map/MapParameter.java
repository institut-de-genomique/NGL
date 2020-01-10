package models.laboratory.parameter.map;

import java.util.Map;

import models.laboratory.parameter.Parameter;

public class MapParameter extends Parameter {

	protected MapParameter() {
		super("map-parameter");		
	}

	public Map<String, String> map;

}
