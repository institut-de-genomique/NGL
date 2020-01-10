package fr.cea.ig.ngl.dao.sra;


import javax.inject.Inject;

import models.sra.submit.common.instance.ExternalSample;
import models.utils.InstanceConstants;
import fr.cea.ig.ngl.dao.GenericMongoDAO;

public class ExternalSampleDAO extends GenericMongoDAO<ExternalSample> {

	@Inject
	public ExternalSampleDAO() {
		super(InstanceConstants.SRA_SAMPLE_COLL_NAME, ExternalSample.class);
	}

}
