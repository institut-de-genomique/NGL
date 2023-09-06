package controllers.projects.api;

import javax.inject.Inject;

import controllers.NGLAPIController;
import controllers.QueryFieldsForm;
import controllers.StateController;
import controllers.authorisation.PermissionHelper;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.lfw.Historized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.projects.ProjectsAPI;
import fr.cea.ig.ngl.dao.projects.ProjectsDAO;
import fr.cea.ig.play.IGBodyParsers;
import models.laboratory.common.instance.State;
import models.laboratory.project.instance.Project;
import play.data.Form;
import play.mvc.BodyParser;
import play.mvc.Result;
import views.components.datatable.DatatableForm;

/**
 * Controller around Project object
 *
 */
@Historized
public class Projects extends NGLAPIController<ProjectsAPI, ProjectsDAO, Project> implements StateController {

	private final Form<Project> projectForm;

	@Inject
	public Projects(NGLApplication app, ProjectsAPI api) {
		super(app, api, ProjectsSearchForm.class);
		this.projectForm = app.formFactory().form(Project.class);
	}

	@Override
	public Project saveImpl() throws APIValidationException, APIException {
		boolean isChefProjet = PermissionHelper.checkPermission(getCurrentUser(), "chef-projet");

		if (isChefProjet) {
			Project projectInput = getFilledForm(projectForm, Project.class).get();
			Project p = api().create(projectInput, getCurrentUser());
			return p;
		} else {
			throw new APIException("Current user is not 'chef-projet'.");
		}	
	}

	@Override
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
	
    @Override
    @BodyParser.Of(value = IGBodyParsers.Json15MB.class)
    public Result update(String code) {
		boolean isChefProjet = PermissionHelper.checkPermission(getCurrentUser(), "chef-projet");

		if (isChefProjet) {
			return super.update(code);
		} else {
			return unauthorized();
		}		
	}

	@Override
	public Project updateImpl(String code) throws Exception, APIException, APIValidationException {
		boolean isChefProjet = PermissionHelper.checkPermission(getCurrentUser(), "chef-projet");

		if (isChefProjet) {
			Project input = getFilledForm(projectForm, Project.class).get();
			
			Form<QueryFieldsForm> filledQueryFieldsForm = filledFormQueryString(updateForm, QueryFieldsForm.class);
			QueryFieldsForm queryFieldsForm = filledQueryFieldsForm.get();
			if (queryFieldsForm.fields == null) {
				if (code.equals(input.code)) {
					return api().update(input, getCurrentUser());
				} else {
					throw new APIException("Project codes are not the same");
				}
			} else {
				if (code.equals(input.code)) {
					return api().update(input, getCurrentUser(), queryFieldsForm.fields);
				} else {
					throw new APIException("Project codes are not the same");
				}
			}
		} else {
			throw new APIException("Current user is not 'chef-projet'.");
		}	
	}

	@Override
	public Project updateStateImpl(String code, State state, String currentUser) throws APIException {
		boolean isChefProjet = PermissionHelper.checkPermission(getCurrentUser(), "chef-projet");

		if (isChefProjet) {
			return api().updateState(code, state, currentUser);
		} else {
			throw new APIException("Current user is not 'chef-projet'.");
		}	
	}

}
