package controllers.reagents.api;

import java.util.Date;
import java.util.List;

import controllers.ListForm;

public class KitSearchForm extends ListForm{
	public String code;
	public String catalogCode;
	public List<String> catalogCodes;
	
	public String catalogRefCode;
	
	public String barCode;
	
	public Date startToUseDate;
	public Date stopToUseDate;
	
	public Date fromReceptionDate;
	public Date toReceptionDate;
	
	public String stateCode;
	
	public String createUser;
	
	public String providerOrderCode;
	public String orderCode;
	
	public Date toExpirationDate;
}
