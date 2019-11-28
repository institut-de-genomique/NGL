package fr.cea.ig.ngl.dao;

import javax.inject.Inject;
import javax.inject.Singleton;

import models.laboratory.resolutions.instance.ResolutionConfiguration;
import models.utils.InstanceConstants;

@Singleton
public class ResolutionConfigurationDAO extends GenericMongoDAO<ResolutionConfiguration>{

	@Inject
	public ResolutionConfigurationDAO() {
		super(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class);
	}
	
}
