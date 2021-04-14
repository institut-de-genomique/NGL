package fr.cea.ig.ngl.dao.api.sra;

import java.util.List;

import javax.inject.Inject;

import org.mongojack.DBQuery.Query;

//import fr.cea.ig.lfw.utils.NotImplementedException;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.GenericAPI;
import fr.cea.ig.ngl.dao.sra.AbstractSampleDAO;
import fr.cea.ig.play.IGGlobals;
import models.sra.submit.common.instance.AbstractSample;

public class AbstractSampleAPI extends GenericAPI<AbstractSampleDAO, AbstractSample> {
	
//	private final AbstractSampleDAO dao;
//	
//	@Inject
//	public AbstractSampleAPI (AbstractSampleDAO abstractSampleDAO) {
//		this.dao = abstractSampleDAO;
//	}

	@Inject
	public AbstractSampleAPI(AbstractSampleDAO dao) {
		super(dao);
	}
	
	public Iterable<AbstractSample> dao_all() {
		return dao.all();
	}
		
	public boolean dao_checkObjectExist(String key, String keyValue) {
		return dao.checkObjectExist(key, keyValue);
	}

	public AbstractSample dao_findOne(Query q) {
		return dao.findOne(q);		
	}

	public AbstractSample dao_getObject(String sampleCode) {
		return dao.getObject(sampleCode);
	}

	public void dao_saveObject(AbstractSample sampleElt) {
		dao.saveObject(sampleElt);
	}

	/*-------------------------------------------------------------------------------------------------*/

	@Override
	protected List<String> authorizedUpdateFields() {
		throw new RuntimeException("NotImplemented");
	}

	@Override
	protected List<String> defaultKeys() {
		throw new RuntimeException("NotImplemented");
	}

	@Override
	public AbstractSample create(AbstractSample input, String currentUser)
			throws APIValidationException, APIException {
		throw new RuntimeException("NotImplemented");
	}

	@Override
	public AbstractSample update(AbstractSample input, String currentUser)
			throws APIException, APIValidationException {
		throw new RuntimeException("NotImplemented");
	}

	@Override
	public AbstractSample update(AbstractSample input, String currentUser,
			List<String> fields) throws APIException, APIValidationException {
		//throw new NotImplementedException();
		throw new RuntimeException("NotImplemented");
	}
	/**
	 * Acces à une instance globale de AbstractSampleAPI, qui permet de remplacer les appels
	 * à MongoDBDAO qui est une globale, dans un contexte ou on ne peut pas injecter. 
	 * Exemple dans objets du domaine (sra.Experiment, ...)
	 * @return StudyAPI
	 */
	public static AbstractSampleAPI get() {
		return IGGlobals.instanceOf(AbstractSampleAPI.class);
	}
}
