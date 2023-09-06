package fr.cea.ig.ngl.dao.api.sra;

import static ngl.refactoring.state.SRASubmissionStateNames.NONE;

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
import fr.cea.ig.ngl.dao.sra.StudyDAO;
import fr.cea.ig.play.IGGlobals;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.sra.submit.sra.instance.Study;
import models.sra.submit.util.EbiAPI;
import models.sra.submit.util.SraCodeHelper;
import validation.ContextValidation;

public class StudyAPI extends GenericAPI<StudyDAO, Study> {
	private final List<String>        DEFAULT_KEYS             = Arrays.asList("accession", "externalId", "state", "_type"); 			   															 
	private final List<String>        AUTHORIZED_UPDATE_FIELDS = null;
	private final SraCodeHelper       sraCodeHelper;

	@Inject
	public StudyAPI(StudyDAO dao,
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
	public Study create(Study userStudy, String currentUser, Boolean modeCopy) throws APIValidationException, APIException {	
		
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
			// en mode copy, le userStudy._id est ignoré est mis à null pour le save.
			userStudy._id = null;
		}
		if (userStudy._id != null && !copy) {
			ctxVal.addError("userStudy.id " + userStudy._id, " presence de l'id incompatible  si mode copy a false ");
		}
		
		if (StringUtils.isBlank(userStudy.code) && (copy)) { 
			ctxVal.addError("userStudy sans code", " absence de code incompatible si mode copy a true");
		} 
		if (StringUtils.isNotBlank(userStudy.code) && (!copy)) {		// autoriser code existant dans inputObject uniquement en mode copy :
			ctxVal.addError("userStudy.code " + userStudy.code, " presence du code incompatible  si mode copy a false  "); 
		}
		if (ctxVal.hasErrors()) { // inutile de faire le try catch 
			throw new APIValidationException(INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
		}
		try {
			// preparation donnée :
			if (!copy) { 
				//code aléatoire qui ne doit pas exister en base :
				userStudy.code = sraCodeHelper.generateStudyCode();
				while (dao.checkObjectExist("code", userStudy.code)) {
					userStudy.code = sraCodeHelper.generateStudyCode();
				}
				userStudy.state = new State(NONE, currentUser);
				userStudy.traceInformation = new TraceInformation(currentUser);
			} else {
				// en mode copy, rien : le study a un code mais pas d'id, et pas de traceIformation ou state ou code à generer puisqu'on 
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
		userStudy.validate(ctxVal);
		if (ctxVal.hasErrors()) {
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
		Study  dbStudy = dao.save(userStudy);
		return dbStudy;
	}
	

	@Override
	@Authenticated
	@Authorized.Write
	public Study create(Study input, String currentUser)
			throws APIValidationException, APIException {
		return create(input, currentUser, false);
	}

	public void validateParamsForUpdateOrThrow(Study userStudy, String currentUser, Boolean modeCopy) throws APIValidationException {
		Boolean copy = false;		
		if(modeCopy != null && modeCopy==true) {
			copy = true;
		}
		ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
		// important de mettre le mode copy dans le context de validation car ce boolean peut etre utilisé pour la validation de l'objet
		ctxVal.putObject("copy", copy);

		if(userStudy == null ) {
			ctxVal.addError("study ",  " valeur nulle  incompatible avec la methode update");
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
		if (StringUtils.isBlank(userStudy.code)) {
			ctxVal.addError("study.code ",  " valeur nulle incompatible avec la methode ");
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}	
		Study dbStudy = get(userStudy.code);
		if (dbStudy == null) {
			ctxVal.addError("study.code " + userStudy.code , " n'existe pas dans la base et mode update");
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}

		// En mode copy ou non, l'id de l'objet qui provient d'une autre collection est mis à l'id de l'objet dans la collection cible :			
		userStudy._id = dbStudy._id;
		// en mode normal, on impose traceInformation, alors qu'en mode copy, on conserve le traceInformation de l'objet en entrée
		if (!copy) {
			userStudy.traceInformation.forceModificationStamp(currentUser);
		}
		userStudy.validate(ctxVal);
		if (ctxVal.hasErrors()) {
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
	}
	
	@Override
	@Authenticated
	@Authorized.Write
	public Study update(Study userStudy, String currentUser) throws APIException, APIValidationException {
		return update(userStudy, currentUser, false);
	}
	
	
	@Authenticated
	@Authorized.Write
	public Study update(Study userStudy, String currentUser, Boolean modeCopy) throws APIException, APIValidationException {
		validateParamsForUpdateOrThrow(userStudy, currentUser, modeCopy);
	    dao.update(userStudy);
	    return  get(userStudy.code);
	}

	
	@Override
	@Authenticated
	@Authorized.Write
	public Study update(Study userStudy, String currentUser, List<String> fields)
		throws APIException, APIValidationException {
		validateParamsForUpdateOrThrow(userStudy, currentUser, false);
	    ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
	    checkAuthorizedUpdateFields(ctxVal, fields);
	    checkIfFieldsAreDefined(ctxVal, fields, userStudy);
		if (ctxVal.hasErrors()) {
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
		Study StudyInDb= dao.getObject(userStudy.code);
		TraceInformation ti = StudyInDb.traceInformation;
		ti.modificationStamp(ctxVal, currentUser);
		dao.updateObject(DBQuery.and(DBQuery.is("code", userStudy.code)), 
									dao.getBuilder(userStudy, fields).set("traceInformation", ti));
		return  get(userStudy.code);
	}	
		



	/*-------------------------------------------------------------------------------------------------*/

	public Iterable<Study> dao_all() {
		return dao.all();
	}
		
	public boolean dao_checkObjectExist(String key, String keyValue) {
		return dao.checkObjectExist(key, keyValue);
	}

	public Study dao_findOne(Query q) {
		return dao.findOne(q);		
	}

	public Study dao_getObject(String sampleCode) {
		return dao.getObject(sampleCode);
	}

	public Study dao_saveObject(Study sampleElt) {
		return dao.saveObject(sampleElt);
	}

	public void dao_update(Query query, Builder set) {
		dao.update(query, set);
	}
	/**
	 * Acces à une instance globale de StudyAPI, qui permet de remplacer les appels
	 * à MongoDBDAO qui est une globale, dans un contexte ou on ne peut pas injecter. 
	 * Exemple dans objets du domaine (sra.Experiment, ...)
	 * @return StudyAPI
	 */
	public static StudyAPI get() {
		return IGGlobals.instanceOf(StudyAPI.class);
	}

	public  MongoDBResult<Study> dao_find(Query q) {
		return dao.dao_find(q);		
	}
	
}
