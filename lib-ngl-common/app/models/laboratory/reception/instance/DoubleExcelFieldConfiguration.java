package models.laboratory.reception.instance;

import static validation.utils.ValidationConstants.ERROR_REQUIRED_MSG;

import java.lang.reflect.Field;
import java.util.Map;

import models.laboratory.reception.instance.ReceptionConfiguration.Action;
import validation.ContextValidation;

public class DoubleExcelFieldConfiguration extends AbstractFieldConfiguration {
	
	public String  headerValue;
	public Integer cellPosition1;
	public Integer cellPosition2;
	public String  concatCharacter;
	
	public String defaultValue;
	
	public DoubleExcelFieldConfiguration() {
		super(AbstractFieldConfiguration.excelType);		
	}

	@Override
	public void populateField(Field                field,
			                  Object               dbObject,
			                  Map<Integer, String> rowMap, 
			                  ContextValidation    contextValidation, 
			                  Action               action) throws Exception {
		if (rowMap.containsKey(cellPosition1) && rowMap.containsKey(cellPosition2)) {
			String value = rowMap.get(cellPosition1) + concatCharacter + rowMap.get(cellPosition2);			
			populateField(field, dbObject, value);				
		} else if (defaultValue != null) {
			populateField(field, dbObject, defaultValue);
		} else if (required) {
			contextValidation.addError(headerValue, ERROR_REQUIRED_MSG);
		}
	}

	@Override
	public void updateFromHeader(ContextValidation vc, Map<Integer, String> header) {
		if (header.containsKey(cellPosition1) && header.containsKey(cellPosition2)) {
			headerValue = header.get(cellPosition1) + " / " + header.get(cellPosition2);
		} else {
			vc.addError("Headers","not found header for cell position " + cellPosition1 + " / " + cellPosition2);
		}
	}

}
