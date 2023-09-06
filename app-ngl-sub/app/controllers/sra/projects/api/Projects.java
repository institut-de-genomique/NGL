package controllers.sra.projects.api;


import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import controllers.NGLAPIController;
import controllers.QueryFieldsForm;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.sra.ProjectAPI;
import fr.cea.ig.ngl.dao.sra.ProjectDAO;
import models.sra.submit.sra.instance.Project;
import play.data.Form;
import play.mvc.Result;
import views.components.datatable.DatatableForm;

public class Projects extends  NGLAPIController<ProjectAPI, ProjectDAO, Project>  {

	//	private static final play.Logger.ALogger logger = play.Logger.of(Studies.class);

	private final Form<Project>              projectForm;
	private final Form<QueryFieldsForm>      updateForm;
	private static final play.Logger.ALogger logger        = play.Logger.of(Projects.class);


	@Inject
	public Projects(NGLApplication app,
			        ProjectAPI     api) {
		super(app, api, ProjectsSearchForm.class);
		this.projectForm            = app.formFactory().form(Project.class);
		this.updateForm             = app.form(QueryFieldsForm.class);
	}


	@Override
	@Authenticated
	@Authorized.Read
	public Result get(String code) {
	    return globalExceptionHandler(() -> {
			DatatableForm form = objectFromRequestQueryString(DatatableForm.class);
			Project project = api().getObject(code, generateBasicDBObjectFromKeys(form));
			if (project == null) {
				return notFound();
			} 
			return okAsJson(project);
		});	
	}	
	
	// methode saveImpl appelée par le save de la classe mere, elle meme appelée par la route 
	@Override
	@Authenticated
	@Authorized.Write
	public Project saveImpl() throws APIException {
		Project userProject = getFilledForm(projectForm, Project.class).get();
		boolean copy = false;
		ProjectsUrlParamForm filledProjectsUrlParamForm = filledFormQueryString(getNGLApplication().formFactory()
				.form(ProjectsUrlParamForm.class),  ProjectsUrlParamForm.class).get();

		if (filledProjectsUrlParamForm != null  && filledProjectsUrlParamForm.copy != null && filledProjectsUrlParamForm.copy == true ) {
			copy = true;
		} else {
			copy = false;
		}
		return api().create(userProject, getCurrentUser(), copy);	
	}


	@Override
	@Authenticated
	@Authorized.Write
	public Project updateImpl(String code) throws Exception, APIException, APIValidationException {
		Project userProject =  getFilledForm(projectForm, Project.class).get();
		//Form<QueryFieldsForm> filledQueryFieldsForm = filledFormQueryString(updateForm, QueryFieldsForm.class);
		//QueryFieldsForm queryFieldsForm = filledQueryFieldsForm.get();
		QueryFieldsForm queryFieldsForm = filledFormQueryString(updateForm, QueryFieldsForm.class).get();
		if (StringUtils.isBlank(code)) {
			throw new Exception("project.code :  valeur nulle  incompatible avec appel de la methode updateImpl");
		}
		if(StringUtils.isNotBlank(userProject.code) && ! userProject.code.equals(code)) {
			throw new Exception("project.code :  valeur " + userProject.code + " != du code indiqué dans la route "  + code);
		}
		boolean copy = false;
		if (queryFieldsForm.fields == null) {	
			return api().update(userProject, getCurrentUser(), false);
		} 
		if(queryFieldsForm.fields.contains("copy")) {
			copy = true;
			queryFieldsForm.fields.remove("copy");
			return api().update(userProject, getCurrentUser(), true);
		} 
		return api().update(userProject, getCurrentUser(), queryFieldsForm.fields); 
	}
	



}
