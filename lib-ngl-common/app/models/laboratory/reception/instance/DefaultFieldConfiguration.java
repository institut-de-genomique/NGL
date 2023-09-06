package models.laboratory.reception.instance;

import java.lang.reflect.Field;
import java.util.Map;

import models.laboratory.reception.instance.ReceptionConfiguration.Action;
import validation.ContextValidation;


public class DefaultFieldConfiguration extends AbstractFieldConfiguration {
	
	public String value;

	public DefaultFieldConfiguration() {
		super(AbstractFieldConfiguration.defaultType);		
	}

	@Override
	public void populateField(Field field, Object dbObject, Map<Integer, String> rowMap, ContextValidation contextValidation, Action action) throws Exception {
		populateField(field, dbObject, value);		
	}

	@Override
	public void updateFromHeader(ContextValidation vc, Map<Integer, String> header) {
		// Nothing to do
	}
	
}
