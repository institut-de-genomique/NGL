package fr.cea.ig.ngl.dao.sra;

import javax.inject.Inject;
import models.sra.submit.common.instance.Sample;
import models.utils.InstanceConstants;
import fr.cea.ig.ngl.dao.GenericMongoDAO;

public class SampleDAO extends GenericMongoDAO<Sample> {
	
	@Inject
	public SampleDAO() {
		super(InstanceConstants.SRA_SAMPLE_COLL_NAME, Sample.class);
	}
	
}
