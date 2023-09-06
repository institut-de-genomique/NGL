package models.laboratory.run.instance;

import java.util.HashMap;
import java.util.Map;

import models.laboratory.common.instance.PropertyValue;

public class ReadSetProperties {
	
	public String code;
	
	public Map<String, PropertyValue> properties = new HashMap<>(); // <String, PropertyValue>();
	
}