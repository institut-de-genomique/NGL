package controllers.instruments.api;

import java.util.List;

import controllers.ListForm;
import models.laboratory.instrument.description.InstrumentQueryParams;

public class InstrumentsSearchForm extends ListForm {
	
	public String typeCode;
	public List<String> typeCodes;
	
	public String categoryCode;
	public List<String> categoryCodes;
	
	public String experimentType;
	public List<String> experimentTypes;
	
	public Boolean active;
	
	public InstrumentQueryParams getInstrumentsQueryParams() {
		InstrumentQueryParams instrumentQueryParams = new InstrumentQueryParams();
		instrumentQueryParams.typeCode = typeCode;
		instrumentQueryParams.typeCodes = typeCodes;
		instrumentQueryParams.categoryCode = categoryCode;
		instrumentQueryParams.categoryCodes = categoryCodes;
		instrumentQueryParams.experimentType = experimentType;
		instrumentQueryParams.experimentTypes = experimentTypes;
		instrumentQueryParams.active = active;
		
		return instrumentQueryParams;
	}

}
