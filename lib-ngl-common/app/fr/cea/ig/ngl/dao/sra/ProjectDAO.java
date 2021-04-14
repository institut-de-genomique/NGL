package fr.cea.ig.ngl.dao.sra;

import javax.inject.Inject;

import models.sra.submit.common.instance.Project;
import models.utils.InstanceConstants;
import fr.cea.ig.ngl.dao.GenericMongoDAO;

public class ProjectDAO extends GenericMongoDAO<Project> {
	
	@Inject
	public ProjectDAO() {
		super(InstanceConstants.SRA_PROJECT_COLL_NAME, Project.class);
	}
	
}
