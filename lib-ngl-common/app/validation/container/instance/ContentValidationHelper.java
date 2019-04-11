package validation.container.instance;

import static validation.utils.ValidationHelper.validateNotEmpty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.sample.description.ImportType;
import models.laboratory.sample.description.SampleCategory;
import models.laboratory.sample.description.SampleType;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.utils.ValidationConstants;
import validation.utils.ValidationHelper;

public class ContentValidationHelper extends CommonValidationHelper {

	// ---------------------------------------------------------------
	// renamed and argument reordered
	
	/**
	 * Validate a required sample type code.
	 * @param sampleTypeCode    sample type code to validate
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateSampleTypeCodeRequired(ContextValidation, String)}
	 */
	@Deprecated
	public static void validateSampleTypeCode_(String sampleTypeCode, ContextValidation contextValidation) {
		ContentValidationHelper.validateSampleTypeCodeRequired(contextValidation, sampleTypeCode);
	}

	/**
	 * Validate a required sample type code.
	 * @param contextValidation validation context
	 * @param sampleTypeCode    sample type code to validate
	 */
	public static void validateSampleTypeCodeRequired(ContextValidation contextValidation, String sampleTypeCode) {
//		BusinessValidationHelper.validateRequiredDescriptionCode(contextValidation, sampleTypeCode, "sampleTypeCode",SampleType.find.get(),false);
		validateCodeForeignRequired(contextValidation, SampleType.miniFind.get(), sampleTypeCode, "sampleTypeCode", false);
	}

	// ---------------------------------------------------------------
	// renamed and argument reordered
	// This is most likely defined multiple times
	
	/**
	 * Validate a sample code.
	 * @param sampleCode        sample code to validate
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateSampleCodeRequired(ContextValidation, String)}
	 */
	@Deprecated
	public static void validateSampleCode(String sampleCode, ContextValidation contextValidation) {
		ContentValidationHelper.validateSampleCodeRequired(contextValidation, sampleCode);
	}
	
	/**
	 * Validate a required sample code.
	 * @param contextValidation validation context
	 * @param sampleCode        sample code to validate
	 */
	public static void validateSampleCodeRequired(ContextValidation contextValidation, String sampleCode) {
//		BusinessValidationHelper.validateRequiredInstanceCode(contextValidation, sampleCode, "sampleCode", Sample.class, InstanceConstants.SAMPLE_COLL_NAME, false);
//		validateRequiredInstanceCode(sampleCode, "sampleCode", Sample.class, InstanceConstants.SAMPLE_COLL_NAME, contextValidation);
		validateCodeForeignRequired(contextValidation, Sample.find.get(), sampleCode, "sampleCode");
	}

	// ---------------------------------------------------------------
	// renamed and arguments reordered
	
	/**
	 * Validate a required sample category code.
	 * @param sampleCategoryCode sample category code
	 * @param contextValidation  validation context
	 * @deprecated use {@link #validateSampleCategoryCodeRequired(ContextValidation, String)}
	 */
	@Deprecated
	public static void validateSampleCategoryCode(String sampleCategoryCode, ContextValidation contextValidation) {
		ContentValidationHelper.validateSampleCategoryCodeRequired(contextValidation, sampleCategoryCode);
	}

	/**
	 * Validate a required sample category code.
	 * @param contextValidation  validation context
	 * @param sampleCategoryCode sample category code
	 */
	public static void validateSampleCategoryCodeRequired(ContextValidation contextValidation, String sampleCategoryCode) {
//		BusinessValidationHelper.validateRequiredDescriptionCode(contextValidation, sampleCategoryCode, "sampleCategoryCode", SampleCategory.find.get(),false);
		validateCodeForeignRequired(contextValidation, SampleCategory.miniFind.get(), sampleCategoryCode, "sampleCategoryCode", false);
	}

	// ---------------------------------------------------------------
	// renamed and arguments reordered
	
	/**
	 * Validate a percentage (in [0,100] ).
	 * @param percentage        percentage value to validate
	 * @param contextValidation validation context
	 * @deprecated use {@link #validatePercentageContentRequired(ContextValidation, Double)}
	 */
	@Deprecated
	public static void validatePercentageContent_(Double percentage, ContextValidation contextValidation) {
		ContentValidationHelper.validatePercentageContentRequired(contextValidation, percentage);
	}

	/**
	 * Validate a required percentage (value in [0,100] ).
	 * @param contextValidation validation context
	 * @param percentage        percentage value to validate
	 */
	public static void validatePercentageContentRequired(ContextValidation contextValidation, Double percentage) {
		if (validateNotEmpty(contextValidation, percentage, "percentage")) {
			// percentage is mandatory
			if (percentage < 0.0 || percentage > 100.00) {
				contextValidation.addError("percentage", ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG, percentage);
			}
		}
	}

	// ---------------------------------------------------------------
	// arguments reordered 
	
	/**
	 * Validate that the project code is in the sample project codes in the database.
	 * @param projectCode       project code
	 * @param sampleCode        sample code
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateSampleCodeWithProjectCode(ContextValidation, String, String)}
	 */
	@Deprecated
	public static void validateSampleCodeWithProjectCode_(String projectCode, String sampleCode, ContextValidation contextValidation) {
		ContentValidationHelper.validateSampleCodeWithProjectCode(contextValidation, projectCode, sampleCode);
	}

	/**
	 * Validate that the project code is in the sample project codes in the database.
	 * @param contextValidation validation context
	 * @param projectCode       project code
	 * @param sampleCode        sample code
	 */
	public static void validateSampleCodeWithProjectCode(ContextValidation contextValidation, String projectCode, String sampleCode) {
		if (!checkSampleWithProject(projectCode, sampleCode)) {
			contextValidation.addError("sample", ValidationConstants.ERROR_NOTEXISTS_MSG, projectCode + " + " + sampleCode);
		}
	}

	private static boolean checkSampleWithProject(String projectCode, String sampleCode) {
		return MongoDBDAO.checkObjectExist(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, 
						DBQuery.is("code", sampleCode).in("projectCodes", projectCode));
	}

	// ----------------------------------------------------------------
	// arguments reordered
	
	/**
	 * Validate content properties for an optional context parameter
	 * (context parameter {@link #FIELD_IMPORT_TYPE_CODE}).
	 * @param sampleTypeCode    sample type code
	 * @param properties        properties
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateProperties(ContextValidation, String, Map)}
	 */
	@Deprecated
	public static void validateProperties(String sampleTypeCode, Map<String, PropertyValue> properties, ContextValidation contextValidation) {
		ContentValidationHelper.validateProperties(contextValidation, sampleTypeCode, properties);
	}
	
	/**
	 * Validate content properties for an optional context parameter
	 * (context parameter optional {@link #FIELD_IMPORT_TYPE_CODE}).
	 * @param contextValidation validation context
	 * @param sampleTypeCode    sample type code
	 * @param properties        properties
	 * @deprecated use {@link #validateProperties(ContextValidation, String, Map, String)}
	 */
	@Deprecated
	public static void validateProperties(ContextValidation contextValidation, String sampleTypeCode, Map<String, PropertyValue> properties) {
		String importTypeCode = (String) contextValidation.getObject(FIELD_IMPORT_TYPE_CODE);
//		List<PropertyDefinition> proDefinitions = new ArrayList<>();
//		if (importTypeCode != null) {
////			ImportType importType = BusinessValidationHelper.validateExistDescriptionCode(contextValidation, importTypeCode,"importTypeCode", ImportType.find.get(),true);
//			ImportType importType = validateCodeForeignOptional(contextValidation, ImportType.miniFind.get(), importTypeCode, "importTypeCode", true);
//			if (importType != null) {
//				proDefinitions.addAll(importType.getPropertiesDefinitionContentLevel());
//			}
//		}
////		SampleType sampleType = BusinessValidationHelper.validateRequiredDescriptionCode(contextValidation, sampleTypeCode, "sampleTypeCode", SampleType.find.get(),true);
//		SampleType sampleType = validateCodeForeignRequired(contextValidation, SampleType.miniFind.get(), sampleTypeCode, "sampleTypeCode", true);
//		if (sampleType != null) {
//			proDefinitions.addAll(sampleType.getPropertiesDefinitionContentLevel());				
//		}
//		if (proDefinitions.size() > 0) {
//			ValidationHelper.validateProperties(contextValidation, properties, proDefinitions, false);
//		}
		validateProperties(contextValidation, sampleTypeCode, properties, importTypeCode);
	}
	
	/**
	 * Validate properties using for a required sample type and an optional import type.
	 * @param contextValidation validation context
	 * @param sampleTypeCode    required sample type code
	 * @param properties        properties
	 * @param importTypeCode    optional import type code
	 */
	public static void validateProperties(ContextValidation contextValidation, String sampleTypeCode, Map<String, PropertyValue> properties, String importTypeCode) {
		List<PropertyDefinition> proDefinitions = new ArrayList<>();
		if (importTypeCode != null) {
//			ImportType importType = BusinessValidationHelper.validateExistDescriptionCode(contextValidation, importTypeCode,"importTypeCode", ImportType.find.get(),true);
			ImportType importType = validateCodeForeignOptional(contextValidation, ImportType.miniFind.get(), importTypeCode, "importTypeCode", true);
			if (importType != null) {
				proDefinitions.addAll(importType.getPropertiesDefinitionContentLevel());
			}
		}
//		SampleType sampleType = BusinessValidationHelper.validateRequiredDescriptionCode(contextValidation, sampleTypeCode, "sampleTypeCode", SampleType.find.get(),true);
		SampleType sampleType = validateCodeForeignRequired(contextValidation, SampleType.miniFind.get(), sampleTypeCode, "sampleTypeCode", true);
		if (sampleType != null) {
			proDefinitions.addAll(sampleType.getPropertiesDefinitionContentLevel());				
		}
		if (proDefinitions.size() > 0) {
			ValidationHelper.validateProperties(contextValidation, properties, proDefinitions, false);
		}
	}

}
