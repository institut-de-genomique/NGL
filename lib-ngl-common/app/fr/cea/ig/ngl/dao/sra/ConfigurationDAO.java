package fr.cea.ig.ngl.dao.sra;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.mongojack.DBQuery.Query;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.dao.GenericMongoDAO;
import models.sra.submit.sra.instance.Configuration;
import models.utils.InstanceConstants;

@Singleton
public class ConfigurationDAO extends GenericMongoDAO<Configuration> {
	
	@Inject
	public ConfigurationDAO() {
		super(InstanceConstants.SRA_CONFIGURATION_COLL_NAME, Configuration.class);
	}
	
    public void deleteObject(Query query) {
        MongoDBDAO.delete(this.getCollectionName(), this.getElementClass(), query);
    }
    
}

