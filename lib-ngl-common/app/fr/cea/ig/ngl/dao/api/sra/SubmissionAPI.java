package fr.cea.ig.ngl.dao.api.sra;


import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import org.mongojack.DBUpdate.Builder;

//import controllers.sra.submissions.api.SubmissionsUrlParamForm;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.GenericAPI;
import fr.cea.ig.ngl.dao.sra.SubmissionDAO;
import fr.cea.ig.play.IGBodyParsers;
import fr.cea.ig.play.IGGlobals;
import models.laboratory.common.instance.TraceInformation;
import models.sra.submit.sra.instance.Submission;
import play.mvc.BodyParser;
import validation.ContextValidation;

public class SubmissionAPI extends GenericAPI<SubmissionDAO, Submission> {
	
	private final List<String> DEFAULT_KEYS            = Arrays.asList(
			"projectCode", "accession", "submissionDate","refStudyCodes", "refSampleCodes", 
			"studyCode", "analysisCode", "umbrellaCode", 
			"sampleCodes", "experimentCodes", "runCodes", "configCode",
			"submissionDirectory", "type", "xmlStudys", "xmlProjects", "xmlSamples", "xmlExperiments", 
			"xmlRuns", "xmlSubmission", "xmlAnalysis", "xmlUmbrella", "ebiResult", "mapUserRefCollab",
			"traceInformation", "state", "typeRawDataSubmitted");
	
    private final List<String> AUTHORIZED_UPDATE_FIELDS = null;

	@Inject
	public SubmissionAPI(SubmissionDAO dao) {
		super(dao);
	}
	
	@Override
	protected List<String> authorizedUpdateFields() {
		return this.AUTHORIZED_UPDATE_FIELDS;
	}

	@Override
	protected List<String> defaultKeys() {
		return this.DEFAULT_KEYS;
	}
	


	@Authenticated
	@Authorized.Write
	public Submission create(Submission userSubmission, String currentUser, Boolean modeCopy) throws APIValidationException, APIException {	
		
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
			// en mode copy, le userSubmission._id est ignoré est mis à null pour le save.
			userSubmission._id = null;
		}
		if (userSubmission._id != null && !copy) {
			ctxVal.addError("userSubmission.id " + userSubmission._id, " presence de l'id incompatible  si copy a false ");
		}
		
		if (StringUtils.isBlank(userSubmission.code) && (copy)) { 
			ctxVal.addError("userSubmission sans code", " absence de code incompatible avec si copy a true");
		} 
		if (StringUtils.isNotBlank(userSubmission.code) && (!copy)) {		// autoriser code existant dans inputObject uniquement en mode copy :
			ctxVal.addError("userSubmission.code " + userSubmission.code, " presence du code incompatible  si copy a false  "); 
		}
		
		if (ctxVal.hasErrors()) { // inutile de faire le try catch 
			throw new APIValidationException(INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
		}
		try {
			// preparation donnée :
			if (!copy) { // En mode classic, le userSubmission est sans code et sans id, il faut generer traceInformation, code et mettre state
				         // mais ici on n'autorise pas sauvegarde des submissions, on doit passer par service de creation de la soumission.
				ctxVal.addError("Exception", "Methode non implemente pour le mode classique (!= mode copy), il faut passer par le service de creation de la soumission");
			} else {
				// en mode copy, rien : le project a un code mais pas d'id, et pas de traceIformation ou state ou code à generer puisqu'on 
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
		userSubmission.validate(ctxVal);
		if (ctxVal.hasErrors()) {
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
		Submission  dbSubmission = dao.save(userSubmission);
		return dbSubmission;
	}
	

	@Override
	@Authenticated
	@Authorized.Write
	public Submission create(Submission input, String currentUser)
			throws APIValidationException, APIException {
		return create(input, currentUser, false);
	}

	public void validateParamsForUpdateOrThrow(Submission userSubmission, String currentUser, Boolean modeCopy) throws APIValidationException {
		Boolean copy = false;		
		if(modeCopy != null && modeCopy==true) {
			copy = true;
		}
		ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
		// important de mettre le mode copy dans le context de validation car ce boolean peut etre utilisé pour la validation de l'objet
		ctxVal.putObject("copy", copy);

		if(userSubmission == null ) {
			ctxVal.addError("submission ",  " valeur nulle  incompatible avec la methode update");
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
		if (StringUtils.isBlank(userSubmission.code)) {
			ctxVal.addError("submission.code ",  " valeur nulle incompatible avec la methode ");
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}	
		Submission dbSubmission = get(userSubmission.code);
		if (dbSubmission == null) {
			ctxVal.addError("submission.code " + userSubmission.code , " n'existe pas dans la base et mode update");
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}

		// En mode copy ou non, l'id de l'objet qui provient d'une autre collection est mis à l'id de l'objet dans la collection cible :			
		userSubmission._id = dbSubmission._id;
		// en mode normal, on impose traceInformation, alors qu'en mode copy, on conserve le traceInformation de l'objet en entrée
		if (!copy) {
			userSubmission.traceInformation.forceModificationStamp(currentUser);
		}
		userSubmission.validate(ctxVal);
		if (ctxVal.hasErrors()) {
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
	}
	
	@Override
	@Authenticated
	@Authorized.Write
	@BodyParser.Of(value = IGBodyParsers.Json15MB.class)
	public Submission update(Submission userSubmission, String currentUser) throws APIException, APIValidationException {
		return update(userSubmission, currentUser, false);
	}
	
	@Authenticated
	@Authorized.Write
	@BodyParser.Of(value = IGBodyParsers.Json15MB.class)
	public Submission update(Submission userSubmission, String currentUser, Boolean modeCopy) throws APIException, APIValidationException {
		validateParamsForUpdateOrThrow(userSubmission, currentUser, modeCopy);
	    dao.update(userSubmission);
	    return  get(userSubmission.code);
	}


	@Override
	@Authenticated
	@Authorized.Write
	public Submission update(Submission userSubmission, String currentUser, List<String> fields)
		throws APIException, APIValidationException {
		validateParamsForUpdateOrThrow(userSubmission, currentUser, false);
	    ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
	    checkAuthorizedUpdateFields(ctxVal, fields);
	    checkIfFieldsAreDefined(ctxVal, fields, userSubmission);
		if (ctxVal.hasErrors()) {
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
		
		Submission confInDb= dao.getObject(userSubmission.code);
		TraceInformation ti = confInDb.traceInformation;
		ti.modificationStamp(ctxVal, currentUser);
		dao.updateObject(DBQuery.and(DBQuery.is("code", userSubmission.code)), 
									dao.getBuilder(userSubmission, fields).set("traceInformation", ti));
		
	    return  get(userSubmission.code);
	}
	
	

	/*-------------------------------------------------------------------------------------------------*/

	public List<Submission> dao_findAsList(Query q) {
		return dao.findAsList(q);
	}
	
	public Iterable<Submission> dao_all() {
		return dao.all();
	}
	
	public Iterable<Submission> dao_all_batch(int cp) {
		return dao.all_batch(cp);
	}
	
	public Submission dao_getObject(String submissionCode) {
		return dao.getObject(submissionCode);
	}
	
	public Submission dao_findOne(Query q) {
		return dao.findOne(q);		
	}

	public void dao_update(Query query, Builder set) {
		dao.update(query, set);
	}

	public boolean dao_checkObjectExist(String key, String keyValue) {
		return dao.checkObjectExist(key, keyValue);
	}

	public Submission dao_saveObject(Submission submission) {
		return dao.saveObject(submission);
	}

	
	/**
	 * Acces à une instance globale de SubmissionAPI, qui permet de remplacer les appels
	 * à MongoDBDAO qui est une globale, dans un contexte ou on ne peut pas injecter. 
	 * Exemple dans objets du domaine (sra.Submission, ...)
	 * @return SubmissionAPI
	 */
	public static SubmissionAPI get() {
		return IGGlobals.instanceOf(SubmissionAPI.class);
	}

	public  MongoDBResult<Submission> dao_find(Query q) {
		return dao.dao_find(q);		
	}
	
}
