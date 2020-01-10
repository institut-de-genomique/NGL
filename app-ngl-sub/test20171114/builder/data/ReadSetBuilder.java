package builder.data;

import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.run.instance.ReadSet;

public class ReadSetBuilder {

	ReadSet readSet = new ReadSet();
	
	public ReadSetBuilder withCode(String code)
	{
		readSet.code=code;
		return this;
	}
	
	public ReadSetBuilder withState(State state)
	{
		readSet.state=state;
		return this;
	}
	
	public ReadSetBuilder withSubmissionState(State state)
	{
		readSet.submissionState=state;
		return this;
	}
	
	public ReadSetBuilder withTraceInformation(TraceInformation traceInformation)
	{
		readSet.traceInformation=traceInformation;
		return this;
	}
	
	public ReadSet build()
	{
		return readSet;
	}
}
