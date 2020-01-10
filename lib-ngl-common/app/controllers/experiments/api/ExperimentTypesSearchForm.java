package controllers.experiments.api;

import java.util.List;

import controllers.ListForm;

public class ExperimentTypesSearchForm extends ListForm{
	
	public String categoryCode;
	public List<String> categoryCodes;
	
	public String previousExperimentTypeCode;
	
	public String processTypeCode;
	
	public Boolean withoutOneToVoid;
	
	
	public Boolean isActive;
}
