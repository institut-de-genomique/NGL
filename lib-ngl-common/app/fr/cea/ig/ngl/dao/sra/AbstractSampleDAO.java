package fr.cea.ig.ngl.dao.sra;


import javax.inject.Inject;

import models.sra.submit.common.instance.AbstractSample;
import models.utils.InstanceConstants;
import fr.cea.ig.ngl.dao.GenericMongoDAO;

public class AbstractSampleDAO extends GenericMongoDAO<AbstractSample> {

	@Inject
	public AbstractSampleDAO() {
		super(InstanceConstants.SRA_SAMPLE_COLL_NAME, AbstractSample.class);
	}

}
