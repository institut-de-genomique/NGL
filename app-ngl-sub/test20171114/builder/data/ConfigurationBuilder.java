package builder.data;

import models.laboratory.common.instance.State;
import models.sra.submit.sra.instance.Configuration;

public class ConfigurationBuilder {
	
	Configuration configuration = new Configuration();
	
	public ConfigurationBuilder withCode(String code)
	{
		configuration.code=code;
		return this;
	}
	
	public ConfigurationBuilder withProjectCode(String projectCode)
	{
		configuration.projectCodes.add(projectCode);
		return this;
	}
	
	public ConfigurationBuilder withStrategySample(String strategy)
	{
		configuration.strategySample=strategy;
		return this;
	}
	
	public ConfigurationBuilder withLibrarySelection(String librarySelection)
	{
		configuration.librarySelection=librarySelection;
		return this;
	}
	
	public ConfigurationBuilder withLibrarySource(String librarySource)
	{
		configuration.librarySource=librarySource;
		return this;
	}
	
	public ConfigurationBuilder withLibraryStrategy(String libraryStrategy)
	{
		configuration.libraryStrategy=libraryStrategy;
		return this;
	}
	
	public ConfigurationBuilder withState(State state)
	{
		configuration.state=state;
		return this;
	}
	
	public Configuration build()
	{
		return configuration;
	}
}
