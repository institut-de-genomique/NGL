package models.laboratory.reception.instance;

import static validation.utils.ValidationConstants.ERROR_REQUIRED_MSG;

import java.lang.reflect.Field;
import java.util.Map;

import models.laboratory.reception.instance.ReceptionConfiguration.Action;
import validation.ContextValidation;


public class ExcelFieldConfiguration extends AbstractFieldConfiguration {
	
	public String headerValue;
	public Integer cellPosition;
	
	public String defaultValue;
	
	public Integer beginIndex;
	public Integer endIndex;
	
	
	public ExcelFieldConfiguration() {
		super(AbstractFieldConfiguration.excelType);		
	}

	@Override
	public void populateField(Field field, Object dbObject,
			Map<Integer, String> rowMap, ContextValidation contextValidation, Action action) throws Exception {
		
		if(rowMap.containsKey(cellPosition)){
			String value = rowMap.get(cellPosition);	
			if(null != beginIndex && null != endIndex){
				value = value.substring(beginIndex, endIndex);
			}else if(null != beginIndex){
				value = value.substring(beginIndex);
			}
			populateField(field, dbObject, value);				
		}else if(defaultValue != null){
			populateField(field, dbObject, defaultValue);
		} else if(required){
			contextValidation.addError(headerValue, ERROR_REQUIRED_MSG);
		}
	}

}
