package fr.cea.ig.ngl.dao.projects;

import javax.inject.Inject;
import javax.inject.Singleton;

import fr.cea.ig.ngl.dao.GenericMongoDAO;
import models.laboratory.project.instance.UmbrellaProject;
import models.utils.InstanceConstants;

@Singleton
public class UmbrellaProjectsDAO extends GenericMongoDAO<UmbrellaProject> {
	
	@Inject
	public UmbrellaProjectsDAO() {
		super(InstanceConstants.UMBRELLA_PROJECT_COLL_NAME, UmbrellaProject.class);
	}	
	
}
