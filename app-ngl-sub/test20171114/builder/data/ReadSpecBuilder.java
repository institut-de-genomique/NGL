package builder.data;

import models.sra.submit.sra.instance.ReadSpec;

public class ReadSpecBuilder {

	ReadSpec readSpec = new ReadSpec();
	
	public ReadSpecBuilder withIndex(int readIndex)
	{
		readSpec.readIndex=readIndex;
		return this;
	}
	
	public ReadSpecBuilder withLastBaseCoord(Integer lastBaseCoord)
	{
		readSpec.baseCoord=lastBaseCoord;
		return this;
	}
	
	public ReadSpecBuilder withReadLabel(String readLabel)
	{
		readSpec.readLabel=readLabel;
		return this;
	}
	
	public ReadSpecBuilder withReadType(String readType)
	{
		readSpec.readType=readType;
		return this;
	}
	
	public ReadSpecBuilder withReadClass(String readClass)
	{
		readSpec.readClass=readClass;
		return this;
	}
	
	
	public ReadSpec build()
	{
		return readSpec;
	}
}
