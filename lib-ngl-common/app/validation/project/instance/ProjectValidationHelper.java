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

public class ProjectValidationHelper extends CommonValidationHelper {

	public static void validateProjectType(String typeCode,
			                               Map<String, PropertyValue> properties,
			                               ContextValidation contextValidation) {
//		ProjectType projectType=BusinessValidationHelper.validateRequiredDescriptionCode(contextValidation, typeCode, "typeCode", ProjectType.find.get(),true);
		ProjectType projectType = validateCodeForeignRequired(contextValidation, ProjectType.miniFind.get(), typeCode, "typeCode",true);
		if (projectType != null) {
//			ValidationHelper.validateProperties(contextValidation, properties, projectType.getPropertiesDefinitionDefaultLevel());
			ValidationHelper.validateProperties(contextValidation, properties, projectType.getPropertiesDefinitionProjectLevel());
		}		
	}

	public static void validateProjectCategoryCode(String categoryCode, ContextValidation contextValidation) {
//		BusinessValidationHelper.validateRequiredDescriptionCode(contextValidation, categoryCode, "categoryCode", ProjectCategory.find.get());	
		validateCodeForeignRequired(contextValidation, ProjectCategory.miniFind.get(), categoryCode, "categoryCode");			
	}
	
	public static void validateUmbrellaProjectCode (String umbrellaProjectCode, ContextValidation contextValidation) {		
		//TODO : temporary unset if
		//if (ValidationHelper.required(contextValidation, umbrellaProjectCode, "umbrellaProjectCode")) {
			if ((umbrellaProjectCode != null) && !MongoDBDAO.checkObjectExist(InstanceConstants.UMBRELLA_PROJECT_COLL_NAME, UmbrellaProject.class,  DBQuery.is("code", umbrellaProjectCode))) {
				contextValidation.addError("umbrellaProjectCode", ValidationConstants.ERROR_CODE_NOTEXISTS_MSG, umbrellaProjectCode);
			}
		//}		 
	}

	public static void validateBioformaticParameters(BioinformaticParameters bioinformaticParameters, ContextValidation contextValidation) {
		if (ValidationHelper.validateNotEmpty(contextValidation, bioinformaticParameters, "bioinformaticParameters"))
			bioinformaticParameters.validate(contextValidation);
	}
	
}
