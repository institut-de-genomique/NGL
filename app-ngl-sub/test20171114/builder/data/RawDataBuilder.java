package builder.data;

import models.sra.submit.sra.instance.RawData;

public class RawDataBuilder {

	RawData rawData = new RawData();
	
	public RawDataBuilder withRelatifName(String relatifName)
	{
		rawData.relatifName=relatifName;
		return this;
	}
	
	public RawDataBuilder withDirectory(String directory)
	{
		rawData.directory=directory;
		return this;
	}
	
	public RawDataBuilder withGzipForSubmission(boolean gzipForSubmission)
	{
		rawData.gzipForSubmission=gzipForSubmission;
		return this;
	}
	
	public RawData build()
	{
		return rawData;
	}
}
