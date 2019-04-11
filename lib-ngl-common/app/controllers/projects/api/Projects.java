package controllers.projects.api;

import javax.inject.Inject;

import controllers.NGLAPIController;
import controllers.StateController;
import fr.cea.ig.lfw.Historized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.projects.ProjectsAPI;
import fr.cea.ig.ngl.dao.projects.ProjectsDAO;
import models.laboratory.common.instance.State;
import models.laboratory.project.instance.Project;
import play.data.Form;
/**
 * Controller around Project object
 *
 */
@Historized
public class Projects extends NGLAPIController<ProjectsAPI, ProjectsDAO, Project> implements StateController{ // implements NGLForms, DBObjectConvertor {

	private final Form<Project> projectForm;
	
	@Inject
	public Projects(NGLApplication app, ProjectsAPI api) {
		super(app, api, ProjectsSearchForm.class);
		this.projectForm = app.formFactory().form(Project.class);
	}
	

	@Override
	public Project saveImpl() throws APIValidationException, APIException {
		Project projectInput = getFilledForm(projectForm, Project.class).get();
		Project p = api().create(projectInput, getCurrentUser());
		return p;
	}

	@Override
	public Project updateImpl(String code) throws Exception, APIException, APIValidationException {
		Form<Project> filledForm = getFilledForm(projectForm, Project.class);
		Project projectInput = filledForm.get();
		if(code.equals(projectInput.code)) {
		Project project = api().update(projectInput, getCurrentUser());
		return project;
		} else {
			throw new Exception("Project codes are not the same");
		}
	}

	@Override
	public Object updateStateImpl(String code, State state, String currentUser) throws APIException {
		return api().updateState(code, state, currentUser);
	}

}
