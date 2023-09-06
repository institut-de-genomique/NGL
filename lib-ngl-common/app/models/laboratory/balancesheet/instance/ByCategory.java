package models.laboratory.balancesheet.instance;

import java.util.ArrayList;
import java.util.List;

public class ByCategory {
	
	public String label;
	public Result result;
	public List<Month> monthly;
	
	public ByCategory() {
		result = new Result();
		monthly = new ArrayList<Month>(0);
	}
}
