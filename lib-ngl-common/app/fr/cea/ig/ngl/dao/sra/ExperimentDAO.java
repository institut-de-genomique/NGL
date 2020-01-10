package fr.cea.ig.ngl.dao.sra;

import javax.inject.Inject;
import models.sra.submit.sra.instance.Experiment;
import models.utils.InstanceConstants;
import fr.cea.ig.ngl.dao.GenericMongoDAO;

public class ExperimentDAO extends GenericMongoDAO<Experiment> {
	
	@Inject
	public ExperimentDAO() {
		super(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class);
	}
	
}
