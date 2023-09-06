package fr.cea.ig.ngl.dao.api.sra;

import static ngl.refactoring.state.SRASubmissionStateNames.NONE;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.sra.submit.sra.instance.Configuration;
import models.sra.submit.util.SraCodeHelper;
//import play.data.Form;
import validation.ContextValidation;

import org.apache.commons.lang.StringUtils;
//import org.mongojack.DBQuery;
import org.mongojack.DBQuery;

import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.GenericAPI;
import fr.cea.ig.ngl.dao.sra.ConfigurationDAO;
import fr.cea.ig.play.IGGlobals;

public class ConfigurationAPI extends GenericAPI<ConfigurationDAO, Configuration> {
    //private final static List<String> authorizedUpdateFields = Arrays.asList("code", "projectCodes", "strategySample","strategyStudy", "librarySelection", "libraryStrategy", "librarySource", "libraryConstructionProtocol", "libraryConstructionProtocol", "traceInformation" );
    // dans le javascript, defaultKeys indique les champs de l'objet à rapatrier par defaut.
	// si utilisation de includes alors on rappatrie les champs indiques dans le include plutot que ceux par defaut
	private final List<String>   DEFAULT_KEYS             = Arrays.asList("code", "projectCodes", "strategySample", "strategyStudy", 
																		  "librarySelection", "libraryStrategy", "librarySource", 
																		  "libraryConstructionProtocol", "libraryConstructionProtocol", 
																		  "traceInformation", "state" );
    private final List<String>   AUTHORIZED_UPDATE_FIELDS = Arrays.asList("code", "projectCodes", "strategySample", "strategyStudy", 
			  															  "librarySelection", "libraryStrategy", "librarySource", 
			  															  "libraryConstructionProtocol", "libraryConstructionProtocol", 
			  															  "state");
	private final SraCodeHelper  sraCodeHelper;
	
	private static final play.Logger.ALogger logger = play.Logger.of(ConfigurationAPI.class);
	@Inject
	public ConfigurationAPI(ConfigurationDAO dao, SraCodeHelper sraCodeHelper) {
		super(dao);
		this.sraCodeHelper = sraCodeHelper;
	}
	
	@Override
	protected List<String> authorizedUpdateFields() {
		return this.AUTHORIZED_UPDATE_FIELDS;
	}

	@Override
	protected List<String> defaultKeys() {
		return this.DEFAULT_KEYS;
	}


	@Override
	@Authenticated
	@Authorized.Write
	public Configuration create(Configuration input, String currentUser) throws APIValidationException, APIException {
		return create(input, currentUser, false);
	}

	@Authenticated
	@Authorized.Write
	public Configuration create(Configuration userConfiguration, String currentUser, Boolean modeCopy) throws APIValidationException, APIException {	
		
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
			// en mode copy, le userConfiguration._id est ignoré est mis à null pour le save.
			userConfiguration._id = null;
		}
		if (userConfiguration._id != null && !copy) {
			ctxVal.addError("userConfiguration.id " + userConfiguration._id, " presence de l'id incompatible  si copy a false ");
		}
		
		if (StringUtils.isBlank(userConfiguration.code) && (copy)) { 
			ctxVal.addError("userConfiguration sans code", " absence de code incompatible avec si copy a true");
		} 
		if (StringUtils.isNotBlank(userConfiguration.code) && (!copy)) {		// autoriser code existant dans inputObject uniquement en mode copy :
			ctxVal.addError("userConfiguration.code " + userConfiguration.code, " presence du code incompatible  si copy a false  "); 
		}
		
		if (ctxVal.hasErrors()) { // inutile de faire le try catch 
			throw new APIValidationException(INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
		}
		try {
			// preparation donnée :
			if (!copy) { // En mode classic, le userConfiguration est sans code et sans id, il faut generer traceInformation, code et mettre state
				userConfiguration.traceInformation = new TraceInformation(currentUser);
				userConfiguration.state = new State(NONE, currentUser);
				userConfiguration.code = sraCodeHelper.generateConfigurationCode(userConfiguration.projectCodes);
				// code aléatoire qui ne doit pas exister dans base
				while (dao.checkObjectExist("code", userConfiguration.code)) {
					userConfiguration.code = sraCodeHelper.generateConfigurationCode(userConfiguration.projectCodes);
				}
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
		userConfiguration.validate(ctxVal);
		if (ctxVal.hasErrors()) {
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
		Configuration  dbConfiguration = dao.save(userConfiguration);
		return dbConfiguration;
	}
	
	@Override
	@Authenticated
	@Authorized.Write
	public Configuration update(Configuration userConfiguration, String currentUser) throws APIException, APIValidationException {
		return update(userConfiguration, currentUser, false);
	}
	
	public void validateParamsForUpdateOrThrow(Configuration userConfiguration, String currentUser, Boolean modeCopy) throws APIValidationException {
		Boolean copy = false;		
		if(modeCopy != null && modeCopy==true) {
			copy = true;
		}
		//logger.debug("kkkkkkkkkkkkkkkkkkkk     Dans validateParamsOrThrow, copy="+ copy);
		ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
		// important de mettre le mode copy dans le context de validation car ce boolean peut etre utilisé pour la validation de l'objet
		ctxVal.putObject("copy", copy);

		if(userConfiguration == null ) {
			ctxVal.addError("configuration ",  " valeur nulle  incompatible avec la methode update");
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
		if (StringUtils.isBlank(userConfiguration.code)) {
			ctxVal.addError("configuration.code ",  " valeur nulle incompatible avec la methode ");
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}	
		Configuration dbConfiguration = get(userConfiguration.code);
		if (dbConfiguration == null) {
			ctxVal.addError("configuration.code " + userConfiguration.code , " n'existe pas dans la base et mode update");
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}

		// En mode copy ou non, l'id de l'objet qui provient d'une autre collection est mis à l'id de l'objet dans la collection cible :			
		userConfiguration._id = dbConfiguration._id;
		// en mode normal, on impose traceInformation, alors qu'en mode copy, on conserve le traceInformation de l'objet en entrée
		if (!copy) {
			logger.debug("traceInformation.forceModificationStamp");
			userConfiguration.traceInformation.forceModificationStamp(currentUser);
		}
		userConfiguration.validate(ctxVal);
		if (ctxVal.hasErrors()) {
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
	}
	
	@Authenticated
	@Authorized.Write
	public Configuration update(Configuration userConfiguration, String currentUser, Boolean modeCopy) throws APIException, APIValidationException {
		validateParamsForUpdateOrThrow(userConfiguration, currentUser, modeCopy);
	    dao.update(userConfiguration);
	    return  get(userConfiguration.code);
	}


	@Override
	@Authenticated
	@Authorized.Write
	public Configuration update(Configuration userConfiguration, String currentUser, List<String> fields)
		throws APIException, APIValidationException {
		validateParamsForUpdateOrThrow(userConfiguration, currentUser, false);
	    ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
	    checkAuthorizedUpdateFields(ctxVal, fields);
	    checkIfFieldsAreDefined(ctxVal, fields, userConfiguration);
		if (ctxVal.hasErrors()) {
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
		
		Configuration confInDb= dao.getObject(userConfiguration.code);
		TraceInformation ti = confInDb.traceInformation;
		ti.modificationStamp(ctxVal, currentUser);
		dao.updateObject(DBQuery.and(DBQuery.is("code", userConfiguration.code)), 
									dao.getBuilder(userConfiguration, fields).set("traceInformation", ti));
		
	    return  get(userConfiguration.code);
	}
	
//	/*-------------------------------------------------------------------------------------------------*/

//	public Configuration dao_findOne(Query q) {
//		return dao.findOne(q);		
//	}
//
//	public void dao_update(Query query, Builder set) {
//		dao.update(query, set);
//	}
//	
//
//  public void dao_deleteByCode(String configurationCode) {
//	  dao.deleteByCode(configurationCode);
//  }

	
	public Iterable<Configuration> dao_all() {
		return dao.all();
	}
	
//	public Configuration dao_getObject(String configurationCode) {
//		return dao.getObject(configurationCode);
//	}

//	public boolean dao_checkObjectExist(String key, String keyValue) {
//		return dao.checkObjectExist(key, keyValue);
//	}

	@Deprecated
	public Configuration dao_saveObject(Configuration configuration) {
		return dao.saveObject(configuration);
	}
	
	
//	/**
//	 * Acces à une instance globale de ConfigurationAPI, qui permet de remplacer les appels
//	 * à MongoDBDAO qui est une globale, dans un contexte ou on ne peut pas injecter. 
//	 * Exemple dans objets du domaine (sra.Experiment, ...)
//	 * @return ConfigurationAPI
//	 */
	public static ConfigurationAPI get() {
		return IGGlobals.instanceOf(ConfigurationAPI.class);
	}

}