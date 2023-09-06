package validation.protocol.instance;

import static validation.utils.ValidationHelper.validateNotEmpty;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.parameter.propertyDefinitionsProtocol.PropertyDefinitionsProtocol;
import models.utils.InstanceConstants;
import play.Logger;
import validation.ContextValidation;
import validation.utils.ValidationConstants;
import validation.utils.ValidationHelper;
public class ProtocolValidationHelper {

	// renamed and arguments reordered
	
	/**
	 * Validate a required protocol category code.
	 * @param categoryCode      protocol category code
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateProtocolCategoryCodeRequired(ContextValidation, String)}
	 */
	@Deprecated
	public static void validateProtocolCategoryCode(String categoryCode, ContextValidation contextValidation) {
		ProtocolValidationHelper.validateProtocolCategoryCodeRequired(contextValidation, categoryCode);
	}
	
	/**
	 * Validate a required protocol category code.
	 * @param contextValidation validation context
	 * @param categoryCode      protocol category code
	 */
	public static void validateProtocolCategoryCodeRequired(ContextValidation contextValidation, String categoryCode) {
		if (validateNotEmpty(contextValidation, categoryCode, "categoryCode")) {
			if (!models.laboratory.experiment.description.ProtocolCategory.find.get().isCodeExist(categoryCode)) {
				contextValidation.addError("protocoles.categoryCode", ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG, categoryCode);
			}
		}
	}
	public static void validateProtocolCode(ContextValidation contextValidation, String code){
		if(code == null){
			contextValidation.addError("code", ValidationConstants.ERROR_BAD_FORMAT);
		}
		/* else{
			boolean hasValideCode =	Pattern.matches("^[a-z0-9-_.]+$",code);
			if(!hasValideCode){
				contextValidation.addError("code", ValidationConstants.ERROR_BAD_FORMAT);
			}
		} */
	}

	public static void validateProtocolProperties(ContextValidation contextValidation, Map<String, PropertyValue> propertiesMap){
		if(propertiesMap != null){
			propertiesMap.entrySet().forEach((Map.Entry<String, PropertyValue> entry) -> {
				String code = entry.getKey();
				PropertyValue property = entry.getValue();
				validatePropertyExist(contextValidation,code);
				ValidationHelper.validateNotEmpty(contextValidation, property.value, "property.value");
			
			});
		}
	}
	public static void validatePropertyExist (ContextValidation contextValidation,String code){
		PropertyDefinitionsProtocol propertyDefinitionsProtocol = MongoDBDAO.findByCode(InstanceConstants.PARAMETER_COLL_NAME, PropertyDefinitionsProtocol.class, "property-definition-protocol");
		if(!propertyDefinitionsProtocol.properties.containsKey(code)){
			contextValidation.addError("protocoles.properties", ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG, code);
		}
		
	}
	public static void validateExperimentTypeCodesForUpdate(ContextValidation contextValidation, List<String> experimentTypeCodes) {
		if (contextValidation.isUpdateMode()) {
			if (experimentTypeCodes.isEmpty()) {
				contextValidation.addError("experimentTypeCodes",ValidationConstants.ERROR_BAD_FORMAT);
			}
		}
	}

	
}
