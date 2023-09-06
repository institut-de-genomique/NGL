package controllers.processes.api;

import java.util.List;

import controllers.ListForm;

public class ProcessTypesSearchForm extends ListForm{
	public String categoryCode;
	
	public List<String> categoryCodes;
	
	public List<String> codes;
	
	public List<String> propertyDefinitionCodes;
	
	public Boolean isActive;
	
	public Boolean light = true;
}
