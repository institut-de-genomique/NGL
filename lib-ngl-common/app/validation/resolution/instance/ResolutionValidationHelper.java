package validation.resolution.instance;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import models.laboratory.resolutions.instance.Resolution;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.utils.ValidationConstants;

public class ResolutionValidationHelper extends CommonValidationHelper {
	
//	public static void validationResolutions(List<Resolution> resolutions, ContextValidation contextValidation) {
//		if (null != resolutions && resolutions.size() > 0) {
//			int index = 0;
//			Set<String> resolutionCodes = new TreeSet<>();
//			for (Resolution resolution : resolutions) {
//				if (resolution != null) {
//					contextValidation.addKeyToRootKeyName("resolutions[" + index + "]");
//					resolution.validate(contextValidation);
//					if(resolutionCodes.contains(resolution.code)){
//						contextValidation.addError("code", ValidationConstants.ERROR_NOTUNIQUE_MSG, resolution.code);
//					}
//					resolutionCodes.add(resolution.code);
//					contextValidation.removeKeyFromRootKeyName("resolutions[" + index + "]");
//				}
//				index++;
//			}
//		}
//	}

	// ---------------------------------------------------------------------
	// renamed and arguments reordered
	
	/**
	 * Validate an optional list of optional resolutions. Resolution codes must be unique
	 * ins the collection.
	 * @param resolutions       resolutions to validate
	 * @param contextValidation validation context
	 * @deprecated use {@link #validateResolutions(ContextValidation, List)}
	 */
	@Deprecated
	public static void validationResolutions_(List<Resolution> resolutions, ContextValidation contextValidation) {
		ResolutionValidationHelper.validateResolutions(contextValidation, resolutions);
	}
	
	/**
	 * Validate an optional list of optional resolutions. Resolution codes must be unique
	 * ins the collection.
	 * @param contextValidation validation context
	 * @param resolutions       resolutions to validate
	 */
	public static void validateResolutions(ContextValidation contextValidation, List<Resolution> resolutions) {
		if (resolutions != null) {
			int index = 0;
			Set<String> resolutionCodes = new TreeSet<>();
			for (Resolution resolution : resolutions) {
				if (resolution != null) {
					String key = "resolutions[" + index + "]";
					contextValidation.addKeyToRootKeyName(key);
					resolution.validate(contextValidation);
					if (resolutionCodes.contains(resolution.code)) {
						contextValidation.addError("code", ValidationConstants.ERROR_NOTUNIQUE_MSG, resolution.code);
					}
					resolutionCodes.add(resolution.code);
					contextValidation.removeKeyFromRootKeyName(key);
				}
				index++;
			}
		}
	}


}
