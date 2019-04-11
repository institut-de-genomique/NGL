package fr.cea.ig.ngl.test.dao.api.factory.project;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import controllers.projects.api.ProjectsSearchForm;
import fr.cea.ig.ngl.support.ListFormWrapper;
import fr.cea.ig.ngl.test.dao.api.factory.QueryMode;
import fr.cea.ig.ngl.test.dao.api.factory.RenderMode;
import fr.cea.ig.ngl.test.dao.api.factory.TestListFormWrapperFactory;
import fr.cea.ig.play.test.DevAppTesting;
import models.laboratory.common.instance.Comment;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.project.instance.BioinformaticParameters;
import models.laboratory.project.instance.Project;

public class TestProjectFactory {
	
	public static Project project(String user) {
		return project(user, "default-project", "default");
	}

	public static Project projectArchived(String user) {
		Project p = project(user);
		p.archive = true;
		return p;
	}
	
	public static Project project(String user, String typeCode, String categoryCode) {
		Project project = new Project(new HashMap<>());
		project.name = "Project Test";
		project.typeCode = typeCode;
		project.categoryCode = categoryCode;
		project.description = "description";
		project.umbrellaProjectCode = null;
		project.lastSampleCode = "lastSampleCode";
		project.nbCharactersInSampleCode = 4;
		project.archive = Boolean.FALSE;
		project.state = new State("N", user);
		project.authorizedUsers = authorizedUsers(user);
		project.bioinformaticParameters = params();
		project.comments = comments(user);
		project.code = DevAppTesting.newCode();
		project.properties.put("unixGroup", new PropertySingleValue("g_cns"));
		project.traceInformation = traceInformation(user);
		/* TODO add values to these fields
		public Map<String, PropertyValue> properties;
		*/
		
		return project;
	}
	
	// Complex field constructors
	private static BioinformaticParameters params() {
		BioinformaticParameters params = new BioinformaticParameters();
		params.regexBiologicalAnalysis = "regexBiologicalAnalysis";
		params.mappingReference = "mappingReference";
		params.fgGroup = "fgGroup";
		params.fgPriority = 1;
		return params;
	}
	
	private static List<String> authorizedUsers(String user){
		return Arrays.asList(user);
	}
	
	private static List<Comment> comments(String user){
		return Arrays.asList(new Comment("very usefull comments", user));
	}
	
	private static TraceInformation traceInformation(String user) {
		return new TraceInformation(user);
	}


	public static ListFormWrapper<Project> wrapper(String code, QueryMode qmode, RenderMode render) throws Exception {
		ProjectsSearchForm form = new ProjectsSearchForm();
		form.projectCodes = Arrays.asList(code);
		return new TestListFormWrapperFactory<Project>().wrapping().apply(form, qmode, render);
	}

	public static ListFormWrapper<Project> wrapper(String code) throws Exception {
		return wrapper(code, null, null);
	}
}
