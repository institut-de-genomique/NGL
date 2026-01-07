package models.laboratory.sample.instance;

import java.util.Date;
import java.util.Set;

public class ResolutionHistoryEntry {
	
	public ResolutionHistoryEntry() {}
	
	public ResolutionHistoryEntry(int index, Set<String> resolutionCodes, String user) {
		this.index = index;
		this.resolutionCodes = resolutionCodes;
		this.date = new Date();
		this.user = user;
	}
	
	public Integer index;
	public Set<String>  resolutionCodes;
	public Date    date;
	public String  user;

}
