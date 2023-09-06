package fr.cea.ig.ngl.dao.api.sra;


import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import org.mongojack.DBUpdate.Builder;

//import controllers.sra.experiments.api.ExperimentsUrlParamForm;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.GenericAPI;
import fr.cea.ig.ngl.dao.sra.ExperimentDAO;
import fr.cea.ig.play.IGBodyParsers;
import fr.cea.ig.play.IGGlobals;
import models.laboratory.common.instance.TraceInformation;
import models.sra.submit.sra.instance.Experiment;
import play.mvc.BodyParser;
import validation.ContextValidation;

public class ExperimentAPI extends GenericAPI<ExperimentDAO, Experiment> {
	
	private final List<String> DEFAULT_KEYS            = Arrays.asList(
			"projectCode", "title", "librarySelection","libraryStrategy", "librarySource", 
			"libraryLayout", "libraryLayoutNominalLength", "libraryLayoutOrientation", "libraryName", "libraryConstructionProtocol", 
			"typePlatform", "instrumentModel", "lastBaseCoord", "spotLength",
			"accession", "sampleCode", "studyCode", "sampleAccession", "studyAccession", "readSetCode", 
			"readSpecs", "run", "state", "adminComment", "traceInformation", "firstSubmissionDate");
	
    private final List<String> AUTHORIZED_UPDATE_FIELDS = null;

	@Inject
	public ExperimentAPI(ExperimentDAO dao) {
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
	public Experiment create(Experiment userExperiment, String currentUser, Boolean modeCopy) throws APIValidationException, APIException {	
		
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
			// en mode copy, le userExperiment._id est ignoré est mis à null pour le save.
			userExperiment._id = null;
		}
		if (userExperiment._id != null && !copy) {
			ctxVal.addError("userExperiment.id " + userExperiment._id, " presence de l'id incompatible  si copy a false ");
		}
		
		if (StringUtils.isBlank(userExperiment.code) && (copy)) { 
			ctxVal.addError("userExperiment sans code", " absence de code incompatible avec si copy a true");
		} 
		if (StringUtils.isNotBlank(userExperiment.code) && (!copy)) {		// autoriser code existant dans inputObject uniquement en mode copy :
			ctxVal.addError("userExperiment.code " + userExperiment.code, " presence du code incompatible  si copy a false  "); 
		}
		
		if (ctxVal.hasErrors()) { // inutile de faire le try catch 
			throw new APIValidationException(INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
		}
		try {
			// preparation donnée :
			if (!copy) { // En mode classic, le userExperiment est sans code et sans id, il faut generer traceInformation, code et mettre state
				         // mais ici on n'autorise pas sauvegarde des experiments, on doit passer par service de creation de la soumission.
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
		userExperiment.validate(ctxVal);
		if (ctxVal.hasErrors()) {
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
		Experiment  dbExperiment = dao.save(userExperiment);
		return dbExperiment;
	}
	

	@Override
	@Authenticated
	@Authorized.Write
	public Experiment create(Experiment input, String currentUser)
			throws APIValidationException, APIException {
		return create(input, currentUser, false);
	}

	public void validateParamsForUpdateOrThrow(Experiment userExperiment, String currentUser, Boolean modeCopy) throws APIValidationException {
		Boolean copy = false;		
		if(modeCopy != null && modeCopy==true) {
			copy = true;
		}
		ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
		// important de mettre le mode copy dans le context de validation car ce boolean peut etre utilisé pour la validation de l'objet
		ctxVal.putObject("copy", copy);

		if(userExperiment == null ) {
			ctxVal.addError("experiment ",  " valeur nulle  incompatible avec la methode update");
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
		if (StringUtils.isBlank(userExperiment.code)) {
			ctxVal.addError("experiment.code ",  " valeur nulle incompatible avec la methode ");
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}	
		Experiment dbExperiment = get(userExperiment.code);
		if (dbExperiment == null) {
			ctxVal.addError("experiment.code " + userExperiment.code , " n'existe pas dans la base et mode update");
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}

		// En mode copy ou non, l'id de l'objet qui provient d'une autre collection est mis à l'id de l'objet dans la collection cible :			
		userExperiment._id = dbExperiment._id;
		// en mode normal, on impose traceInformation, alors qu'en mode copy, on conserve le traceInformation de l'objet en entrée
		if (!copy) {
			userExperiment.traceInformation.forceModificationStamp(currentUser);
		}
		userExperiment.validate(ctxVal);
		if (ctxVal.hasErrors()) {
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
	}
	
	@Override
	@Authenticated
	@Authorized.Write
	@BodyParser.Of(value = IGBodyParsers.Json15MB.class)
	public Experiment update(Experiment userExperiment, String currentUser) throws APIException, APIValidationException {
		return update(userExperiment, currentUser, false);
	}
	
	@Authenticated
	@Authorized.Write
	@BodyParser.Of(value = IGBodyParsers.Json15MB.class)
	public Experiment update(Experiment userExperiment, String currentUser, Boolean modeCopy) throws APIException, APIValidationException {
		validateParamsForUpdateOrThrow(userExperiment, currentUser, modeCopy);
	    dao.update(userExperiment);
	    return  get(userExperiment.code);
	}

	

	@Override
	@Authenticated
	@Authorized.Write
	public Experiment update(Experiment userExperiment, String currentUser, List<String> fields)
		throws APIException, APIValidationException {
		validateParamsForUpdateOrThrow(userExperiment, currentUser, false);
	    ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
	    checkAuthorizedUpdateFields(ctxVal, fields);
	    checkIfFieldsAreDefined(ctxVal, fields, userExperiment);
		if (ctxVal.hasErrors()) {
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
		
		Experiment confInDb= dao.getObject(userExperiment.code);
		TraceInformation ti = confInDb.traceInformation;
		ti.modificationStamp(ctxVal, currentUser);
		dao.updateObject(DBQuery.and(DBQuery.is("code", userExperiment.code)), 
									dao.getBuilder(userExperiment, fields).set("traceInformation", ti));
		
	    return  get(userExperiment.code);
	}

	/*-------------------------------------------------------------------------------------------------*/

	public List<Experiment> dao_findAsList(Query q) {
		return dao.findAsList(q);
	}
	
	public Iterable<Experiment> dao_all() {
		return dao.all();
	}
	
	public Iterable<Experiment> dao_all_batch(int cp) {
		return dao.all_batch(cp);
	}
	
//	public Experiment dao_getObject(String experimentCode) {
//		return dao.getObject(experimentCode);
//	}
	
	public Experiment dao_findOne(Query q) {
		return dao.findOne(q);		
	}

	public void dao_update(Query query, Builder set) {
		dao.update(query, set);
	}

	public boolean dao_checkObjectExist(String key, String keyValue) {
		return dao.checkObjectExist(key, keyValue);
	}

	public Experiment dao_saveObject(Experiment experiment) {
		return dao.saveObject(experiment);
	}

	
	/**
	 * Acces à une instance globale de ExperimentAPI, qui permet de remplacer les appels
	 * à MongoDBDAO qui est une globale, dans un contexte ou on ne peut pas injecter. 
	 * Exemple dans objets du domaine (sra.Experiment, ...)
	 * @return ExperimentAPI
	 */
	public static ExperimentAPI get() {
		return IGGlobals.instanceOf(ExperimentAPI.class);
	}

	public  MongoDBResult<Experiment> dao_find(Query q) {
		return dao.dao_find(q);		
	}
	
}
