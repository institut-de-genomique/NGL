package fr.cea.ig.ngl.dao.sra;

import javax.inject.Inject;

import org.mongojack.DBQuery.Query;

import models.sra.submit.sra.instance.Project;
import models.utils.InstanceConstants;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.dao.GenericMongoDAO;

public class ProjectDAO extends GenericMongoDAO<Project> {
	
	@Inject
	public ProjectDAO() {
		super(InstanceConstants.SRA_PROJECT_COLL_NAME, Project.class);
	}
	
    public void deleteObject(Query query) {
        MongoDBDAO.delete(this.getCollectionName(), this.getElementClass(), query);
    }	
}


