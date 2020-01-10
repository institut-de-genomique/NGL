package controllers.resolutions.api;

import java.util.List;

import controllers.ListForm;

public class ResolutionConfigurationsSearchForm extends ListForm {
	
	public String typeCode;
	public List<String> typeCodes;
	
	public String objectTypeCode;
	public List<String> objectTypeCodes;
	
	public Boolean distinct = false;
	
	
}