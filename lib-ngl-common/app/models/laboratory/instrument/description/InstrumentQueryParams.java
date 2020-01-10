package models.laboratory.instrument.description;

import java.util.List;


public class InstrumentQueryParams {
	
	public String typeCode;
	public List<String> typeCodes;
	
	public String categoryCode;
	public List<String> categoryCodes;
	
	public String experimentType;
	public List<String> experimentTypes;
	
	public Boolean active;
	
	public boolean isAtLeastOneParam(){
		return (this.typeCodes != null || this.typeCode != null 
				|| this.categoryCode != null || this.categoryCodes != null
				|| this.active != null);
	}
}
