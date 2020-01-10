package builder.data;

import java.util.ArrayList;
import java.util.Date;

import models.sra.submit.sra.instance.RawData;
import models.sra.submit.sra.instance.Run;

public class RunBuilder {

	Run run = new Run();
	
	public RunBuilder withCode(String code)
	{
		run.code=code;
		return this;
	}
	
	public RunBuilder withRunDate(Date runDate)
	{
		run.runDate = runDate;
		return this;
	}
	
	public RunBuilder withRunCenter(String runCenter)
	{
		run.runCenter = runCenter;
		return this;
	}
	
	public RunBuilder addRawData(RawData rawData)
	{
		if(run.listRawData==null)
			run.listRawData=new ArrayList<RawData>();
		run.listRawData.add(rawData);
		return this;
	}
	
	public Run build()
	{
		return run;
	}
	
	
}
