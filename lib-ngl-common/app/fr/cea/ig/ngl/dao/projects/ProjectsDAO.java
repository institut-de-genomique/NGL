package fr.cea.ig.ngl.dao.projects;

import javax.inject.Inject;
import javax.inject.Singleton;

import fr.cea.ig.ngl.dao.GenericMongoDAO;
import models.laboratory.project.instance.Project;
import models.utils.InstanceConstants;

@Singleton
public class ProjectsDAO extends GenericMongoDAO<Project> {
	
	@Inject
	public ProjectsDAO() {
		super(InstanceConstants.PROJECT_COLL_NAME, Project.class);
	}	
	
}
