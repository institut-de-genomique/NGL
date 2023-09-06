package fr.cea.ig.ngl.dao.api.sra;


import static ngl.refactoring.state.SRASubmissionStateNames.SUB_F;

import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.GenericAPI;
import fr.cea.ig.ngl.dao.sra.ExternalSampleDAO;
import fr.cea.ig.play.IGGlobals;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.sra.submit.sra.instance.ExternalSample;
import models.sra.submit.util.SraCodeHelper;
import validation.ContextValidation;


public class ExternalSampleAPI extends GenericAPI<ExternalSampleDAO, ExternalSample> {
	private final List<String>    DEFAULT_KEYS             = Arrays.asList("accession", "externalId", "state", "_type"); 			   															 
	private final List<String>    AUTHORIZED_UPDATE_FIELDS = null;
	private final SraCodeHelper   sraCodeHelper;



	@Inject
	public ExternalSampleAPI(ExternalSampleDAO dao,
							 SraCodeHelper     sraCodeHelper) {
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
	public ExternalSample create(ExternalSample userExternalSample, String currentUser, Boolean modeCopy) throws APIValidationException, APIException {	
		//logger.debug("AAAAAAAAAAAAAAA                   Dans  ExternalSampleAPI.create");

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
			// en mode copy, le userExternalSample._id est ignoré est mis à null pour le save.
			userExternalSample._id = null;
		}
		if (userExternalSample._id != null && !copy) {
			ctxVal.addError("userExternalSample.id " + userExternalSample._id, " presence de l'id incompatible  si mode copy a false ");
		}
		
		if (StringUtils.isBlank(userExternalSample.code) && (copy)) { 
			ctxVal.addError("userExternalSample sans code", " absence de code incompatible si mode copy a true");
		} 
		if (StringUtils.isNotBlank(userExternalSample.code) && (!copy)) {		// autoriser code existant dans inputObject uniquement en mode copy :
			ctxVal.addError("userExternalSample.code " + userExternalSample.code, " presence du code incompatible  si mode copy a false  "); 
		}
		if (StringUtils.isBlank(userExternalSample.accession)) { 
			ctxVal.addError("userExternalSample.accession " , " absence de numeros d'accession incompatible avec mode creation pour externalSample "); 
		}
		if (ctxVal.hasErrors()) { // inutile de faire le try catch 
			throw new APIValidationException(INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
		}
		try {
			// preparation donnée :
			if (!copy) { 
				if (StringUtils.isBlank(userExternalSample.accession)) {
					ctxVal.addError("userExternalSample.accession sans valeur", " incompatible avec userExternalSample._type=externalSample ");  // si solution filledForm.reject
				}
				userExternalSample.code = sraCodeHelper.generateExternalSampleCode(userExternalSample.accession);
				// ici code logique et non aléatoire, donc si existe dans base declenchera erreur lors de la validation
				userExternalSample.code = sraCodeHelper.generateExternalSampleCode(userExternalSample.accession);
				userExternalSample.state = new State(SUB_F, currentUser);
			} else {
				// en mode copy, rien : le externalSample a un code mais pas d'id, et pas de traceIformation ou state ou code à generer puisqu'on 
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
		userExternalSample.validate(ctxVal);
		if (ctxVal.hasErrors()) {
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
		ExternalSample  dbExternalSample = dao.save(userExternalSample);
		return dbExternalSample;
	}
	

	@Override
	@Authenticated
	@Authorized.Write
	public ExternalSample create(ExternalSample input, String currentUser)
			throws APIValidationException, APIException {
		return create(input, currentUser, false);
	}

	public void validateParamsForUpdateOrThrow(ExternalSample userExternalSample, String currentUser, Boolean modeCopy) throws APIValidationException {
		Boolean copy = false;		
		if(modeCopy != null && modeCopy==true) {
			copy = true;
		}
		ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
		// important de mettre le mode copy dans le context de validation car ce boolean peut etre utilisé pour la validation de l'objet
		ctxVal.putObject("copy", copy);

		if(userExternalSample == null ) {
			ctxVal.addError("externalSample ",  " valeur nulle  incompatible avec la methode update");
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
		if (StringUtils.isBlank(userExternalSample.code)) {
			ctxVal.addError("externalSample.code ",  " valeur nulle incompatible avec la methode ");
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}	
		ExternalSample dbExternalSample = get(userExternalSample.code);
		if (dbExternalSample == null) {
			ctxVal.addError("externalSample.code " + userExternalSample.code , " n'existe pas dans la base et mode update");
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}

		// En mode copy ou non, l'id de l'objet qui provient d'une autre collection est mis à l'id de l'objet dans la collection cible :			
		userExternalSample._id = dbExternalSample._id;
		// en mode normal, on impose traceInformation, alors qu'en mode copy, on conserve le traceInformation de l'objet en entrée
		if (!copy) {
			userExternalSample.traceInformation.forceModificationStamp(currentUser);
		}
		userExternalSample.validate(ctxVal);
		if (ctxVal.hasErrors()) {
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
	}
	
	@Override
	@Authenticated
	@Authorized.Write
	public ExternalSample update(ExternalSample userExternalSample, String currentUser) throws APIException, APIValidationException {
		return update(userExternalSample, currentUser, false);
	}
	
	
	@Authenticated
	@Authorized.Write
	public ExternalSample update(ExternalSample userExternalSample, String currentUser, Boolean modeCopy) throws APIException, APIValidationException {
		validateParamsForUpdateOrThrow(userExternalSample, currentUser, modeCopy);
	    dao.update(userExternalSample);
	    return  get(userExternalSample.code);
	}

	
	@Override
	@Authenticated
	@Authorized.Write
	public ExternalSample update(ExternalSample userExternalSample, String currentUser, List<String> fields)
		throws APIException, APIValidationException {
		validateParamsForUpdateOrThrow(userExternalSample, currentUser, false);
	    ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
	    checkAuthorizedUpdateFields(ctxVal, fields);
	    checkIfFieldsAreDefined(ctxVal, fields, userExternalSample);
		if (ctxVal.hasErrors()) {
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
		ExternalSample sampleInDb= dao.getObject(userExternalSample.code);
		TraceInformation ti = sampleInDb.traceInformation;
		ti.modificationStamp(ctxVal, currentUser);
		dao.updateObject(DBQuery.and(DBQuery.is("code", userExternalSample.code)), 
									dao.getBuilder(userExternalSample, fields).set("traceInformation", ti));
		return  get(userExternalSample.code);
	}	
	
	
	/*-------------------------------------------------------------------------------------------------*/

	public boolean dao_checkObjectExist(String key, String keyValue) {
		return dao.checkObjectExist(key, keyValue);
	}

	public ExternalSample dao_findOne(Query q) {
		return dao.findOne(q);		
	}

	public void dao_deleteByCode(String studyCode) {
		dao.deleteByCode(studyCode);
	}

	public ExternalSample dao_getObject(String sampleCode) {
		return dao.getObject(sampleCode);
	}
	
	public ExternalSample dao_saveObject(ExternalSample externalSample) {
		return dao.saveObject(externalSample);

	}
	

	/**
	 * Acces à une instance globale de ExternalSampleAPI, qui permet de remplacer les appels
	 * à MongoDBDAO qui est une globale, dans un contexte ou on ne peut pas injecter. 
	 * Exemple dans objets du domaine (sra.Experiment, ...)
	 * @return ExternalSampleAPI
	 */
	public static ExternalSampleAPI get() {
		return IGGlobals.instanceOf(ExternalSampleAPI.class);
	}

	
}
