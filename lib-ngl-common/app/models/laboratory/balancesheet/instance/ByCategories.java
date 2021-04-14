package models.laboratory.balancesheet.instance;

import java.util.ArrayList;
import java.util.List;

public class ByCategories {
	
	public List<ByCategory> sequencingTypes;
	public List<ByCategory> sampleTypes;
	public List<ByCategory> projects;
	
	public ByCategories() {
		sequencingTypes = new ArrayList<ByCategory>(0);
		sampleTypes = new ArrayList<ByCategory>(0);
		projects = new ArrayList<ByCategory>(0);
	}

}
