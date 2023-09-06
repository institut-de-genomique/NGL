package controllers.experiments.api;

import java.util.List;

import controllers.ListForm;

public class ExperimentTypesSearchForm extends ListForm{
	
	public String code;
	public List<String> codes;
	
	public String categoryCode;
	public List<String> categoryCodes;
	
	public String instrumentUsedTypeCode;
	public List<String> instrumentUsedTypeCodes;
	
	public String propertyDefinitionName;
	public List<String> propertyDefinitionNames;
	
	public String previousExperimentTypeCode;
	
	public String processTypeCode;
	
	public Boolean withoutOneToVoid;
	
	public Boolean withoutExtTo;
	
	public Boolean isActive;
}
