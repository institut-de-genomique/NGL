package fr.cea.ig.ngl.dao.sra;

import javax.inject.Inject;

import models.sra.submit.sra.instance.Configuration;
import models.utils.InstanceConstants;
import fr.cea.ig.ngl.dao.GenericMongoDAO;

public class ConfigurationDAO extends GenericMongoDAO<Configuration> {
	
	@Inject
	public ConfigurationDAO() {
		super(InstanceConstants.SRA_CONFIGURATION_COLL_NAME, Configuration.class);
	}
	
}
