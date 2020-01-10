package validation.experiment.instance;

import models.laboratory.container.description.ContainerSupportCategory;
import models.laboratory.instrument.description.Instrument;
import models.laboratory.instrument.description.InstrumentCategory;
import models.laboratory.instrument.description.InstrumentUsedType;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;

public class InstrumentUsedValidationHelper extends CommonValidationHelper {

	// ---------------------------------------------------------
	// renamed and arguments reordered
	
	/**
	 * Validate a required instrument used type code ({@link InstrumentUsedType}).
	 * @param instrumentUsedTypeCode code to validate
	 * @param contextValidation      validation context
	 * @deprecated use {@link #validateInstrumentUsedTypeCodeRequired(ContextValidation, String)}
	 */
	@Deprecated
	public static void validationTypeCode(String instrumentUsedTypeCode, ContextValidation contextValidation) {
		InstrumentUsedValidationHelper.validateInstrumentUsedTypeCodeRequired(contextValidation, instrumentUsedTypeCode);
	}
	
	/**
	 * Validate a required instrument used type code ({@link InstrumentUsedType}).
	 * @param contextValidation      validation context
	 * @param instrumentUsedTypeCode code to validate
	 */
	public static void validateInstrumentUsedTypeCodeRequired(ContextValidation contextValidation, String instrumentUsedTypeCode) {
//		BusinessValidationHelper.validateRequiredDescriptionCode(contextValidation, instrumentUsedTypeCode, "typeCode", InstrumentUsedType.find.get());	
		validateCodeForeignRequired(contextValidation, InstrumentUsedType.miniFind.get(), instrumentUsedTypeCode, "typeCode");	
	}

	// -----------------------------------------------------------
	// renamed and arguments reordered
	
	/**
	 * Validate a required instrument code.
	 * @param code              code to validate
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateInstrumentCodeRequired(ContextValidation, String)}
	 */
	@Deprecated
	public static void validationCode(String code, ContextValidation contextValidation) {
		InstrumentUsedValidationHelper.validateInstrumentCodeRequired(contextValidation, code);
	}

	/**
	 * Validate a required instrument code.
	 * @param contextValidation validation context
	 * @param code              code to validate
	 */
	public static void validateInstrumentCodeRequired(ContextValidation contextValidation, String code) {
//		BusinessValidationHelper.validateRequiredDescriptionCode(contextValidation, code, "code", Instrument.find.get());
		validateCodeForeignRequired(contextValidation, Instrument.miniFind.get(), code, "code");		
	}

	// -----------------------------------------------------------
	// renamed and arguments reordered

	/**
	 * Validate a required instrument category code. 
	 * @param categoryCode      instrument category code to validate
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateInstrumentCategoryCodeRequired(ContextValidation, String)}
	 */
	@Deprecated
	public static void validationCategoryCode(String categoryCode, ContextValidation contextValidation) {
		InstrumentUsedValidationHelper.validateInstrumentCategoryCodeRequired(contextValidation, categoryCode);
	}

	/**
	 * Validate a required instrument category code. 
	 * @param contextValidation validation context
	 * @param categoryCode      instrument category code to validate
	 */
	public static void validateInstrumentCategoryCodeRequired(ContextValidation contextValidation, String categoryCode) {
//		BusinessValidationHelper.validateRequiredDescriptionCode(contextValidation, categoryCode, "categoryCode", InstrumentCategory.find.get());
		validateCodeForeignRequired(contextValidation, InstrumentCategory.miniFind.get(), categoryCode, "categoryCode");
	}

	// -----------------------------------------------------------
	// factored
	
	/**
	 * Validate a required container support category code ({@link ContainerSupportCategory}).
	 * @param contextValidation            validation context
	 * @param containerSupportCategoryCode container support category code
	 * @param field                        name of validated field
	 */
	public static void validateContainerSupportCategoryCodeRequired(ContextValidation contextValidation, String containerSupportCategoryCode, String field) {
		validateCodeForeignRequired(contextValidation, ContainerSupportCategory.miniFind.get(), containerSupportCategoryCode, field);				
	}
	
	// -----------------------------------------------------------
	// renamed and arguments reordered
	
	/**
	 * Validate a required input container support category code ({@link ContainerSupportCategory}).
	 * @param inContainerSupportCategoryCode container support category code
	 * @param contextValidation              validation context
	 * @deprecated use {@link #validateInContainerSupportCategoryCodeRequired(ContextValidation, String)}
	 */
	@Deprecated
	public static void validationInContainerSupportCategoryCode_(String inContainerSupportCategoryCode, ContextValidation contextValidation) {
		InstrumentUsedValidationHelper.validateInContainerSupportCategoryCodeRequired(contextValidation,	inContainerSupportCategoryCode);
	}

	/**
	 * Validate a required input container support category code ({@link ContainerSupportCategory}).
	 * @param contextValidation              validation context
	 * @param inContainerSupportCategoryCode container support category code
	 */
	public static void validateInContainerSupportCategoryCodeRequired(ContextValidation contextValidation, String inContainerSupportCategoryCode) {
//		BusinessValidationHelper.validateRequiredDescriptionCode(contextValidation, inContainerSupportCategoryCode, "inContainerSupportCategoryCode", ContainerSupportCategory.find.get());				
//		validateCodeForeignRequired(contextValidation, ContainerSupportCategory.miniFind.get(), inContainerSupportCategoryCode, "inContainerSupportCategoryCode");				
		validateContainerSupportCategoryCodeRequired(contextValidation, inContainerSupportCategoryCode, "inContainerSupportCategoryCode");				
	}

	// -----------------------------------------------------------

	/**
	 * Validate a required output container support category code ({@link ContainerSupportCategory}).
	 * @param contextValidation               validation context
	 * @param outContainerSupportCategoryCode container support category code
	 * @deprecated use {@link #validateOutContainerSupportCategoryCodeRequired(ContextValidation, String)}
	 */
	@Deprecated
	public static void validationOutContainerSupportCategoryCode_(String outContainerSupportCategoryCode, ContextValidation contextValidation) {
		InstrumentUsedValidationHelper.validateOutContainerSupportCategoryCodeRequired(contextValidation, outContainerSupportCategoryCode);
	}

	/**
	 * Validate a required output container support category code ({@link ContainerSupportCategory}).
	 * @param contextValidation               validation context
	 * @param outContainerSupportCategoryCode container support category code
	 */
	public static void validateOutContainerSupportCategoryCodeRequired(ContextValidation contextValidation, String outContainerSupportCategoryCode) {
//		BusinessValidationHelper.validateRequiredDescriptionCode(contextValidation, outContainerSupportCategoryCode, "outContainerSupportCategoryCode", ContainerSupportCategory.find.get());
//		validateCodeForeignRequired(contextValidation, ContainerSupportCategory.miniFind.get(), outContainerSupportCategoryCode, "outContainerSupportCategoryCode");
		validateContainerSupportCategoryCodeRequired(contextValidation, outContainerSupportCategoryCode, "outContainerSupportCategoryCode");
	}

}
