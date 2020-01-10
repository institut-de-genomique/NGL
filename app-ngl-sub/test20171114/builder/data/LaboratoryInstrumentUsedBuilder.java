package builder.data;

import models.laboratory.run.instance.InstrumentUsed;

public class LaboratoryInstrumentUsedBuilder {

	InstrumentUsed instrumentUsed = new InstrumentUsed();
	
	public LaboratoryInstrumentUsedBuilder withCode(String code)
	{
		instrumentUsed.code=code;
		return this;
	}
	
	public LaboratoryInstrumentUsedBuilder withTypeCode(String typeCode)
	{
		instrumentUsed.typeCode=typeCode;
		return this;
	}
	
	public InstrumentUsed build()
	{
		return instrumentUsed;
	}
}
