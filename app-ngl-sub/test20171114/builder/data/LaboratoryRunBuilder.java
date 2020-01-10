package builder.data;

import java.util.ArrayList;
import java.util.HashMap;

import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.run.instance.InstrumentUsed;
import models.laboratory.run.instance.Run;
import models.laboratory.run.instance.Treatment;

public class LaboratoryRunBuilder {

	Run run = new Run();
	
	public LaboratoryRunBuilder withCode(String code)
	{
		run.code=code;
		return this;
	}
	
	public LaboratoryRunBuilder withTypeCode(String typeCode)
	{
		run.typeCode=typeCode;
		return this;
	}
	
	public LaboratoryRunBuilder withInstrumentUsed(InstrumentUsed instrumentUsed)
	{
		run.instrumentUsed=instrumentUsed;
		return this;
	}
	
	public LaboratoryRunBuilder initLanes()
	{
		run.lanes=new ArrayList<>();
		return this;
	}
	
	public LaboratoryRunBuilder addProperty(String key, Object value)
	{
		if(run.properties==null)
			run.properties=new HashMap<>();
		run.properties.put(key, new PropertySingleValue(value));
		return this;
	}
	
	public LaboratoryRunBuilder addTreatment(String key, Treatment treatment)
	{
		run.treatments.put(key, treatment);
		return this;
	}
	
	public Run build()
	{
		return run;
	}
}
