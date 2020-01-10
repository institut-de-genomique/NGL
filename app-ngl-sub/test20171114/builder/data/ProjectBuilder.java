package builder.data;

import models.laboratory.project.instance.Project;

public class ProjectBuilder {

	Project project = new Project();
	
	public ProjectBuilder withCode(String code)
	{
		project.code=code;
		return this;
	}
	
	
	public Project build()
	{
		return project;
	}
}
