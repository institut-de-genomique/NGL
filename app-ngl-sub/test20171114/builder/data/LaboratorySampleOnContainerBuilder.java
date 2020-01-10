package builder.data;

import java.util.HashMap;

import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.run.instance.SampleOnContainer;

public class LaboratorySampleOnContainerBuilder {

	SampleOnContainer sampleOnContainer = new SampleOnContainer();
	
	public LaboratorySampleOnContainerBuilder addProperty(String key, Object value)
	{
		if(sampleOnContainer.properties==null)
			sampleOnContainer.properties=new HashMap<>();
		sampleOnContainer.properties.put(key, new PropertySingleValue(value));
		return this;
	}
	
	public SampleOnContainer build()
	{
		return sampleOnContainer;
	}
}
