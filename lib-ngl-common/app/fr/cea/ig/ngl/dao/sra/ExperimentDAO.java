package fr.cea.ig.ngl.dao.sra;

import javax.inject.Inject;

import org.mongojack.DBQuery.Query;

import models.sra.submit.sra.instance.Experiment;
import models.utils.InstanceConstants;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.dao.GenericMongoDAO;

public class ExperimentDAO extends GenericMongoDAO<Experiment> {
	
	@Inject
	public ExperimentDAO() {
		super(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class);
	}
	
    public void deleteObject(Query query) {
        MongoDBDAO.delete(this.getCollectionName(), this.getElementClass(), query);
    }
}
