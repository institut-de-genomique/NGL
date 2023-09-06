package workflows.project;

import javax.inject.Inject;
//import javax.inject.Inject;
import javax.inject.Singleton;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.State;
import models.laboratory.project.instance.Project;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import workflows.Workflows;

@Singleton
public class ProjectWorkflows extends Workflows<Project> {

	private static final play.Logger.ALogger logger = play.Logger.of(ProjectWorkflows.class);
	
	private final ProjectWorkflowHelper projectWorkflowHelper;
		
	@Inject
	public ProjectWorkflows(ProjectWorkflowHelper projectWorkflowHelper) {
		this.projectWorkflowHelper=projectWorkflowHelper;
	}
	
	@Override
	public void applyPreStateRules(ContextValidation validation, Project project, State nextState) {
	}

	@Override
	public void applyPreValidateCurrentStateRules(ContextValidation validation, Project project) {
	}

	@Override
	public void applyPostValidateCurrentStateRules(ContextValidation validation, Project project) {
	}

	@Override
	public void applySuccessPostStateRules(ContextValidation validation, Project project){
		if ("IP".equals(project.state.code)) {
			projectWorkflowHelper.createMembersProject(validation, project);
		}
	}

	@Override
	public void applyErrorPostStateRules(ContextValidation validation, Project project, State nextState) {
	}

	@Override
	public void setState(ContextValidation contextValidation, Project project, State nextState) {
		contextValidation.setUpdateMode();
//		CommonValidationHelper.validateState(project.typeCode, nextState, contextValidation);
		CommonValidationHelper.validateStateRequired(contextValidation, project.typeCode, nextState);
		if(!contextValidation.hasErrors() && !nextState.code.equals(project.state.code)){
//			boolean goBack = goBack(project.state, nextState);
			boolean backward = models.laboratory.common.description.State.find.get().isBackward(project.state.code, nextState.code);
			if (backward)
				logger.debug(project.code+" : back to the workflow. "+project.state.code+" -> "+nextState.code);
//			project.traceInformation = updateTraceInformation(project.traceInformation, nextState);
			project.traceInformation.forceModificationStamp(nextState.user, nextState.date);
//			project.state = updateHistoricalNextState(project.state, nextState);
			project.state = nextState.createHistory(project.state);
			
			MongoDBDAO.update(InstanceConstants.PROJECT_COLL_NAME, Project.class,
					DBQuery.is("code", project.code),
					DBUpdate.set("state", project.state).set("traceInformation", project.traceInformation));
			applySuccessPostStateRules(contextValidation, project);
			nextState(contextValidation, project);
		}else{
			logger.error("Error validation "+contextValidation.getErrors());
		}
		
	}

	@Override
	public void nextState(ContextValidation contextValidation, Project project) {
		
	}

}
