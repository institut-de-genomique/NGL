package controllers.projects.api;


import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.inject.Inject;

import org.mongojack.DBQuery;

import controllers.NGLController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.projects.ProjectBioinformaticParametersAPI;
import fr.cea.ig.ngl.dao.projects.ProjectsAPI;
import fr.cea.ig.ngl.support.NGLForms;
import fr.cea.ig.play.IGBodyParsers;
import models.laboratory.project.instance.BioinformaticParameters;
import models.laboratory.project.instance.Project;
import play.data.Form;
import play.mvc.BodyParser;
import play.mvc.Result;

public class ProjectBioinformaticParameters extends NGLController implements NGLForms {

	private final ProjectBioinformaticParametersAPI api;
    private final ProjectsAPI projectsApi;
    private final Form<BioinformaticParameters> bioinformaticParametersForm; 

    @Inject
    public ProjectBioinformaticParameters(NGLApplication app, ProjectBioinformaticParametersAPI api, ProjectsAPI projectsApi) {
        super(app);
        this.api           = api;
        this.projectsApi    = projectsApi;
        this.bioinformaticParametersForm = app.formFactory().form(BioinformaticParameters.class);
    }
    
    private Project getProject(String projectCode) {
    	return projectsApi.get(projectCode);
    }
    
    @Authenticated
    @Authorized.Read
    public Result list(){
        return globalExceptionHandler(() -> {
        	try {
	        	Iterable <Project> projects = projectsApi.find(DBQuery.exists("bioinformaticParameters.fgGroup"));
	        	List<BioinformaticParameters> bioinformaticParameters = StreamSupport.stream(projects.spliterator(), false)
	        			.map(api::getInnerObject).collect(Collectors.toList());
                return okAsJson(bioinformaticParameters);
        	} catch (APIValidationException e) {
                return badRequestLoggingForValidationException(e);
            } catch (APIException e) {
            	 getLogger().error(e.getMessage(), e);
                 return badRequestAsJson(e.getMessage());
			}
        });
    }

    @Authenticated
    @Authorized.Read
    public Result get(String projectCode){
        return globalExceptionHandler(() -> {
            Project project = getProject(projectCode);
            if (project != null && api.isInnerObject(project)) {
            	BioinformaticParameters bioinformaticParameters = api.getInnerObject(project);
                return okAsJson(bioinformaticParameters);
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
            		BioinformaticParameters bioinformaticParameters = getFilledForm(bioinformaticParametersForm, BioinformaticParameters.class).get();
            		return okAsJson(api.save(project, bioinformaticParameters, projectCode));
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
            		BioinformaticParameters bioinformaticParameters = getFilledForm(bioinformaticParametersForm, BioinformaticParameters.class).get();
            		return okAsJson(api.update(project, bioinformaticParameters, getCurrentUser()));
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
