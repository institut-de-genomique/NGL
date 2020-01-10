package builder.data;

import java.util.HashMap;
import java.util.Map;

import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.run.instance.Treatment;

public class LaboratoryTreatmentBuilder {

	Treatment treatment = new Treatment();
	
	public LaboratoryTreatmentBuilder addPropertyResults(String keyResults, String key, Object value)
	{
		Map<String, PropertyValue> map = new HashMap<String, PropertyValue>();
		if(treatment.results.containsKey(keyResults))
			map=treatment.results.get(keyResults);
		map.put(key, new PropertySingleValue(value));
		treatment.results.put(keyResults, map);
		return this;
	}
	
	public Treatment build()
	{
		return treatment;
	}
}
