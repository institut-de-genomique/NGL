package fr.cea.ig.ngl.dao.api.sra;

import static ngl.refactoring.state.SRASubmissionStateNames.SUB_F;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import org.mongojack.DBUpdate.Builder;

import fr.cea.ig.MongoDBResult;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.GenericAPI;
import fr.cea.ig.ngl.dao.sra.ExternalStudyDAO;
import fr.cea.ig.play.IGGlobals;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.sra.submit.sra.instance.ExternalStudy;
import models.sra.submit.util.EbiAPI;
import models.sra.submit.util.SraCodeHelper;
import validation.ContextValidation;

public class ExternalStudyAPI extends GenericAPI<ExternalStudyDAO, ExternalStudy> {
	private final List<String>        DEFAULT_KEYS             = Arrays.asList("accession", "externalId", "state", "_type"); 			   															 
	private final List<String>        AUTHORIZED_UPDATE_FIELDS = null;
	private final SraCodeHelper       sraCodeHelper;

	@Inject
	public ExternalStudyAPI(ExternalStudyDAO dao,
							 SraCodeHelper     sraCodeHelper,
							 EbiAPI            ebiAPI) {
		super(dao);
		this.sraCodeHelper = sraCodeHelper;
	}

	@Override
	protected List<String> authorizedUpdateFields() {
		return AUTHORIZED_UPDATE_FIELDS;
	}

	@Override
	protected List<String> defaultKeys() {
		return DEFAULT_KEYS;
	}
	
	
	@Authenticated
	@Authorized.Write
	public ExternalStudy create(ExternalStudy userExternalStudy, String currentUser, Boolean modeCopy) throws APIValidationException, APIException {	
		
		Boolean copy = false;
		
		if(modeCopy != null && modeCopy==true) {
			copy = true;
		}
		
	    ContextValidation ctxVal = ContextValidation.createCreationContext(currentUser);
		// important de mettre le mode copy dans le context de validation car ce boolean est utilisé pour la validation de l'objet
		ctxVal.putObject("copy", copy);

		// code difficilement  deportable dans validate car dans validate necessité d'avoir le code avant de sauver quel que soit le mode (copy a true ou false) 
		// alors qu'ici on veut tester avant generation eventuelle du code . => sinon doublons des tests 
		if(copy) {
			// en mode copy, le userExternalStudy._id est ignoré est mis à null pour le save.
			userExternalStudy._id = null;
		}
		if (userExternalStudy._id != null && !copy) {
			ctxVal.addError("userExternalStudy.id " + userExternalStudy._id, " presence de l'id incompatible  si mode copy a false ");
		}
		
		if (StringUtils.isBlank(userExternalStudy.code) && (copy)) { 
			ctxVal.addError("userExternalStudy sans code", " absence de code incompatible si mode copy a true");
		} 
		if (StringUtils.isNotBlank(userExternalStudy.code) && (!copy)) {		// autoriser code existant dans inputObject uniquement en mode copy :
			ctxVal.addError("userExternalStudy.code " + userExternalStudy.code, " presence du code incompatible  si mode copy a false  "); 
		}
		if (StringUtils.isBlank(userExternalStudy.accession)) { 
			ctxVal.addError("userExternalStudy.accession " , " absence de numeros d'accession incompatible avec mode creation pour externalStudy "); 
		}
		if (ctxVal.hasErrors()) { // inutile de faire le try catch 
			throw new APIValidationException(INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
		}
		try {
			// preparation donnée :
			if (!copy) { 
				// ici code logique et non aléatoire, donc si existe dans base declenchera erreur lors de la validation
				userExternalStudy.code = sraCodeHelper.generateExternalStudyCode(userExternalStudy.accession);
				userExternalStudy.state = new State(SUB_F, currentUser);
				// Pas de traceInformation dans ExternalStudy
			} else {
				// en mode copy, rien : le externalStudy a un code mais pas d'id, et pas de traceIformation ou state ou code à generer puisqu'on 
				// conserve les champs d'origine
			}
		} catch(RuntimeException e) {
			ctxVal.addError("RuntimeException", e.getMessage());
		} catch(Exception e) {
			ctxVal.addError("Exception", e.getMessage());
		}
		if (ctxVal.hasErrors()) {
			throw new APIValidationException("Exception", ctxVal.getErrors());
		}
		// validation et sauvegarde dans base
		userExternalStudy.validate(ctxVal);
		if (ctxVal.hasErrors()) {
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
		ExternalStudy  dbExternalStudy = dao.save(userExternalStudy);
		return dbExternalStudy;
	}
	

	@Override
	@Authenticated
	@Authorized.Write
	public ExternalStudy create(ExternalStudy input, String currentUser)
			throws APIValidationException, APIException {
		return create(input, currentUser, false);
	}

	public void validateParamsForUpdateOrThrow(ExternalStudy userExternalStudy, String currentUser, Boolean modeCopy) throws APIValidationException {
		Boolean copy = false;		
		if(modeCopy != null && modeCopy==true) {
			copy = true;
		}
		ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
		// important de mettre le mode copy dans le context de validation car ce boolean peut etre utilisé pour la validation de l'objet
		ctxVal.putObject("copy", copy);

		if(userExternalStudy == null ) {
			ctxVal.addError("externalStudy ",  " valeur nulle  incompatible avec la methode update");
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
		if (StringUtils.isBlank(userExternalStudy.code)) {
			ctxVal.addError("externalStudy.code ",  " valeur nulle incompatible avec la methode ");
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}	
		ExternalStudy dbExternalStudy = get(userExternalStudy.code);
		if (dbExternalStudy == null) {
			ctxVal.addError("externalStudy.code " + userExternalStudy.code , " n'existe pas dans la base et mode update");
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}

		// En mode copy ou non, l'id de l'objet qui provient d'une autre collection est mis à l'id de l'objet dans la collection cible :			
		userExternalStudy._id = dbExternalStudy._id;
		// en mode normal, on impose traceInformation, alors qu'en mode copy, on conserve le traceInformation de l'objet en entrée
		if (!copy) {
			userExternalStudy.traceInformation.forceModificationStamp(currentUser);
		}
		userExternalStudy.validate(ctxVal);
		if (ctxVal.hasErrors()) {
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
	}
	
	@Override
	@Authenticated
	@Authorized.Write
	public ExternalStudy update(ExternalStudy userExternalStudy, String currentUser) throws APIException, APIValidationException {
		return update(userExternalStudy, currentUser, false);
	}
	
	
	@Authenticated
	@Authorized.Write
	public ExternalStudy update(ExternalStudy userExternalStudy, String currentUser, Boolean modeCopy) throws APIException, APIValidationException {
		validateParamsForUpdateOrThrow(userExternalStudy, currentUser, modeCopy);
	    dao.update(userExternalStudy);
	    return  get(userExternalStudy.code);
	}

	
	@Override
	@Authenticated
	@Authorized.Write
	public ExternalStudy update(ExternalStudy userExternalStudy, String currentUser, List<String> fields)
		throws APIException, APIValidationException {
		validateParamsForUpdateOrThrow(userExternalStudy, currentUser, false);
	    ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
	    checkAuthorizedUpdateFields(ctxVal, fields);
	    checkIfFieldsAreDefined(ctxVal, fields, userExternalStudy);
		if (ctxVal.hasErrors()) {
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
		ExternalStudy StudyInDb= dao.getObject(userExternalStudy.code);
		TraceInformation ti = StudyInDb.traceInformation;
		ti.modificationStamp(ctxVal, currentUser);
		dao.updateObject(DBQuery.and(DBQuery.is("code", userExternalStudy.code)), 
									dao.getBuilder(userExternalStudy, fields).set("traceInformation", ti));
		return  get(userExternalStudy.code);
	}	
	



	/*-------------------------------------------------------------------------------------------------*/

	public Iterable<ExternalStudy> dao_all() {
		return dao.all();
	}
		
	public boolean dao_checkObjectExist(String key, String keyValue) {
		return dao.checkObjectExist(key, keyValue);
	}

	public ExternalStudy dao_findOne(Query q) {
		return dao.findOne(q);		
	}

	public ExternalStudy dao_getObject(String sampleCode) {
		return dao.getObject(sampleCode);
	}

	public ExternalStudy dao_saveObject(ExternalStudy sampleElt) {
		return dao.saveObject(sampleElt);
	}

	public void dao_update(Query query, Builder set) {
		dao.update(query, set);
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

	public  MongoDBResult<ExternalStudy> dao_find(Query q) {
		return dao.dao_find(q);		
	}
	
}
