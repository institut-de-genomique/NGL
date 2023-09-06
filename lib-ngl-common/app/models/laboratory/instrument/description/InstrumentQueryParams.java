package models.laboratory.instrument.description;

import java.util.List;

import org.apache.commons.lang3.ObjectUtils;


public class InstrumentQueryParams {
	
	public String code;
	public List<String> codes;
	
	public String typeCode;
	public List<String> typeCodes;
	
	public String categoryCode;
	public List<String> categoryCodes;
	
	public String experimentType;
	public List<String> experimentTypes;
	
	public Boolean active;
	
	public boolean isAtLeastOneParam(){
		return ObjectUtils.anyNotNull(code, codes, typeCode, typeCodes, categoryCode, categoryCodes, active);
	}
}
