package controllers.projects.api;

import javax.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;

import controllers.NGLController;
import controllers.QueryFieldsForm;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.projects.ProjectSystemParametersAPI;
import fr.cea.ig.ngl.dao.projects.ProjectsAPI;
import fr.cea.ig.ngl.support.NGLForms;
import fr.cea.ig.play.IGBodyParsers;
import models.laboratory.project.instance.Project;
import models.laboratory.project.instance.SystemParameters;
import play.data.Form;
import play.mvc.BodyParser;
import play.mvc.Result;

public class ProjectSystemParameters extends NGLController implements NGLForms {

	private final ProjectSystemParametersAPI api;
    private final ProjectsAPI projectsApi;
    private final Form<QueryFieldsForm> updateForm;
    private final Form<SystemParameters> systemParametersForm; 
    private final Form<SystemParameters.ArchiveEntry> archiveEntryForm; 

    @Inject
    public ProjectSystemParameters(NGLApplication app, ProjectSystemParametersAPI api, ProjectsAPI projectsApi) {
        super(app);
        this.api           = api;
        this.projectsApi    = projectsApi;
        this.updateForm = app.formFactory().form(QueryFieldsForm.class);
        this.systemParametersForm = app.formFactory().form(SystemParameters.class);
        this.archiveEntryForm = app.formFactory().form(SystemParameters.ArchiveEntry.class);
    }
    
    private Project getProject(String projectCode) {
    	return projectsApi.get(projectCode);
    }

    @Authenticated
    @Authorized.Read
    public Result get(String projectCode){
        return globalExceptionHandler(() -> {
            Project project = getProject(projectCode);
            if (project != null && api.isInnerObject(project)) {
            	SystemParameters systemParameters = api.getInnerObject(project);
                return okAsJson(systemParameters);
            } else {
                return notFound();
            }
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
            		SystemParameters systemParameters = getFilledForm(systemParametersForm, SystemParameters.class).get();
            		return okAsJson(api.save(project, systemParameters, projectCode));
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
    
    private SystemParameters updateSystemParameters(Project project, SystemParameters systemParameters) throws APIException {
    	QueryFieldsForm queryFieldsForm = filledFormQueryString(updateForm, QueryFieldsForm.class).get();
    	return CollectionUtils.isEmpty(queryFieldsForm.fields) ? 
				api.update(project, systemParameters, getCurrentUser()) : api.update(project, systemParameters, getCurrentUser(), queryFieldsForm.fields);
    }
    
    @Authenticated
    @Authorized.Write
    @BodyParser.Of(value = IGBodyParsers.Json5MB.class)
    public Result update(String projectCode){
        return globalExceptionHandler(() -> {
            try {
            	Project project = getProject(projectCode);
            	if (project != null) {
            		SystemParameters systemParameters = getFilledForm(systemParametersForm, SystemParameters.class).get();
            		return okAsJson(updateSystemParameters(project, systemParameters));
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
    
    @Authenticated
    @Authorized.Write
    public Result saveArchiveEntry(String projectCode) {
    	return globalExceptionHandler(() -> {
            try {
            	Project project = getProject(projectCode);
            	if (project != null) {
            		SystemParameters.ArchiveEntry entry = getFilledForm(archiveEntryForm, SystemParameters.ArchiveEntry.class).get();
            		return okAsJson(api.appendToArchiveHistory(project, entry, getCurrentUser()));
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
