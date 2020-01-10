package controllers.reagents.api;

import java.util.List;

import controllers.ListForm;

public class KitCatalogSearchForm extends ListForm {
	public String code;
	public List<String> codes;
	public String name;
	public String catalogRefCode;
	public String providerRefName;
	public String providerCode;
	public List<String> codesFromBoxCatalog;
	public List<String> codesFromReagentCatalog;
	public List<String> experimentTypeCodes;
	public Boolean isActive;	
}
