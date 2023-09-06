package validation.project.instance;

import java.util.Map;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.project.description.UmbrellaProjectCategory;
import models.laboratory.project.description.UmbrellaProjectType;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.utils.ValidationHelper;

public class UmbrellaProjectValidationHelper {

	/**
	 * Validate a required project type code and project properties.
	 * 
	 * @param contextValidation validation context
	 * @param typeCode          project type code
	 * @param properties        properties
	 */
	public static void validateUmbrellaProjectTypeCodeRequired(ContextValidation contextValidation, String typeCode, Map<String, PropertyValue> properties) {
		UmbrellaProjectType umbrellaProjectType = CommonValidationHelper.validateCodeForeignRequired(contextValidation, UmbrellaProjectType.miniFind.get(), typeCode, "typeCode", true);

		if (umbrellaProjectType != null) {
			ValidationHelper.validateProperties(contextValidation, properties, umbrellaProjectType.getPropertiesDefinitionProjectLevel());
		}
	}

	/**
	 * Validate a required umbrella project category code.
	 * 
	 * @param contextValidation validation context
	 * @param categoryCode      project category code
	 */
	public static void validateUmbrellaProjectCategoryCodeRequired(ContextValidation contextValidation, String categoryCode) {
		CommonValidationHelper.validateCodeForeignRequired(contextValidation, UmbrellaProjectCategory.miniFind.get(), categoryCode, "categoryCode");
	}
}
