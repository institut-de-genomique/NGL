package models.laboratory.parameter.propertyDefinitionsProtocol;

import java.util.Map;

import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.parameter.Parameter;

public class PropertyDefinitionsProtocol extends Parameter{

	public Map<String, PropertyDefinition> properties;

	protected PropertyDefinitionsProtocol() {
		super("map-property-definitions");
	}

}
