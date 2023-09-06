package validation.experiment.instance;

import static models.utils.InstanceConstants.CONTAINER_COLL_NAME;
import static models.utils.InstanceConstants.CONTAINER_SUPPORT_COLL_NAME;
import static models.utils.InstanceConstants.EXPERIMENT_COLL_NAME;
import static validation.utils.ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG;

import java.util.List;
import java.util.Map;

import org.mongojack.DBQuery;
import fr.cea.ig.MongoDBDAO;

import models.laboratory.common.description.Level;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.container.description.ContainerCategory;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.container.instance.Content;
import models.laboratory.container.instance.LocationOnContainerSupport;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.InputContainerUsed;
import models.laboratory.instrument.description.InstrumentUsedType;
import models.laboratory.instrument.instance.InstrumentUsed;
import ngl.refactoring.state.StateNames;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.utils.ValidationHelper;

public class ContainerUsedValidationHelper extends CommonValidationHelper {

	// ----------------------------------------------------------------
	// renamed and arguments reordered
	
	/**
	 * Validate that an input container used matches a source container (content size, 
	 * {@link ContainerCategory}).
	 * @param inputContainer    input container used to validate
	 * @param container         source container
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateInputContainerMatchesContainer(ContextValidation, InputContainerUsed, Container)}
	 */
	@Deprecated
	public static void compareInputContainerWithContainer_(InputContainerUsed inputContainer, Container container, ContextValidation contextValidation) {
		ContainerUsedValidationHelper.validateInputContainerMatchesContainer(contextValidation, inputContainer, container);
	}

	/**
	 * Validate that an input container used matches a source container (content size, 
	 * {@link ContainerCategory}).
	 * @param contextValidation validation context
	 * @param inputContainer    input container used to validate
	 * @param container         source container
	 */
	public static void validateInputContainerMatchesContainer(ContextValidation contextValidation,
				                                              InputContainerUsed inputContainer, 
				                                              Container container) {
		
		if (!inputContainer.categoryCode.equals(container.categoryCode)) {
			contextValidation.addError("categoryCode", "error.validationexp.inputContainer.categoryCode.notequals", inputContainer.code);
		}
		if (inputContainer.contents.size() != container.contents.size()) {
			contextValidation.addError("categoryCode", "error.validationexp.inputContainer.contents.sizenotequals", inputContainer.code);
		}
		// String stateCode = getObjectFromContext(FIELD_STATE_CODE, String.class, contextValidation);
		if (contextValidation.isCreationMode() && !container.state.code.startsWith("A")) {
			contextValidation.addError("state.code", "error.validationexp.inputContainer.state.code", inputContainer.code);
		}
		// GA: improve comparison
	}
	
	// -----------------------------------------------------------------
	
	/**
	 * Validate experiment properties using a fixed state {@link StateNames#IP}.
	 * @param properties        properties to validate
	 * @param level             properties level
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateExperimentProperties(ContextValidation, Map, Level.CODE)}
	 */
	@Deprecated
	public static void validateExperimentProperties(Map<String, PropertyValue> properties, Level.CODE level,	ContextValidation contextValidation) {
		ContainerUsedValidationHelper.validateExperimentProperties(contextValidation, properties, level);
	}

	/**
	 * Validate experiment properties using a default fixed state {@link StateNames#IP}
	 * (context parameters {@link #FIELD_TYPE_CODE} {@link #FIELD_STATE_CODE}).
	 * See {@link ValidationHelper#validateProperties(ContextValidation, Map, List, boolean, boolean, String, String)}.
	 * @param contextValidation validation context
	 * @param properties        properties to validate
	 * @param level             properties level
	 * @deprecated use explicit parameter method {@link #validateExperimentProperties(ContextValidation, Map, Level.CODE, String, String)}
	 */
	@Deprecated
	public static void validateExperimentProperties(ContextValidation contextValidation, Map<String,PropertyValue> properties, Level.CODE level) {
//		String typeCode = getObjectFromContext(FIELD_TYPE_CODE, String.class, contextValidation);
//		String stateCode = getObjectFromContext(FIELD_STATE_CODE, String.class, contextValidation);
		String typeCode  = contextValidation.getTypedObject(FIELD_TYPE_CODE);
		String stateCode = contextValidation.getTypedObject(FIELD_STATE_CODE);
		
//		ExperimentType exType = ExperimentType.find.get().findByCode(typeCode);
//		if (exType != null) {
//			contextValidation.addKeyToRootKeyName("experimentProperties");
//			List<PropertyDefinition> propertyDefinitions = exType.getPropertyDefinitionByLevel(level);
//			//if("N".equals(stateCode)){
//			//	ValidationHelper.validateProperties(contextValidation, properties, propertyDefinitions, true, false, null, null);				
//			//}else{			
////				ValidationHelper.validateProperties(contextValidation, properties, propertyDefinitions, true, true, stateCode, "IP");	
//			ValidationHelper.validateProperties(contextValidation, properties, propertyDefinitions, true, true, stateCode, StateNames.IP);	
//			//}
//			contextValidation.removeKeyFromRootKeyName("experimentProperties");
//		}
		validateExperimentProperties(contextValidation, properties, level, typeCode, stateCode);
	}
	
	public static void validateExperimentProperties(ContextValidation         contextValidation, 
			                                        Map<String,PropertyValue> properties, 
			                                        Level.CODE                level,
			                                        String                    experimentTypeCode,
			                                        String                    stateCode) {
		ExperimentType exType = ExperimentType.find.get().findByCode(experimentTypeCode);
		if (exType != null) {
			contextValidation.addKeyToRootKeyName("experimentProperties");
			List<PropertyDefinition> propertyDefinitions = exType.getPropertyDefinitionByLevel(level);
			//if("N".equals(stateCode)){
			//	ValidationHelper.validateProperties(contextValidation, properties, propertyDefinitions, true, false, null, null);				
			//}else{			
//				ValidationHelper.validateProperties(contextValidation, properties, propertyDefinitions, true, true, stateCode, "IP");	
			ValidationHelper.validateProperties(contextValidation, properties, propertyDefinitions, true, true, stateCode, StateNames.IP);	
			//}
			contextValidation.removeKeyFromRootKeyName("experimentProperties");
		}
	}
	
	// -----------------------------------------------------------------

	/**
	 * Validate instrument properties (context parameters {@link #FIELD_INST_USED} {@link #FIELD_STATE_CODE})
	 * with default state {@link StateNames#IP}.
	 * @param properties        instrument properties to validate
	 * @param level             level
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateInstrumentProperties(ContextValidation, Map, Level.CODE)}
	 */
	@Deprecated
	public static void validateInstrumentProperties_(Map<String, PropertyValue> properties, Level.CODE level, ContextValidation contextValidation) {
		ContainerUsedValidationHelper.validateInstrumentProperties(contextValidation, properties, level);
	}

	/**
	 * Validate instrument properties (context parameters {@link #FIELD_INST_USED} {@link #FIELD_STATE_CODE})
	 * with default state {@link StateNames#IP}.
	 * @param contextValidation validation context
	 * @param properties        instrument properties to validate
	 * @param level             level
	 * @deprecated use {@link #validateInstrumentProperties(ContextValidation, Map, Level.CODE, InstrumentUsed, String)}
	 */
	@Deprecated
	public static void validateInstrumentProperties(ContextValidation contextValidation, Map<String,PropertyValue> properties, Level.CODE level) {
//		InstrumentUsed instrument = getObjectFromContext(FIELD_INST_USED, InstrumentUsed.class, contextValidation);
//		String stateCode = getObjectFromContext(FIELD_STATE_CODE, String.class, contextValidation);
		InstrumentUsed instrument = contextValidation.getTypedObject(FIELD_INST_USED);
		String         stateCode  = contextValidation.getTypedObject(FIELD_STATE_CODE);
		
//		InstrumentUsedType instType = InstrumentUsedType.find.get().findByCode(instrument.typeCode);
//		if (instType != null) {
//			contextValidation.addKeyToRootKeyName("instrumentProperties");
//			List<PropertyDefinition> propertyDefinitions = instType.getPropertyDefinitionByLevel(level);
//			//if("N".equals(stateCode)){
//			//	ValidationHelper.validateProperties(contextValidation, properties, propertyDefinitions, true, false, null, null);				
//			//}else{
////				ValidationHelper.validateProperties(contextValidation, properties, propertyDefinitions, true, true, stateCode, "IP");
//			ValidationHelper.validateProperties(contextValidation, properties, propertyDefinitions, true, true, stateCode, StateNames.IP);				
//			//}
//			contextValidation.removeKeyFromRootKeyName("instrumentProperties");
//		}
		validateInstrumentProperties(contextValidation, properties, level, instrument, stateCode);
	}
	
	public static void validateInstrumentProperties(ContextValidation contextValidation, Map<String,PropertyValue> properties, Level.CODE level, InstrumentUsed instrument, String stateCode) {
		InstrumentUsedType instType = InstrumentUsedType.find.get().findByCode(instrument.typeCode);
		if (instType != null) {
			contextValidation.addKeyToRootKeyName("instrumentProperties");
			List<PropertyDefinition> propertyDefinitions = instType.getPropertyDefinitionByLevel(level);
			//if("N".equals(stateCode)){
			//	ValidationHelper.validateProperties(contextValidation, properties, propertyDefinitions, true, false, null, null);				
			//}else{
//				ValidationHelper.validateProperties(contextValidation, properties, propertyDefinitions, true, true, stateCode, "IP");
			ValidationHelper.validateProperties(contextValidation, properties, propertyDefinitions, true, true, stateCode, StateNames.IP);				
			//}
			contextValidation.removeKeyFromRootKeyName("instrumentProperties");
		}
	}

	// ---------------------------------------------------------------------------
	
	/**
	 * Validate a percentage (value in ]0,100]).
	 * @param percentage        percentage value to validate
	 * @param contextValidation validation context
	 * @deprecated use {@link #validatePercentage(ContextValidation, Double)}
	 */
	@Deprecated
	public static void validatePercentage(Double percentage, ContextValidation contextValidation) {
		ContainerUsedValidationHelper.validatePercentage(contextValidation, percentage);
	}

	/**
	 * Validate a percentage (value in ]0,100]).
	 * @param contextValidation validation context
	 * @param percentage        percentage value to validate
	 */
	public static void validatePercentage(ContextValidation contextValidation, Double percentage) {
		if (ValidationHelper.validateNotEmpty(contextValidation, percentage, "percentage")) 
			if (percentage.doubleValue() > 100 || percentage.doubleValue() <= 0) 
				contextValidation.addError("percentage", "error.validationexp.inputContainer.percentage");
	}
	
	// ---------------------------------------------------------------------------

	/*
	public static void validateVolume(PropertyValue volume, ContextValidation contextValidation) {
		if(volume!=null && volume.value!=null){
			Collection<PropertyDefinition> pdefs = new ArrayList<>();		
			PropertyDefinition pd = new PropertyDefinition();			
			pd.code = "volume";
			pd.valueType = Double.class.getName();
			pd.propertyValueType = PropertyValue.singleType;
			pdefs.add(pd);
			contextValidation.putObject("propertyDefinitions", pdefs);
			volume.validate(contextValidation);
			contextValidation.removeObject("propertyDefinitions");			
		}
	}


	public static void validateConcentration(PropertyValue concentration, ContextValidation contextValidation) {
		if(concentration!=null && concentration.value!=null){
			Collection<PropertyDefinition> pdefs = new ArrayList<>();
			PropertyDefinition pd = new PropertyDefinition();
			pd.code = "concentration";
			pd.valueType = Double.class.getName();
			pd.propertyValueType = PropertyValue.singleType;
			pdefs.add(pd);
			contextValidation.putObject("propertyDefinitions", pdefs);
			concentration.validate(contextValidation);
			contextValidation.removeObject("propertyDefinitions");
		}
		
	}
	
	public static void validateQuantity(PropertyValue quantity,	ContextValidation contextValidation) {
		if(quantity != null && quantity.value != null){
			Collection<PropertyDefinition> pdefs = new ArrayList<>();
			PropertyDefinition pd = new PropertyDefinition();
			pd.code = "quantity";
			pd.valueType = Double.class.getName();
			pd.propertyValueType = PropertyValue.singleType;
			pdefs.add(pd);
			contextValidation.putObject("propertyDefinitions", pdefs);
			quantity.validate(contextValidation);
			contextValidation.removeObject("propertyDefinitions");
		}
		
	}

	
	public static void validateSize(PropertyValue size, ContextValidation contextValidation) {
		if(size!=null && size.value!=null){
			Collection<PropertyDefinition> pdefs = new ArrayList<>();		
			PropertyDefinition pd = new PropertyDefinition();			
			pd.code = "size";
			pd.valueType = Double.class.getName();
			pd.propertyValueType = PropertyValue.singleType;
			pdefs.add(pd);
			contextValidation.putObject("propertyDefinitions", pdefs);
			size.validate(contextValidation);
			contextValidation.removeObject("propertyDefinitions");
		}
	}
*/
	
	// --------------------------------------------------------------------------
	// arguments reordered
	
	/**
	 * Validate output container code (context parameter {@link #FIELD_STATE_CODE}).
	 * @param code              output container code to validate
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateOutputContainerCode(ContextValidation, String)}
	 */
	@Deprecated
	public static void validateOutputContainerCode_(String code, ContextValidation contextValidation) {
		ContainerUsedValidationHelper.validateOutputContainerCode(contextValidation, code);
	}

	/**
	 * Validate output container code (context parameter {@link #FIELD_STATE_CODE}).
	 * @param contextValidation validation context
	 * @param code              output container code to validate
	 * @deprecated use {@link #validateOutputContainerCode(ContextValidation, String, String)}
	 */
	@Deprecated
	public static void validateOutputContainerCode(ContextValidation contextValidation,	String code) {
//		String stateCode = getObjectFromContext(FIELD_STATE_CODE, String.class, contextValidation);
		String stateCode = contextValidation.getTypedObject(FIELD_STATE_CODE);
//		if (("N".equals(stateCode) && code != null) || "IP".equals(stateCode)) {
//			if (ValidationHelper.validateNotEmpty(contextValidation, code, "code")) {
//				if (validateUniqueInstanceCode(contextValidation, code, Container.class, CONTAINER_COLL_NAME)) {
//					validateContainerNotUsedInOtherExperiment(contextValidation, code);
//				}
//			}
//		} else if ("F".equals(stateCode)) {
////			validateRequiredInstanceCode(code, "code",  Container.class, CONTAINER_COLL_NAME,contextValidation);
//			validateCodeForeignRequired(contextValidation, Container.find.get(), code,  "code");			
//		}
		validateOutputContainerCode(contextValidation, code, stateCode);
	}
	
	/**
	 * Validate output container code for a given state.
	 * @param contextValidation validation context
	 * @param code              output container code to validate
	 * @param stateCode         state code
	 */
	public static void validateOutputContainerCode(ContextValidation contextValidation,	String code, String stateCode) {
		if (("N".equals(stateCode) && code != null) || "IP".equals(stateCode)) {
			if (ValidationHelper.validateNotEmpty(contextValidation, code, "code")) {
				if (validateUniqueInstanceCode(contextValidation, code, Container.class, CONTAINER_COLL_NAME)) {
					validateContainerNotUsedInOtherExperiment(contextValidation, code);
				}
			}
		} else if ("F".equals(stateCode)) {
//			validateRequiredInstanceCode(code, "code",  Container.class, CONTAINER_COLL_NAME,contextValidation);
			validateCodeForeignRequired(contextValidation, Container.find.get(), code,  "code");			
		}
	}

	// --------------------------------------------------------------------------

	/**
	 * Validates that no other experiment references the container code.
	 * @param contextValidation validation context
	 * @param containerCode     container code
	 */
	private static void validateContainerNotUsedInOtherExperiment(ContextValidation contextValidation, String containerCode) {
//		Experiment exp = getObjectFromContext(FIELD_EXPERIMENT, Experiment.class, contextValidation);
		Experiment exp = contextValidation.getTypedObject(FIELD_EXPERIMENT);
		if (MongoDBDAO.checkObjectExist(EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.notEquals("code", exp.code).in("outputContainerCodes", containerCode))){
			contextValidation.addError("container code", "error.validationexp.container.alreadyused",containerCode);
		}
	}
	
	/**
	 * Validates that no other experiment reference the container support code.
	 * @param contextValidation    validation context
	 * @param supportContainerCode container support code
	 */
	private static void validateSupportContainerNotUsedInOtherExperiment(ContextValidation contextValidation, String supportContainerCode) {
//		Experiment exp = getObjectFromContext(FIELD_EXPERIMENT, Experiment.class, contextValidation);
		Experiment exp = contextValidation.getTypedObject(FIELD_EXPERIMENT);
		if (MongoDBDAO.checkObjectExist(EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.notEquals("code", exp.code)
				.in("atomicTransfertMethods.outputContainerUseds.locationOnContainerSupport.code", supportContainerCode))) {
			contextValidation.addError("container support code", "error.validationexp.support.alreadyused",supportContainerCode);
		}
	}

	// ---------------------------------------------------------------
	// arguments reordered
	
	/**
	 * Validate a location on support (context parameter {@link #FIELD_INST_USED}).
	 * @param locationOnContainerSupport location on container support to validate
	 * @param contextValidation          validation context
	 * @deprecated use {@link #validateLocationOnSupportOnContainer(ContextValidation, LocationOnContainerSupport)}
	 */
	@Deprecated
	public static void validateLocationOnSupportOnContainer(LocationOnContainerSupport locationOnContainerSupport,	ContextValidation contextValidation) {
		ContainerUsedValidationHelper.validateLocationOnSupportOnContainer(contextValidation, locationOnContainerSupport);
	}

	/**
	 * Validate a location on support (context parameter {@link #FIELD_INST_USED} {@link #FIELD_STATE_CODE}).
	 * @param contextValidation          validation context
	 * @param locationOnContainerSupport location on container support to validate
	 * @deprecated use {@link #validateLocationOnSupportOnContainer(ContextValidation, LocationOnContainerSupport, InstrumentUsed, String)}
	 */
	@Deprecated
	public static void validateLocationOnSupportOnContainer(ContextValidation contextValidation, LocationOnContainerSupport locationOnContainerSupport) {
//		if (ValidationHelper.validateNotEmpty(contextValidation, locationOnContainerSupport, "locationOnContainerSupport")) {
//			
//			contextValidation.addKeyToRootKeyName("locationOnContainerSupport");
////			InstrumentUsed instrument = getObjectFromContext(FIELD_INST_USED, InstrumentUsed.class, contextValidation);
//			InstrumentUsed instrument = contextValidation.getTypedObject(FIELD_INST_USED);
//			if (!instrument.outContainerSupportCategoryCode.equals(locationOnContainerSupport.categoryCode)) {
//				contextValidation.addError("categoryCode", ERROR_VALUENOTAUTHORIZED_MSG, locationOnContainerSupport.categoryCode);
//			}
//			
////			String stateCode = getObjectFromContext(FIELD_STATE_CODE, String.class, contextValidation);
//			String stateCode = contextValidation.getTypedObject(FIELD_STATE_CODE);
//			
//			if (!"N".equals(stateCode)) {
//				ValidationHelper.validateNotEmpty(contextValidation, locationOnContainerSupport.line,   "line");
//				ValidationHelper.validateNotEmpty(contextValidation, locationOnContainerSupport.column, "column");
//			}
//			
//			if (("N".equals(stateCode) && locationOnContainerSupport.code != null) || "IP".equals(stateCode)){
//				if (ValidationHelper.validateNotEmpty(contextValidation, locationOnContainerSupport.code, "code")) {
//					if (validateUniqueInstanceCode(contextValidation, locationOnContainerSupport.code, ContainerSupport.class, CONTAINER_SUPPORT_COLL_NAME)) {
//						validateSupportContainerNotUsedInOtherExperiment(contextValidation, locationOnContainerSupport.code);
//					}
//				}
//			} else if("F".equals(stateCode)) {
////				validateRequiredInstanceCode(locationOnContainerSupport.code, "code",  ContainerSupport.class, CONTAINER_SUPPORT_COLL_NAME,contextValidation);					
//			    validateCodeForeignRequired(contextValidation, ContainerSupport.find.get(), locationOnContainerSupport.code,  "code");					
//			}
//			contextValidation.removeKeyFromRootKeyName("locationOnContainerSupport");
//		}
		InstrumentUsed instrument = contextValidation.getTypedObject(FIELD_INST_USED);
		String stateCode = contextValidation.getTypedObject(FIELD_STATE_CODE);
		validateLocationOnSupportOnContainer(contextValidation, locationOnContainerSupport, instrument, stateCode);
	}
	
	public static void validateLocationOnSupportOnContainer(ContextValidation          contextValidation, 
			                                                LocationOnContainerSupport locationOnContainerSupport,
			                                                InstrumentUsed             instrument,
			                                                String                     stateCode) {
		if (! ValidationHelper.validateNotEmpty(contextValidation, locationOnContainerSupport, "locationOnContainerSupport"))
			return;
			
		contextValidation.addKeyToRootKeyName("locationOnContainerSupport");
		if (!instrument.outContainerSupportCategoryCode.equals(locationOnContainerSupport.categoryCode)) {
			contextValidation.addError("categoryCode", ERROR_VALUENOTAUTHORIZED_MSG, locationOnContainerSupport.categoryCode);
		}

		if (!"N".equals(stateCode)) {
			ValidationHelper.validateNotEmpty(contextValidation, locationOnContainerSupport.line,   "line");
			ValidationHelper.validateNotEmpty(contextValidation, locationOnContainerSupport.column, "column");
		}

		if (("N".equals(stateCode) && locationOnContainerSupport.code != null) || "IP".equals(stateCode)){
			if (ValidationHelper.validateNotEmpty(contextValidation, locationOnContainerSupport.code, "code")) {
				if (validateUniqueInstanceCode(contextValidation, locationOnContainerSupport.code, ContainerSupport.class, CONTAINER_SUPPORT_COLL_NAME)) {
					validateSupportContainerNotUsedInOtherExperiment(contextValidation, locationOnContainerSupport.code);
				}
			}
		} else if("F".equals(stateCode)) {
			//				validateRequiredInstanceCode(locationOnContainerSupport.code, "code",  ContainerSupport.class, CONTAINER_SUPPORT_COLL_NAME,contextValidation);					
			validateCodeForeignRequired(contextValidation, ContainerSupport.find.get(), locationOnContainerSupport.code,  "code");					
		}
		contextValidation.removeKeyFromRootKeyName("locationOnContainerSupport");
	}
	
	// -----------------------------------------------------------------------------
	// renamed and arguments reordered
	
//	public static void validateInputContainerCategoryCode(String categoryCode, ContextValidation contextValidation) {
//		/* many input category support in experiment => validation creates errors */ 
//		
//		/*InstrumentUsed instrument = getObjectFromContext(FIELD_INST_USED, InstrumentUsed.class, contextValidation);
//		if(ValidationHelper.required(contextValidation, categoryCode, "categoryCode") && null != instrument.inContainerSupportCategoryCode){
//			ContainerCategory outputContainerCategory = ContainerCategory.find.findByContainerSupportCategoryCode(instrument.inContainerSupportCategoryCode);
//			if(!categoryCode.equals(outputContainerCategory.code)){
//				contextValidation.addErrors("categoryCode", ERROR_VALUENOTAUTHORIZED_MSG, categoryCode);
//			}
//		}*/
//		
////		BusinessValidationHelper.validateRequiredDescriptionCode(contextValidation, categoryCode,"inputContainerCategory", ContainerCategory.find.get(),true);
//		validateCodeForeignRequired(contextValidation, ContainerCategory.miniFind.get(),categoryCode, "inputContainerCategory",true);
//	}
	
	/**
	 * Validate a category code.
	 * @param categoryCode      category code to validate
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateInputContainerCategoryCodeRequired(ContextValidation, String)}
	 */
	@Deprecated
	public static void validateInputContainerCategoryCode_(String categoryCode, ContextValidation contextValidation) {
		ContainerUsedValidationHelper.validateInputContainerCategoryCodeRequired(contextValidation, categoryCode);
	}
	
	/**
	 * Validate a {@link ContainerCategory} code.
	 * @param contextValidation validation context
	 * @param categoryCode      category code to validate
	 */
	public static void validateInputContainerCategoryCodeRequired(ContextValidation contextValidation, String categoryCode) {
		// LOGIC: this does not require that we fetch the object from the database so the last argument should be 'false'.
		validateCodeForeignRequired(contextValidation, ContainerCategory.miniFind.get(), categoryCode, "inputContainerCategory", true);
	}

	// ------------------------------------------------------------------------------------
	// arguments reordered
	
	/**
	 * Validate an output container category code that is relative to an instrument
	 * (context parameter {@link #FIELD_INST_USED}).
	 * @param categoryCode      container category code
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateOutputContainerCategoryCode(ContextValidation, String)}
	 */
	@Deprecated
	public static void validateOutputContainerCategoryCode_(String categoryCode, ContextValidation contextValidation) {
		ContainerUsedValidationHelper.validateOutputContainerCategoryCode(contextValidation, categoryCode);
	}

	/**
	 * Validate an output container category code that is relative to an instrument
	 * (context parameter {@link #FIELD_INST_USED}).
	 * @param contextValidation validation context
	 * @param categoryCode      container category code
	 * @deprecated use {@link #validateOutputContainerCategoryCode(ContextValidation, String, InstrumentUsed)}
	 */
	@Deprecated
	public static void validateOutputContainerCategoryCode(ContextValidation contextValidation, String categoryCode) {
//		InstrumentUsed instrument = getObjectFromContext(FIELD_INST_USED, InstrumentUsed.class, contextValidation);
		InstrumentUsed instrument = contextValidation.getTypedObject(FIELD_INST_USED);
//		if (ValidationHelper.validateNotEmpty(contextValidation, categoryCode, "categoryCode") && instrument.outContainerSupportCategoryCode != null) {
//			ContainerCategory outputContainerCategory = ContainerCategory.find.get().findByContainerSupportCategoryCode(instrument.outContainerSupportCategoryCode);
//			if (!categoryCode.equals(outputContainerCategory.code)) {
//				contextValidation.addError("categoryCode", ERROR_VALUENOTAUTHORIZED_MSG, categoryCode);
//			}
//		}
		validateOutputContainerCategoryCode(contextValidation, categoryCode, instrument);
	}
	
	/**
	 * Validate an output container category code that is relative to an instrument.
	 * @param contextValidation validation context
	 * @param categoryCode      container category code
	 * @param instrumentUsed    instrument used
	 */
	public static void validateOutputContainerCategoryCode(ContextValidation contextValidation, String categoryCode, InstrumentUsed instrumentUsed) {
		if (ValidationHelper.validateNotEmpty(contextValidation, categoryCode, "categoryCode") && instrumentUsed.outContainerSupportCategoryCode != null) {
			ContainerCategory outputContainerCategory = ContainerCategory.find.get().findByContainerSupportCategoryCode(instrumentUsed.outContainerSupportCategoryCode);
			if (!categoryCode.equals(outputContainerCategory.code)) {
				contextValidation.addError("categoryCode", ERROR_VALUENOTAUTHORIZED_MSG, categoryCode);
			}
		}
	}
	
	// ------------------------------------------------------------------------------------	
	// arguments reordered
	
	/**
	 * Validate contents (context parameter {@link CommonValidationHelper#FIELD_IMPORT_TYPE_CODE}
	 * {@link CommonValidationHelper#FIELD_STATE_CODE}).
	 * @param contents          contents to validate
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateOutputContents(ContextValidation, List)}
	 */
	@Deprecated
	public static void validateOutputContents_(List<Content> contents, ContextValidation contextValidation) {
		ContainerUsedValidationHelper.validateOutputContents(contextValidation, contents);
	}

	/**
	 * Validate contents (context parameter {@link CommonValidationHelper#FIELD_IMPORT_TYPE_CODE}
	 * {@link CommonValidationHelper#FIELD_STATE_CODE}).
	 * @param contextValidation validation context
	 * @param contents          contents to validate
	 * @deprecated use {@link #validateOutputContents(ContextValidation, List, String, String)}
	 */
	@Deprecated
	public static void validateOutputContents(ContextValidation contextValidation, List<Content> contents) {
//		String stateCode = getObjectFromContext(FIELD_STATE_CODE, String.class, contextValidation);
		String stateCode = contextValidation.getTypedObject(FIELD_STATE_CODE);
		if ("N".equals(stateCode) && contents != null) {
			int i = 0;
			for (Content content : contents) {
				contextValidation.addKeyToRootKeyName("contents["+i+"]");
				content.validate(contextValidation);
				contextValidation.removeKeyFromRootKeyName("contents["+i+++"]");
			}
		} else if ("IP".equals(stateCode)  && contents != null) { // GA: 22/01/2016 hack for old experiment without contents, remove in 03/2016
			if (ValidationHelper.validateNotEmpty(contextValidation, contents, "contents")) {
				int i = 0;
				for (Content content : contents) {
					contextValidation.addKeyToRootKeyName("contents["+i+"]");
					content.validate(contextValidation);
					contextValidation.removeKeyFromRootKeyName("contents["+i+++"]");
				}
			}
		} else if ("F".equals(stateCode) && contents != null) { // GA 22/01/2016 hack for old experiment without contents, remove in 2017
			if (ValidationHelper.validateNotEmpty(contextValidation, contents, "contents")) {
				int i = 0;
				for (Content content : contents) {
					contextValidation.addKeyToRootKeyName("contents["+i+"]");
					content.validate(contextValidation);
					contextValidation.removeKeyFromRootKeyName("contents["+i+++"]");
				}
			}
		}
	}

	/**
	 * Validate output contents.
	 * @param contextValidation validation context
	 * @param contents          contents to validate
	 * @param stateCode         optional state code (validation succeeds when null)
	 * @param importTypeCode    optional import type (see {@link Content#validate(ContextValidation, String)})
	 */

	public static void validateOutputContents(ContextValidation contextValidation, List<Content> contents, String stateCode, String importTypeCode) {

		if ("N".equals(stateCode) && contents != null) {
			int i = 0;
			for (Content content : contents) {
				contextValidation.addKeyToRootKeyName("contents["+i+"]");
				content.validate(contextValidation, importTypeCode);
				contextValidation.removeKeyFromRootKeyName("contents["+i+++"]");
			}
		} else if ("IP".equals(stateCode)  && contents != null) { // GA: 22/01/2016 hack for old experiment without contents, remove in 03/2016
			if (ValidationHelper.validateNotEmpty(contextValidation, contents, "contents")) {
				int i = 0;
				for (Content content : contents) {
					contextValidation.addKeyToRootKeyName("contents["+i+"]");
					content.validate(contextValidation, importTypeCode);
					contextValidation.removeKeyFromRootKeyName("contents["+i+++"]");
				}
			}
		} else if ("F".equals(stateCode) && contents != null) { // GA 22/01/2016 hack for old experiment without contents, remove in 2017
			if (ValidationHelper.validateNotEmpty(contextValidation, contents, "contents")) {
				int i = 0;
				for (Content content : contents) {
					contextValidation.addKeyToRootKeyName("contents["+i+"]");
					content.validate(contextValidation, importTypeCode);
					
					contextValidation.removeKeyFromRootKeyName("contents["+i+++"]");
				}
			}
		}
	}
	
}
