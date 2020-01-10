package builder.data;

import models.laboratory.sample.instance.Sample;

public class LaboratorySampleBuilder {

	Sample sample = new Sample();
	
	public LaboratorySampleBuilder withCode(String code)
	{
		sample.code=code;
		return this;
	}
	
	public LaboratorySampleBuilder withName(String name)
	{
		sample.name=name;
		return this;
	}
	
	public LaboratorySampleBuilder withRefCollab(String refCollab)
	{
		sample.referenceCollab=refCollab;
		return this;
	}
	
	public LaboratorySampleBuilder withTaxonCode(String taxonCode)
	{
		sample.taxonCode=taxonCode;
		return this;
	}
	
	public Sample build()
	{
		return sample;
	}
}
