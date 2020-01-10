package fr.cea.ig.ngl.dao.projects;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.GenericAPI;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.project.instance.Project;
import ngl.refactoring.state.ProjectStateNames;
import validation.ContextValidation;
import workflows.project.ProjectWorkflows;

@Singleton
public class ProjectsAPI extends GenericAPI<ProjectsDAO, Project> {

	private static final play.Logger.ALogger logger = play.Logger.of(ProjectsAPI.class);
	
	private final ProjectWorkflows workflows;
	@Inject
	public ProjectsAPI(ProjectsDAO dao,ProjectWorkflows workflows) {
		super(dao);
		this.workflows=workflows;
	}
	
	@Override
	protected List<String> authorizedUpdateFields() {
		return null;
	}


	@Override
	protected List<String> defaultKeys() {
		return null;
	}
	
	public Iterable<Project> all() throws APIException {
		return dao.all();
	}
	
	@Override
	public Project create(Project project, String currentUser) throws APIException, APIValidationException {
		if (project._id != null)
			throw new APIException(CREATE_SEMANTIC_ERROR_MSG); 
//		ContextValidation ctxVal = new ContextValidation(currentUser);
//		ctxVal.setCreationMode();
		ContextValidation ctxVal = ContextValidation.createCreationContext(currentUser);
		project.traceInformation = new TraceInformation();
		project.traceInformation.creationStamp(ctxVal, currentUser);
		if (project.state == null)
			project.state = new State();
		project.state.code = ProjectStateNames.N;
		project.state.user = currentUser;
		project.state.date = new Date();
		
//		ctxVal.setCreationMode();
		project.validate(ctxVal);
		if (ctxVal.hasErrors())
			throw new APIValidationException(INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
		return dao.saveObject(project);
	}
	
	// GA: may be need to change implementation
	@Override
	public Project update(Project input, String currentUser, List<String> fields) throws APIException, APIValidationException {
		return update(input, currentUser);
		
	}
	
	@Override
	public Project update(Project input, String currentUser) throws APIException, APIValidationException {
		Project project = this.get(input.code);
		if (project == null)
			throw new APIException("Project with code " + input.code + " not exist");
//		ContextValidation ctxVal = new ContextValidation(currentUser); 
//		ctxVal.setUpdateMode();
		ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser); 
		if (input.traceInformation != null) {
			input.traceInformation.modificationStamp(ctxVal, currentUser);
		} else {
			logger.error("traceInformation is null !!");
		}
//		ctxVal.setUpdateMode();
		input.validate(ctxVal);
		if (ctxVal.hasErrors()) 
			throw new APIValidationException("Invalid Project object", ctxVal.getErrors());		
		dao.updateObject(input);
		return get(input.code);
	}
	
	
	public Project updateState(String code, State state, String currentUser) throws APIException
	{
		Project projectInDb = this.get(code);
		if(projectInDb == null) {
			throw new APIException("Project with code " + code + " not exist");
		} else {
//			ContextValidation ctxVal = new ContextValidation(currentUser);
			ContextValidation ctxVal = ContextValidation.createUndefinedContext(currentUser);
			workflows.setState(ctxVal, projectInDb, state);
			if (!ctxVal.hasErrors()) {
				return get(code);
			} else {
				throw new APIValidationException("Invalid state modification", ctxVal.getErrors());
			}
		}
	}
}
