package fr.cea.ig.ngl.dao.experiments;

import static validation.common.instance.CommonValidationHelper.FIELD_STATE_CODE;
import static validation.experiment.instance.ExperimentValidationHelper.validateReagents;
import static validation.experiment.instance.ExperimentValidationHelper.validateStatusRequired;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.mongojack.DBQuery;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APISemanticException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.GenericAPI;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.sample.instance.Sample;
import models.utils.CodeHelper;
import models.utils.InstanceConstants;
import models.utils.instance.ExperimentHelper;
import ngl.refactoring.state.ExperimentStateNames;
import play.Logger.ALogger;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import workflows.experiment.ExpWorkflows;

public class ExperimentsAPI extends GenericAPI<ExperimentsDAO, Experiment> {

	private static final ALogger logger = play.Logger.of(ExperimentsAPI.class);
			
    private final static List<String> DEFAULT_KEYS             = Arrays.asList("categoryCode",
                                                                               "code",
                                                                               "inputContainerSupportCodes",
                                                                               "instrument",
                                                                               "outputContainerSupportCodes",
                                                                               "projectCodes",
                                                                               "protocolCode",
                                                                               "reagents",
                                                                               "sampleCodes",
                                                                               "state",
                                                                               "status",
                                                                               "traceInformation",
                                                                               "typeCode",
                                                                               "atomicTransfertMethods.inputContainerUseds.contents");
    private final static List<String> AUTHORIZED_UPDATE_FIELDS = Arrays.asList("status", "reagents", "state", "atomicTransfertMethods");
    public static final String        CALCULATION_RULES        = "calculations";
	
	private final ExpWorkflows workflows;
	
	@Inject
	public ExperimentsAPI(ExperimentsDAO dao, ExpWorkflows workflows) {
		super(dao);
		this.workflows = workflows;
	}

	@Override
	protected List<String> authorizedUpdateFields() {
		return AUTHORIZED_UPDATE_FIELDS;
	}

	@Override
	protected List<String> defaultKeys() {
		return DEFAULT_KEYS;
	}

	@Override
	public Experiment create(Experiment input, String currentUser) throws APIValidationException, APISemanticException {
		if (input._id != null)
			throw new APISemanticException(CREATE_SEMANTIC_ERROR_MSG);
		input.code             = CodeHelper.getInstance().generateExperimentCode(input);
		input.traceInformation = new TraceInformation();
		input.traceInformation.setTraceInformation(currentUser);
			
		if (input.state == null) input.state = new State();
			
		input.state.code = ExperimentStateNames.N;
		input.state.user = currentUser;
		input.state.date = new Date();
		
		ContextValidation ctxVal = ContextValidation.createCreationContext(currentUser);
		workflows.applyPreStateRules(ctxVal, input, input.state);
		ExperimentHelper.doCalculations(input, CALCULATION_RULES);
//		input.validate(ctxVal);	
		input.validate(ctxVal, null);	
		if (ctxVal.hasErrors()) {
			workflows.applyErrorPostStateRules(ctxVal, input, input.state);
			throw new APIValidationException(INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
		}
		input = dao.saveObject(input);
		workflows.applySuccessPostStateRules(ctxVal, input, false);
		return input;
	}

	@Override
	public Experiment update(Experiment input, String currentUser) throws APIException, APIValidationException {
		return update(input, currentUser, false);
	}
	
	/**
	 * 
	 * @param input 		experiment to update
	 * @param currentUser	current user
	 * @return 				the experiment updated
	 * @throws APIException 			if the code doesn't correspond to an object
	 * @throws APIValidationException 	validation failure
	 */
	public Experiment updateWithCascadingContentProperties(Experiment input, String currentUser) throws APIException, APIValidationException {
		return update(input, currentUser, true);
	}
	
	private Experiment update(Experiment input, String currentUser, boolean updateContentProperties) throws APIException, APIValidationException {
		if (! dao.isObjectExist(input.code))
			throw new APIException("Experiement with code " + input.code + " not exist");
		ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
		if (input.traceInformation != null) {
			input.traceInformation.modificationStamp(ctxVal, currentUser);
		} else {
			logger.error("traceInformation is null !!");
		}
//			ctxVal.setUpdateMode();
				// update in cascading contentProperties, only for administrator => verif permission au niveau du controller
//				if (queryFieldsForm.fields != null && queryFieldsForm.fields.contains("updateContentProperties")) {
//					ctxVal.putObject("updateContentProperties", Boolean.TRUE);
//				}
		if (updateContentProperties) 
			ctxVal.putObject("updateContentProperties", Boolean.TRUE); // enable update of content properties
		ExperimentHelper.doCalculations(input, CALCULATION_RULES);
		workflows.applyPreValidateCurrentStateRules(ctxVal, input);
//			input.validate(ctxVal);	
		input.validate(ctxVal, null);	

		if (ctxVal.hasErrors()) 	
			throw new APIValidationException("Invalid Experiment object", ctxVal.getErrors());
		workflows.applyPostValidateCurrentStateRules(ctxVal, input);
		dao.updateObject(input);
		return get(input.code);
	}

	@Override
	public Experiment update(Experiment input, String currentUser, List<String> fields) throws APIException, APIValidationException {
		Experiment expInDb = dao.getObject(input.code);
		if (expInDb == null)  
			throw new APIException("Experiement with code " + input.code + " not exist");
		ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
		checkAuthorizedUpdateFields(ctxVal, fields);
		checkIfFieldsAreDefined(ctxVal, fields, input);
		if (ctxVal.hasErrors())
			throw new APIValidationException("Invalid Experiment object", ctxVal.getErrors());
		TraceInformation ti = expInDb.traceInformation;
		ti.modificationStamp(ctxVal, currentUser);
		ctxVal.putObject(FIELD_STATE_CODE , expInDb.state.code);

		if (fields.contains("state")) {
			CommonValidationHelper.validateStateRequired(ctxVal, expInDb.typeCode, input.state);
			// ce n'est pas un changement de code mais uniquement de résolution (pas d'appel workflow) 
			// il faudrait ajouter une condition pour ne pas permettre le changement de code
//			if (!input.state.code.equals(expInDb.code))
//				ctxVal.addError("state", "cannot set state when updating");
		}

		if (fields.contains("status"))   validateStatusRequired(ctxVal, expInDb.typeCode, input.status);				
		if (fields.contains("reagents")) validateReagents      (ctxVal, expInDb.reagents);				
		if (ctxVal.hasErrors()) 
			throw new APIValidationException("Invalid fields", ctxVal.getErrors());
		dao.updateObject(DBQuery.and(DBQuery.is("code", input.code)), dao.getBuilder(input, fields).set("traceInformation", ti));
		return get(input.code);
	}

	/**
	 * Common case shorthand {@link #updateState(String, State, String)}.
	 * @param experimentCode          experiment code
	 * @param newStateCode            new state code
	 * @param user                    user
	 * @return                        updated experiment
	 * @throws APIException           error
	 * @throws APIValidationException error
	 */
	public Experiment updateState(String experimentCode, String newStateCode, String user) throws APIException, APIValidationException {
		return updateState(experimentCode,
				           new State(newStateCode, user),
				           user);
	}
	
	public Experiment updateState(String code, State state, String currentUser) throws APIException, APIValidationException {
		Experiment expInDb = get(code);
		if (expInDb == null) 
			throw new APIException("Experiment with code " + code + " not exist");
		ContextValidation ctxVal = ContextValidation.createUndefinedContext(currentUser);
		workflows.setState(ctxVal, expInDb, state);
		if (ctxVal.hasErrors()) 
			throw new APIValidationException(INVALID_STATE_ERROR_MSG, ctxVal.getErrors());
		return get(code);
	}

	public void delete(String code, String currentUser) throws APIException, APIValidationException {
		Experiment expInDb = get(code);
		if (expInDb == null)
			throw new APIException("Experiment with code " + code + " not exist");
		ContextValidation ctxVal = ContextValidation.createUndefinedContext(currentUser);
		workflows.delete(ctxVal, expInDb);
		if (ctxVal.hasErrors()) 
			throw new APIValidationException("Error during delete:", ctxVal.getErrors());
		else {
			// Mise à jour du champ "processesToLaunchDate" qui permet de prendre le sample en compte dans le reporting data.
			List<Sample> sampleList = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.in("code", expInDb.sampleCodes)).toList();
			sampleList.forEach(s -> {
				s.setProcessesToLaunchDate(new Date());

				MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, s);
			});
		}
		
	}
	
}
