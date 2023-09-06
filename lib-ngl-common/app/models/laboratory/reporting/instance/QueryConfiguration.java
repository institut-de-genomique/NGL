package models.laboratory.reporting.instance;

import java.util.List;

public class QueryConfiguration {
	
	/*
	 * @see Column.queryIncludeKeys;
	 *  
	 */
	@Deprecated
	public List<String> includeKeys;
	
	public String type; //object (by default), reporting (native mongo query) or aggregate, 
	public String query; //only necessary for reporting or aggregate
}
