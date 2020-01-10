package fr.cea.ig.ngl.dao.api.sra;

import java.util.List;

import javax.inject.Inject;

import models.sra.submit.sra.instance.Configuration;

import org.mongojack.DBQuery.Query;
import org.mongojack.DBUpdate.Builder;

import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.GenericAPI;
import fr.cea.ig.ngl.dao.sra.ConfigurationDAO;
import fr.cea.ig.play.IGGlobals;

public class ConfigurationAPI extends GenericAPI<ConfigurationDAO, Configuration> {
	
	@Inject
	public ConfigurationAPI(ConfigurationDAO dao) {
		super(dao);
	}
	
	public Iterable<Configuration> dao_all() {
		return dao.all();
	}
	
	public Configuration dao_getObject(String configurationCode) {
		return dao.getObject(configurationCode);
	}
	
	public Configuration dao_findOne(Query q) {
		return dao.findOne(q);		
	}

	public void dao_update(Query query, Builder set) {
		dao.update(query, set);
	}

	public boolean dao_checkObjectExist(String key, String keyValue) {
		return dao.checkObjectExist(key, keyValue);
	}

	public void dao_saveObject(Configuration configuration) {
		dao.saveObject(configuration);
	}

	public void dao_deleteByCode(String configurationCode) {
		dao.deleteByCode(configurationCode);
	}
	
	/*-------------------------------------------------------------------------------------------------*/

	@Override
	protected List<String> authorizedUpdateFields() {
		return null;
	}

	@Override
	protected List<String> defaultKeys() {
		return null;
	}

	@Override
	public Configuration create(Configuration input, String currentUser)
			throws APIValidationException, APIException {
		return null;
	}

	@Override
	public Configuration update(Configuration input, String currentUser) throws APIException,
			APIValidationException {
		return null;
	}

	@Override
	public Configuration update(Configuration input, String currentUser, List<String> fields)
			throws APIException, APIValidationException {
		return null;
	}
	
	/**
	 * Acces à une instance globale de ConfigurationAPI, qui permet de remplacer les appels
	 * à MongoDBDAO qui est une globale, dans un contexte ou on ne peut pas injecter. 
	 * Exemple dans objets du domaine (sra.Experiment, ...)
	 * @return ConfigurationAPI
	 */
	public static ConfigurationAPI get() {
		return IGGlobals.instanceOf(ConfigurationAPI.class);
	}
		
}
