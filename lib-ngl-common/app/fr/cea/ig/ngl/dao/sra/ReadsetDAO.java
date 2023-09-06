package fr.cea.ig.ngl.dao.sra;

import javax.inject.Inject;

import models.sra.submit.sra.instance.Readset;
import models.utils.InstanceConstants;
import fr.cea.ig.ngl.dao.GenericMongoDAO;

public class ReadsetDAO extends GenericMongoDAO<Readset> {
	
	@Inject
	public ReadsetDAO() {
		super(InstanceConstants.SRA_READSET_COLL_NAME, Readset.class);
	}
	
}
