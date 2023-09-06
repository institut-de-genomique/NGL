package models.laboratory.balancesheet.instance;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class Computation {
	
	public String collection;
	public Set<String> matches;
	public String propertyDate;
	
	/**
	 * Used for incremental update, not used in production
	 */
	public AtomicReference<Date> propertyDateMax;
	
	public String property;
	public String method;
	public Result result;
	public ByCategories by;
	public List<Month> monthly;
	
	public Computation() {
		matches = new HashSet<String>();
		propertyDateMax = new AtomicReference<Date>();
		result = new Result();
		by = new ByCategories();
		monthly = new ArrayList<Month>(0);
	}

}
