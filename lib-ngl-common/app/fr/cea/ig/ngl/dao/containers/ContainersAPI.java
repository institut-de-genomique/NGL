package fr.cea.ig.ngl.dao.containers;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.mongojack.DBQuery;

import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APISemanticException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.GenericAPI;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.LocationOnContainerSupport;
import models.utils.InstanceHelpers;
import ngl.refactoring.state.ContainerStateNames;
import validation.ContextValidation;
import validation.container.instance.ContainerValidationHelper;
import workflows.container.ContWorkflows;

public class ContainersAPI extends GenericAPI<ContainersDAO, Container> {

//    private static final play.Logger.ALogger logger = play.Logger.of(ContainersAPI.class); 
	private static final List<String> AUTHORIZED_UPDATE_FIELDS = Arrays.asList("valuation",
																			   "state.resolutionCodes",
																			   "comments",
																			   "volume",
																			   "qualityControlResults",
																			   "quantity",
																			   "size",
																			   "concentration");
	
	private static final List<String> DEFAULT_KEYS = Arrays.asList("code",
																   "importTypeCode",
																   "categoryCode",
																   "state",
																   "valuation",
																   "traceInformation",
																   "properties",
																   "comments",
																   "support",
																   "contents",
																   "volume",
																   "concentration",
																   "quantity",
																   "size",
																   "projectCodes",
																   "sampleCodes",
																   "fromTransformationTypeCodes",
																   "processTypeCodes");
	private final ContWorkflows workflows;
	
	@Inject
	public ContainersAPI(ContainersDAO dao, ContWorkflows workflows) {
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

//	@Override
//	public Container create(Container input, String currentUser) throws APIValidationException, APISemanticException {
////		ContextValidation ctxVal = new ContextValidation(currentUser); 
////		ctxVal.setCreationMode();
//		ContextValidation ctxVal = ContextValidation.createCreationContext(currentUser); 
//		if (input.code != null && !dao.isObjectExist(input.code)) { 
//			input.traceInformation = new TraceInformation();
//			input.traceInformation.creationStamp(ctxVal, currentUser);
////			input.setTraceCreationStamp(ctxVal, currentUser);
//			if (input.state == null)
//				input.state = new State();
//			input.state.code = ContainerStateNames.N;
//			input.state.user = currentUser;
//			input.state.date = new Date();		
//			
//		} else {
//			throw new APISemanticException(CREATE_SEMANTIC_ERROR_MSG); 
//		}
////		input.validate(ctxVal);
//		input.validate(ctxVal, null, null);
//		if (!ctxVal.hasErrors()) {
//			return dao.saveObject(input);
//		} else {
//			throw new APIValidationException(INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
//		}
//	}
	@Override
	public Container create(Container input, String currentUser) throws APIValidationException, APISemanticException {
		ContextValidation ctxVal = ContextValidation.createCreationContext(currentUser); 
		if (input.code == null)
			throw new APISemanticException(CREATE_SEMANTIC_ERROR_MSG);
		if (dao.isObjectExist(input.code))
			throw new APISemanticException(CREATE_SEMANTIC_ERROR_MSG); 
		input.traceInformation = new TraceInformation();
		input.traceInformation.creationStamp(ctxVal, currentUser);
//			input.setTraceCreationStamp(ctxVal, currentUser);
		if (input.state == null)
			input.state = new State();
		input.state.code = ContainerStateNames.N;
		input.state.user = currentUser;
		input.state.date = new Date();					
//		input.validate(ctxVal);
		input.validate(ctxVal, null, null);
		if (ctxVal.hasErrors())
			throw new APIValidationException(INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
		return dao.saveObject(input);
	}

	@Override
	public Container update(Container input, String currentUser) throws APIException, APIValidationException {
//		Container containerInDb = get(input.code);
//		if (containerInDb == null) {
//			throw new APIException("Container with code " + input.code + " not exist");
//		} else {
		if (! dao.isCodeExist(input.code))
			throw new APIException("Container with code " + input.code + " does not exist");
//			ContextValidation ctxVal = new ContextValidation(currentUser);
//			ctxVal.setUpdateMode();
		ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
//			if (input.traceInformation != null) {
//				input.traceInformation.modificationStamp(ctxVal, currentUser);
//			} else {
//				logger.error("traceInformation is null !!");
//			}
		input.setTraceModificationStamp(ctxVal, currentUser);
//			ctxVal.setUpdateMode();
		input.comments = InstanceHelpers.updateComments(ctxVal, input.comments);
		cleanProperties(input);
//			input.validate(ctxVal);
//			input.validate(ctxVal, null, null);
//			if (!ctxVal.hasErrors()) {
//				dao.updateObject(input);
//				return get(input.code);
//			} else {
//				throw new APIValidationException("Invalid Container object", ctxVal.getErrors());
//			}
		input.validate(ctxVal, null, null);
		if (ctxVal.hasErrors()) 
			throw new APIValidationException("Invalid Container object", ctxVal.getErrors());
		dao.updateObject(input);
		return get(input.code);
//		}
	}

//	@Override
//	public Container update(Container input, String currentUser, List<String> fields) throws APIException, APIValidationException {
//		Container containerInDb = get(input.code);
//		if (containerInDb == null) {
//			throw new APIException("Container with code " + input.code + " not exist");
//		} else {
////			ContextValidation ctxVal = new ContextValidation(currentUser);
////			ctxVal.setUpdateMode();
//			ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
//			checkAuthorizedUpdateFields(ctxVal, fields);
//			checkIfFieldsAreDefined(ctxVal, fields, input);
//			if (!ctxVal.hasErrors()) {
//				input.comments = InstanceHelpers.updateComments(ctxVal, input.comments);
//				TraceInformation ti = containerInDb.traceInformation;
//				ti.modificationStamp(ctxVal, currentUser);
//				if(fields.contains("valuation")){
//					input.valuation.user = currentUser;
//					input.valuation.date = new Date();
//				}
//				
//				if (fields.contains("volume"))        ContainerValidationHelper.validateVolumeOptional       (ctxVal, input.volume);					
//				if (fields.contains("quantity"))	  ContainerValidationHelper.validateQuantityOptional     (ctxVal, input.quantity);
//				if (fields.contains("size"))          ContainerValidationHelper.validateSizeOptional         (ctxVal, input.size);
//				if (fields.contains("concentration")) ContainerValidationHelper.validateConcentrationOptional(ctxVal, input.concentration);					
//
//				if (!ctxVal.hasErrors()) {
//					dao.updateObject(DBQuery.and(DBQuery.is("code", input.code)), dao.getBuilder(input, fields).set("traceInformation", ti));
//					return get(input.code);
//				} else {
//					throw new APIValidationException("Invalid fields", ctxVal.getErrors());
//				}
//			} else {
//				throw new APIValidationException("Invalid Container object", ctxVal.getErrors());
//			}
//		}
//	}
	@Override
	public Container update(Container input, String currentUser, List<String> fields) throws APIException, APIValidationException {
		Container containerInDb = get(input.code);
		if (containerInDb == null)
			throw new APIException("Container with code " + input.code + " not exist");
		ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
		checkAuthorizedUpdateFields(ctxVal, fields);
		checkIfFieldsAreDefined(ctxVal, fields, input);
		if (ctxVal.hasErrors())
			throw new APIValidationException("Invalid Container object", ctxVal.getErrors());
		input.comments = InstanceHelpers.updateComments(ctxVal, input.comments);
		TraceInformation ti = containerInDb.traceInformation;
		ti.modificationStamp(ctxVal, currentUser);
		if (fields.contains("valuation")) {
			input.valuation.user = currentUser;
			input.valuation.date = new Date();
		}

		if (fields.contains("volume"))        ContainerValidationHelper.validateVolumeOptional       (ctxVal, input.volume);					
		if (fields.contains("quantity"))	  ContainerValidationHelper.validateQuantityOptional     (ctxVal, input.quantity);
		if (fields.contains("size"))          ContainerValidationHelper.validateSizeOptional         (ctxVal, input.size);
		if (fields.contains("concentration")) ContainerValidationHelper.validateConcentrationOptional(ctxVal, input.concentration);					

		if (ctxVal.hasErrors()) 
			throw new APIValidationException("Invalid fields", ctxVal.getErrors());
		dao.updateObject(DBQuery.and(DBQuery.is("code", input.code)), dao.getBuilder(input, fields).set("traceInformation", ti));
		return get(input.code);
	}
	
	/**
	 * Shorthand for {@link #updateState(String, State, String)}.
	 * @param containerCode           container code
	 * @param newStateCode            new state code
	 * @param user                    user
	 * @return                        update container
	 * @throws APIException           error
	 * @throws APIValidationException error
	 */
	public Container updateState(String containerCode, String newStateCode, String user) throws APIException, APIValidationException {
		return updateState(containerCode, new State(newStateCode, user), user);
	}
	
	public Container updateState(String code, State state, String currentUser) throws APIException, APIValidationException {
		Container containerInDb = get(code);
		if (containerInDb == null) 
			throw new APIException("Container with code " + code + " not exist");
		ContextValidation ctxVal = ContextValidation.createUndefinedContext(currentUser);
		ctxVal.putObject(ContainerValidationHelper.FIELD_STATE_CONTAINER_CONTEXT,        "controllers");
		ctxVal.putObject(ContainerValidationHelper.FIELD_UPDATE_CONTAINER_SUPPORT_STATE, Boolean.TRUE);		
//			workflows.setState(ctxVal, containerInDb, state);
		workflows.setState(ctxVal, containerInDb, state, "controllers", true);
//		if (!ctxVal.hasErrors()) {
//			return get(code);
//		} else {
//			throw new APIValidationException(INVALID_STATE_ERROR_MSG, ctxVal.getErrors());
//		}
		if (ctxVal.hasErrors())
			throw new APIValidationException(INVALID_STATE_ERROR_MSG, ctxVal.getErrors());
		return get(code);
	}
	
	private PropertySingleValue emptyAsNull(PropertySingleValue p) {
		if (p == null)
			return null;
		if (p.value == null)
			return null;
		return p;
	}
	
	private void cleanProperties(Container input) {
		input.volume        = emptyAsNull(input.volume);
		input.concentration = emptyAsNull(input.concentration);
		input.size          = emptyAsNull(input.size);
		input.quantity      = emptyAsNull(input.quantity);
	}
	
	protected void updateStorageCode(String containerSupportCode, String storageCode, TraceInformation ti) {
		List<String> fields = Arrays.asList("storageCode");
		Container container = new Container();
		container.support = new LocationOnContainerSupport();
		container.support.storageCode = storageCode;
		dao.updateObject(DBQuery.and(DBQuery.is("support.code", containerSupportCode)), dao.getBuilder(container, fields, "support").set("traceInformation", ti));
	}
	
}
