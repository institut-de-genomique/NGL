package builder.data;

import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.sra.submit.common.instance.ExternalSample;
import models.sra.submit.common.instance.Sample;


public class ExternalSampleBuilder {

	ExternalSample externalSample = new ExternalSample();
	
	public ExternalSampleBuilder withCode(String code)
	{
		externalSample.code=code;
		return this;
	}
	
	
	public ExternalSampleBuilder withTraceInformation(TraceInformation traceInformation)
	{
		externalSample.traceInformation=traceInformation;
		return this;
	}
	
	public ExternalSampleBuilder withState(State state)
	{
		externalSample.state=state;
		return this;
	}
	
	public ExternalSample build()
	{
		return externalSample;
	}
}
