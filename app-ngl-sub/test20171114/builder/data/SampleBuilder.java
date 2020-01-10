package builder.data;

import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.sra.submit.common.instance.Sample;


public class SampleBuilder {

	Sample sample = new Sample();
	
	public SampleBuilder withCode(String code)
	{
		sample.code=code;
		return this;
	}
	
	public SampleBuilder withProjectCode(String projectCode)
	{
		sample.projectCode=projectCode;
		return this;
	}
	
	public SampleBuilder withTraceInformation(TraceInformation traceInformation)
	{
		sample.traceInformation=traceInformation;
		return this;
	}
	
	public SampleBuilder withState(State state)
	{
		sample.state=state;
		return this;
	}
	
	public Sample build()
	{
		return sample;
	}
}
