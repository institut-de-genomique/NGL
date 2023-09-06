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
import fr.cea.ig.ngl.dao.sra.AnalysisDAO;
import fr.cea.ig.play.IGGlobals;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.sra.submit.sra.instance.Analysis;
import models.sra.submit.util.SraCodeHelper;
import validation.ContextValidation;

public class AnalysisAPI extends GenericAPI<AnalysisDAO, Analysis> {
	private final List<String> DEFAULT_KEYS            = Arrays.asList("analysisCodes", "title", "description","accession",  
																	   "sampleCode", "studyCode", "sampleAccession", "studyAccession", 
																	   "state", "firstSubmissionDate");
	
    private final List<String> AUTHORIZED_UPDATE_FIELDS = null;
	private final SraCodeHelper       sraCodeHelper;
	
	@Inject
	public AnalysisAPI(AnalysisDAO dao,
			           SraCodeHelper sraCodeHelper) {
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
	public Analysis create(Analysis userAnalysis, String currentUser, Boolean modeCopy) throws APIValidationException, APIException {	
		
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
			// en mode copy, le userAnalysis._id est ignoré est mis à null pour le save.
			userAnalysis._id = null;
		}
		if (userAnalysis._id != null && !copy) {
			ctxVal.addError("userAnalysis.id " + userAnalysis._id, " presence de l'id incompatible  si copy a false ");
		}
		
		if (StringUtils.isBlank(userAnalysis.code) && (copy)) { 
			ctxVal.addError("userAnalysis sans code", " absence de code incompatible avec si copy a true");
		} 
		if (StringUtils.isNotBlank(userAnalysis.code) && (!copy)) {		// autoriser code existant dans inputObject uniquement en mode copy :
			ctxVal.addError("userAnalysis.code " + userAnalysis.code, " presence du code incompatible  si copy a false  "); 
		}
		
		if (ctxVal.hasErrors()) { // inutile de faire le try catch 
			throw new APIValidationException(INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
		}
		try {
			// preparation donnée :
			if (!copy) { /// En mode classic, le analysis est sans code et sans id, il faut generer traceInformation, code et mettre state
				userAnalysis.traceInformation = new TraceInformation(currentUser);
				userAnalysis.state = new State(NONE, currentUser);
				userAnalysis.code = sraCodeHelper.generateAnalysisCode();
				//ok, code aléatoire qui ne doit pas exister en base :
				while (dao.checkObjectExist("code", userAnalysis.code)) {
					userAnalysis.code = sraCodeHelper.generateAnalysisCode();
				}
				//userAnalysis.state = new State(SUB_N, currentUser); // todo : a verifier
			} else {
				// en mode copy, rien : le analysis a un code mais pas d'id, et pas de traceIformation ou state ou code à generer puisqu'on 
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
		userAnalysis.validate(ctxVal);
		if (ctxVal.hasErrors()) {
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
		Analysis  dbAnalysis = dao.save(userAnalysis);
		return dbAnalysis;
	}
	

	@Override
	@Authenticated
	@Authorized.Write
	public Analysis create(Analysis input, String currentUser)
			throws APIValidationException, APIException {
		return create(input, currentUser, false);
	}

	public void validateParamsForUpdateOrThrow(Analysis userAnalysis, String currentUser, Boolean modeCopy) throws APIValidationException {
		Boolean copy = false;		
		if(modeCopy != null && modeCopy==true) {
			copy = true;
		}
		ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
		// important de mettre le mode copy dans le context de validation car ce boolean peut etre utilisé pour la validation de l'objet
		ctxVal.putObject("copy", copy);

		if(userAnalysis == null ) {
			ctxVal.addError("analysis ",  " valeur nulle  incompatible avec la methode update");
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
		if (StringUtils.isBlank(userAnalysis.code)) {
			ctxVal.addError("analysis.code ",  " valeur nulle incompatible avec la methode ");
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}	
		Analysis dbAnalysis = get(userAnalysis.code);
		if (dbAnalysis == null) {
			ctxVal.addError("analysis.code " + userAnalysis.code , " n'existe pas dans la base et mode update");
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}

		// En mode copy ou non, l'id de l'objet qui provient d'une autre collection est mis à l'id de l'objet dans la collection cible :			
		userAnalysis._id = dbAnalysis._id;
		// en mode normal, on impose traceInformation, alors qu'en mode copy, on conserve le traceInformation de l'objet en entrée
		if (!copy) {
			userAnalysis.traceInformation.forceModificationStamp(currentUser);
		}
		userAnalysis.validate(ctxVal);
		if (ctxVal.hasErrors()) {
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
	}
	
	@Override
	@Authenticated
	@Authorized.Write
	public Analysis update(Analysis userAnalysis, String currentUser) throws APIException, APIValidationException {
		return update(userAnalysis, currentUser, false);
	}
	
	
	@Authenticated
	@Authorized.Write
	public Analysis update(Analysis userAnalysis, String currentUser, Boolean modeCopy) throws APIException, APIValidationException {
		validateParamsForUpdateOrThrow(userAnalysis, currentUser, modeCopy);
	    dao.update(userAnalysis);
	    return  get(userAnalysis.code);
	}

	@Override
	@Authenticated
	@Authorized.Write
	public Analysis update(Analysis userAnalysis, String currentUser, List<String> fields)
		throws APIException, APIValidationException {
		validateParamsForUpdateOrThrow(userAnalysis, currentUser, false);
	    ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
	    checkAuthorizedUpdateFields(ctxVal, fields);
	    checkIfFieldsAreDefined(ctxVal, fields, userAnalysis);
		if (ctxVal.hasErrors()) {
			throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
		}
		
		Analysis confInDb= dao.getObject(userAnalysis.code);
		TraceInformation ti = confInDb.traceInformation;
		ti.modificationStamp(ctxVal, currentUser);
		dao.updateObject(DBQuery.and(DBQuery.is("code", userAnalysis.code)), 
									dao.getBuilder(userAnalysis, fields).set("traceInformation", ti));
		
	    return  get(userAnalysis.code);
	}
	
	/*-------------------------------------------------------------------------------------------------*/

	public List<Analysis> dao_findAsList(Query q) {
		return dao.findAsList(q);
	}
	
	public Iterable<Analysis> dao_all() {
		return dao.all();
	}
	
	public Iterable<Analysis> dao_all_batch(int cp) {
		return dao.all_batch(cp);
	}
	
	public Analysis dao_getObject(String analysisCode) {
		return dao.getObject(analysisCode);
	}
	
	public Analysis dao_findOne(Query q) {
		return dao.findOne(q);		
	}

	public void dao_update(Query query, Builder set) {
		dao.update(query, set);
	}

	public boolean dao_checkObjectExist(String key, String keyValue) {
		return dao.checkObjectExist(key, keyValue);
	}

	public Analysis dao_saveObject(Analysis analysis) {
		return dao.saveObject(analysis);
	}

	public void dao_deleteByCode(String analysisCode) {
		dao.deleteByCode(analysisCode);
	}
	

	/**
	 * Acces à une instance globale de AnalysisAPI, qui permet de remplacer les appels
	 * à MongoDBDAO qui est une globale, dans un contexte ou on ne peut pas injecter. 
	 * Exemple dans objets du domaine (sra.Analysis, ...)
	 * @return AnalysisAPI
	 */
	public static AnalysisAPI get() {
		return IGGlobals.instanceOf(AnalysisAPI.class);
	}
	
}
