package validation.experiment.instance;

import java.math.BigDecimal;
import java.util.List;

import models.laboratory.experiment.instance.InputContainerUsed;
import models.laboratory.experiment.instance.OutputContainerUsed;
import models.laboratory.instrument.instance.InstrumentUsed;
import ngl.refactoring.state.StateNames;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.utils.ValidationConstants;
import validation.utils.ValidationHelper;

public class AtomicTransfertMethodValidationHelper extends CommonValidationHelper {
	
	// ------------------------------------------------------------------
	// renamed and arguments reordered
	
	/**
	 * Validate that the output container used collection is either null (as in 'optional') or
	 * that it contains at most one element.  
	 * @param outputContainerUseds collection to validate
	 * @param contextValidation    validation context
	 * @deprecated use {@link #validateOutputContainerAtMostOne(List, ContextValidation)}
	 */
	@Deprecated
	public static void validateOneOutputContainer(List<OutputContainerUsed> outputContainerUseds, ContextValidation contextValidation) {
		AtomicTransfertMethodValidationHelper.validateOutputContainerAtMostOne(outputContainerUseds, contextValidation);
	}

	/**
	 * Validate that the output container used collection is either null (as in 'optional') or
	 * that it contains at most one element.  
	 * @param outputContainerUseds collection to validate
	 * @param contextValidation    validation context
	 */
//	public static void validateOutputContainerAtMostOne(List<OutputContainerUsed> outputContainerUseds, ContextValidation contextValidation) {					
//		if (outputContainerUseds != null && outputContainerUseds.size() > 1) {						
//			contextValidation.addError("outputContainerUseds", ValidationConstants.ERROR_BADSIZEARRAY, outputContainerUseds.size(), "1");
//		}
//	}
	public static void validateOutputContainerAtMostOne(List<OutputContainerUsed> outputContainerUseds, ContextValidation contextValidation) {					
		if (outputContainerUseds        == null) return;
		if (outputContainerUseds.size() <= 1   ) return;						
		contextValidation.addError("outputContainerUseds", ValidationConstants.ERROR_BADSIZEARRAY, outputContainerUseds.size(), "1");
	}
	
	// ------------------------------------------------------------------
	
	/**
	 * Validate that the output container used collection is empty (null or size 0).
	 * @param outputContainerUseds collection to validate
	 * @param contextValidation    validation context
	 * @deprecated use {@link #validateOutputContainerNone(ContextValidation, List)}
	 */
	@Deprecated
	public static void validateVoidOutputContainer(List<OutputContainerUsed> outputContainerUseds, ContextValidation contextValidation) {
		AtomicTransfertMethodValidationHelper.validateOutputContainerNone(contextValidation, outputContainerUseds);
	}

	/**
	 * Validate that the output container used collection is empty (null or size 0).
	 * @param contextValidation    validation context
	 * @param outputContainerUseds collection to validate
	 */
//	public static void validateOutputContainerNone(List<OutputContainerUsed> outputContainerUseds,ContextValidation contextValidation) {
//		if (outputContainerUseds != null && outputContainerUseds.size() != 0) {							
//			contextValidation.addError("outputContainerUseds", ValidationConstants.ERROR_BADSIZEARRAY, outputContainerUseds.size(), "0");
//		}	
//	}
	public static void validateOutputContainerNone(ContextValidation contextValidation, List<OutputContainerUsed> outputContainerUseds) {
		if (outputContainerUseds        == null) return;
		if (outputContainerUseds.size() == 0   ) return;							
		contextValidation.addError("outputContainerUseds", ValidationConstants.ERROR_BADSIZEARRAY, outputContainerUseds.size(), "0");
	}
	
	// ------------------------------------------------------------------

	/**
	 * Validate that the input container list is of size 1.
	 * @param inputContainers   input container list to validate
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateInputContainerOne(ContextValidation, List)} 
	 */
	@Deprecated
	public static void validateOneInputContainer_(List<InputContainerUsed> inputContainers,	ContextValidation contextValidation) {
		AtomicTransfertMethodValidationHelper.validateInputContainerOne(contextValidation, inputContainers);
	}

	/**
	 * Validate that the input container list is of size 1.
	 * @param contextValidation validation context
	 * @param inputContainers   input container list to validate
	 */
	public static void validateInputContainerOne(ContextValidation contextValidation, List<InputContainerUsed> inputContainers) {		
		if (inputContainers.size() != 1) {							
			contextValidation.addError("inputContainerUseds", ValidationConstants.ERROR_BADSIZEARRAY, inputContainers.size(), "1");
		}			
	}

	// ------------------------------------------------------------------
	
	/**
	 * Validate a collection of input container used
	 * (context parameters {@link CommonValidationHelper#FIELD_TYPE_CODE} {@link CommonValidationHelper#FIELD_STATE_CODE}
	 * {@link CommonValidationHelper#FIELD_INST_USED} {@link CommonValidationHelper#FIELD_STATE_CODE}).
	 * @param contextValidation   validation context
	 * @param inputContainerUseds collection of input container used to validate
	 * @deprecated use {@link #validateInputContainers(ContextValidation, List, String, String, InstrumentUsed)}
	 */
	@Deprecated
//	public static void validateInputContainers(ContextValidation contextValidation,	List<InputContainerUsed> inputContainerUseds) {
//		if (ValidationHelper.validateNotEmpty(contextValidation, inputContainerUseds, "inputContainerUseds")) {
//			int i = 1;
//			double percentage = 0.0;
//			for (InputContainerUsed icu : inputContainerUseds) {
//				contextValidation.addKeyToRootKeyName("inputContainerUseds["+i+"]");
//				percentage += icu.percentage.doubleValue();
//				icu.validate(contextValidation);
//				contextValidation.removeKeyFromRootKeyName("inputContainerUseds["+i+++"]");
//			}
//			if (!(percentage >= 99.9 && percentage <= 100)) {
//				contextValidation.addError("inputContainerUseds", "error.validationexp.percentperoutputcontainerdefault", percentage);
//			}
//		}
//	}
	public static void validateInputContainers(ContextValidation contextValidation,	List<InputContainerUsed> inputContainerUseds) {
		if (! ValidationHelper.validateNotEmpty(contextValidation, inputContainerUseds, "inputContainerUseds"))
			return;
		int i = 1;
		BigDecimal bdPercentage = BigDecimal.ZERO;
		for (InputContainerUsed icu : inputContainerUseds) {
			contextValidation.addKeyToRootKeyName("inputContainerUseds["+i+"]");
			bdPercentage = bdPercentage.add(BigDecimal.valueOf(icu.percentage.doubleValue()));
			icu.validate(contextValidation);
			contextValidation.removeKeyFromRootKeyName("inputContainerUseds["+i+++"]");
		}
		double percentage = bdPercentage.doubleValue();
		if (!(percentage >= 99.9 && percentage <= 100)) {
			contextValidation.addError("inputContainerUseds", "error.validationexp.percentperoutputcontainerdefault", percentage);
		}
	}

	/**
	 * Validate that the input container list is not empty and that the percentage
	 * sum of all containers is roughly 100. 
	 * @param contextValidation   validation context
	 * @param inputContainerUseds input containers to validate
	 * @param experimentTypeCode  experiment type code
	 * @param stateCode           state code
	 * @param instrumentUsed      instrument used
	 */
	public static void validateInputContainers(ContextValidation contextValidation,	List<InputContainerUsed> inputContainerUseds, String experimentTypeCode, String stateCode, InstrumentUsed instrumentUsed) {
		if (! ValidationHelper.validateNotEmpty(contextValidation, inputContainerUseds, "inputContainerUseds"))
			return;
		int i = 1;
		BigDecimal bdPercentage = BigDecimal.ZERO;
		for (InputContainerUsed icu : inputContainerUseds) {
			String key = "inputContainerUseds["+ i++ +"]";
//			contextValidation.addKeyToRootKeyName("inputContainerUseds["+i+"]");
			contextValidation.addKeyToRootKeyName(key);
			bdPercentage = bdPercentage.add(BigDecimal.valueOf(icu.percentage.doubleValue()));
			icu.validate(contextValidation, experimentTypeCode, stateCode, instrumentUsed);
//			contextValidation.removeKeyFromRootKeyName("inputContainerUseds["+i+++"]");
			contextValidation.removeKeyFromRootKeyName(key);
		}
		double percentage = bdPercentage.doubleValue();
		if (!(percentage >= 99.9 && percentage <= 100)) {
			contextValidation.addError("inputContainerUseds", "error.validationexp.percentperoutputcontainerdefault", percentage);
		}
	}
		
	// ------------------------------------------------------------------

	/**
	 * Validate output containers.
	 * @param contextValidation    validation context
	 * @param outputContainerUseds output containers
	 * @deprecated use {@link #validateOutputContainers(ContextValidation, List, String, InstrumentUsed, String, String)}
	 */
	@Deprecated
	public static void validateOutputContainers(ContextValidation contextValidation, List<OutputContainerUsed> outputContainerUseds) {
//		String stateCode = getObjectFromContext(FIELD_STATE_CODE, String.class, contextValidation);
		String stateCode = contextValidation.getTypedObject(FIELD_STATE_CODE);
		if ("N".equals(stateCode) && outputContainerUseds != null) {
			int i = 1;
			for (OutputContainerUsed icu : outputContainerUseds) {
				contextValidation.addKeyToRootKeyName("outputContainerUseds["+i+"]");
				icu.validate(contextValidation);
				contextValidation.removeKeyFromRootKeyName("outputContainerUseds["+i+++"]");
			}
		} else if (!"N".equals(stateCode)) {
			if (ValidationHelper.validateNotEmpty(contextValidation, outputContainerUseds, "outputContainerUseds")) {
				int i = 1;
				for (OutputContainerUsed icu: outputContainerUseds) {
					contextValidation.addKeyToRootKeyName("outputContainerUseds["+i+"]");
					icu.validate(contextValidation);
					contextValidation.removeKeyFromRootKeyName("outputContainerUseds["+i+++"]");
				}
			}
		}
	}

	/**
	 * Validate output containers.
	 * @param contextValidation    validation context
	 * @param outputContainerUseds output containers to validate
	 * @param stateCode            state code
	 * @param instrumentUsed       instrument used
	 * @param importTypeCode       optional import type code
	 * @param experimentTypeCode   experiment type code
	 */
	public static void validateOutputContainers(ContextValidation contextValidation, 
			                                    List<OutputContainerUsed> outputContainerUseds, 
			                                    String stateCode, 
			                                    InstrumentUsed instrumentUsed, 
			                                    String importTypeCode, 
			                                    String experimentTypeCode) {
		if ("N".equals(stateCode) && outputContainerUseds != null) {
			int i = 1;
			for (OutputContainerUsed icu : outputContainerUseds) {
				contextValidation.addKeyToRootKeyName("outputContainerUseds["+i+"]");
				icu.validate(contextValidation, stateCode, instrumentUsed, importTypeCode, experimentTypeCode);
				contextValidation.removeKeyFromRootKeyName("outputContainerUseds["+i+++"]");
			}
		} else if (!"N".equals(stateCode)) {
			if (ValidationHelper.validateNotEmpty(contextValidation, outputContainerUseds, "outputContainerUseds")) {
				int i = 1;
				for (OutputContainerUsed icu: outputContainerUseds) {
					contextValidation.addKeyToRootKeyName("outputContainerUseds["+i+"]");
					icu.validate(contextValidation, stateCode, instrumentUsed, importTypeCode, experimentTypeCode);
					contextValidation.removeKeyFromRootKeyName("outputContainerUseds["+i+++"]");
				}
			}
		}
	}
	
	// ---------------------------------------------------------------
	// renamed and arguments reordered
	
	/**
	 * Validate line and column using a context state name (context parameter FIELD_STATE_CODE).
	 * If the state code is not 'N', the line and column are required ({@link ValidationHelper#validateNotEmpty(ContextValidation, Object, String)});
	 * @param contextValidation validation context
	 * @param line              line
	 * @param column            column
	 * @deprecated use explicit parameter {@link #validateLineAndColumn(ContextValidation, String, String)}
	 */
	@Deprecated
	public static void validationLineAndColumn(ContextValidation contextValidation, String line, String column) {
		AtomicTransfertMethodValidationHelper.validateLineAndColumn(contextValidation, line, column);
	}
	
	/**
	 * Validate line and column using a context state name (context parameter FIELD_STATE_CODE).
	 * If the state code is not 'N', the line and column are required 
	 * ({@link ValidationHelper#validateNotEmpty(ContextValidation, Object, String)}).
	 * @param contextValidation validation context
	 * @param line              line
	 * @param column            column
	 * @deprecated use explicit parameter {@link #validateLineAndColumn(ContextValidation, String, String, String)}
	 */
	@Deprecated
	public static void validateLineAndColumn(ContextValidation contextValidation, String line, String column) {
//		String stateCode = getObjectFromContext(FIELD_STATE_CODE, String.class, contextValidation);
		String stateCode = contextValidation.getTypedObject(FIELD_STATE_CODE);
		if (!"N".equals(stateCode)) {
			ValidationHelper.validateNotEmpty(contextValidation, line,   "line");
			ValidationHelper.validateNotEmpty(contextValidation, column, "column");			
		}
	}

	/**
	 * Validate line and column using a state name.
	 * If the state code is not {@link StateNames#N}, the line and column are required 
	 * ({@link ValidationHelper#validateNotEmpty(ContextValidation, Object, String)}).
	 * @param contextValidation validation context
	 * @param stateCode         the state code of some enclosing object
	 * @param line              line
	 * @param column            column
	 */
	public static void validateLineAndColumn(ContextValidation contextValidation, String stateCode, String line, String column) {
		if (!StateNames.N.equals(stateCode)) {
			ValidationHelper.validateNotEmpty(contextValidation, line,   "line");
			ValidationHelper.validateNotEmpty(contextValidation, column, "column");			
		}
	}

}
