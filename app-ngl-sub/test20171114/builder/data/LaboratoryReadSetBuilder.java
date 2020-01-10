package builder.data;

import java.util.ArrayList;
import java.util.Date;

import models.laboratory.common.instance.Valuation;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.SampleOnContainer;

public class LaboratoryReadSetBuilder {

	ReadSet readSet = new ReadSet();
	
	public LaboratoryReadSetBuilder withCode(String code)
	{
		readSet.code=code;
		return this;
	}
	
	public LaboratoryReadSetBuilder withSampleCode(String sampleCode)
	{
		readSet.sampleCode=sampleCode;
		return this;
	}
	
	public LaboratoryReadSetBuilder withRunCode(String runCode)
	{
		readSet.runCode=runCode;
		return this;
	}
	
	public LaboratoryReadSetBuilder withRunSequencingStartDate(Date date)
	{
		readSet.runSequencingStartDate=date;
		return this;
	}
	public LaboratoryReadSetBuilder withValuation(Valuation valuation)
	{
		readSet.bioinformaticValuation=valuation;
		return this;
	}
	
	public LaboratoryReadSetBuilder withSampleOnContainer(SampleOnContainer sampleOnContainer)
	{
		readSet.sampleOnContainer=sampleOnContainer;
		return this;
	}
	
	public LaboratoryReadSetBuilder initFiles()
	{
		readSet.files=new ArrayList<>();
		return this;
	}
	
	public ReadSet build()
	{
		return readSet;
	}
}
