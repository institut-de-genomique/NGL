package controllers.projects.api;

import javax.inject.Inject;

import controllers.NGLController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.projects.ProjectAnalysisTypesAPI;
import fr.cea.ig.ngl.dao.projects.ProjectsAPI;
import fr.cea.ig.ngl.support.NGLForms;
import fr.cea.ig.play.IGBodyParsers;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.project.instance.Project;
import play.data.Form;
import play.mvc.BodyParser;
import play.mvc.Result;

public class ProjectAnalysisTypes extends NGLController implements NGLForms {
	
	private final ProjectAnalysisTypesAPI api;
    private final ProjectsAPI projectsApi;
    private final Form<PropertyValue> analysisTypesForm;

    @Inject
	public ProjectAnalysisTypes(NGLApplication app, ProjectAnalysisTypesAPI api, ProjectsAPI projectsApi) {
		super(app);
		this.api = api;
		this.projectsApi = projectsApi;
		this.analysisTypesForm = app.formFactory().form(PropertyValue.class);
	}
    
    private Project getProject(String projectCode) {
    	return projectsApi.get(projectCode);
    }
    
    @Authenticated
    @Authorized.Read
    public Result head(String projectCode){
        return globalExceptionHandler(() -> {
            Project project = getProject(projectCode);
            return project != null && api.isInnerObject(project) ? ok() : notFound();
        });
    }

    @Authenticated
    @Authorized.Write
    @BodyParser.Of(value = IGBodyParsers.Json5MB.class)
    public Result save(String projectCode){
        return globalExceptionHandler(() -> {
        	try {
            	Project project = getProject(projectCode);
            	if (project != null) {
            		PropertyValue analysisTypes = getFilledForm(analysisTypesForm, PropertyValue.class).get();
            		return okAsJson(api.save(project, analysisTypes, projectCode));
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
    
    @Authenticated
    @Authorized.Write
    @BodyParser.Of(value = IGBodyParsers.Json5MB.class)
    public Result update(String projectCode){
        return globalExceptionHandler(() -> {
            try {
            	Project project = getProject(projectCode);
            	if (project != null) {
            		PropertyValue analysisTypes = getFilledForm(analysisTypesForm, PropertyValue.class).get();
            		return okAsJson(api.update(project, analysisTypes, getCurrentUser()));
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
    
    @Authenticated
    @Authorized.Write
    public Result delete(String projectCode){
        return globalExceptionHandler(() -> {
            try {
            	Project project = getProject(projectCode);
            	if (project != null) {
                    api.delete(project, getCurrentUser());
                    return ok();
            	} else {
                	return badRequestAsJson("Project with code " + projectCode + " doesn't exist");
                }
            } catch (APIException e) {
            	getLogger().error(e.getMessage(), e);
                return badRequestAsJson(e.getMessage());
            }
        });  
    }

}
