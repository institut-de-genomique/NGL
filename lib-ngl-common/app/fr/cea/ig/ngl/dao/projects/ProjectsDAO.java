package fr.cea.ig.ngl.dao.projects;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jongo.MongoCollection;
import org.jongo.MongoCursor;

import fr.cea.ig.ngl.dao.GenericMongoDAO;
import models.laboratory.project.instance.Project;
import models.utils.InstanceConstants;
import play.modules.jongo.MongoDBPlugin;

@Singleton
public class ProjectsDAO extends GenericMongoDAO<Project> {
	
	@Inject
	public ProjectsDAO() {
		super(InstanceConstants.PROJECT_COLL_NAME, Project.class);
	}	
	
}
