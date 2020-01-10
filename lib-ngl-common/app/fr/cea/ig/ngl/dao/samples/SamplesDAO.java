package fr.cea.ig.ngl.dao.samples;

import javax.inject.Inject;
import javax.inject.Singleton;

import fr.cea.ig.ngl.dao.GenericMongoDAO;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;

@Singleton
public class SamplesDAO extends GenericMongoDAO<Sample> {

	@Inject
	public SamplesDAO() {
		super(InstanceConstants.SAMPLE_COLL_NAME, Sample.class);
	}	

}
