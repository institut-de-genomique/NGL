package validation.container.instance;

import static validation.utils.ValidationHelper.required;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.run.instance.Run;
import models.laboratory.sample.description.ImportType;
import models.laboratory.sample.description.SampleCategory;
import models.laboratory.sample.description.SampleType;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.utils.BusinessValidationHelper;
import validation.utils.ValidationConstants;
import validation.utils.ValidationHelper;
import play.Logger;

public class ContentValidationHelper extends CommonValidationHelper {

	public static void validateSampleTypeCode(String sampleTypeCode,
			ContextValidation contextValidation) {
		BusinessValidationHelper.validateRequiredDescriptionCode(contextValidation, sampleTypeCode, "sampleTypeCode",SampleType.find,false);
	}

	public static void validateSampleCode(String sampleCode,
			ContextValidation contextValidation) {
		BusinessValidationHelper.validateRequiredInstanceCode(contextValidation, sampleCode, "sampleCode", Sample.class, InstanceConstants.SAMPLE_COLL_NAME, false);

	}

	public static void validateSampleCategoryCode(String sampleCategoryCode,
			ContextValidation contextValidation) {
		BusinessValidationHelper.validateRequiredDescriptionCode(contextValidation, sampleCategoryCode, "sampleCategoryCode", SampleCategory.find,false);

	}
	
	public static void validatePercentageContent(Double percentage, ContextValidation contextValidation){

		if(required(contextValidation, percentage, "percentage")){
			//pecentage is mandatory
			if(percentage<0.0 || percentage>100.00){
				contextValidation.addErrors("percentage", ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG, percentage);
			}
		}
	}

	public static void validateSampleCodeWithProjectCode(String projectCode,
			String sampleCode, ContextValidation contextValidation) {
		if(!checkSampleWithProject(projectCode, sampleCode)){
			Logger.debug("ContentValidationHelper !checkSampleWithProject(projectCode, sampleCode");
			contextValidation.addErrors("sample", ValidationConstants.ERROR_NOTEXISTS_MSG, projectCode+" + "+sampleCode);
			Logger.debug("ContentValidationHelper !checkSampleWithProject(projectCode, sampleCode after contextValidation");
		}
		Logger.debug("ContentValidationHelper after checkSampleWithProject");
	}

	private static boolean checkSampleWithProject(String projectCode,
			String sampleCode) {
		Logger.debug("ContentValidationHelper checkSampleWithProject "+ InstanceConstants.SAMPLE_COLL_NAME);
		return MongoDBDAO.checkObjectExist(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, 
						DBQuery.is("code", sampleCode).in("projectCodes", projectCode));
	}

	public static void validateProperties(String sampleTypeCode, Map<String, PropertyValue> properties,	ContextValidation contextValidation) {
		List<PropertyDefinition> proDefinitions=new ArrayList<PropertyDefinition>();
		
		String importTypeCode = (String) contextValidation.getObject(FIELD_IMPORT_TYPE_CODE);
		if(null != importTypeCode){
			ImportType importType = BusinessValidationHelper.validateExistDescriptionCode(contextValidation, importTypeCode,"importTypeCode", ImportType.find,true);
			if(null != importType){
				proDefinitions.addAll(importType.getPropertiesDefinitionContentLevel());
			}
		}
		
		SampleType sampleType=BusinessValidationHelper.validateRequiredDescriptionCode(contextValidation, sampleTypeCode, "sampleTypeCode", SampleType.find,true);
		if(sampleType!=null ){
			proDefinitions.addAll(sampleType.getPropertiesDefinitionContentLevel());				
		}
		
		if(proDefinitions.size() > 0){
			ValidationHelper.validateProperties(contextValidation,properties, proDefinitions,false);
		}
		
	}
}
