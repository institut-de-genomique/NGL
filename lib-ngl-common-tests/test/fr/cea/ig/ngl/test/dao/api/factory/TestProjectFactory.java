package fr.cea.ig.ngl.test.dao.api.factory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import controllers.projects.api.ProjectsSearchForm;
import fr.cea.ig.ngl.support.ListFormWrapper;
import fr.cea.ig.play.test.DevAppTesting;
import models.laboratory.common.instance.Comment;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.project.instance.BioinformaticParameters;
import models.laboratory.project.instance.Project;
import ngl.refactoring.state.ProjectStateNames;

/**
 * Factory to create Project objects.
 * 
 * @author ajosso
 *
 */
public class TestProjectFactory {
	
//	private static final play.Logger.ALogger logger = play.Logger.of(TestProjectFactory.class);
	
	public static Project project(String user) {
		return project(user, "default-project", "default");
	}

	public static Project projectArchived(String code, String user) {
		Project p = project(user);
		p.code = code;
		p.archive = true;
		return p;
	}
	
	public static Project project(String user, String typeCode, String categoryCode) {
		Project project                  = new Project(new HashMap<>());
		project.name                     = "Project Test";
		project.typeCode                 = typeCode;
		project.categoryCode             = categoryCode;
		project.description              = "description";
		project.umbrellaProjectCode      = null;
		project.lastSampleCode           = "lastSampleCode";
		project.nbCharactersInSampleCode = 4;
		project.archive                  = Boolean.FALSE;
		project.state                    = new State(ProjectStateNames.N, user);
		project.authorizedUsers          = authorizedUsers(user);
		project.bioinformaticParameters  = params();
		project.comments                 = comments(user);
		project.code                     = DevAppTesting.newCode(); 
		project.traceInformation         = traceInformation(user);
		project.properties.put("unixGroup", new PropertySingleValue("g_cns"));
		return project;
	}
	
	//-- Complex field constructors
	
	private static BioinformaticParameters params() {
		BioinformaticParameters params = new BioinformaticParameters();
		params.regexBiologicalAnalysis = "regexBiologicalAnalysis";
		params.mappingReference        = "mappingReference";
		params.fgGroup                 = "fgGroup";
		params.fgPriority              = 1;
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
		form.projectCodes       = Arrays.asList(code);
		return new TestListFormWrapperFactory<Project>().wrapping().apply(form, qmode, render);
	}

	public static ListFormWrapper<Project> wrapper(String code) throws Exception {
		return wrapper(code, null, null);
	}
}
