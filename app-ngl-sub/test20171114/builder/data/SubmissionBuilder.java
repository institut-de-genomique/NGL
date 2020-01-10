package builder.data;

import java.util.ArrayList;
import java.util.Date;

import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.sra.submit.common.instance.Submission;

public class SubmissionBuilder {

	Submission submission = new Submission();
	
	public SubmissionBuilder withCode(String code)
	{
		submission.code=code;
		return this;
	}
	
	public SubmissionBuilder withProjectCode(String projectCode)
	{
		submission.projectCodes.add(projectCode);
		return this;
	}
	public SubmissionBuilder withConfigCode(String configCode)
	{
		submission.configCode=configCode;
		return this;
	}
	
	public SubmissionBuilder withSubmissionDirectory(String submissionDirectory)
	{
		submission.submissionDirectory=submissionDirectory;
		return this;
	}
	
	
	public SubmissionBuilder withSubmissionDate(Date submissionDate)
	{
		submission.creationDate=submissionDate;
		return this;
	}
	
	public SubmissionBuilder withState(State state)
	{
		submission.state=state;
		return this;
	}
	
	
	
	public SubmissionBuilder withTraceInformation(TraceInformation traceInformation)
	{
		submission.traceInformation=traceInformation;
		return this;
	}
	
	public SubmissionBuilder addExperimentCode(String code)
	{
		if(submission.experimentCodes==null)
			submission.experimentCodes=new ArrayList<String>();
		submission.experimentCodes.add(code);
		return this;
	}
	
	public SubmissionBuilder addSampleCode(String code)
	{
		if(submission.sampleCodes==null)
			submission.sampleCodes=new ArrayList<String>();
		submission.sampleCodes.add(code);
		return this;
	}
	
	public SubmissionBuilder addRunCode(String code)
	{
		if(submission.runCodes==null)
			submission.runCodes=new ArrayList<String>();
		submission.runCodes.add(code);
		return this;
	}
	
	public SubmissionBuilder withStudyCode(String code)
	{
		submission.studyCode=code;
		return this;
	}
	
/*	public SubmissionBuilder withRefStudyCode(String code)
	{
		submission.refStudyCode=code;
		return this;
	}
	*/
	public Submission build()
	{
		return submission;
	}
	
	
	
}
