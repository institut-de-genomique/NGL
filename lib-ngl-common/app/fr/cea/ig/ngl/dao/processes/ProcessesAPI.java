package fr.cea.ig.ngl.dao.processes;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;
import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APISemanticException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.GenericAPI;
import fr.cea.ig.ngl.dao.containers.ContainersDAO;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.container.instance.Container;
import models.laboratory.processes.instance.Process;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import models.utils.instance.ProcessHelper;
import ngl.refactoring.state.ContainerStateNames;
import ngl.refactoring.state.ProcessStateNames;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.utils.ValidationConstants;
import workflows.process.ProcWorkflows;

public class ProcessesAPI extends GenericAPI<ProcessesDAO, Process> {

	private static final play.Logger.ALogger logger = play.Logger.of(ProcessesAPI.class);
	
	private static final List<String> AUTHORIZED_UPDATE_FIELDS = Collections.unmodifiableList(Arrays.asList(
																	"state.resolutionCodes",
																	"comments"
																));
	
	private static final List<String> DEFAULT_KEYS = Collections.unmodifiableList(Arrays.asList("*"));

	private final ProcWorkflows workflows;
	private final ContainersDAO containerDao;

	@Inject
	public ProcessesAPI(ProcessesDAO dao, ContainersDAO containerDao, ProcWorkflows workflows) {
		super(dao);
		this.workflows = workflows;
		this.containerDao = containerDao;
	}

	@Override
	protected List<String> authorizedUpdateFields() {
		return AUTHORIZED_UPDATE_FIELDS;
	}

	@Override
	protected List<String> defaultKeys() {
		return DEFAULT_KEYS;
	}

	/**
	 * @throws APIException unsupported method for Process entities
	 * @see #createProcesses(Process, String, String)
	 */
	@Override
	public Process create(Process input, String currentUser) throws APIValidationException, APIException {
		throw new APIException("not supported");
	}

	@Override
	public Process update(Process input, String currentUser) throws APIException, APIValidationException {
		Process objectInDB = get(input.code);
		if (objectInDB == null) 
			throw new APIException("Process with code " + input.code + " does not exist");
		ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
		if (input.traceInformation != null) {
			input.traceInformation.modificationStamp(ctxVal, currentUser);
		} else {
			logger.error("traceInformation is null !!");
		}
		workflows.applyPreValidateCurrentStateRules(ctxVal, input);
		input.validateUpdate(ctxVal);	// no state code		
		if (ctxVal.hasErrors())
			throw new APIValidationException("Invalid Process object", ctxVal.getErrors());
		dao.updateObject(input);
		workflows.applyPostValidateCurrentStateRules(ctxVal, input);
		return get(input.code);
	}

	@Override
	public Process update(Process input, String currentUser, List<String> fields) throws APIException, APIValidationException {
		Process objectInDB = get(input.code);
		if (objectInDB == null) 
			throw new APIException("Process with code " + input.code + " does not exist");
		ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
		checkAuthorizedUpdateFields(ctxVal, fields);
		checkIfFieldsAreDefined(ctxVal, fields, input);
		if (ctxVal.hasErrors())
			throw new APIValidationException("Invalid Process object", ctxVal.getErrors());
		TraceInformation ti = objectInDB.traceInformation;
		ti.modificationStamp(ctxVal, currentUser);
		// check fields validations
		if (ctxVal.hasErrors()) 
			throw new APIValidationException("Invalid fields", ctxVal.getErrors());
		dao.updateObject(DBQuery.and(DBQuery.is("code", input.code)), dao.getBuilder(input, fields).set("traceInformation", ti));
		return get(input.code);
	}

	/**
	 * Create process and all required/linked processes.
	 * @param input 		          main process to create
	 * @param currentUser 	          current user
	 * @param from			          string to indicate creation procedure
	 * @return				          the list of processes created 
	 * @throws APIValidationException validation failure
	 * @throws APISemanticException   try to update the instance instead of creating new one
	 */
	@Deprecated
	public List<Process> createProcesses(Process input, String currentUser, String from) throws APIValidationException, APISemanticException {
		//	    ContextValidation ctxVal = new ContextValidation(currentUser);
		//		ctxVal.setCreationMode();
		ContextValidation ctxVal = ContextValidation.createCreationContext(currentUser);
		if (input._id == null) {
			input.traceInformation = new TraceInformation();
			input.traceInformation.creationStamp(ctxVal, currentUser);
		} else {
			throw new APISemanticException(CREATE_SEMANTIC_ERROR_MSG);
		}
		//		ctxVal.setCreationMode();
		ctxVal.putObject(CommonValidationHelper.FIELD_PROCESS_CREATION_CONTEXT, CommonValidationHelper.VALUE_PROCESS_CREATION_CONTEXT_COMMON);
		//		input.validate(ctxVal); // no state code
		input.validateCreationCommon(ctxVal); // no state code

		if (!ctxVal.hasErrors()) {
			List<Process> processes = ProcessHelper.getNewProcessList(ctxVal, input, from);
			if (processes.size() > 0) {
				processes = ProcessHelper.applyRules(processes, ctxVal, "processCreation");
			}
			ctxVal.putObject(CommonValidationHelper.FIELD_PROCESS_CREATION_CONTEXT, CommonValidationHelper.VALUE_PROCESS_CREATION_CONTEXT_SPECIFIC);
			//			processes.stream().forEach(p -> p.validate(ctxVal)); // no state code
			processes.stream().forEach(p -> p.validateCreationSpecific(ctxVal)); // no state code
			if (!ctxVal.hasErrors()) {
				processes = processes.parallelStream()
						.map(p -> {
							Process newP = dao.saveObject(p);
							workflows.applySuccessPostStateRules(ctxVal, newP);
							return newP;							
						})
						.collect(Collectors.toList());
				return processes;
			} else {
				throw new APIValidationException("Invalid processes from NewProcessList", ctxVal.getErrors());
			}	
		} else {
			throw new APIValidationException(INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
		}
	}

	public List<Process> createProcessesFromSample(Process input, String currentUser) throws APIValidationException, APISemanticException {
		return createProcesses(input, currentUser, ProcessHelper::newProcessListFromSample);
	}

	public List<Process> createProcessesFromContainer(Process input, String currentUser) throws APIValidationException, APISemanticException {
		return createProcesses(input, currentUser, ProcessHelper::newProcessListFromContainer);
	}

	/**
	 * Create process and all required/linked processes.
	 * @param input 		          main process to create
	 * @param currentUser 	          current user
	 * @param from			          linked process creation function
	 * @return				          the list of processes created 
	 * @throws APIValidationException validation failure
	 * @throws APISemanticException   try to update the instance instead of creating new one
	 */
	public List<Process> createProcesses(Process input, String currentUser, BiFunction<ContextValidation,Process,List<Process>> from) throws APIValidationException, APISemanticException {
		ContextValidation ctxVal = ContextValidation.createCreationContext(currentUser);
		if (input._id != null) 
			throw new APISemanticException(CREATE_SEMANTIC_ERROR_MSG);
		input.traceInformation = new TraceInformation();
		input.traceInformation.creationStamp(ctxVal, currentUser);
		input.validateCreationCommon(ctxVal);
		if (ctxVal.hasErrors()) 
			throw new APIValidationException(INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
		List<Process> processes = from.apply(ctxVal, input);
		if (processes.size() > 0)
			processes = ProcessHelper.applyRules(processes, ctxVal, "processCreation");
		processes.stream().forEach(p -> p.validateCreationSpecific(ctxVal)); // no state code
		if (ctxVal.hasErrors()) 
			throw new APIValidationException("Invalid processes from NewProcessList", ctxVal.getErrors());
		processes = processes.parallelStream()
		//processes = processes.stream()
					.map(p -> {
					Process newP = dao.saveObject(p);
					workflows.applySuccessPostStateRules(ctxVal, newP);
					return newP;							
				})
				.collect(Collectors.toList());
		return processes;
	}

	public Process updateState(String code, State state, String currentUser) throws APIException, APIValidationException {
		Process objectInDB = get(code);
		if (objectInDB == null) 
			throw new APIException("Process with code " + code + " does not exist");
		state.date = new Date();
		ContextValidation ctxVal = ContextValidation.createUndefinedContext(currentUser);
		workflows.setState(ctxVal, objectInDB, state);
		if (ctxVal.hasErrors()) 
			throw new APIValidationException(INVALID_STATE_ERROR_MSG, ctxVal.getErrors());
		return get(code);
	}

	// GA: review avec GA et ancien code pour vérification (surtout autour du container null ou pas)
	public void delete(String code, String currentUser) throws APIException, APIValidationException { 
		Process process = get(code);
		if (process == null) 
			throw new APIException("Process with code " + code + " not exist");
		Container container = null;
		if (process.inputContainerCode != null && !process.state.code.equals(ProcessStateNames.IW_C)) {
			container = containerDao.findByCode(process.inputContainerCode);
			if (container == null) 
				throw new APIException("Container process " + code + "with code " + process.inputContainerCode + " does not exist");
			ContextValidation ctxVal = ContextValidation.createUndefinedContext(currentUser);
			if (process.state.code.equals(ProcessStateNames.IP)) {
				ctxVal.addError("process.state.code", ValidationConstants.ERROR_BADSTATE_MSG, container.code);
			} else if (CollectionUtils.isNotEmpty(process.experimentCodes)) {
				ctxVal.addError("process.experimentCodes", ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG, process.experimentCodes);
			} else if (! ContainerStateNames.IS.equals(container.state.code) &&
					! ContainerStateNames.UA.equals(container.state.code) &&
					! ContainerStateNames.IW_P.equals(container.state.code)) {
				ctxVal.addError("process.inputContainerCode", ValidationConstants.ERROR_BADSTATE_MSG, container.state.code);
			}
			if (!ctxVal.hasErrors()) {
				super.delete(code);

				updateSampleLaunchDate(process);
			} else
				throw new APIValidationException("error during deleting the process", ctxVal.getErrors());
		} else if(process.state.code.equals(ProcessStateNames.IW_C) && process.inputContainerCode == null) {
			super.delete(code);

			updateSampleLaunchDate(process);
		} else { // GA: review
			throw new APIException("cannot delete process: " + code);
		}
	}

	private void updateSampleLaunchDate(Process process) {
		// Mise à jour du champ "processesToLaunchDate" qui permet de prendre le sample en compte dans le reporting data.
		List<Sample> sampleList = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.in("code", process.sampleCodes)).toList();
		sampleList.forEach(s -> {
			s.setProcessesToLaunchDate(new Date());

			MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, s);
		});
	}

}
