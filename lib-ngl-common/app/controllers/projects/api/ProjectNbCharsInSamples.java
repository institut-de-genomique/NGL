package controllers.projects.api;

import javax.inject.Inject;

import controllers.NGLController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.projects.ProjectNbCharsInSamplesAPI;
import fr.cea.ig.ngl.dao.projects.ProjectsAPI;
import fr.cea.ig.ngl.support.NGLForms;
import models.laboratory.project.instance.Project;
import play.data.Form;
import play.mvc.Result;

public class ProjectNbCharsInSamples extends NGLController implements NGLForms {
	
	private final ProjectNbCharsInSamplesAPI api;
    private final ProjectsAPI projectsApi;
    private final Form<Integer> nbCharactersInSampleCodeForm;

    @Inject
	public ProjectNbCharsInSamples(NGLApplication app, ProjectNbCharsInSamplesAPI api, ProjectsAPI projectsApi) {
		super(app);
		this.api = api;
		this.projectsApi = projectsApi;
		this.nbCharactersInSampleCodeForm = app.formFactory().form(Integer.class);
	}
	
	private Project getProject(String projectCode) {
    	return projectsApi.get(projectCode);
    }
    
    @Authenticated
    @Authorized.Write
    public Result update(String projectCode){
        return globalExceptionHandler(() -> {
            try {
            	Project project = getProject(projectCode);
            	if (project != null) {
            		Integer nbCharactersInSampleCode = getFilledForm(nbCharactersInSampleCodeForm, Integer.class).get();
            		return okAsJson(api.update(project, nbCharactersInSampleCode, getCurrentUser()));
            	} else {
                	return badRequestAsJson("Project with code " + projectCode + " doesn't exist");
                }
            } catch (APIValidationException e) {
                return badRequestLoggingForValidationException(e);
            } catch (APIException e) {
            	 getLogger().error(e.getMessage(), e);
                 return badRequestAsJson(e.getMessage());
			}
        });
    }

}
