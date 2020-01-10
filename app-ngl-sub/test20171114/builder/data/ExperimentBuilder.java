package builder.data;

import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.ReadSpec;
import models.sra.submit.sra.instance.Run;

public class ExperimentBuilder {

	Experiment experiment = new Experiment();
	
	public ExperimentBuilder withCode(String code)
	{
		experiment.code=code;
		return this;
	}
	
	public ExperimentBuilder withProjectCode(String projectCode)
	{
		experiment.projectCode=projectCode;
		return this;
	}
	
	public ExperimentBuilder withLibraryConstructionProtocol(String libraryConstructionProtocol)
	{
		experiment.libraryConstructionProtocol=libraryConstructionProtocol;
		return this;
	}
	
	public ExperimentBuilder withLibrarySource(String librarySource)
	{
		experiment.librarySource=librarySource;
		return this;
	}
	
	public ExperimentBuilder withLibraryLayoutNominalLength(Integer libraryLayoutNominalLength){
		experiment.libraryLayoutNominalLength=libraryLayoutNominalLength;
		return this;
	}
	
	public ExperimentBuilder withLibrarySelection(String librarySelection)
	{
		experiment.librarySelection=librarySelection;
		return this;
	}
	
	public ExperimentBuilder withLibraryStrategy(String libraryStrategy)
	{
		experiment.libraryStrategy=libraryStrategy;
		return this;
	}
	
	public ExperimentBuilder withLibraryLayoutOrientation(String libraryLayoutOrientation)
	{
		experiment.libraryLayoutOrientation=libraryLayoutOrientation;
		return this;
	}
	
	public ExperimentBuilder withLibraryLayout(String libraryLayout)
	{
		experiment.libraryLayout=libraryLayout;
		return this;
	}
	
	public ExperimentBuilder withTitle(String title)
	{
		experiment.title=title;
		return this;
	}
	
	public ExperimentBuilder withInstrumentModel(String instrumentModel)
	{
		experiment.instrumentModel=instrumentModel;
		return this;
	}
	
	public ExperimentBuilder withSpotLength(Long spotLength)
	{
		experiment.spotLength=spotLength;
		return this;
	}
	
	public ExperimentBuilder withLibraryName(String libraryName)
	{
		experiment.libraryName=libraryName;
		return this;
	}
	
	public ExperimentBuilder withSampleCode(String sampleCode)
	{
		experiment.sampleCode=sampleCode;
		return this;
	}
	
	public ExperimentBuilder withReadSetCode(String readSetCode)
	{
		experiment.readSetCode=readSetCode;
		return this;
	}
	
	public ExperimentBuilder withStudyCode(String studyCode)
	{
		experiment.studyCode=studyCode;
		return this;
	}
	
	
	public ExperimentBuilder withTraceInformation(TraceInformation traceInformation)
	{
		experiment.traceInformation=traceInformation;
		return this;
	}
	
	public ExperimentBuilder withState(State state)
	{
		experiment.state=state;
		return this;
	}
	
	public ExperimentBuilder withRun(Run run)
	{
		experiment.run=run;
		return this;
	}
	
	public ExperimentBuilder addReadSpec(ReadSpec readSpec)
	{
		experiment.readSpecs.add(readSpec);
		return this;
	}
	
	public Experiment build()
	{
		return experiment;
	}
}
