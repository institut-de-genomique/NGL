package fr.cea.ig.ngl.dao.readsets;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.mongojack.DBQuery.Query;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.dao.GenericMongoDAO;
import models.laboratory.run.instance.ReadSet;
import models.utils.InstanceConstants;

@Singleton
public class ReadSetsDAO extends GenericMongoDAO<ReadSet> {
	
	@Inject
	public ReadSetsDAO() {
		super(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class);
	}

    public void deleteObject(Query query) {
        MongoDBDAO.delete(this.getCollectionName(), this.getElementClass(), query);
    }
    
}
