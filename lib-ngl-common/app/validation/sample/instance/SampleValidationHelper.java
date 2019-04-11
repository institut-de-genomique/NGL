package validation.sample.instance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.instance.PropertyValue;
// import models.laboratory.common.instance.State;
// import models.laboratory.experiment.instance.AtomicTransfertMethod;
// import models.laboratory.experiment.instance.Experiment;
import models.laboratory.sample.description.ImportType;
import models.laboratory.sample.description.SampleCategory;
import models.laboratory.sample.description.SampleType;
import models.laboratory.sample.instance.Sample;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.utils.ValidationHelper;


public class SampleValidationHelper extends CommonValidationHelper {

	// ---------------------------------------------------------------------
	// renamed and arguments reordered
	
	/**
	 * Validate sample category code.
	 * @param categoryCode      sample category code
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateSampleCategoryCodeRequired(ContextValidation, String)}
	 */
	@Deprecated
	public static void validateSampleCategoryCode_(String categoryCode, ContextValidation contextValidation) {
		SampleValidationHelper.validateSampleCategoryCodeRequired(contextValidation, categoryCode);
	}

	/**
	 * Validate sample category code.
	 * @param contextValidation validation context
	 * @param categoryCode      sample category code
	 */
	public static void validateSampleCategoryCodeRequired(ContextValidation contextValidation, String categoryCode) {
//		BusinessValidationHelper.validateRequiredDescriptionCode(contextValidation, categoryCode, "categoryCode", SampleCategory.find.get(),false);
		validateCodeForeignRequired(contextValidation, SampleCategory.miniFind.get(), categoryCode, "categoryCode", false);
	}
	
	// -----------------------------------------------------------------------
	
//	public static void validateSampleType(String typeCode,
//										  String importTypeCode, 
//										  Map<String, PropertyValue> properties,
//										  ContextValidation contextValidation) {
//
//		List<PropertyDefinition> proDefinitions = new ArrayList<>();
////		SampleType sampleType = BusinessValidationHelper.validateRequiredDescriptionCode(contextValidation, typeCode, "typeCode", SampleType.find.get(),true);
//		SampleType sampleType = validateCodeForeignRequired(contextValidation, SampleType.miniFind.get(), typeCode, "typeCode", true);
//		if (sampleType != null) {
//			proDefinitions.addAll(sampleType.getPropertiesDefinitionDefaultLevel());				
//		}
////		ImportType importType = BusinessValidationHelper.validateRequiredDescriptionCode(contextValidation, importTypeCode,"importTypeCode", ImportType.find.get(),true);
//		ImportType importType = validateCodeForeignRequired(contextValidation, ImportType.miniFind.get(), importTypeCode, "importTypeCode", true);
//		if (importType != null) {
//			proDefinitions.addAll(importType.getPropertiesDefinitionSampleLevel());			
//		}
//		if (proDefinitions.size() > 0) {
//			ValidationHelper.validateProperties(contextValidation,properties, proDefinitions, false); // need false because we generate sample from another sample
//		}
//	}
	
	/**
	 * Validate the sample type (typeCode), the import type (importTypeCode) and the
	 * properties (properties).
	 * @param typeCode          sample type code
	 * @param importTypeCode    sample import type code
	 * @param properties        sample properties
	 * @param contextValidation validation context
	 */
	public static void validateSampleType(String                     typeCode,
			                              String                     importTypeCode, 
			                              Map<String, PropertyValue> properties,
			                              ContextValidation          contextValidation) {
		List<PropertyDefinition> proDefinitions = new ArrayList<>();
		SampleType sampleType = validateCodeForeignRequired(contextValidation, SampleType.miniFind.get(), typeCode, "typeCode", true);
		if (sampleType != null)
//			proDefinitions.addAll(sampleType.getPropertiesDefinitionDefaultLevel()); // getPropertyDefinitionByLevel(Level.CODE.Sample)			
			proDefinitions.addAll(sampleType.getPropertiesDefinitionSampleLevel());			
		ImportType importType = validateCodeForeignRequired(contextValidation, ImportType.miniFind.get(), importTypeCode, "importTypeCode", true);
		if (importType != null)
			proDefinitions.addAll(importType.getPropertiesDefinitionSampleLevel());	
		if (proDefinitions.size() > 0)
			// need false (ignore extraneous properties) because we generate sample from another sample
			ValidationHelper.validateProperties(contextValidation, properties, proDefinitions, false); 
	}

//	public static void validateRules(Sample sample,ContextValidation contextValidation) {
//		ArrayList<Object> validationfacts = new ArrayList<>();
//		validationfacts.add(sample);
//		validateRules(contextValidation, validationfacts);
//	}
	
	/**
	 * Validate rules.
	 * @param sample            sample
	 * @param contextValidation validation context
	 * @deprecated use {@link CommonValidationHelper#validateRulesWithObjects(ContextValidation, Object...)}
	 */
	@Deprecated
	public static void validateRules(Sample sample, ContextValidation contextValidation) {
//		validateRulesWithList(contextValidation, Arrays.asList(sample));
		validateRulesWithObjects(contextValidation, sample);
	}

//	public static void validateName(String name, ContextValidation contextValidation) {
//		ValidationHelper.validateNotEmpty(contextValidation, name, "name");		
//	}
	
}
