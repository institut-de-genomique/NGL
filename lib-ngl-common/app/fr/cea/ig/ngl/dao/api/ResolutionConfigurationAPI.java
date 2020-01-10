package fr.cea.ig.ngl.dao.api;

import javax.inject.Inject;
import javax.inject.Singleton;

import fr.cea.ig.ngl.dao.ResolutionConfigurationDAO;
import models.laboratory.resolutions.instance.ResolutionConfiguration;
import models.utils.dao.DAOException;

@Singleton
public class ResolutionConfigurationAPI {
	
	private final ResolutionConfigurationDAO dao;
	
	@Inject
	public ResolutionConfigurationAPI(ResolutionConfigurationDAO dao) {
		this.dao = dao;
	}

	public Iterable<ResolutionConfiguration> all() throws DAOException, APIException {
		return dao.all();
	}
	
}
