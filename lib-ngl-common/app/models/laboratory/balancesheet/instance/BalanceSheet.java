package models.laboratory.balancesheet.instance;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fr.cea.ig.DBObject;

public class BalanceSheet extends DBObject {
	
	public String code;
	public String year;
	public String type;
	public Date lastUpdateDate;
	public List<Computation> computations;
	
	public BalanceSheet() {
		computations = new ArrayList<Computation>(0);
	}

}
