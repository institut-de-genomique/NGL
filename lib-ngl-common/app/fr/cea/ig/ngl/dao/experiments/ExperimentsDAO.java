package fr.cea.ig.ngl.dao.experiments;

import javax.inject.Inject;

import fr.cea.ig.ngl.dao.GenericMongoDAO;
import models.laboratory.experiment.instance.Experiment;
import models.utils.InstanceConstants;

public class ExperimentsDAO extends GenericMongoDAO<Experiment> {
	
	@Inject
	public ExperimentsDAO() {
		super(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class);
	}

}
