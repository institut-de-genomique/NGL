package controllers.instruments.api;

import java.util.List;

import controllers.ListForm;
import models.laboratory.instrument.description.InstrumentUsedTypeQueryParams;

public class InstrumentUsedTypesSearchForm extends ListForm{
	public String code;
	public List<String> codes;
	
	public String categoryCode;
	public List<String> categoryCodes;
	
	public String experimentTypeCode;
	
	public Boolean isActive; 
	
	public InstrumentUsedTypeQueryParams getInstrumentUsedTypesQueryParams() {
		InstrumentUsedTypeQueryParams instrumentUsedTypeQueryParams = new InstrumentUsedTypeQueryParams();
		instrumentUsedTypeQueryParams.code = code;
		instrumentUsedTypeQueryParams.codes = codes;
		instrumentUsedTypeQueryParams.categoryCode = categoryCode;
		instrumentUsedTypeQueryParams.categoryCodes = categoryCodes;
		return instrumentUsedTypeQueryParams;
	}
}
