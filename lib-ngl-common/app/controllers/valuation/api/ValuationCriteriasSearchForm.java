package controllers.valuation.api;


import java.util.List;

import models.laboratory.common.description.ObjectType;



import controllers.ListForm;

public class ValuationCriteriasSearchForm  extends ListForm{

	public ObjectType.CODE objectTypeCode;
	public List<String> typeCodes;
	public String typeCode;

}
