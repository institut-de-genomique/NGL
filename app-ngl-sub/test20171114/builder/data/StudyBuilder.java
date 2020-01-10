package builder.data;

import models.sra.submit.common.instance.Study;
import models.laboratory.common.instance.State;

public class StudyBuilder {

	Study study = new Study();
	
	public StudyBuilder withCode(String code)
	{
		study.code=code;
		return this;
	}
	
	public StudyBuilder withState(State state)
	{
		study.state=state;
		return this;
	}
	
	public StudyBuilder withProjectCode(String projectCode)
	{
		study.projectCodes.add(projectCode);
		return this;
	}
	
	public Study build()
	{
		return study;
	}
}
