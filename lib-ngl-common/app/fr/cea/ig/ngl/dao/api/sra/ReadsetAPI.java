package fr.cea.ig.ngl.dao.api.sra;

import java.util.List;

import javax.inject.Inject;

import org.mongojack.DBQuery.Query;
import org.mongojack.DBUpdate.Builder;

import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.GenericAPI;
import fr.cea.ig.ngl.dao.sra.ReadsetDAO;
import fr.cea.ig.play.IGGlobals;
import models.sra.submit.common.instance.Readset;

public class ReadsetAPI extends GenericAPI<ReadsetDAO, Readset> {

	@Inject
	public ReadsetAPI(ReadsetDAO dao) {
		super(dao);
	}

//	private final ExperimentDAO dao;
//	
//	@Inject
//	public ExperimentAPI (ExperimentDAO experimentDAO) {
//		this.dao = experimentDAO;
//	}

	public List<Readset> dao_findAsList(Query q) {
		return dao.findAsList(q);
	}
	
	public Iterable<Readset> dao_all() {
		return dao.all();
	}
	
	public Readset dao_getObject(String experimentCode) {
		return dao.getObject(experimentCode);
	}
	
	public Readset dao_findOne(Query q) {
		return dao.findOne(q);		
	}

	public void dao_update(Query query, Builder set) {
		dao.update(query, set);
	}

	public boolean dao_checkObjectExist(String key, String keyValue) {
		return dao.checkObjectExist(key, keyValue);
	}

	public void dao_saveObject(Readset readset) {
		dao.saveObject(readset);
	}

	public void dao_deleteByCode(String readsetCode) {
		dao.deleteByCode(readsetCode);
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
	public Readset update(Readset input, String currentUser)
			throws APIException, APIValidationException {
		return null;
	}

	@Override
	public Readset create(Readset input, String currentUser)
			throws APIValidationException, APIException {
		return null;
	}

	@Override
	public Readset update(Readset input, String currentUser,
			List<String> fields) throws APIException, APIValidationException {
		return null;
	}

	/**
	 * Acces à une instance globale de ExperimentAPI, qui permet de remplacer les appels
	 * à MongoDBDAO qui est une globale, dans un contexte ou on ne peut pas injecter. 
	 * Exemple dans objets du domaine (sra.Experiment, ...)
	 * @return ExperimentAPI
	 */
	public static ReadsetAPI get() {
		return IGGlobals.instanceOf(ReadsetAPI.class);
	}
	
}
