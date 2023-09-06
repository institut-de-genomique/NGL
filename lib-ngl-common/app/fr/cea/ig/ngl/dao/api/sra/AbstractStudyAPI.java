package fr.cea.ig.ngl.dao.api.sra;

import static ngl.refactoring.state.SRASubmissionStateNames.NONE;
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
import fr.cea.ig.ngl.dao.sra.AbstractStudyDAO;
import fr.cea.ig.play.IGGlobals;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.sra.submit.sra.instance.AbstractStudy;
import models.sra.submit.sra.instance.ExternalStudy;
import models.sra.submit.sra.instance.Study;
import models.sra.submit.util.EbiAPI;
import models.sra.submit.util.SraCodeHelper;
import validation.ContextValidation;

public class AbstractStudyAPI extends GenericAPI<AbstractStudyDAO, AbstractStudy> {
	private final List<String>        DEFAULT_KEYS             = Arrays.asList("accession", "externalId", "state", "_type"); 			   															 
	private final List<String>        AUTHORIZED_UPDATE_FIELDS = null;
	private final SraCodeHelper       sraCodeHelper;

	@Inject
	public AbstractStudyAPI(AbstractStudyDAO dao,
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
	public AbstractStudy create(AbstractStudy userAbstractStudy, String currentUser, Boolean modeCopy) throws APIValidationException, APIException {	
		
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
			// en mode copy, le userAbstractStudy._id est ignoré est mis à null pour le save.
			userAbstractStudy._id = null;
		}
		if (userAbstractStudy._id != null && !copy) {
			ctxVal.addError("userAbstractStudy.id " + userAbstractStudy._id, " presence de l'id incompatible  si mode copy a false ");
		}
		
		if (StringUtils.isBlank(userAbstractStudy.code) && (copy)) { 
			ctxVal.addError("userAbstractStudy sans code", " absence de code incompatible si mode copy a true");
		} 
		if (StringUtils.isNotBlank(userAbstractStudy.code) && (!copy)) {		// autoriser code existant dans inputObject uniquement en mode copy :
			ctxVal.addError("userAbstractStudy.code " + userAbstractStudy.code, " presence du code incompatible  si mode copy a false  "); 
		}
		if (ctxVal.hasErrors()) { // inutile de faire le try catch 
			throw new APIValidationException(INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
		}
		try {
			// preparation donnée :
			if (!copy) { 
				if (userAbstractStudy instanceof Study) {
					//code aléatoire qui ne doit pas exister en base :
					userAbstractStudy.code = sraCodeHelper.generateStudyCode();
					while (dao.checkObjectExist("code", userAbstractStudy.code)) {
						userAbstractStudy.code = sraCodeHelper.generateStudyCode();
					}
					userAbstractStudy.state = new State(NONE, currentUser);
					userAbstractStudy.traceInformation = new TraceInformation(currentUser);
				} else if(userAbstractStudy instanceof ExternalStudy)  {
					// ici code logique et non aléatoire, donc si existe dans base declenchera erreur lors de la validation
					userAbstractStudy.code = sraCodeHelper.generateExternalStudyCode(userAbstractStudy.accession);
					userAbstractStudy.state = new State(SUB_F, currentUser);
				} else {
					ctxVal.addError("userAbstractStudy avec type inconnu", userAbstractStudy._type);
				}
			} else {
				// en mode copy, rien : le abstractStudy a un code mais pas d'id, et pas de traceIformation ou state ou code à generer puisqu'on 
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
		userAbstractStudy.validate(ctxVal);
		if (ctxVal.hasErrors()) {
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
		AbstractStudy  dbAbstractStudy = dao.save(userAbstractStudy);
		return dbAbstractStudy;
	}
	

	@Override
	@Authenticated
	@Authorized.Write
	public AbstractStudy create(AbstractStudy input, String currentUser)
			throws APIValidationException, APIException {
		return create(input, currentUser, false);
	}

	public void validateParamsForUpdateOrThrow(AbstractStudy userAbstractStudy, String currentUser, Boolean modeCopy) throws APIValidationException {
		Boolean copy = false;		
		if(modeCopy != null && modeCopy==true) {
			copy = true;
		}
		ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
		// important de mettre le mode copy dans le context de validation car ce boolean peut etre utilisé pour la validation de l'objet
		ctxVal.putObject("copy", copy);

		if(userAbstractStudy == null ) {
			ctxVal.addError("abstractStudy ",  " valeur nulle  incompatible avec la methode update");
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
		if (StringUtils.isBlank(userAbstractStudy.code)) {
			ctxVal.addError("abstractStudy.code ",  " valeur nulle incompatible avec la methode ");
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}	
		AbstractStudy dbAbstractStudy = get(userAbstractStudy.code);
		if (dbAbstractStudy == null) {
			ctxVal.addError("abstractStudy.code " + userAbstractStudy.code , " n'existe pas dans la base et mode update");
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}

		// En mode copy ou non, l'id de l'objet qui provient d'une autre collection est mis à l'id de l'objet dans la collection cible :			
		userAbstractStudy._id = dbAbstractStudy._id;
		// en mode normal, on impose traceInformation, alors qu'en mode copy, on conserve le traceInformation de l'objet en entrée
		if (!copy) {
			userAbstractStudy.traceInformation.forceModificationStamp(currentUser);
		}
		userAbstractStudy.validate(ctxVal);
		if (ctxVal.hasErrors()) {
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
	}
	
	@Override
	@Authenticated
	@Authorized.Write
	public AbstractStudy update(AbstractStudy userAbstractStudy, String currentUser) throws APIException, APIValidationException {
		return update(userAbstractStudy, currentUser, false);
	}
	
	
	@Authenticated
	@Authorized.Write
	public AbstractStudy update(AbstractStudy userAbstractStudy, String currentUser, Boolean modeCopy) throws APIException, APIValidationException {
		validateParamsForUpdateOrThrow(userAbstractStudy, currentUser, modeCopy);
	    dao.update(userAbstractStudy);
	    return  get(userAbstractStudy.code);
	}


	@Override
	@Authenticated
	@Authorized.Write
	public AbstractStudy update(AbstractStudy userAbstractStudy, String currentUser, List<String> fields)
		throws APIException, APIValidationException {
		validateParamsForUpdateOrThrow(userAbstractStudy, currentUser, false);
	    ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
	    checkAuthorizedUpdateFields(ctxVal, fields);
	    checkIfFieldsAreDefined(ctxVal, fields, userAbstractStudy);
		if (ctxVal.hasErrors()) {
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
		AbstractStudy abstractStudyInDb= dao.getObject(userAbstractStudy.code);
		TraceInformation ti = abstractStudyInDb.traceInformation;
		ti.modificationStamp(ctxVal, currentUser);
		dao.updateObject(DBQuery.and(DBQuery.is("code", userAbstractStudy.code)), 
									dao.getBuilder(userAbstractStudy, fields).set("traceInformation", ti));
		return  get(userAbstractStudy.code);
	}	


	/*-------------------------------------------------------------------------------------------------*/

	public Iterable<AbstractStudy> dao_all() {
		return dao.all();
	}
		
	public boolean dao_checkObjectExist(String key, String keyValue) {
		return dao.checkObjectExist(key, keyValue);
	}

	public AbstractStudy dao_findOne(Query q) {
		return dao.findOne(q);		
	}

	public AbstractStudy dao_getObject(String sampleCode) {
		return dao.getObject(sampleCode);
	}

	public AbstractStudy dao_saveObject(AbstractStudy sampleElt) {
		return dao.saveObject(sampleElt);
	}

	public void dao_update(Query query, Builder set) {
		dao.update(query, set);
	}
	/**
	 * Acces à une instance globale de AbstractStudyAPI, qui permet de remplacer les appels
	 * à MongoDBDAO qui est une globale, dans un contexte ou on ne peut pas injecter. 
	 * Exemple dans objets du domaine (sra.Experiment, ...)
	 * @return AbstractStudyAPI
	 */
	public static AbstractStudyAPI get() {
		return IGGlobals.instanceOf(AbstractStudyAPI.class);
	}

	public  MongoDBResult<AbstractStudy> dao_find(Query q) {
		return dao.dao_find(q);		
	}
	
}
