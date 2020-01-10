package fr.cea.ig.ngl.dao.api.sra;

import java.util.List;

import javax.inject.Inject;

import models.sra.submit.common.instance.ExternalStudy;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.GenericAPI;
import fr.cea.ig.ngl.dao.sra.ExternalStudyDAO;
import fr.cea.ig.play.IGGlobals;

import org.mongojack.DBQuery.Query;


public class ExternalStudyAPI extends GenericAPI<ExternalStudyDAO, ExternalStudy> {
	
	@Inject
	public ExternalStudyAPI(ExternalStudyDAO dao) {
		super(dao);
	}

//	private final ExternalStudyDAO dao;
//	
//	@Inject
//	public ExternalStudyAPI (ExternalStudyDAO externalStudyDAO) {
//		this.dao = externalStudyDAO;
//	}

	public boolean dao_checkObjectExist(String key, String keyValue) {
		return dao.checkObjectExist(key, keyValue);
	}

	public ExternalStudy dao_findOne(Query q) {
		return dao.findOne(q);		
	}

	public void dao_deleteByCode(String studyCode) {
		dao.deleteByCode(studyCode);
		
	}
	
	public void dao_saveObject(ExternalStudy study) {
		dao.saveObject(study);
	}
	
	/*-------------------------------------------------------------------------------------------------*/

	@Override
	protected List<String> authorizedUpdateFields() {
		// SGAS: Auto-generated method stub
		return null;
	}

	@Override
	protected List<String> defaultKeys() {
		// SGAS: Auto-generated method stub
		return null;
	}

	@Override
	public ExternalStudy create(ExternalStudy input, String currentUser)
			throws APIValidationException, APIException {
		// SGAS: Auto-generated method stub
		return null;
	}

	@Override
	public ExternalStudy update(ExternalStudy input, String currentUser)
			throws APIException, APIValidationException {
		// SGAS: Auto-generated method stub
		return null;
	}

	@Override
	public ExternalStudy update(ExternalStudy input, String currentUser,
			List<String> fields) throws APIException, APIValidationException {
		// SGAS: Auto-generated method stub
		return null;
	}

	/**
	 * Acces à une instance globale de ExternalStudyAPI, qui permet de remplacer les appels
	 * à MongoDBDAO qui est une globale, dans un contexte ou on ne peut pas injecter. 
	 * Exemple dans objets du domaine (sra.Experiment, ...)
	 * @return ExternalStudyAPI
	 */
	public static ExternalStudyAPI get() {
		return IGGlobals.instanceOf(ExternalStudyAPI.class);
	}
		
}
