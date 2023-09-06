package workflows.project;

import javax.inject.Inject;
import javax.inject.Singleton;

import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.projects.MembersProjectsAPI;
import models.laboratory.project.instance.Members;
import models.laboratory.project.instance.Project;
import validation.ContextValidation;

@Singleton
public class ProjectWorkflowHelper {

	private static final play.Logger.ALogger logger = play.Logger.of(ProjectWorkflowHelper.class);
	
	private final MembersProjectsAPI membersAPI;
	
	@Inject
	public ProjectWorkflowHelper(MembersProjectsAPI membersAPI) {
		this.membersAPI = membersAPI;
	}
	
	public void createMembersProject(ContextValidation contextValidation, Project project) {
		Members members = new Members();
		members.codeProjet = project.code;
		//Convert to lowercase for ad convention
		members.codeProjet =members.codeProjet.toLowerCase();
		try {
			membersAPI.create(members, contextValidation.getUser());
		} catch (APIValidationException e) {
			// EJACOBY: properly handle errors
			logger.error(e.getMessage());
		} 
	}
	
}
