package validation.experiment.instance;

import static validation.utils.ValidationHelper.validateNotEmpty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.LFWApplication;
import fr.cea.ig.play.IGGlobals;
import models.laboratory.common.instance.Comment;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.common.instance.Valuation;
import models.laboratory.experiment.description.ExperimentCategory;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.experiment.instance.AbstractContainerUsed;
import models.laboratory.experiment.instance.AtomicTransfertMethod;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.InputContainerUsed;
import models.laboratory.experiment.instance.OutputContainerUsed;
import models.laboratory.instrument.description.InstrumentUsedType;
import models.laboratory.instrument.instance.InstrumentUsed;
import models.laboratory.protocol.instance.Protocol;
import models.laboratory.reagent.instance.ReagentUsed;
import models.laboratory.container.instance.Container;
import models.laboratory.sample.instance.Sample; // FDS
import models.utils.InstanceConstants;
import ngl.refactoring.state.ExperimentStateNames;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.sample.instance.SampleValidationHelper;
import validation.utils.ValidationConstants;
import validation.utils.ValidationHelper;

public class ExperimentValidationHelper extends CommonValidationHelper {

	private static final play.Logger.ALogger logger = play.Logger.of(ExperimentValidationHelper.class);
	
	// ----------------------------------------------------------------------------------
	// renamed and arguments reordered

	/**
	 * Validate an experiment protocol code.
	 * @param typeCode          experiment type code
	 * @param protocolCode      experiment protocol code
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateProtocolCode(ContextValidation, String, String)}
	 */
	@Deprecated
	public static void validationProtocoleCode(String typeCode, String protocolCode, ContextValidation contextValidation) {
		ExperimentValidationHelper.validateProtocolCode(contextValidation, typeCode, protocolCode);
	}

	/**
	 * Validate an experiment protocol code (context parameter FIELD_STATE_CODE).
	 * @param contextValidation validation context
	 * @param typeCode          experiment type code
	 * @param protocolCode      experiment protocol code
	 * @deprecated use {@link #validateProtocolCode(ContextValidation, String, String, String)}
	 */
	@Deprecated
	public static void validateProtocolCode(ContextValidation contextValidation, String typeCode, String protocolCode)  {
//		String stateCode = getObjectFromContext(FIELD_STATE_CODE, String.class, contextValidation);
//		String stateCode = contextValidation.getTypedObject(FIELD_STATE_CODE); // CTX: should be an explicit argument
//		if (!stateCode.equals("N")) {
//			if (validateNotEmpty(contextValidation, protocolCode, "protocolCode")) {				
//				if (!MongoDBDAO.checkObjectExist(InstanceConstants.PROTOCOL_COLL_NAME, Protocol.class, DBQuery.and(DBQuery.is("code",protocolCode), DBQuery.in("experimentTypeCodes", typeCode)))) {
//					contextValidation.addError("protocolCode", ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG, protocolCode);
//				}				
//			}
//		} else if (StringUtils.isNotBlank(protocolCode)) {
//			if (!MongoDBDAO.checkObjectExist(InstanceConstants.PROTOCOL_COLL_NAME, Protocol.class, DBQuery.and(DBQuery.is("code",protocolCode), DBQuery.in("experimentTypeCodes", typeCode)))) {
//				contextValidation.addError("protocolCode", ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG, protocolCode);
//			}
//		}
		String stateCode = contextValidation.getTypedObject(FIELD_STATE_CODE); // CTX: should be an explicit argument
		validateProtocolCode(contextValidation, typeCode, protocolCode, stateCode);
	}
	
	/**
	 * Validate an experiment protocol code.
	 * @param contextValidation validation context
	 * @param typeCode          experiment type code
	 * @param protocolCode      experiment protocol code
	 * @param stateCode         required experiment state code
	 */
	public static void validateProtocolCode(ContextValidation contextValidation, String typeCode, String protocolCode, String stateCode)  {
		if (!stateCode.equals("N")) {
			if (validateNotEmpty(contextValidation, protocolCode, "protocolCode")) {				
				if (!MongoDBDAO.checkObjectExist(InstanceConstants.PROTOCOL_COLL_NAME, Protocol.class, DBQuery.and(DBQuery.is("code",protocolCode), DBQuery.in("experimentTypeCodes", typeCode)))) {
					contextValidation.addError("protocolCode", ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG, protocolCode);
				}				
			}
		} else if (StringUtils.isNotBlank(protocolCode)) {
			if (!MongoDBDAO.checkObjectExist(InstanceConstants.PROTOCOL_COLL_NAME, Protocol.class, DBQuery.and(DBQuery.is("code",protocolCode), DBQuery.in("experimentTypeCodes", typeCode)))) {
				contextValidation.addError("protocolCode", ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG, protocolCode);
			}
		}
	}
	
	// ----------------------------------------------------------------------------------
	// renamed and arguments reordered
		
	/**
	 * Validate a required experiment type (foreign key to experiment type) and experiment properties 
	 * as defined for the given type code.
	 * @param typeCode          experiment type code
	 * @param properties        experiment properties
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateExperimentTypeRequired(ContextValidation, String, Map)}
	 */
	@Deprecated
	public static void validationExperimentType(String typeCode, Map<String, PropertyValue> properties, ContextValidation contextValidation) {
		ExperimentValidationHelper.validateExperimentTypeRequired(contextValidation, typeCode, properties);
	}
	
	/**
	 * Validate a required experiment type (foreign key to experiment type) and experiment properties 
	 * as defined for the given type code.
	 * @param contextValidation validation context
	 * @param typeCode          experiment type code
	 * @param properties        experiment properties
	 */
	public static void validateExperimentTypeRequired(ContextValidation contextValidation, String typeCode, Map<String,PropertyValue> properties) {
		ExperimentType exType = validateCodeForeignRequired(contextValidation, ExperimentType.miniFind.get(), typeCode, "typeCode", true);
		if (exType != null) {
			String stateCode = contextValidation.getTypedObject(FIELD_STATE_CODE);
			contextValidation.addKeyToRootKeyName("experimentProperties");
			ValidationHelper.validateProperties(contextValidation, properties, exType.getPropertiesDefinitionExperimentLevel(), true, true, stateCode, "IP");
			contextValidation.removeKeyFromRootKeyName("experimentProperties");
		}		
	}

	// ----------------------------------------------------------------------------------
	// renamed and arguments reordered

	/**
	 * Validate a required state.
	 * @param typeCode          experiment type code
	 * @param state             state to validate
	 * @param contextValidation validation context
	 * @deprecated use {@link CommonValidationHelper#validateStateRequired(ContextValidation, String, State)} 
	 */
	@Deprecated
	public static void validateState(String typeCode, State state, ContextValidation contextValidation) {
		if (ValidationHelper.validateNotEmpty(contextValidation, state, "state")) {
			contextValidation.putObject(FIELD_TYPE_CODE, typeCode);
			contextValidation.addKeyToRootKeyName("state");
			state.validate(contextValidation);
			contextValidation.removeKeyFromRootKeyName("state");
			contextValidation.removeObject(FIELD_TYPE_CODE);
		}		
	}
	
	// --------------------------------------------------------------------------
	// renamed and arguments reordered

	/**
	 * Validate valuation status for the given experiment type code.
	 * @param typeCode          experiment type code
	 * @param status            status
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateStatusRequired(ContextValidation, String, Valuation)}
	 */
	@Deprecated
	public static void validateStatus(String typeCode, Valuation status, ContextValidation contextValidation) {
		ExperimentValidationHelper.validateStatusRequired(contextValidation, typeCode, status);
	}

	/**
	 * Validate valuation status for the given experiment type code (context parameter FIELD_STATE_CODE).
	 * @param contextValidation validation context
	 * @param typeCode          experiment type code
	 * @param status            status
	 */	
	public static void validateStatusRequired(ContextValidation contextValidation, String typeCode, Valuation status) {
		if (ValidationHelper.validateNotEmpty(contextValidation, status, "status")) {
//			contextValidation.putObject(FIELD_TYPE_CODE, typeCode);
			contextValidation.addKeyToRootKeyName("status");
			status.validate(contextValidation, typeCode);
			contextValidation.removeKeyFromRootKeyName("status");
//			contextValidation.removeObject(FIELD_TYPE_CODE);
			
//			String stateCode = getObjectFromContext(FIELD_STATE_CODE, String.class, contextValidation);			
			String stateCode = contextValidation.getTypedObject(FIELD_STATE_CODE);			
			if ("F".equals(stateCode) && TBoolean.UNSET.equals(status.valid)) {
				contextValidation.addError("status", "error.validationexp.status.empty");
			}			
		}	
	}
	
	// --------------------------------------------------------------------------
	// renamed and arguments reordered
	
	/**
	 * Validate a required experiment category code.
	 * @param categoryCode      experiment category code
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateExperimentCategoryCodeRequired(ContextValidation, String)}
	 */
	@Deprecated
	public static void validationExperimentCategoryCode(String categoryCode, ContextValidation contextValidation) {
		ExperimentValidationHelper.validateExperimentCategoryCodeRequired(contextValidation, categoryCode);
	}

	/**
	 * Validate a required experiment category code.
	 * @param contextValidation validation context
	 * @param categoryCode      experiment category code
	 */
	public static void validateExperimentCategoryCodeRequired(ContextValidation contextValidation, String categoryCode) {
		validateCodeForeignRequired(contextValidation, ExperimentCategory.miniFind.get(), categoryCode, "categoryCode", false);
	}
	
	// --------------------------------------------------------------------------
	// arguments reordered

	/**
	 * Validate an optional list of required reagents. 
	 * @param reagentsUsed      list of reagents
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateReagents(ContextValidation, List)}
	 */
	@Deprecated
	public static void validateReagents_(List<ReagentUsed> reagentsUsed, ContextValidation contextValidation) {
		ExperimentValidationHelper.validateReagents(contextValidation, reagentsUsed);
	}

	/**
	 * Validate an optional list of required reagents (does not validate anything except that there
	 * are no null instances in the list). 
	 * @param contextValidation validation context
	 * @param reagentsUsed      list of reagents
	 */
	public static void validateReagents(ContextValidation contextValidation, List<ReagentUsed> reagentsUsed) {
		if (reagentsUsed == null)
			return;
		for (ReagentUsed reagentUsed : reagentsUsed)
			reagentUsed.validate(contextValidation);
	}

	// --------------------------------------------------------------------------

	
	// CTX: should be rewritten using context appendPath().
	/**
	 * Validate atomic transfer methods (context parameters {@link #FIELD_STATE_CODE}, {@link #FIELD_EXPERIMENT}).
	 * @param expCategoryCode        experiment category code
	 * @param expTypeCode            experiment type code
	 * @param instrument             instrument
	 * @param atomicTransfertMethods methods to validate
	 * @param contextValidation      validation context
	 * @deprecated use {@link #validateAtomicTransfertMethods(ContextValidation, String, String, InstrumentUsed, List, Experiment, String)}
	 */
	@Deprecated
	public static void validateAtomicTransfertMethods(String                      expCategoryCode, 
			                                          String                      expTypeCode, 
			                                          InstrumentUsed              instrument, 
			                                          List<AtomicTransfertMethod> atomicTransfertMethods, 
			                                          ContextValidation           contextValidation) {
		String     stateCode  = contextValidation.getTypedObject(FIELD_STATE_CODE);
		Experiment experiment = contextValidation.getTypedObject(FIELD_EXPERIMENT);
		validateAtomicTransfertMethods(contextValidation, expCategoryCode, expTypeCode, instrument, atomicTransfertMethods, experiment, stateCode);
	}
	
	public static void validateAtomicTransfertMethods(ContextValidation           contextValidation, 
                                                      String                      expCategoryCode, 
                                                      String                      expTypeCode, 
                                                      InstrumentUsed              instrument, 
                                                      List<AtomicTransfertMethod> atomicTransfertMethods,
                                                      Experiment                  experiment,
                                                      String                      stateCode) {
		IGGlobals.instanceOf(LFWApplication.class).parallelRun(() -> {
			IntStream.range(0, atomicTransfertMethods.size()).parallel().forEach(i -> {
				ContextValidation cv = ContextValidation.createUndefinedContext(contextValidation.getUser());
				cv.setMode(contextValidation.getMode());			
				cv.putObject(FIELD_STATE_CODE, stateCode);
				cv.putObject(FIELD_EXPERIMENT, experiment);
				cv.putObject(FIELD_TYPE_CODE,  expTypeCode);
				cv.putObject(FIELD_INST_USED,  instrument);
				Integer index = i;
				if (atomicTransfertMethods.get(i).viewIndex != null) 
					index = atomicTransfertMethods.get(i).viewIndex;
				String rootKeyName = "atomictransfertmethods[" + index + "]";
				cv.addKeyToRootKeyName(rootKeyName);
				atomicTransfertMethods.get(i).validate(cv, expTypeCode, stateCode, instrument, null);
				if (cv.hasErrors()) {
					contextValidation.addErrors(cv.getErrors());
				}
				cv.removeKeyFromRootKeyName(rootKeyName);
				
			});

			validateUniqOutputContainerCodeInsideExperiment(atomicTransfertMethods, contextValidation);
			
			/** FDS 11/09/2019 NGL-2607 problem appears first for experiment 'permeabilization-transposition-purification'
			 * at CNRGH the user provide MANUALLY the newSampleCodes ==> can be wrong!!! [at CNS the new samples codes are calculated by NGL]	
			 * Same validations must apply to any experiment with boolean  newSample=TRUE -AND- type=oneToOne
			 * validations only if experiment state =NEW
			 */
			ExperimentType	experimentType = ExperimentType.find.get().findByCode(expTypeCode);
			if ( experimentType.newSample && experimentType.atomicTransfertMethod.equals("OneToOne") &&  stateCode.equals("N") ){
				
				// validations for NewSamples (passer aussi experiment)
				validateNewSamples(experiment, atomicTransfertMethods,contextValidation);
				
				// validation for inputContainers
				// => NewSample creation require only one parent to clone his properties, if we have more=> problem!!
				validateContainersOnlyOneContent(atomicTransfertMethods, contextValidation);
			}

			validateStorageCodes(expCategoryCode, atomicTransfertMethods, contextValidation);
			// GA: validate number of ATM against SupportContainerCategory nbLine and nbColumn
		});
	}

	private static void validateUniqOutputContainerCodeInsideExperiment(List<AtomicTransfertMethod> atomicTransfertMethods, 
			                                                            ContextValidation contextValidation) {
		Set<String> outputContainerCodes = new TreeSet<>();
		
		IntStream.range(0, atomicTransfertMethods.size()).forEach(i -> {
			ContextValidation cv = ContextValidation.createUndefinedContext(contextValidation.getUser());
			cv.setRootKeyName(contextValidation.getRootKeyName()); // FDS correction
			String rootKeyName = "atomictransfertmethod[" + i + "]";
			cv.addKeyToRootKeyName(rootKeyName);
			
			AtomicTransfertMethod atm = atomicTransfertMethods.get(i);
			List<OutputContainerUsed> outputContainerUseds = atm.outputContainerUseds;
			if (outputContainerUseds != null) {
				for (int j = 0 ; j < outputContainerUseds.size(); j++) {
					String rootKeyName2 = "outputContainerUseds[" + j + "]";
					cv.addKeyToRootKeyName(rootKeyName2);
					String containerCode = atm.outputContainerUseds.get(j).code;
					if (containerCode != null) {
						//System.out.println("FDS-DEBUG: containerCode="+ containerCode);// passe ici seulement quand on va passer a l'etat Finished !!! 
						if (outputContainerCodes.contains(containerCode)) {
							//contextValidation.addError("code", "error.validationexp.container.alreadyused", containerCode);
							cv.addError("code", "error.validationexp.container.alreadyused", containerCode);// FDS correction
						} else {
							outputContainerCodes.add(containerCode);
						}
					}
					cv.removeKeyFromRootKeyName(rootKeyName2);
				}
			}
			
			// remonter les erreurs au contexte parent s'il y en a
			if (cv.hasErrors()) {
				contextValidation.addErrors(cv.getErrors());
			}
			cv.removeKeyFromRootKeyName(rootKeyName);
		});
	}
	
	/** Vérifier que les samples a créer :
	 *  1) n'existent pas deja en base
	 *  2) ne sont pas en cours de création dans une autre expérience
	 *  3) le meme sample code ne doit pas etre specifié plusieurs fois
	 * @author fdsantos
	 * @since 19/09/2019
	 * @param atomicTransfertMethods ...
	 * @param contextValidation...
	 *  assume AtomicTransfertMethod is oneToOne !!! => atm.outputContainerUseds.get(0)
	 *  assume state is "N"
	 */
	private static void validateNewSamples(Experiment                  experiment,
										   List<AtomicTransfertMethod> atomicTransfertMethods, 
										   ContextValidation            contextValidation) {
		
		Set<String> sampleCodes = new TreeSet<>(); // uniquement pour validateUniqueSampleCodeInsideExperiment 
		
		IntStream.range(0, atomicTransfertMethods.size()).forEach(i -> {
			ContextValidation cv = ContextValidation.createUndefinedContext(contextValidation.getUser());
			cv.setMode(contextValidation.getMode());
			cv.setRootKeyName(contextValidation.getRootKeyName());
			String rootKeyName = "atomictransfertmethod[" + i + "]";
			cv.addKeyToRootKeyName(rootKeyName);
			cv.putObject(FIELD_EXPERIMENT, experiment);
			
			AtomicTransfertMethod atm = atomicTransfertMethods.get(i);
			List <OutputContainerUsed> outputContainerUseds = atm.outputContainerUseds;
			if (outputContainerUseds != null && outputContainerUseds.get(0)!=null && outputContainerUseds.get(0).experimentProperties.get("sampleCode")!=null) {
				// a l'etat New, outputContainerUseds.contents n'existe pas encore...=> experimentProperties.get("sampleCode").value
				String sampleCode = atm.outputContainerUseds.get(0).experimentProperties.get("sampleCode").value.toString();
				if (sampleCode != null) {
					String rootKeyName2="outputContainerUseds[0].experimentProperties.sampleCode";
					
				    //-1- vérifier si un des codes sample n'existe pas déja (samples importés par fichier ou par transfert vieux LIMS)
				    logger.debug("validateNewSamples: 1- validateSamplesNotAlreadyExists"); 
				    ///validateSampleNotAlreadyExists(sampleCode, cv);
					if (MongoDBDAO.checkObjectExistByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, sampleCode)){
						cv.addError(rootKeyName2, "error.objectexist", "Sample", sampleCode);// OUI!!!!!!!!
					}
				    
				    //-2- vérifier si un des codes sample n'est pas en cours de creation dans une autre experience
				    logger.debug("validateNewSamples: 2- validateSamplesNotUsedInOtherExperiments");
				    //validateSampleNotUsedInOtherExperiments(sampleCode, cv);
					SampleValidationHelper.validateSampleNotUsedInOtherExperiment(sampleCode, cv);

					//-3- vérifier si un des codes sample n'a pas été donné plusieurs fois dans l'expérience
					logger.debug("3-FDS-DEBUG: validateUniqueSampleCodeInsideExperiment ???");
				    //validateUniqueSampleCodeInsideExperiment(sampleCodes, sampleCode, cv);
				    if (sampleCodes.contains(sampleCode)) {

						cv.addError(rootKeyName2, "error.validationexp.sample.duplicated", sampleCode);
					} else {
						sampleCodes.add(sampleCode);
					}
				}
			}
			// remonter les erreurs au contexte parent s'il y en a
			if (cv.hasErrors()) {
				contextValidation.addErrors(cv.getErrors());
			}
		
			cv.removeKeyFromRootKeyName(rootKeyName);
		});
	}
	
	/** Vérifier que les containers en entree n'ont qu'un seul Content
	 * @author fdsantos
	 * @since 19/09/2019
	 * @param atomicTransfertMethods		list of experiment AtomicTransfertMethods
	 * @param contextValidation				validation context object
	 */		
	private static void validateContainersOnlyOneContent(List<AtomicTransfertMethod> atomicTransfertMethods, 
														ContextValidation contextValidation) {	

	    logger.debug("validateContainersWithOneContentOnly");
		
		IntStream.range(0, atomicTransfertMethods.size()).forEach(i -> {
			ContextValidation cv = ContextValidation.createUndefinedContext(contextValidation.getUser());
			cv.setMode(contextValidation.getMode());
			cv.setRootKeyName(contextValidation.getRootKeyName());
			String rootKeyName = "atomictransfertmethod[" + i + "]";
			cv.addKeyToRootKeyName(rootKeyName); 
			
			//assume oneToOne=>inputContainerUseds.get(0)
			AtomicTransfertMethod atm = atomicTransfertMethods.get(i);
			List<InputContainerUsed> inputContainerUseds = atm.inputContainerUseds;
			String containerCode = atm.inputContainerUseds.get(0).code;
			String rootKeyName2 = "inputContainerUsed[0].content";
			
			if ( inputContainerUseds.get(0).contents.size() > 1) {
				cv.addKeyToRootKeyName(rootKeyName2);
				cv.addError("size","error.validationexp.inputcontainer.moreThanOneContent",containerCode);
				cv.removeKeyFromRootKeyName(rootKeyName2);
			}
			
			// remonter les erreurs au contexte parent s'il y en a
			if (cv.hasErrors()) {
				contextValidation.addErrors(cv.getErrors());
			}
		
			cv.removeKeyFromRootKeyName(rootKeyName);
		});
	}
	
	// --------------------------------------------------------------------
	// arguments reordered

	/**
	 * Validate instrument used (context parameter {@link #FIELD_STATE_CODE}).
	 * @param instrumentUsed    instrument used to validate
	 * @param properties        properties
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateInstrumentUsed(ContextValidation, InstrumentUsed, Map)}
	 */
	@Deprecated
	public static void validateInstrumentUsed_(InstrumentUsed instrumentUsed, Map<String, PropertyValue> properties, ContextValidation contextValidation) {
		ExperimentValidationHelper.validateInstrumentUsed(contextValidation, instrumentUsed, properties);
	}

	/**
	 * Validate instrument used (context parameter {@link #FIELD_STATE_CODE}).
	 * @param contextValidation validation context
	 * @param instrumentUsed    instrument used to validate
	 * @param properties        properties
	 * @deprecated use {@link #validateInstrumentUsed(ContextValidation, InstrumentUsed, Map, String)}
	 */
	@Deprecated
	public static void validateInstrumentUsed(ContextValidation contextValidation, InstrumentUsed instrumentUsed, Map<String,PropertyValue> properties) {
//		if (ValidationHelper.validateNotEmpty(contextValidation, instrumentUsed, "instrumentUsed")) {
//			contextValidation.addKeyToRootKeyName("instrumentUsed");
//			instrumentUsed.validate(contextValidation);
//			contextValidation.removeKeyFromRootKeyName("instrumentUsed");
////			InstrumentUsedType instrumentUsedType = BusinessValidationHelper.validateRequiredDescriptionCode(contextValidation, instrumentUsed.typeCode, "typeCode", InstrumentUsedType.find.get(),true);
//			InstrumentUsedType instrumentUsedType = validateCodeForeignRequired(contextValidation, InstrumentUsedType.miniFind.get(), instrumentUsed.typeCode, "typeCode", true);
//			if (instrumentUsedType != null) {
////				String stateCode = getObjectFromContext(FIELD_STATE_CODE, String.class, contextValidation);
//				String stateCode = contextValidation.getTypedObject(FIELD_STATE_CODE);
//				contextValidation.addKeyToRootKeyName("instrumentProperties");
////				ValidationHelper.validateProperties(contextValidation, properties, instrumentUsedType.getPropertiesDefinitionDefaultLevel(), true, true, stateCode, "IP");
//				ValidationHelper.validateProperties(contextValidation, properties, instrumentUsedType.getPropertiesDefinitionInstrumentLevel(), true, true, stateCode, "IP");
//				contextValidation.removeKeyFromRootKeyName("instrumentProperties");
//			}
//		}
		String stateCode = contextValidation.getTypedObject(FIELD_STATE_CODE);
		validateInstrumentUsed(contextValidation, instrumentUsed, properties, stateCode);
	}
	
	/**
	 * Validate instrument used, with a state code for the properties to validate
	 * (using IP as default state).
	 * @param contextValidation validation context
	 * @param instrumentUsed    instrument used to validate
	 * @param properties        properties
	 * @param stateCode         properties validation state code ({@link ValidationHelper#validateProperties(ContextValidation, Map, List, boolean, boolean, String, String)}).
	 */
	public static void validateInstrumentUsed(ContextValidation contextValidation, InstrumentUsed instrumentUsed, Map<String,PropertyValue> properties, String stateCode) {
		if (ValidationHelper.validateNotEmpty(contextValidation, instrumentUsed, "instrumentUsed")) {
			contextValidation.addKeyToRootKeyName("instrumentUsed");
			instrumentUsed.validate(contextValidation);
			contextValidation.removeKeyFromRootKeyName("instrumentUsed");
			InstrumentUsedType instrumentUsedType = validateCodeForeignRequired(contextValidation, InstrumentUsedType.miniFind.get(), instrumentUsed.typeCode, "typeCode", true);
			if (instrumentUsedType != null) {
				contextValidation.addKeyToRootKeyName("instrumentProperties");
//				ValidationHelper.validateProperties(contextValidation, properties, instrumentUsedType.getPropertiesDefinitionInstrumentLevel(), true, true, stateCode, "IP");
				ValidationHelper.validateProperties(contextValidation, properties, instrumentUsedType.getPropertiesDefinitionInstrumentLevel(), true, true, stateCode, ExperimentStateNames.IP);
				contextValidation.removeKeyFromRootKeyName("instrumentProperties");
			}
		}
	}
	
	// --------------------------------------------------------------------
	// arguments reordered

	/**
	 * Apply validation rules for the given experience.
	 * @param exp               experience
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateRules(ContextValidation, Experiment)}
	 */
	@Deprecated
	public static void validateRules(Experiment exp, ContextValidation contextValidation) {
		ExperimentValidationHelper.validateRules(contextValidation, exp);
	}

	/**
	 * Apply validation rules for the given experience (context argument FIELD_STATE_CODE).
	 * See {@link #validateRulesWithList(ContextValidation, List)}.
	 * @param contextValidation validation context
	 * @param exp               experience
	 */
	public static void validateRules(ContextValidation contextValidation, Experiment exp) {
		ArrayList<Object> validationfacts = new ArrayList<>();
		validationfacts.add(exp);		
		for (int i=0; i < exp.atomicTransfertMethods.size(); i++) {
			AtomicTransfertMethod atomic = exp.atomicTransfertMethods.get(i);
			if (atomic.viewIndex == null)
				atomic.viewIndex = i + 1; //used to have the position in the list
			validationfacts.add(atomic);
		}
		
		State s = new State();
//		s.code = getObjectFromContext(FIELD_STATE_CODE, String.class, contextValidation);
		s.code = contextValidation.getTypedObject(FIELD_STATE_CODE);
		
		validationfacts.add(s);
		validateRulesWithList(contextValidation, validationfacts);
	}

	// ---------------------------------------------------------------------
	
	public static void validateInputContainerSupport(Set<String>              inputContainerSupportCodes,
			                                         List<InputContainerUsed> allInputContainers,
			                                         ContextValidation        contextValidation) {
		if (validateNotEmpty(contextValidation, inputContainerSupportCodes, "inputContainerSupportCodes")) {
//			Set<String> allInputCode = allInputContainers.stream().map((InputContainerUsed i) -> i.locationOnContainerSupport.code).collect(Collectors.toSet());
			Set<String> allInputCode = allInputContainers.stream().map(i -> i.locationOnContainerSupport.code).collect(Collectors.toSet());
			if (!allInputCode.equals(inputContainerSupportCodes)) {
				contextValidation.addError("inputContainerSupportCodes", "error.inputContainerSupportCodes.notequals.allinputContainerSupportCode");
			}
		}
	}

	public static void validateOutputContainerSupport(Set<String>               outputContainerSupportCodes,
			                                          List<OutputContainerUsed> allOutputContainers,
			                                          ContextValidation         contextValidation) {
//		String stateCode = getObjectFromContext(FIELD_STATE_CODE, String.class, contextValidation);
		String stateCode = contextValidation.getTypedObject(FIELD_STATE_CODE);
		
		if (! "N".equals(stateCode)) {
			if (validateNotEmpty(contextValidation, outputContainerSupportCodes, "outputContainerSupportCodes")) {
//				Set<String> allInputCode = allOutputContainers.stream().map((OutputContainerUsed i) -> i.locationOnContainerSupport.code).collect(Collectors.toSet());
				Set<String> allInputCode = allOutputContainers.stream().map(i -> i.locationOnContainerSupport.code).collect(Collectors.toSet());
				if (!allInputCode.equals(outputContainerSupportCodes)) {
					contextValidation.addError("outputContainerSupportCodes", "error.validationexp.outputContainerSupportCodes");
				}
			}
		}
	}

	// ------------------------------------------------------------------
	// arguments reordered
	
	/**
	 * Validate an optional list of required comments.  
	 * @param contextValidation validation context
	 * @param comments          list of comment to validate
	 * @deprecated use {@link #validateComments(ContextValidation, List)}
	 */
	@Deprecated
	public static void validateComments(List<Comment> comments, ContextValidation contextValidation) {
		ExperimentValidationHelper.validateComments(contextValidation, comments);
	}
	
//	public static void validateComments(List<Comment> comments, ContextValidation contextValidation) {
//		if (comments != null && comments.size() > 0) {
//			for (int i = 0; i < comments.size(); i++) {
//				String rootKeyName = "comments[" + i + "]";
//				if (comments.get(i) != null) {
//					contextValidation.addKeyToRootKeyName(rootKeyName);
//					comments.get(i).validate(contextValidation);
//					contextValidation.removeKeyFromRootKeyName(rootKeyName);
//				} else {
//					contextValidation.addError(rootKeyName, "error.validationexp.comments.null");
//				}
//			}
//		}
//	}
	
	/**
	 * Validate an optional list of required comments.  
	 * @param contextValidation validation context
	 * @param comments          list of comment to validate
	 */
	public static void validateComments(ContextValidation contextValidation, List<Comment> comments) {
		if (comments == null) 
			return;
		for (int i = 0; i < comments.size(); i++) {
			String rootKeyName = "comments[" + i + "]";
			if (comments.get(i) != null) {
				contextValidation.addKeyToRootKeyName(rootKeyName);
				comments.get(i).validate(contextValidation);
				contextValidation.removeKeyFromRootKeyName(rootKeyName);
			} else {
				contextValidation.addError(rootKeyName, "error.validationexp.comments.null");
			}
		}
	}

	// ----------------------------------------------------------------------
	
	/**
	 * Méthode permettant de valider les codes de stockage pour une expérience donnée.
	 * Similaire à la méthode validateStorageCodes() mais ici on vérifier sur la totalité
	 * des containers du support et pas que sur les containers de l'expérience.
	 * 
	 * @param categoryCode La catégorie de l'expérience.
	 * @param atomicTransfertMethods La liste des différentes ATM de l'expérience.
	 * @param contextValidation Le contexte de validation à utiliser pour la validation.
	 */
	public static void validateGlobalStorageCodes(String categoryCode, List<AtomicTransfertMethod> atomicTransfertMethods, ContextValidation contextValidation) {
		Map<String, Set<String>> storageMap = new HashMap<>();

		atomicTransfertMethods.forEach(atm -> {
			if (ExperimentCategory.CODE.qualitycontrol.toString().equals(categoryCode)) {
				atm.inputContainerUseds.forEach(extractStorageCodesGlobal(storageMap));
			} else if (atm.outputContainerUseds != null) {
				atm.outputContainerUseds.forEach(extractStorageCodesGlobal(storageMap));
			} else {
				logger.debug("no validation of storageCodes");
			}
		});

		Map<String, Set<String>> mapCont = new HashMap<>();

		atomicTransfertMethods.forEach(atm -> {
			if (ExperimentCategory.CODE.qualitycontrol.toString().equals(categoryCode)) {
				atm.inputContainerUseds.forEach(extractContainerNamesGlobal(mapCont));
			} else if (atm.outputContainerUseds != null) {
				atm.outputContainerUseds.forEach(extractContainerNamesGlobal(mapCont));
			} 
		});

		storageMap.forEach((key, values) -> {
			if (values.size() == 1 && key != null && !values.iterator().next().isEmpty()) {
				List<Container> containersList = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.is("support.code", key)).toList();
		
				if (containersList.size() > mapCont.get(key).size()) {
					for (int i = 0; i < containersList.size(); i++) {
						Container cont = containersList.get(i);
						Iterator<String> it = values.iterator();
						boolean isStopped = false;
							
						while (it.hasNext()) {
							String stCode = it.next();
		
							if (cont.support != null && ((cont.support.storageCode == null && stCode != null) || (cont.support.storageCode != null && !cont.support.storageCode.equals(stCode)))) {
								contextValidation.addError("storageCode","experiments.atm.error.storageCode.withoutContainer", key, containersList.get(i).code);
								isStopped = true;
								break; 
							}
						}
		
						if (isStopped) {
							break;
						}
					}
				}
			}
		});
	}

	/**
	 * Check if storageCodes are correctly defined for containers used into AtomicTransfertMethod.
	 * @param categoryCode				categoryCode of Experiment
	 * @param atomicTransfertMethods	atomicTransfertMethods
	 * @param contextValidation			context of validation
	 */
	private static void validateStorageCodes(String                      categoryCode, 
			                                 List<AtomicTransfertMethod> atomicTransfertMethods,
			                                 ContextValidation           contextValidation) {		
		Map<String, Set<String>> storageMap = new HashMap<>();
		// extract storageCodes
		atomicTransfertMethods.forEach(atm -> {
			if (ExperimentCategory.CODE.qualitycontrol.toString().equals(categoryCode)) {
				atm.inputContainerUseds.forEach(extractStorageCodes(storageMap));
			} else if (atm.outputContainerUseds != null) {
				atm.outputContainerUseds.forEach(extractStorageCodes(storageMap));
			} else {
				logger.debug("no validation of storageCodes");
			}
			
//			if (atm.outputContainerUseds == null &&	ExperimentCategory.CODE.qualitycontrol.toString().equals(categoryCode)) {
//				atm.inputContainerUseds.forEach(extractStorageCodes(storageMap));
//			} else if (atm.outputContainerUseds != null) {
//				atm.outputContainerUseds.forEach(extractStorageCodes(storageMap));
//			}
		});
		// validate storageCodes
		storageMap.forEach((key, values) -> {
			if (values.size() != 1 && key != null) { 
				contextValidation.addError("storageCode","experiments.atm.error.storageCode", key, values.toString());
			} // else no error
		});
	}

	private static Consumer<AbstractContainerUsed> extractStorageCodesGlobal(Map<String, Set<String>> storageMap) {
		return cu -> {
			String key   = cu.locationOnContainerSupport.code;
			String value = (cu.locationOnContainerSupport.storageCode == null) ? "" : cu.locationOnContainerSupport.storageCode;

			if (storageMap.containsKey(key)) {
				storageMap.get(key).add(value);
			} else {
				storageMap.put(key, new HashSet<>(Arrays.asList(value)));
			}
		};
	}

	private static Consumer<AbstractContainerUsed> extractContainerNamesGlobal(Map<String, Set<String>> contMap) {
		return cu -> {
			String key   = cu.locationOnContainerSupport.code;
			String value = cu.code;

			if (contMap.containsKey(key)) {
				contMap.get(key).add(value);
			} else {
				contMap.put(key, new HashSet<>(Arrays.asList(value)));
			}
		};
	}


	private static Consumer<AbstractContainerUsed> extractStorageCodes(Map<String, Set<String>> storageMap) {
		return cu -> {
			// whatever the category of support we don't check that (generic approach)
			//if (ContainerCategory.find.getInstance().hasMultipleContainers(cu.locationOnContainerSupport.categoryCode)) {
			String key   = cu.locationOnContainerSupport.code;
			String value = (cu.locationOnContainerSupport.storageCode == null) ? "" : cu.locationOnContainerSupport.storageCode;
			if (storageMap.containsKey(key)) {
				storageMap.get(key).add(value);
			} else {
				storageMap.put(key, new HashSet<>(Arrays.asList(value)));
			}
			//} 
			// the type of support doesn't involve any verification
			// else {}
		};
	}
	
}
