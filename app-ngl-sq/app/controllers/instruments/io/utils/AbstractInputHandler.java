package controllers.instruments.io.utils;

import java.util.HashMap;

import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.experiment.instance.InputContainerUsed;

public class AbstractInputHandler {
	
	/**
	 * Get or create a property single value for a name experiment property
	 * in an input container, creates the properties map if needed.
	 * @param icu  input container
	 * @param code property name
	 * @return     existing of freshly created value for the experiment property name
	 * @deprecated use {@link #getOrCreatePSV(InputContainerUsed, String)}
	 */
	@Deprecated
	public static PropertySingleValue getPSV(InputContainerUsed icu, String code) {
		return AbstractInput.getOrCreatePSV(icu, code);
	}
	
	/**
	 * Get or create a property single value for a name experiment property
	 * in an input container, creates the properties map if needed.
	 * @param icu  input container
	 * @param code property name
	 * @return     existing of freshly created value for the experiment property name
	 */
	public static PropertySingleValue getOrCreatePSV(InputContainerUsed icu, String code) {
		PropertySingleValue psv;
		if (icu.experimentProperties == null)
			icu.experimentProperties = new HashMap<>();
		if (!icu.experimentProperties.containsKey(code)) {
			psv = new PropertySingleValue();
			icu.experimentProperties.put(code, psv);
		} else {
			psv = (PropertySingleValue)icu.experimentProperties.get(code);
		}
		return psv;
	}

}
