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
import fr.cea.ig.ngl.dao.sra.AbstractSampleDAO;
import fr.cea.ig.play.IGGlobals;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.sra.submit.sra.instance.AbstractSample;
import models.sra.submit.sra.instance.ExternalSample;
import models.sra.submit.sra.instance.Sample;
import models.sra.submit.util.EbiAPI;
import models.sra.submit.util.SraCodeHelper;
import validation.ContextValidation;

public class AbstractSampleAPI extends GenericAPI<AbstractSampleDAO, AbstractSample> {
	private final List<String>        DEFAULT_KEYS             = Arrays.asList("accession", "externalId", "state", "_type"); 			   															 
	private final List<String>        AUTHORIZED_UPDATE_FIELDS = null;
	private final SraCodeHelper       sraCodeHelper;
	private final EbiAPI              ebiAPI;
	private static final play.Logger.ALogger  logger = play.Logger.of(AbstractSampleAPI.class);

	@Inject
	public AbstractSampleAPI(AbstractSampleDAO dao,
							 SraCodeHelper     sraCodeHelper,
							 EbiAPI            ebiAPI) {
		super(dao);
		this.sraCodeHelper = sraCodeHelper;
		this.ebiAPI        = ebiAPI;

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
	public AbstractSample create(AbstractSample userAbstractSample, String currentUser, Boolean modeCopy) throws APIValidationException, APIException {	
		logger.debug("AAAAAAAAAAAAAAA                   Dans  AbstractSampleAPI.create");

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
			// en mode copy, le userAbstractSample._id est ignoré est mis à null pour le save.
			userAbstractSample._id = null;
		}
		if (userAbstractSample._id != null && !copy) {
			ctxVal.addError("userAbstractSample.id " + userAbstractSample._id, " presence de l'id incompatible  si mode copy a false ");
		}
		
		if (StringUtils.isBlank(userAbstractSample.code) && (copy)) { 
			ctxVal.addError("userAbstractSample sans code", " absence de code incompatible si mode copy a true");
		} 
		if (StringUtils.isNotBlank(userAbstractSample.code) && (!copy)) {		// autoriser code existant dans inputObject uniquement en mode copy :
			ctxVal.addError("userAbstractSample.code " + userAbstractSample.code, " presence du code incompatible  si mode copy a false  "); 
		}
		
		if (ctxVal.hasErrors()) { // inutile de faire le try catch 
			logger.debug("AAAAAAAAAAAAAAA                   Dans  AbstractSampleAPI.create ctxVal avec erreurs");
			throw new APIValidationException(INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
		}
		logger.debug("AAAAAAAAAAAAAAA   2222                Dans  AbstractSampleAPI.create avant try");

		try {
			// preparation donnée :
			if (!copy) { // En mode classic, le userExperiment est sans code et sans id, il faut generer traceInformation, code et mettre state
			             // mais ici on n'autorise pas sauvegarde des experiments, on doit passer par service de creation de la soumission.
				if (userAbstractSample instanceof Sample) {
					logger.debug("AAAAAAAAAAAAAAA                   Dans  AbstractSampleAPI.create cas instanceof Sample");

					String projectCode = ((Sample) userAbstractSample).projectCode;
					int taxonId = ((Sample) userAbstractSample).taxonId;
					
					logger.debug("AAAAAAAAAAAAAAA                   Dans  AbstractSampleAPI.create int_taxonId= " + taxonId);

					// verifier si taxonId soumettable et recuperer scientificName :
					if(! ebiAPI.submittable(taxonId)) {
						ctxVal.addError("userAbstractSample.taxonId", taxonId + " non soumettable");  // si solution filledForm.reject
					}
					String scientificName = ebiAPI.getScientificName(taxonId);
					if (StringUtils.isBlank(scientificName)) {
						ctxVal.addError("userAbstractSample.scientificName", "scientificName non recuperable pour le taxonId " + taxonId);
					}
					((Sample) userAbstractSample).scientificName = scientificName;
					// si on voulait utiliser refCollab, il faudrait voir comment recuperer cet argument qui n'est pas un champs de sample;
					// ici code logique et non aléatoire, donc si existe dans base declenchera erreur lors de la validation
					userAbstractSample.code = sraCodeHelper.generateSampleCode(projectCode, taxonId, null);
					//logger.debug("userAbstractSampleCode=" + userAbstractSample.code);
					userAbstractSample.state = new State(NONE, currentUser);
					userAbstractSample.traceInformation = new TraceInformation(currentUser);
				} else if(userAbstractSample instanceof ExternalSample)  {
					logger.debug("AAAAAAAAAAAAAAA                   Dans  AbstractSampleAPI.create cas instanceof ExternalSample");

					if (StringUtils.isBlank(userAbstractSample.accession)) {
						ctxVal.addError("userAbstractSample.accession sans valeur", " incompatible avec userAbstractSample._type=externalSample ");  // si solution filledForm.reject
					}
					// ici code logique et non aléatoire, donc si existe dans base declenchera erreur lors de la validation
					userAbstractSample.code = sraCodeHelper.generateExternalSampleCode(userAbstractSample.accession);
					userAbstractSample.state = new State(SUB_F, currentUser);
				} else {
					ctxVal.addError("userAbstractSample avec type inconnu", userAbstractSample._type);
				}
			} else {
				// en mode copy, rien : le abstractSample a un code mais pas d'id, et pas de traceIformation ou state ou code à generer puisqu'on 
				// conserve les champs d'origine
			}
		} catch(RuntimeException e) {
			ctxVal.addError("RuntimeException", e.getMessage());
		} catch(Exception e) {
			ctxVal.addError("Exception", e.getMessage());
		}
		// validation et sauvegarde dans base
		logger.debug("AAAAAAAAAAAAAAA                   Dans  AbstractSampleAPI.create avant validate et dao.save");

		userAbstractSample.validate(ctxVal);
		if (ctxVal.hasErrors()) {
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
		AbstractSample  dbAbstractSample = dao.save(userAbstractSample);
		return dbAbstractSample;
	}
	

	@Override
	@Authenticated
	@Authorized.Write
	public AbstractSample create(AbstractSample input, String currentUser)
			throws APIValidationException, APIException {
		return create(input, currentUser, false);
	}

	public void validateParamsForUpdateOrThrow(AbstractSample userAbstractSample, String currentUser, Boolean modeCopy) throws APIValidationException {
		Boolean copy = false;		
		if(modeCopy != null && modeCopy==true) {
			copy = true;
		}
		ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
		// important de mettre le mode copy dans le context de validation car ce boolean peut etre utilisé pour la validation de l'objet
		ctxVal.putObject("copy", copy);

		if(userAbstractSample == null ) {
			ctxVal.addError("userAbstractSample ",  " valeur nulle  incompatible avec la methode update");
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
		if (StringUtils.isBlank(userAbstractSample.code)) {
			ctxVal.addError("userAbstractSample.code ",  " valeur nulle incompatible avec la methode ");
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}	
		AbstractSample dbAbstractSample = get(userAbstractSample.code);
		if (dbAbstractSample == null) {
			ctxVal.addError("userAbstractSample.code " + userAbstractSample.code , " n'existe pas dans la base et mode update");
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}

		// En mode copy ou non, l'id de l'objet qui provient d'une autre collection est mis à l'id de l'objet dans la collection cible :			
		userAbstractSample._id = dbAbstractSample._id;
		// en mode normal, on impose traceInformation, alors qu'en mode copy, on conserve le traceInformation de l'objet en entrée
		if (!copy) {
			userAbstractSample.traceInformation.forceModificationStamp(currentUser);
		}
		userAbstractSample.validate(ctxVal);

		if (ctxVal.hasErrors()) {
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
	}
	
	@Override
	@Authenticated
	@Authorized.Write
	public AbstractSample update(AbstractSample userAbstractSample, String currentUser) throws APIException, APIValidationException {
		return update(userAbstractSample, currentUser, false);
	}
	
	
	@Authenticated
	@Authorized.Write
	public AbstractSample update(AbstractSample userAbstractSample, String currentUser, Boolean modeCopy) throws APIException, APIValidationException {
		validateParamsForUpdateOrThrow(userAbstractSample, currentUser, modeCopy);
	    dao.update(userAbstractSample);
	    return  get(userAbstractSample.code);
	}

	
	@Override
	@Authenticated
	@Authorized.Write
	public AbstractSample update(AbstractSample userAbstractSample, String currentUser, List<String> fields)
		throws APIException, APIValidationException {
		validateParamsForUpdateOrThrow(userAbstractSample, currentUser, false);
	    ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
	    checkAuthorizedUpdateFields(ctxVal, fields);
	    checkIfFieldsAreDefined(ctxVal, fields, userAbstractSample);
		if (ctxVal.hasErrors()) {
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
		AbstractSample abstractSampleInDb= dao.getObject(userAbstractSample.code);
		TraceInformation ti = abstractSampleInDb.traceInformation;
		ti.modificationStamp(ctxVal, currentUser);
		dao.updateObject(DBQuery.and(DBQuery.is("code", userAbstractSample.code)), 
									dao.getBuilder(userAbstractSample, fields).set("traceInformation", ti));
		return  get(userAbstractSample.code);
	}	


	/*-------------------------------------------------------------------------------------------------*/

	public Iterable<AbstractSample> dao_all() {
		return dao.all();
	}
		
	public boolean dao_checkObjectExist(String key, String keyValue) {
		return dao.checkObjectExist(key, keyValue);
	}

	public AbstractSample dao_findOne(Query q) {
		return dao.findOne(q);		
	}

	public AbstractSample dao_getObject(String sampleCode) {
		return dao.getObject(sampleCode);
	}

	public AbstractSample dao_saveObject(AbstractSample sampleElt) {
		return dao.saveObject(sampleElt);
	}

	public void dao_update(Query query, Builder set) {
		dao.update(query, set);
	}
	/**
	 * Acces à une instance globale de AbstractSampleAPI, qui permet de remplacer les appels
	 * à MongoDBDAO qui est une globale, dans un contexte ou on ne peut pas injecter. 
	 * Exemple dans objets du domaine (sra.Experiment, ...)
	 * @return AbstractSampleAPI
	 */
	public static AbstractSampleAPI get() {
		return IGGlobals.instanceOf(AbstractSampleAPI.class);
	}

	public  MongoDBResult<AbstractSample> dao_find(Query q) {
		return dao.dao_find(q);		
	}
	
}
