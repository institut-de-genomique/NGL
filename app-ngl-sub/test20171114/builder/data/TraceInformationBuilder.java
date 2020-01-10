package builder.data;

import java.util.Date;

import models.laboratory.common.instance.TraceInformation;

public class TraceInformationBuilder {

	TraceInformation traceInformation = new TraceInformation();

	public TraceInformationBuilder withCreateUser(String createUser)
	{
		traceInformation.createUser=createUser;
		return this;
	}
	
	public TraceInformationBuilder withCreationDate(Date creationDate)
	{
		traceInformation.creationDate=creationDate;
		return this;
	}
	
	public TraceInformationBuilder withModifyUser(String modifyUser)
	{
		traceInformation.modifyUser=modifyUser;
		return this;
	}
	
	public TraceInformationBuilder withModifyDate(Date modifyDate)
	{
		traceInformation.modifyDate=modifyDate;
		return this;
	}
	
	public TraceInformation build()
	{
		return traceInformation;
	}
}
