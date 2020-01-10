package controllers.reagents.api;

import java.util.List;

import controllers.ListForm;

public class BoxCatalogSearchForm extends ListForm{
	public String kitCatalogCode;
	public String catalogRefCode;
	public List<String> kitCatalogCodes;
	public Boolean isActive;
}
