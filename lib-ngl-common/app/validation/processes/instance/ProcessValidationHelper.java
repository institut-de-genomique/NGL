package validation.processes.instance;

import static ngl.refactoring.state.ProcessStateNames.IW_C;
import static ngl.refactoring.state.ProcessStateNames.N;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult.Sort;
import models.laboratory.common.description.ObjectType;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.processes.description.ProcessCategory;
import models.laboratory.processes.description.ProcessType;
import models.laboratory.processes.instance.Process;
import models.laboratory.processes.instance.SampleOnInputContainer;
import models.utils.InstanceConstants;
import ngl.refactoring.state.ProcessStateNames;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.utils.ValidationConstants;
import validation.utils.ValidationHelper;

public class ProcessValidationHelper {

	// ------------------------------------------------------------------
	// renamed and arguments reordered
	
	/**
	 * Validate a required process type code and associated properties.
	 * @param typeCode          process type code
	 * @param properties        properties
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateProcessTypeRequired(ContextValidation, String, Map)}
	 */
	@Deprecated
	public static void validateProcessType(String typeCode, Map<String, PropertyValue> properties, ContextValidation contextValidation) {
		ProcessValidationHelper.validateProcessTypeRequired(contextValidation, typeCode, properties);
	}
	
	/**
	 * Validate a required process type code and associated properties.
	 * @param contextValidation validation context
	 * @param typeCode          process type code
	 * @param properties        properties
	 */
	public static void validateProcessTypeRequired(ContextValidation contextValidation, String typeCode, Map<String, PropertyValue> properties) {
//		ProcessType processType = BusinessValidationHelper.validateRequiredDescriptionCode(contextValidation, typeCode, "typeCode", ProcessType.find.get(),true);
		ProcessType processType = CommonValidationHelper.validateCodeForeignRequired(contextValidation, ProcessType.miniFind.get(), typeCode, "typeCode", true);
		if (processType != null) {
			contextValidation.addKeyToRootKeyName("properties");
//			ValidationHelper.validateProperties(contextValidation, properties, processType.getPropertiesDefinitionDefaultLevel());
			ValidationHelper.validateProperties(contextValidation, properties, processType.getPropertiesDefinitionProcessLevel());
			contextValidation.removeKeyFromRootKeyName("properties");
		}
	}

	// -------------------------------------------------------------------
	// renamed and arguments reordered
	
	/**
	 * Validate a required process category code.
	 * @param categoryCode      process category code to validate
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateProcessCategoryCodeRequired(ContextValidation, String)}
	 */
	@Deprecated
	public static void validateProcessCategory(String categoryCode, ContextValidation contextValidation) {
		ProcessValidationHelper.validateProcessCategoryCodeRequired(contextValidation, categoryCode);
	}

	/**
	 * Validate a required process category code.
	 * @param contextValidation validation context
	 * @param categoryCode      process category code to validate
	 */
	public static void validateProcessCategoryCodeRequired(ContextValidation contextValidation, String categoryCode) {
		CommonValidationHelper.validateCodeForeignRequired(contextValidation, ProcessCategory.miniFind.get(), categoryCode, "categoryCode");
	}

	// -------------------------------------------------------------------
	// renamed and arguments reordered

	/**
	 * Validate an optional experiment type code as property 
	 * named 'currentExperimentTypeCode'. 
	 * @param currentExperimentTypeCode experiment type code to validate 
	 * @param contextValidation         validation context
	 * @deprecated {@link #validateCurrentExperimentTypeCodeOptional(ContextValidation, String)}
	 */
	@Deprecated
	public static void validateCurrentExperimentTypeCode_(String currentExperimentTypeCode, ContextValidation contextValidation) {
		ProcessValidationHelper.validateCurrentExperimentTypeCodeOptional(contextValidation, currentExperimentTypeCode);
	}

	/**
	 * Validate an optional experiment type code as property 
	 * named 'currentExperimentTypeCode'. 
	 * @param contextValidation         validation context
	 * @param currentExperimentTypeCode experiment type code to validate 
	 */
	public static void validateCurrentExperimentTypeCodeOptional(ContextValidation contextValidation, String currentExperimentTypeCode) {
		CommonValidationHelper.validateCodeForeignOptional(contextValidation, ExperimentType.miniFind.get(), currentExperimentTypeCode, "currentExperimentTypeCode");
	}

	// -------------------------------------------------------------------

	public static void validateExperimentCodes(List<String> experimentCodes, ContextValidation contextValidation) {
		if (CollectionUtils.isNotEmpty(experimentCodes)) 
			for (String expCode : experimentCodes) 
				CommonValidationHelper.validateExperimentCodeOptional(contextValidation, expCode);
	}
	
	public static void validateStateCode(String stateCode, ContextValidation contextValidation) {
//		contextValidation.addKeyToRootKeyName("state");
//		CommonValidationHelper.validateStateCodeRequired(contextValidation, ObjectType.CODE.Process, stateCode);
//		contextValidation.removeKeyFromRootKeyName("state");
		CommonValidationHelper.validateStateCodeRequired(contextValidation.appendPath("state"), ObjectType.CODE.Process, stateCode);
	}
	
	// -------------------------------------------------------------------------
	// arguments reordered
	
	/**
	 * Validate sample on input container ({@link SampleOnInputContainer})
	 * (context parameter {@link CommonValidationHelper#FIELD_STATE_CODE}). 
	 * @param soic              object to validate
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateSampleOnInputContainer(ContextValidation, SampleOnInputContainer)}
	 */
	@Deprecated
	public static void validateSampleOnInputContainer(SampleOnInputContainer soic, ContextValidation contextValidation) {
		ProcessValidationHelper.validateSampleOnInputContainer(contextValidation, soic);
	}

	/**
	 * Validate sample on input container ({@link SampleOnInputContainer})
	 * (context parameter {@link CommonValidationHelper#FIELD_STATE_CODE}). 
	 * @param contextValidation validation context
	 * @param soic              object to validate
	 * @deprecated use {@link #validateSampleOnInputContainer(ContextValidation, SampleOnInputContainer, String)}
	 */
	@Deprecated
	public static void validateSampleOnInputContainer(ContextValidation contextValidation, SampleOnInputContainer soic ) {				
////		String stateCode = getObjectFromContext(FIELD_STATE_CODE, String.class, contextValidation);
//		String stateCode = contextValidation.getTypedObject(FIELD_STATE_CODE);		
////		if (!"IW-C".equals(stateCode) && ValidationHelper.validateNotEmpty(contextValidation, soic, "sampleOnInputContainer")) {		
//		if (! IW_C.equals(stateCode) && ValidationHelper.validateNotEmpty(contextValidation, soic, "sampleOnInputContainer")) {		
//			contextValidation.addKeyToRootKeyName("sampleOnInputContainer");
//			soic.validate(contextValidation);
//			contextValidation.removeKeyFromRootKeyName("sampleOnInputContainer");
//		}
		String stateCode = contextValidation.getTypedObject(CommonValidationHelper.FIELD_STATE_CODE);
		validateSampleOnInputContainer(contextValidation, soic, stateCode);
	}
	
	/**
	 * Validate sample on input container ({@link SampleOnInputContainer}). 
	 * @param contextValidation validation context
	 * @param soic              object to validate
	 * @param stateCode         process state code
	 */
	public static void validateSampleOnInputContainer(ContextValidation contextValidation, SampleOnInputContainer soic, String stateCode) {				
		if (! IW_C.equals(stateCode) && ValidationHelper.validateNotEmpty(contextValidation, soic, "sampleOnInputContainer")) {	
			contextValidation.addKeyToRootKeyName("sampleOnInputContainer");
			soic.validate(contextValidation);
			contextValidation.removeKeyFromRootKeyName("sampleOnInputContainer");
		}
	}
	
	// -------------------------------------------------------------------------
	// arguments reordered
	
	/**
	 * Validate container support code (foreign key only for state {@link ProcessStateNames#IW_C})
	 * (context parameter {@link CommonValidationHelper#FIELD_STATE_CODE}).
	 * @param containerSupportCode code to validate
	 * @param contextValidation    validation context
	 * @param propertyName         property name
	 * @deprecated use {@link #validateContainerSupportCode(ContextValidation, String, String)}
	 */
	@Deprecated
	public static void validateContainerSupportCode_(String containerSupportCode, ContextValidation contextValidation, String propertyName) {
		ProcessValidationHelper.validateContainerSupportCode(contextValidation, containerSupportCode, propertyName);
	}

	/**
	 * Validate container support code (foreign key only for state {@link ProcessStateNames#IW_C})
	 * (context parameter {@link CommonValidationHelper#FIELD_STATE_CODE}).
	 * @param contextValidation    validation context
	 * @param containerSupportCode code to validate
	 * @param propertyName         property name
	 * @deprecated use {@link #validateContainerSupportCode(ContextValidation, String, String, String)}
	 */
	@Deprecated
	public static void validateContainerSupportCode(ContextValidation contextValidation, String containerSupportCode, String propertyName) {
////		String stateCode = getObjectFromContext(FIELD_STATE_CODE, String.class, contextValidation);
//		String stateCode = contextValidation.getTypedObject(FIELD_STATE_CODE);
////		if (!"IW-C".equals(stateCode)) {
//		if (! IW_C.equals(stateCode)) {
////			BusinessValidationHelper.validateRequiredInstanceCode(contextValidation, containerSupportCode, propertyName, ContainerSupport.class,InstanceConstants.CONTAINER_SUPPORT_COLL_NAME);
////			validateRequiredInstanceCode(containerSupportCode, propertyName, ContainerSupport.class, InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, contextValidation);
//			validateCodeForeignRequired(contextValidation, ContainerSupport.find.get(), containerSupportCode, propertyName);
//		}
		String stateCode = contextValidation.getTypedObject(CommonValidationHelper.FIELD_STATE_CODE);
		validateContainerSupportCode(contextValidation, containerSupportCode, propertyName, stateCode);
	}
	
	/**
	 * Validate container support code (foreign key only for state {@link ProcessStateNames#IW_C}, 
	 * see {@link CommonValidationHelper#validateContainerSupportCodeRequired(ContextValidation, String, String)}).
	 * @param contextValidation    validation context
	 * @param containerSupportCode code to validate
	 * @param propertyName         property name
	 * @param stateCode            process state code
	 */
	public static void validateContainerSupportCode(ContextValidation contextValidation, String containerSupportCode, String propertyName, String stateCode) {
		if (! IW_C.equals(stateCode))
			CommonValidationHelper.validateCodeForeignRequired(contextValidation, ContainerSupport.find.get(), containerSupportCode, propertyName);
	}
	
	// ---------------------------------------------------------------------------
	// This should be in the container validation helper.
	// renamed and arguments reordered
	
	/**
	 * Validate a required container code (context parameter {@link CommonValidationHelper#FIELD_STATE_CODE}).
	 * @param containerCode     code of container
	 * @param contextValidation validation context
	 * @param propertyName      property name
	 * @deprecated use {@link #validateContainerCodeRequired(ContextValidation, String, String)}
	 */
	@Deprecated
	public static void validateContainerCode_(String containerCode, ContextValidation contextValidation, String propertyName) {
		ProcessValidationHelper.validateContainerCodeRequired(contextValidation, containerCode, propertyName);
	}

	/**
	 * Validate a required container code (context parameter {@link CommonValidationHelper#FIELD_STATE_CODE}).
	 * @param contextValidation validation context
	 * @param containerCode     code of container
	 * @param propertyName      property name
	 * @deprecated use {@link #validateContainerCodeRequired(ContextValidation, String, String, String)}
	 */
	@Deprecated
	public static void validateContainerCodeRequired(ContextValidation contextValidation, String containerCode, String propertyName) {
////		String stateCode = getObjectFromContext(FIELD_STATE_CODE, String.class, contextValidation);
//		String stateCode = contextValidation.getTypedObject(FIELD_STATE_CODE);		
////		if ("N".equals(stateCode) && contextValidation.isCreationMode()) {
//		if (N.equals(stateCode) && contextValidation.isCreationMode()) {
////			Container c = BusinessValidationHelper.validateRequiredInstanceCode(contextValidation, containerCode, propertyName, Container.class,InstanceConstants.CONTAINER_COLL_NAME, true);
////			Container c = validateRequiredInstanceCode(containerCode, propertyName, Container.class,InstanceConstants.CONTAINER_COLL_NAME, contextValidation, true);
//			Container c = validateCodeForeignRequired(contextValidation, Container.find.get(), containerCode, propertyName, true);
//			if (c != null && !"IW-P".equals(c.state.code)) {
//				contextValidation.addError("inputContainerCode", ValidationConstants.ERROR_BADSTATE_MSG, c.state.code);
//			}
////		} else if ("IW-C".equals(stateCode) && contextValidation.isUpdateMode() && containerCode != null) {
//		} else if (IW_C.equals(stateCode) && contextValidation.isUpdateMode() && containerCode != null) {
////			Container c = BusinessValidationHelper.validateRequiredInstanceCode(contextValidation, containerCode, propertyName, Container.class,InstanceConstants.CONTAINER_COLL_NAME, true);
////			Container c = validateRequiredInstanceCode(containerCode, propertyName, Container.class,InstanceConstants.CONTAINER_COLL_NAME, contextValidation, true);
//			Container c = validateCodeForeignRequired(contextValidation, Container.find.get(), containerCode, propertyName, true);
//			if (c != null && !"IW-P".equals(c.state.code)) {
//				contextValidation.addError("inputContainerCode", ValidationConstants.ERROR_BADSTATE_MSG, c.state.code);
//			}
////		} else if (!"IW-C".equals(stateCode)) {
//		} else if (! IW_C.equals(stateCode)) {
////			Container c = 
////					BusinessValidationHelper.validateRequiredInstanceCode(contextValidation, containerCode, propertyName, Container.class,InstanceConstants.CONTAINER_COLL_NAME, true);			
////			validateRequiredInstanceCode(containerCode, propertyName, Container.class,InstanceConstants.CONTAINER_COLL_NAME, contextValidation, true);
//			validateCodeForeignRequired(contextValidation, Container.find.get(), containerCode, propertyName, true);
//		}
		String stateCode = contextValidation.getTypedObject(CommonValidationHelper.FIELD_STATE_CODE);		
		validateContainerCodeRequired(contextValidation, containerCode, propertyName, stateCode);
	}
	
	/**
	 * Validate a required container code.
	 * @param contextValidation validation context
	 * @param containerCode     code of container
	 * @param propertyName      property name
	 * @param stateCode         process state code
	 */
	public static void validateContainerCodeRequired(ContextValidation contextValidation, String containerCode, String propertyName, String stateCode) {
		if (N.equals(stateCode) && contextValidation.isCreationMode()) {
			Container c = CommonValidationHelper.validateCodeForeignRequired(contextValidation, Container.find.get(), containerCode, propertyName, true);
			if (c != null && ! "IW-P".equals(c.state.code)) {
				contextValidation.addError("inputContainerCode", ValidationConstants.ERROR_BADSTATE_MSG, c.state.code);
			}
		} else if (IW_C.equals(stateCode) && contextValidation.isUpdateMode() && containerCode != null) {
			Container c = CommonValidationHelper.validateCodeForeignRequired(contextValidation, Container.find.get(), containerCode, propertyName, true);
			if (c != null && ! "IW-P".equals(c.state.code)) {
				contextValidation.addError("inputContainerCode", ValidationConstants.ERROR_BADSTATE_MSG, c.state.code);
			}
		} else if (! IW_C.equals(stateCode)) {
			CommonValidationHelper.validateCodeForeignRequired(contextValidation, Container.find.get(), containerCode, propertyName, true);
		}
	}
	
	// -------------------------------------------------------------------------------
	
	public static void validateNextState(Process process, State nextState, ContextValidation contextValidation) {
		CommonValidationHelper.validateStateRequired(contextValidation, ObjectType.CODE.Process, nextState);
		if (!contextValidation.hasErrors() && !nextState.code.equals(process.state.code)) {
			String nextStateCode = nextState.code;
			String currentStateCode = process.state.code;
			contextValidation.addKeyToRootKeyName("state");
			if ("IW-C".equals(currentStateCode)	&& (!nextStateCode.equals("N") || (nextStateCode.equals("N") && process.inputContainerCode == null))) {
//				contextValidation.addErrors("code",ValidationConstants.ERROR_BADSTATE_MSG, nextStateCode );
				contextValidation.addError("code",ValidationConstants.ERROR_BADSTATE_MSG, nextStateCode );
			} else if("N".equals(currentStateCode) 
					&& nextStateCode.equals("IW-C") && process.inputContainerCode != null){
				Container container = MongoDBDAO.findOne(InstanceConstants.CONTAINER_COLL_NAME, Container.class,DBQuery.is("code", process.inputContainerCode));
				if(!Arrays.asList("UA","IS","IW-P").contains(container.state.code)){
					contextValidation.addError("inputContainerCode."+container.code, ValidationConstants.ERROR_BADSTATE_MSG, container.state.code);
				}else if(process.experimentCodes != null && process.experimentCodes.size() > 0){
					contextValidation.addError("code",ValidationConstants.ERROR_BADSTATE_MSG, nextStateCode );
				}
			} else if ("IP".equals(currentStateCode) &&	!nextStateCode.equals("F")) {
				contextValidation.addError("code",ValidationConstants.ERROR_BADSTATE_MSG, nextStateCode );			
			} else if ("F".equals(currentStateCode) && !nextStateCode.equals("F")) {
				contextValidation.addError("code",ValidationConstants.ERROR_BADSTATE_MSG, nextStateCode );			
			} else if ("F".equals(nextStateCode) && process.outputContainerCodes != null && process.outputContainerCodes.size() > 0) {
				Container container = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,DBQuery.in("code", process.outputContainerCodes))
					.sort("traceInformation.creationDate", Sort.DESC).limit(1).toList().get(0);
				if (!Arrays.asList("UA","IS","IW-P").contains(container.state.code)) {
					contextValidation.addError("outputContainerCodes."+container.code, ValidationConstants.ERROR_BADSTATE_MSG, container.state.code);
				}
			} else if("F".equals(nextStateCode)	&& (process.outputContainerCodes == null || process.outputContainerCodes.size() == 0)) {
				Container container = MongoDBDAO.findOne(InstanceConstants.CONTAINER_COLL_NAME, Container.class,DBQuery.is("code", process.inputContainerCode));
				if (!Arrays.asList("UA","IS","IW-P").contains(container.state.code)) {
					contextValidation.addError("inputContainerCode."+container.code, ValidationConstants.ERROR_BADSTATE_MSG, container.state.code);
				}
			}
			contextValidation.removeKeyFromRootKeyName("state");
		}	
	}

}
