package controllers.sra.api;

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
import fr.cea.ig.ngl.dao.sra.AtlaseaDAO;
import fr.cea.ig.play.IGBodyParsers;
import fr.cea.ig.play.IGGlobals;
import models.laboratory.common.instance.TraceInformation;
import models.sra.submit.sra.instance.Atlasea;
import play.mvc.BodyParser;
import validation.ContextValidation;

public class AtlaseaAPI extends GenericAPI<AtlaseaDAO, Atlasea> {
	
	private final List<String> DEFAULT_KEYS            = Arrays.asList();
	
    private final List<String> AUTHORIZED_UPDATE_FIELDS = null;
	private static final play.Logger.ALogger  logger = play.Logger.of(AtlaseaAPI.class);

	@Inject
	public AtlaseaAPI(AtlaseaDAO dao) {
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
	public Atlasea create(Atlasea userAtlasea, String currentUser, Boolean modeCopy) throws APIValidationException, APIException {	
		
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
			// en mode copy, le userAtlasea._id est ignoré est mis à null pour le save.
			userAtlasea._id = null;
		}
		if (userAtlasea._id != null && !copy) {
			ctxVal.addError("userAtlasea.id " + userAtlasea._id, " presence de l'id incompatible  si copy a false ");
		}
		
		if (StringUtils.isBlank(userAtlasea.code) && (copy)) { 
			ctxVal.addError("userAtlasea sans code", " absence de code incompatible avec si copy a true");
		} 
		if (StringUtils.isNotBlank(userAtlasea.code) && (!copy)) {		// autoriser code existant dans inputObject uniquement en mode copy :
			ctxVal.addError("userAtlasea.code " + userAtlasea.code, " presence du code incompatible  si copy a false  "); 
		}
		
		if (ctxVal.hasErrors()) { // inutile de faire le try catch 
			throw new APIValidationException(INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
		}
		try {
			// preparation donnée :
			if (!copy) { // En mode classic, le userAtlasea est sans code et sans id, il faut generer traceInformation, code et mettre state
				         // mais ici on n'autorise pas sauvegarde des atlaseas, on doit passer par service de creation de la soumission.
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
		//userAtlasea.validate(ctxVal);
		if (ctxVal.hasErrors()) {
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
		Atlasea  dbAtlasea = dao.save(userAtlasea);
		return dbAtlasea;
	}
	

	@Override
	@Authenticated
	@Authorized.Write
	public Atlasea create(Atlasea input, String currentUser)
			throws APIValidationException, APIException {
		return create(input, currentUser, false);
	}

	public void validateParamsForUpdateOrThrow(Atlasea userAtlasea, String currentUser, Boolean modeCopy) throws APIValidationException {
		Boolean copy = false;		
		if(modeCopy != null && modeCopy==true) {
			copy = true;
		}
		//logger.debug("iiiiiiiiiiiiiiiiiiiiiiii Entree dans validateParamsForUpdateOrThrow  mode copyFalse pour " + userAtlasea.code);
		ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
		// important de mettre le mode copy dans le context de validation car ce boolean peut etre utilisé pour la validation de l'objet
		ctxVal.putObject("copy", copy);

		if(userAtlasea == null ) {
			ctxVal.addError("atlasea ",  " valeur nulle  incompatible avec la methode update");
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
		if (StringUtils.isBlank(userAtlasea.code)) {
			ctxVal.addError("atlasea.code ",  " valeur nulle incompatible avec la methode ");
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}	
		Atlasea dbAtlasea = get(userAtlasea.code);
		if (dbAtlasea == null) {
			ctxVal.addError("atlasea.code " + userAtlasea.code , " n'existe pas dans la base et mode update");
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}

		// En mode copy ou non, l'id de l'objet qui provient d'une autre collection est mis à l'id de l'objet dans la collection cible :			
		userAtlasea._id = dbAtlasea._id;
		// en mode normal, on impose traceInformation, alors qu'en mode copy, on conserve le traceInformation de l'objet en entrée
		if (!copy) {
			userAtlasea.traceInformation.forceModificationStamp(currentUser);
		}
		
		//sgas a remettre
		//userAtlasea.validate(ctxVal);
		
		// todo sgas a retirer test :
		ctxVal.addError("TEST_ERROR", "TEST_ERROR");

		if (ctxVal.hasErrors()) {
			logger.debug("Cas d'erreur avec APIValidationExeption");
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
	}
	
	@Override
	@Authenticated
	@Authorized.Write
	@BodyParser.Of(value = IGBodyParsers.Json15MB.class)
	public Atlasea update(Atlasea userAtlasea, String currentUser) throws APIException, APIValidationException {
		return update(userAtlasea, currentUser, false);
	}
	
	@Authenticated
	@Authorized.Write
	@BodyParser.Of(value = IGBodyParsers.Json15MB.class)
	public Atlasea update(Atlasea userAtlasea, String currentUser, Boolean modeCopy) throws APIException, APIValidationException {
		//logger.debug("iiiiiiiiiiiiiiiiiiiiiiii   Entree dans AtlaseaAPI.update avec booleanCopy");
		validateParamsForUpdateOrThrow(userAtlasea, currentUser, modeCopy);
	    dao.update(userAtlasea);
	    return  get(userAtlasea.code);
	}

	

	@Override
	@Authenticated
	@Authorized.Write
	public Atlasea update(Atlasea userAtlasea, String currentUser, List<String> fields)
		throws APIException, APIValidationException {
		logger.debug("Dans AtlaseaAPI.update avec fields");
		validateParamsForUpdateOrThrow(userAtlasea, currentUser, false);
	    ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
	    checkAuthorizedUpdateFields(ctxVal, fields);
	    checkIfFieldsAreDefined(ctxVal, fields, userAtlasea);
		if (ctxVal.hasErrors()) {
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
		
		Atlasea confInDb= dao.getObject(userAtlasea.code);
		TraceInformation ti = confInDb.traceInformation;
		ti.modificationStamp(ctxVal, currentUser);
		dao.updateObject(DBQuery.and(DBQuery.is("code", userAtlasea.code)), 
									dao.getBuilder(userAtlasea, fields).set("traceInformation", ti));
		
	    return  get(userAtlasea.code);
	}

	/*-------------------------------------------------------------------------------------------------*/

	public List<Atlasea> dao_findAsList(Query q) {
		return dao.findAsList(q);
	}
	
	public Iterable<Atlasea> dao_all() {
		return dao.all();
	}
	
	public Iterable<Atlasea> dao_all_batch(int cp) {
		return dao.all_batch(cp);
	}
	
//	public Atlasea dao_getObject(String AtlaseaCode) {
//		return dao.getObject(atlaseaCode);
//	}
	
	public Atlasea dao_findOne(Query q) {
		return dao.findOne(q);		
	}

	public void dao_update(Query query, Builder set) {
		dao.update(query, set);
	}

	public boolean dao_checkObjectExist(String key, String keyValue) {
		return dao.checkObjectExist(key, keyValue);
	}

	public Atlasea dao_saveObject(Atlasea atlasea) {
		return dao.saveObject(atlasea);
	}

	
	/**
	 * Acces à une instance globale de AtlaseaAPI, qui permet de remplacer les appels
	 * à MongoDBDAO qui est une globale, dans un contexte ou on ne peut pas injecter. 
	 * Exemple dans objets du domaine (sra.Atlasea, ...)
	 * @return AtlaseaAPI
	 */
	public static AtlaseaAPI get() {
		return IGGlobals.instanceOf(AtlaseaAPI.class);
	}

	public  MongoDBResult<Atlasea> dao_find(Query q) {
		return dao.dao_find(q);		
	}
	
}
