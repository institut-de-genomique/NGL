package fr.cea.ig.ngl.dao.sra;

import javax.inject.Inject;

import org.mongojack.DBQuery.Query;

import models.sra.submit.sra.instance.Atlasea;
import models.utils.InstanceConstants;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.dao.GenericMongoDAO;

public class AtlaseaDAO extends GenericMongoDAO<Atlasea> {
	
	@Inject
	public AtlaseaDAO() {
		super(InstanceConstants.SRA_ATLASEA_COLL_NAME, Atlasea.class);
	}
	
    public void deleteObject(Query query) {
        MongoDBDAO.delete(this.getCollectionName(), this.getElementClass(), query);
    }
}
