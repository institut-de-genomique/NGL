package controllers.reagents.api;

import java.util.Date;
import java.util.List;

import controllers.ListForm;

public class BoxSearchForm extends ListForm{
	
	public String code;
	public String kitCode;
	public String name;
	public String providerID;
	public String lotNumber;
	public String catalogRefCode;
	
	public String orderCode;
	public Date toExpirationDate;
	public List<String> catalogCodes;
	
	public Date fromReceptionDate;
	public Date toReceptionDate;
	
	public String providerOrderCode;
	
	public String createUser;
}
