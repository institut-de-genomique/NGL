package validation.project.instance;

import java.util.Map;

import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.project.description.ProjectCategory;
import models.laboratory.project.description.ProjectType;
import models.laboratory.project.instance.BioinformaticParameters;
import models.laboratory.project.instance.UmbrellaProject;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.utils.ValidationConstants;
import validation.utils.ValidationHelper;

public class ProjectValidationHelper {

	// -----------------------------------------------------------------------------------
	// renamed and arguments reordered
	
	/**
	 * Validate a required project type code and project properties.
	 * @param typeCode          project type code
	 * @param properties        properties
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateProjectTypeCodeRequired(ContextValidation, String, Map)}
	 */
	@Deprecated
	public static void validateProjectType(String typeCode, Map<String, PropertyValue> properties, ContextValidation contextValidation) {
		ProjectValidationHelper.validateProjectTypeCodeRequired(contextValidation, typeCode, properties);
	}
	
	/**
	 * Validate a required project type code and project properties.
	 * @param contextValidation validation context
	 * @param typeCode          project type code
	 * @param properties        properties
	 */
	public static void validateProjectTypeCodeRequired(ContextValidation contextValidation, String typeCode, Map<String, PropertyValue> properties) {
		ProjectType projectType = CommonValidationHelper.validateCodeForeignRequired(contextValidation, ProjectType.miniFind.get(), typeCode, "typeCode",true);
		if (projectType != null)
			ValidationHelper.validateProperties(contextValidation, properties, projectType.getPropertiesDefinitionProjectLevel());
	}

	// -----------------------------------------------------------------------------------
	// renamed and arguments reordered
	
	/**
	 * Validate a required project category code.
	 * @param categoryCode      project category code
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateProjectCategoryCodeRequired(ContextValidation, String)}
	 */
	@Deprecated
	public static void validateProjectCategoryCode(String categoryCode, ContextValidation contextValidation) {
		ProjectValidationHelper.validateProjectCategoryCodeRequired(contextValidation, categoryCode);
	}
	
	/**
	 * Validate a required project category code.
	 * @param contextValidation validation context
	 * @param categoryCode      project category code
	 */
	public static void validateProjectCategoryCodeRequired(ContextValidation contextValidation, String categoryCode) {
		CommonValidationHelper.validateCodeForeignRequired(contextValidation, ProjectCategory.miniFind.get(), categoryCode, "categoryCode");			
	}
		
	// -----------------------------------------------------------------------------------
	// renamed and arguments reordered
	
	/**
	 * Validate an optional umbrella project code.
	 * @param umbrellaProjectCode umbrella project code 
	 * @param contextValidation   validation context
	 * @deprecated use {@link #validateUmbrellaProjectCodeOptional(ContextValidation, String)}
	 */
	@Deprecated
	public static void validateUmbrellaProjectCode(String umbrellaProjectCode, ContextValidation contextValidation) {
		ProjectValidationHelper.validateUmbrellaProjectCodeOptional(contextValidation, umbrellaProjectCode);
	}

	/**
	 * Validation an optional umbrella project code.
	 * @param contextValidation   validation context
	 * @param umbrellaProjectCode umbrella project code 
	 */
	public static void validateUmbrellaProjectCodeOptional(ContextValidation contextValidation, String umbrellaProjectCode) {		
		// GA: temporary unset if
		//if (ValidationHelper.required(contextValidation, umbrellaProjectCode, "umbrellaProjectCode")) {
			if ((umbrellaProjectCode != null) && !MongoDBDAO.checkObjectExist(InstanceConstants.UMBRELLA_PROJECT_COLL_NAME, UmbrellaProject.class,  DBQuery.is("code", umbrellaProjectCode))) {
				contextValidation.addError("umbrellaProjectCode", ValidationConstants.ERROR_CODE_NOTEXISTS_MSG, umbrellaProjectCode);
			}
		//}		 
	}

	// -----------------------------------------------------------------------------------
	// renamed and arguments reordered
	
	/**
	 * Validate a required bioinformatics parameter object. 
	 * @param bioinformaticParameters bioinformatics parameter to validate
	 * @param contextValidation       validation context
	 * @deprecated use {@link #validateBioformaticParametersRequired(ContextValidation, BioinformaticParameters)}
	 */
	@Deprecated
	public static void validateBioformaticParameters_(BioinformaticParameters bioinformaticParameters, ContextValidation contextValidation) {
		ProjectValidationHelper.validateBioformaticParametersRequired(contextValidation, bioinformaticParameters);
	}

	/**
	 * Validate a required bioinformatics parameter object. 
	 * @param contextValidation       validation context
	 * @param bioinformaticParameters bioinformatics parameter to validate
	 */
	public static void validateBioformaticParametersRequired(ContextValidation contextValidation, BioinformaticParameters bioinformaticParameters) {
		if (ValidationHelper.validateNotEmpty(contextValidation, bioinformaticParameters, "bioinformaticParameters"))
			bioinformaticParameters.validate(contextValidation);
	}

}
