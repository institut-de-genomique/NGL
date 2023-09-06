package fr.cea.ig.ngl.dao.runs;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.mongojack.DBQuery;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APISemanticException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.GenericAPI;
import fr.cea.ig.ngl.dao.readsets.ReadSetsDAO;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.Valuation;
import models.laboratory.run.description.RunCategory;
import models.laboratory.run.instance.Run;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import ngl.refactoring.state.RunStateNames;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import workflows.run.RunWorkflows;

@Singleton
public class RunsAPI extends GenericAPI<RunsDAO, Run> {

	private static final String INVALID_RUN_OBJECT = "Invalid Run object";

	private static final play.Logger.ALogger logger = play.Logger.of(RunsAPI.class);

	private static final List<String> AUTHORIZED_UPDATE_FIELDS = Arrays.asList("keep", "deleted", "valuation.resolutionCodes", "valuation.criteriaCode", "valuation.comment");
	private static final List<String> DEFAULT_KEYS =  Arrays.asList("code", 
			"typeCode", 
			"sequencingStartDate", 
			"state", 
			"valuation",
			"lanes");
	private final RunWorkflows workflows;
	private final ReadSetsDAO readSetDao;

	@Inject
	public RunsAPI(RunsDAO dao, RunWorkflows workflows, ReadSetsDAO readSetDao) {
		super(dao);
		this.workflows  = workflows;  
		this.readSetDao = readSetDao;
	}

	@Override
	protected List<String> authorizedUpdateFields() {
		return AUTHORIZED_UPDATE_FIELDS;
	}

	@Override
	protected List<String> defaultKeys() {
		return DEFAULT_KEYS;
	}

	public Run create(Run input, String currentUser, Boolean external) throws APIValidationException, APISemanticException {
		if (input._id != null)
			throw new APISemanticException(CREATE_SEMANTIC_ERROR_MSG); 

		ContextValidation ctxVal = ContextValidation.createCreationContext(currentUser); 
		input.traceInformation = new TraceInformation();
		input.traceInformation.setTraceInformation(currentUser);
		//        input.setTraceCreationStamp(ctxVal, currentUser);

		if (input.state == null) input.state = new State();
		input.state.code = RunStateNames.N;
		input.state.user = currentUser;
		input.state.date = new Date();

		if (input.categoryCode == null && input.typeCode != null) 
			input.categoryCode = RunCategory.find.get().findByTypeCode(input.typeCode).code;

		//        if (external != null) {
		//        	ctxVal.putObject("external", external);
		//        } else {
		//        	ctxVal.putObject("external", false);
		//        }
		ctxVal.putObject("external", Boolean.TRUE.equals(external));
		input.validate(ctxVal);
		if (ctxVal.hasErrors()) 
			throw new APIValidationException(INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
		return dao.save(input);
	}

	@Override
	public Run update(Run input, String currentUser) throws APIException, APIValidationException {
		//        Run runInDB = get(input.code);
		//        if (runInDB == null)
		//            throw new APIException(dao.getElementClass().getSimpleName() + " with code " + input.code + " does not exist");
		if (!isObjectExist(input.code))
			throw new APIException(dao.getElementClass().getSimpleName() + " with code " + input.code + " does not exist");
		ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
		//        if (input.traceInformation != null) {
		//        	input.traceInformation.modificationStamp(ctxVal, currentUser);
		//        } else {
		//        	logger.warn("traceInformation is null !!");
		//        }
		input.setTraceModificationStamp(ctxVal, currentUser);
		input.validate(ctxVal);
		if (ctxVal.hasErrors()) 
			throw new APIValidationException(INVALID_RUN_OBJECT, ctxVal.getErrors());
		dao.updateObject(input);
		return input;
	}

	@Override
	public Run update(Run input, String currentUser, List<String> fields) throws APIException, APIValidationException {
		Run runInDB = get(input.code);
		if (runInDB == null)
			throw new APIException(dao.getElementClass().getSimpleName() + " with code " + input.code + " not exist");
		ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);     
		checkAuthorizedUpdateFields(ctxVal, fields);
		checkIfFieldsAreDefined(ctxVal, fields, input);
		if (ctxVal.hasErrors()) 
			throw new APIValidationException(INVALID_RUN_OBJECT, ctxVal.getErrors());
		TraceInformation ti = runInDB.traceInformation;
		ti.modificationStamp(ctxVal, currentUser);
		dao.updateObject(DBQuery.and(DBQuery.is("code", input.code)), dao.getBuilder(input, fields).set("traceInformation", ti));
		return get(input.code);
	}

	// NGL-3444
	public Run updateExternal(Run input, String currentUser) throws APIException, APIValidationException {
		if (!isObjectExist(input.code)) {
			throw new APIException(dao.getElementClass().getSimpleName() + " with code " + input.code + " does not exist");
		}
		ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
		ctxVal.putObject("external",true);
		input.setTraceModificationStamp(ctxVal, currentUser);
		input.validate(ctxVal);
		if (ctxVal.hasErrors()) {
			throw new APIValidationException(INVALID_RUN_OBJECT, ctxVal.getErrors());
		}
		dao.updateObject(input);
		return input;
	}

	public Run valuation(String code, Valuation valuation, String currentUser) throws APIException, APIValidationException {
		Run runInDB = get(code);
		if (runInDB == null) 
			throw new APIException(dao.getElementClass().getSimpleName() + " with code " + code + " not exist");
		ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
		manageValidation(runInDB, valuation, ctxVal);
		if (ctxVal.hasErrors())  
			throw new APIValidationException("Invalid valuation modification", ctxVal.getErrors());
		Run input = new Run();
		input.valuation = valuation;
		runInDB.setTraceModificationStamp(ctxVal, currentUser);
		dao.updateObject(DBQuery.and(DBQuery.is("code", code)), dao.getBuilder(input, Arrays.asList("valuation")).set("traceInformation", runInDB.traceInformation));
		runInDB = get(code);
		workflows.nextState(ctxVal, runInDB);
		if (ctxVal.hasErrors()) 
			throw new APIValidationException("Invalid state modification after valuation modification", ctxVal.getErrors());
		return get(code);
	}

	private void manageValidation(Run run, Valuation valuation, ContextValidation ctxVal) {
		if (run.valuation.valid == null ? valuation.valid != null : run.valuation.isntEquivalentOf(valuation)) {
			valuation.date = new Date();
			valuation.user = ctxVal.getUser();
			CommonValidationHelper.validateValuationRequired(ctxVal, run.typeCode, valuation);
		}
	}

	@Override
	public void delete(String code) throws APIException {
		//        Run runInDB = get(code);
		//        if (runInDB == null)
		//            throw new APIException(dao.getElementClass().getSimpleName() + " with code " + code + " not exist");
		if (!isObjectExist(code))
			throw new APIException(dao.getElementClass().getSimpleName() + " with code " + code + " not exist");
		// Mise à jour du champ "processesToLaunchDate" qui permet de prendre les samples en compte dans le reporting data.
		Run run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, code);
		
		List<Sample> sampleList = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.in("code", run.sampleCodes)).toList();
		sampleList.forEach(s -> {
			s.setProcessesToLaunchDate(new Date());

			MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, s);
		});

		dao.deleteObject(code);
		logger.debug("delete readset(s) linked to the Run " + code);
		readSetDao.deleteObject(DBQuery.is("runCode", code));  

		// GA: delete analysis (not implemented yet)
	}

	public Run get(String code, Integer laneNumber, String treatmentCode) {
		return dao.findOne(DBQuery.and(DBQuery.is("code", code), 
				DBQuery.elemMatch("lanes", DBQuery.and(DBQuery.is("number", laneNumber),
						DBQuery.exists("treatments."+treatmentCode)))));
	}

	/**
	 * Call {@link #create(Run, String, Boolean)} with null as value for Boolean argument
	 * @see fr.cea.ig.ngl.dao.api.GenericAPI#create(fr.cea.ig.DBObject, java.lang.String)
	 */
	@Override
	public Run create(Run input, String currentUser) throws APIValidationException, APIException {
		return create(input, currentUser, null);
	}

	/**
	 * Shorthand for {@link #updateState(String, State, String)}.
	 * @param runCode                 run code
	 * @param newStateCode            new state code
	 * @param user                    user
	 * @return                        updated run
	 * @throws APIValidationException error
	 * @throws APIException           error
	 */
	public Run updateState(String runCode, String newStateCode, String user) throws APIValidationException, APIException {
		return updateState(runCode, new State(newStateCode, user), user);
	}

	public Run updateState(String code, State state, String currentUser) throws APIValidationException, APIException {
		Run run = get(code);
		if (run == null)
			throw new APIException(dao.getElementClass().getSimpleName() + " with code " + code + " not exist");
		state.date = new Date();
		state.user = currentUser;
		ContextValidation ctxVal = ContextValidation.createUndefinedContext(currentUser);
		workflows.setState(ctxVal, run, state);
		if (ctxVal.hasErrors()) 
			throw new APIValidationException(INVALID_STATE_ERROR_MSG, ctxVal.getErrors());
		return get(code);
	}

}
