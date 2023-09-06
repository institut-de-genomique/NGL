package fr.cea.ig.ngl.dao.api.sra;


import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery.Query;
import org.mongojack.DBUpdate.Builder;

//import controllers.sra.readsets.api.ReadsetsUrlParamForm;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.GenericAPI;
import fr.cea.ig.ngl.dao.sra.ReadsetDAO;
import fr.cea.ig.play.IGBodyParsers;
import fr.cea.ig.play.IGGlobals;
import models.sra.submit.sra.instance.Readset;
import play.mvc.BodyParser;
import validation.ContextValidation;

public class ReadsetAPI extends GenericAPI<ReadsetDAO, Readset> {
	
	private final List<String> DEFAULT_KEYS            = Arrays.asList(
			"projectCode", "title", "librarySelection","libraryStrategy", "librarySource", 
			"libraryLayout", "libraryLayoutNominalLength", "libraryLayoutOrientation", "libraryName", "libraryConstructionProtocol", 
			"typePlatform", "instrumentModel", "lastBaseCoord", "spotLength",
			"accession", "sampleCode", "studyCode", "sampleAccession", "studyAccession", "readSetCode", 
			"readSpecs", "run", "state", "adminComment", "traceInformation", "firstSubmissionDate");
	
    private final List<String> AUTHORIZED_UPDATE_FIELDS = null;

	@Inject
	public ReadsetAPI(ReadsetDAO dao) {
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
	public Readset create(Readset userReadset, String currentUser, Boolean modeCopy) throws APIValidationException, APIException {	
		
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
			// en mode copy, le userReadset._id est ignoré est mis à null pour le save.
			userReadset._id = null;
		}
		if (userReadset._id != null && !copy) {
			ctxVal.addError("userReadset.id " + userReadset._id, " presence de l'id incompatible  si copy a false ");
		}
		
		if (StringUtils.isBlank(userReadset.code) && (copy)) { 
			ctxVal.addError("userReadset sans code", " absence de code incompatible avec si copy a true");
		} 
		if (StringUtils.isNotBlank(userReadset.code) && (!copy)) {		// autoriser code existant dans inputObject uniquement en mode copy :
			ctxVal.addError("userReadset.code " + userReadset.code, " presence du code incompatible  si copy a false  "); 
		}
		
		if (ctxVal.hasErrors()) { // inutile de faire le try catch 
			throw new APIValidationException(INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
		}
		try {
			// preparation donnée :
			if (!copy) { // En mode classic, le userReadset est sans code et sans id, il faut generer traceInformation, code et mettre state
				         // mais ici on n'autorise pas sauvegarde des readsets, on doit passer par service de creation de la soumission.
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
		userReadset.validate(ctxVal);
		if (ctxVal.hasErrors()) {
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
		Readset  dbReadset = dao.save(userReadset);
		return dbReadset;
	}
	

	@Override
	@Authenticated
	@Authorized.Write
	public Readset create(Readset input, String currentUser)
			throws APIValidationException, APIException {
		return create(input, currentUser, false);
	}

	public void validateParamsForUpdateOrThrow(Readset userReadset, String currentUser, Boolean modeCopy) throws APIValidationException {
		Boolean copy = false;		
		if(modeCopy != null && modeCopy==true) {
			copy = true;
		}
		ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
		// important de mettre le mode copy dans le context de validation car ce boolean peut etre utilisé pour la validation de l'objet
		ctxVal.putObject("copy", copy);

		if(userReadset == null ) {
			ctxVal.addError("readset ",  " valeur nulle  incompatible avec la methode update");
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
		if (StringUtils.isBlank(userReadset.code)) {
			ctxVal.addError("readset.code ",  " valeur nulle incompatible avec la methode ");
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}	
		Readset dbReadset = get(userReadset.code);
		if (dbReadset == null) {
			ctxVal.addError("readset.code " + userReadset.code , " n'existe pas dans la base et mode update");
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}

		// En mode copy ou non, l'id de l'objet qui provient d'une autre collection est mis à l'id de l'objet dans la collection cible :			
		userReadset._id = dbReadset._id;
		// en mode normal, on impose traceInformation, alors qu'en mode copy, on conserve le traceInformation de l'objet en entrée
		if (!copy) {
			//userReadset.traceInformation.forceModificationStamp(currentUser);
		}
		userReadset.validate(ctxVal);
		if (ctxVal.hasErrors()) {
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
	}
	
	@Override
	@Authenticated
	@Authorized.Write
	@BodyParser.Of(value = IGBodyParsers.Json15MB.class)
	public Readset update(Readset userReadset, String currentUser) throws APIException, APIValidationException {
		return update(userReadset, currentUser, false);
	}
	
	@Authenticated
	@Authorized.Write
	@BodyParser.Of(value = IGBodyParsers.Json15MB.class)
	public Readset update(Readset userReadset, String currentUser, Boolean modeCopy) throws APIException, APIValidationException {
		validateParamsForUpdateOrThrow(userReadset, currentUser, modeCopy);
	    dao.update(userReadset);
	    return  get(userReadset.code);
	}

	
	@Override
	@Authenticated
	@Authorized.Write
	@BodyParser.Of(value = IGBodyParsers.Json15MB.class)
	public Readset update(Readset userReadset, String currentUser, List<String> fields)
			throws APIException, APIValidationException {
		return update(userReadset, currentUser, fields, false);
	}
	
	// pas utilisé dans le code de NGL-SUB
	@Authenticated
	@Authorized.Write
	@BodyParser.Of(value = IGBodyParsers.Json15MB.class)
	public Readset update(Readset userReadset, String currentUser, List<String> fields, Boolean modeCopy)
		throws APIException, APIValidationException {
		validateParamsForUpdateOrThrow(userReadset, currentUser, modeCopy);
	    ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
	    checkAuthorizedUpdateFields(ctxVal, fields);
	    checkIfFieldsAreDefined(ctxVal, fields, userReadset);
		if (ctxVal.hasErrors()) {
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
	    dao.update(userReadset);
	    return  get(userReadset.code);
	}
	

	/*-------------------------------------------------------------------------------------------------*/

	public List<Readset> dao_findAsList(Query q) {
		return dao.findAsList(q);
	}
	
	public Iterable<Readset> dao_all() {
		return dao.all();
	}
	
	public Iterable<Readset> dao_all_batch(int cp) {
		return dao.all_batch(cp);
	}
	
//	public Readset dao_getObject(String readsetCode) {
//		return dao.getObject(readsetCode);
//	}
	
	public Readset dao_findOne(Query q) {
		return dao.findOne(q);		
	}

	public void dao_update(Query query, Builder set) {
		dao.update(query, set);
	}

	public boolean dao_checkObjectExist(String key, String keyValue) {
		return dao.checkObjectExist(key, keyValue);
	}

	public Readset dao_saveObject(Readset readset) {
		return dao.saveObject(readset);
	}

	
	/**
	 * Acces à une instance globale de ReadsetAPI, qui permet de remplacer les appels
	 * à MongoDBDAO qui est une globale, dans un contexte ou on ne peut pas injecter. 
	 * Exemple dans objets du domaine (sra.Readset, ...)
	 * @return ReadsetAPI
	 */
	public static ReadsetAPI get() {
		return IGGlobals.instanceOf(ReadsetAPI.class);
	}

	public  MongoDBResult<Readset> dao_find(Query q) {
		return dao.dao_find(q);		
	}
	
}
