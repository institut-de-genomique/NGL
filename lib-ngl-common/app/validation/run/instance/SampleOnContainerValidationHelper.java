package validation.run.instance;

import models.laboratory.sample.description.SampleCategory;
import models.laboratory.sample.description.SampleType;
import models.laboratory.sample.instance.Sample;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.utils.ValidationConstants;
import validation.utils.ValidationHelper;

public class SampleOnContainerValidationHelper extends CommonValidationHelper {
	
	public static void validateSampleCode(String sampleCode, ContextValidation contextValidation) {
//		BusinessValidationHelper.validateRequiredInstanceCode(contextValidation, sampleCode, "sampleCode", Sample.class,InstanceConstants.SAMPLE_COLL_NAME);
//		validateRequiredInstanceCode(sampleCode, "sampleCode", Sample.class,InstanceConstants.SAMPLE_COLL_NAME, contextValidation);
		validateCodeForeignRequired(contextValidation, Sample.find.get(), sampleCode, "sampleCode");
	}
	
	public static SampleType validateSampleTypeCode(String sampleTypeCode, ContextValidation contextValidation) {
		return validateCodeForeignRequired(contextValidation, SampleType.miniFind.get(), sampleTypeCode, "typeCode",true);
	}
	
	public static void validateSampleCategoryCode(String sampleCategoryCode, SampleType sampleType, ContextValidation contextValidation) {
		if (ValidationHelper.validateNotEmpty(contextValidation, sampleCategoryCode, "sampleCategoryCode")) {
			SampleCategory sc = validateCodeForeignOptional(contextValidation, SampleCategory.miniFind.get(), sampleCategoryCode, "sampleCategoryCode", true);
			if (!sampleType.category.equals(sc)) {
				contextValidation.addError("categorySampleCode", ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG, sampleCategoryCode);
			}
		}
	}
	
}
