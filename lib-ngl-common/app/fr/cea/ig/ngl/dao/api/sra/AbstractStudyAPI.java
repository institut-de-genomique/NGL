package fr.cea.ig.ngl.dao.api.sra;

import java.util.List;

import javax.inject.Inject;

import org.mongojack.DBQuery.Query;

//import fr.cea.ig.lfw.utils.NotImplementedException;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.GenericAPI;
import fr.cea.ig.ngl.dao.sra.AbstractStudyDAO;
import fr.cea.ig.play.IGGlobals;
import models.sra.submit.common.instance.AbstractStudy;
import models.sra.submit.common.instance.ExternalStudy;
import models.sra.submit.common.instance.Study;

public class AbstractStudyAPI extends GenericAPI<AbstractStudyDAO, AbstractStudy> {
	
	private StudyAPI studyAPI;
	private ExternalStudyAPI externalStudyAPI;

	@Inject
	public AbstractStudyAPI(AbstractStudyDAO dao,
							StudyAPI studyAPI,
							ExternalStudyAPI externalStudyAPI) {
		super(dao);
		this.studyAPI = studyAPI;
	}

//	private final AbstractStudyDAO dao;
//	
//	@Inject
//	public AbstractStudyAPI (AbstractStudyDAO abstractStudyDAO) {
//		this.dao = abstractStudyDAO;
//	}
	
	public Iterable<AbstractStudy> dao_all() {
		return dao.all();
	}
	
	public boolean dao_checkObjectExist(String key, String keyValue) {
		return dao.checkObjectExist(key, keyValue);
	}

	public AbstractStudy dao_findOne(Query q) {
		return dao.findOne(q);	
	}

	public AbstractStudy dao_getObject(String studyCode) {
		return dao.getObject(studyCode);
	}
	
	public void dao_saveObject(AbstractStudy absStudyElt) {
		dao.saveObject(absStudyElt);
	}
	
	/*-------------------------------------------------------------------------------------------------*/
		
	@Override
	protected List<String> authorizedUpdateFields() {
		// throw new NotImplementedException();
		throw new RuntimeException("NotImplementedException");
	}

	@Override
	protected List<String> defaultKeys() {
		//throw new NotImplementedException();
		throw new RuntimeException("NotImplementedException");
	}

	@Override
	public AbstractStudy create(AbstractStudy input, String currentUser)
			throws APIValidationException, APIException {
		if (input instanceof Study) {
			studyAPI.create((Study) input, currentUser);	
		} else if (input instanceof ExternalStudy) {
			externalStudyAPI.create((ExternalStudy) input, currentUser);
		} else {
			throw new APIException("type de study "+  input.getClass() + "non supporté");
		}
		return input;
	}

	@Override
	public AbstractStudy update(AbstractStudy input, String currentUser)
			throws APIException, APIValidationException {
		//throw new NotImplementedException();
		throw new RuntimeException("NotImplementedException");

	}

	@Override
	public AbstractStudy update(AbstractStudy input, String currentUser,
			List<String> fields) throws APIException, APIValidationException {
//		throw new NotImplementedException();
		throw new RuntimeException("NotImplementedException");

	}
	/**
	 * Acces à une instance globale de AbstractStudyAPI, qui permet de remplacer les appels
	 * à MongoDBDAO qui est une globale, dans un contexte ou on ne peut pas injecter. 
	 * Exemple dans objets du domaine (sra.Experiment, ...)
	 * @return StudyAPI
	 */
	public static AbstractStudyAPI get() {
		return IGGlobals.instanceOf(AbstractStudyAPI.class);
	}
		

}
