package controllers.projects.api;

import javax.inject.Inject;

import controllers.NGLAPIController;
import controllers.QueryFieldsForm;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.projects.UmbrellaProjectsAPI;
import fr.cea.ig.ngl.dao.projects.UmbrellaProjectsDAO;
import fr.cea.ig.play.IGBodyParsers;
import models.laboratory.project.instance.UmbrellaProject;
import play.data.Form;
import play.mvc.BodyParser;
import play.mvc.Result;

/**
 * Controller around UmbrellaProject object
 *
 */
public class UmbrellaProjects extends NGLAPIController<UmbrellaProjectsAPI, UmbrellaProjectsDAO, UmbrellaProject> {

	private final Form<UmbrellaProject> umbrellaProjectForm;

	@Inject
	public UmbrellaProjects(NGLApplication app, UmbrellaProjectsAPI api) {
		super(app, api, UmbrellaProjectsSearchForm.class);
		this.umbrellaProjectForm = app.formFactory().form(UmbrellaProject.class);
	}

	@Override
	public UmbrellaProject saveImpl() throws APIValidationException, APIException {
		UmbrellaProject umbrellaProjectInput = getFilledForm(umbrellaProjectForm, UmbrellaProject.class).get();
		UmbrellaProject p = api().create(umbrellaProjectInput, getCurrentUser());
		return p;	
	}
	
	@Override
    @BodyParser.Of(value = IGBodyParsers.Json15MB.class)
	@Authorized.ChefProjet
    public Result update(String code) {
		return super.update(code);	
	}

	@Override
	public UmbrellaProject updateImpl(String code) throws Exception, APIException, APIValidationException {
		UmbrellaProject input = getFilledForm(umbrellaProjectForm, UmbrellaProject.class).get();
			
		Form<QueryFieldsForm> filledQueryFieldsForm = filledFormQueryString(updateForm, QueryFieldsForm.class);
		QueryFieldsForm queryFieldsForm = filledQueryFieldsForm.get();
		
		if (queryFieldsForm.fields == null) {
			if (code.equals(input.code)) {
				return api().update(input, getCurrentUser());
			} else {
				throw new APIException("UmbrellaProject codes are not the same");
			}
		} else {
			if (code.equals(input.code)) {
				return api().update(input, getCurrentUser(), queryFieldsForm.fields);
			} else {
				throw new APIException("UmbrellaProject codes are not the same");
			}
		}
	}
}

