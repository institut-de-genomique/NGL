package fr.cea.ig.ngl.dao.api.sra;

import static ngl.refactoring.state.SRASubmissionStateNames.NONE;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import org.mongojack.DBUpdate.Builder;

import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.GenericAPI;
import fr.cea.ig.ngl.dao.sra.SampleDAO;
import fr.cea.ig.play.IGGlobals;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.sra.submit.sra.instance.Sample;
import models.sra.submit.util.EbiAPI;
import models.sra.submit.util.SraCodeHelper;
import validation.ContextValidation;

public class SampleAPI extends GenericAPI<SampleDAO, Sample> {

	
	private final List<String>    DEFAULT_KEYS             = Arrays.asList("accession", "externalId", "state", "_type"); 			   															 
	private final List<String>    AUTHORIZED_UPDATE_FIELDS = null;

	private final SraCodeHelper       sraCodeHelper;
	private final EbiAPI              ebiAPI;
	private static final play.Logger.ALogger  logger = play.Logger.of(SampleAPI.class);

	@Inject
	public SampleAPI(SampleDAO dao,
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
	public Sample create(Sample userSample, String currentUser, Boolean modeCopy) throws APIValidationException, APIException {	
		logger.debug("BBBBBBBBB                   Dans  SampleAPI.create ");

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
			// en mode copy, le userSample._id est ignoré est mis à null pour le save.
			userSample._id = null;
		}
		if (userSample._id != null && !copy) {
			ctxVal.addError("userSample.id " + userSample._id, " presence de l'id incompatible  si mode copy a false ");
		}
		
		if (StringUtils.isBlank(userSample.code) && (copy)) { 
			ctxVal.addError("userSample sans code", " absence de code incompatible si mode copy a true");
		} 
		if (StringUtils.isNotBlank(userSample.code) && (!copy)) {		// autoriser code existant dans inputObject uniquement en mode copy :
			ctxVal.addError("userSample.code " + userSample.code, " presence du code incompatible  si mode copy a false  "); 
		}
		logger.debug("BBBBBBBBB  222                 Dans  SampleAPI.create ");

		if (ctxVal.hasErrors()) { // inutile de faire le try catch 
			throw new APIValidationException(INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
		}
		logger.debug("BBBBBBBBB  333                 Dans  SampleAPI.create ");

		try {
			// preparation donnée :
			if (!copy) { 
				logger.debug("BBBBBBBBB  444                 Dans  SampleAPI.create cas !copy");

				String projectCode = ((Sample) userSample).projectCode;
				int taxonId = ((Sample) userSample).taxonId;
				// verifier si taxonId soumettable et recuperer scientificName :
				if(! ebiAPI.submittable(taxonId)) {
					ctxVal.addError("userSample.taxonId", taxonId + " non soumettable");  // si solution filledForm.reject
				}
				String scientificName = ebiAPI.getScientificName(taxonId);
				if (StringUtils.isBlank(scientificName)) {
					ctxVal.addError("userSample.scientificName", "scientificName non recuperable pour le taxonId " + taxonId);
				}
				((Sample) userSample).scientificName = scientificName;
				// si on voulait utiliser refCollab, il faudrait voir comment recuperer cet argument qui n'est pas un champs de sample;
				// ici code logique et non aléatoire, donc si existe dans base declenchera erreur lors de la validation
				userSample.code = sraCodeHelper.generateSampleCode(projectCode, taxonId, null);
				//logger.debug("userSampleCode=" + userSample.code);
				userSample.state = new State(NONE, currentUser);
				userSample.traceInformation = new TraceInformation(currentUser);
			} else {
				// en mode copy, rien : le sample a un code mais pas d'id, et pas de traceIformation ou state ou code à generer puisqu'on 
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
		logger.debug("BBBBBBBBB                   Dans  SampleAPI.create avant validate() et dao.save()");

		userSample.validate(ctxVal);
		if (ctxVal.hasErrors()) {
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
		Sample  dbSample = dao.save(userSample);
		return dbSample;
	}
	

	@Override
	@Authenticated
	@Authorized.Write
	public Sample create(Sample input, String currentUser)
			throws APIValidationException, APIException {
		return create(input, currentUser, false);
	}

	public void validateParamsForUpdateOrThrow(Sample userSample, String currentUser, Boolean modeCopy) throws APIValidationException {
		Boolean copy = false;		
		if(modeCopy != null && modeCopy==true) {
			copy = true;
		}
		ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
		// important de mettre le mode copy dans le context de validation car ce boolean peut etre utilisé pour la validation de l'objet
		ctxVal.putObject("copy", copy);

		if(userSample == null ) {
			ctxVal.addError("sample ",  " valeur nulle  incompatible avec la methode update");
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
		if (StringUtils.isBlank(userSample.code)) {
			ctxVal.addError("sample.code ",  " valeur nulle incompatible avec la methode ");
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}	
		Sample dbSample = get(userSample.code);
		if (dbSample == null) {
			ctxVal.addError("sample.code " + userSample.code , " n'existe pas dans la base et mode update");
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}

		// En mode copy ou non, l'id de l'objet qui provient d'une autre collection est mis à l'id de l'objet dans la collection cible :			
		userSample._id = dbSample._id;
		// en mode normal, on impose traceInformation, alors qu'en mode copy, on conserve le traceInformation de l'objet en entrée
		if (!copy) {
			userSample.traceInformation.forceModificationStamp(currentUser);
		}
		userSample.validate(ctxVal);
		if (ctxVal.hasErrors()) {
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
	}
	
	@Override
	@Authenticated
	@Authorized.Write
	public Sample update(Sample userSample, String currentUser) throws APIException, APIValidationException {
		return update(userSample, currentUser, false);
	}
	
	
	@Authenticated
	@Authorized.Write
	public Sample update(Sample userSample, String currentUser, Boolean modeCopy) throws APIException, APIValidationException {
		validateParamsForUpdateOrThrow(userSample, currentUser, modeCopy);
	    dao.update(userSample);
	    return  get(userSample.code);
	}


	
	@Override
	@Authenticated
	@Authorized.Write
	public Sample update(Sample userSample, String currentUser, List<String> fields)
		throws APIException, APIValidationException {
		validateParamsForUpdateOrThrow(userSample, currentUser, false);
	    ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
	    checkAuthorizedUpdateFields(ctxVal, fields);
	    checkIfFieldsAreDefined(ctxVal, fields, userSample);
		if (ctxVal.hasErrors()) {
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
		Sample sampleInDb= dao.getObject(userSample.code);
		TraceInformation ti = sampleInDb.traceInformation;
		ti.modificationStamp(ctxVal, currentUser);
		dao.updateObject(DBQuery.and(DBQuery.is("code", userSample.code)), 
									dao.getBuilder(userSample, fields).set("traceInformation", ti));
		return  get(userSample.code);
	}	

	

	// -------------------------------------------------------------------------------------------------
	
	public Sample dao_getObject(String sampleCode) {
		return dao.getObject(sampleCode);
	}
	
	public Sample dao_findOne(Query q) {
		return dao.findOne(q);		
	}
	
	public Iterable<Sample> dao_find(Query q) {
		return dao.find(q);
	}
	
	public void dao_update(Query query, Builder set) {
		dao.update(query, set);
	}

	public boolean dao_checkObjectExist(String key, String keyValue) {
		return dao.checkObjectExist(key, keyValue);
	}

	public Sample dao_saveObject(Sample sample) {
		return dao.saveObject(sample);
	}
	

	public void dao_deleteByCode(String sampleCode) {
		dao.deleteByCode(sampleCode);
	}
	
	/**
	 * Acces à une instance globale de SampleAPI, qui permet de remplacer les appels
	 * à MongoDBDAO qui est une globale, dans un contexte ou on ne peut pas injecter. 
	 * Exemple dans objets du domaine (sra.Experiment, ...)
	 * @return SampleAPI
	 */
	public static SampleAPI get() {
		return IGGlobals.instanceOf(SampleAPI.class);
	}

	//SGAS
	// Retourner uniquement de type Sample et non ExternalSample en ajoutant filtre
	public Iterable<Sample> dao_all() {
		return dao.all();
	}

		
}
