package lims.cng.dao;

import java.util.Date;

public class LimsExperiment {
	public Date date;
	public String code;
	public String categoryCode;
	public Integer nbCycles;
	@Override
	public String toString() {
		return "LimsExperiment [date=" + date + ", code=" + code
				+ ", categoryCode=" + categoryCode + ", nbCycles=" + nbCycles
				+ "]";
	}
	
	
}
