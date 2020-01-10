package lims.cns.dao;

import java.util.Date;

public class LimsExperiment {
	public Date date;
	public String code;
	public Integer nbCycles;
	@Override
	public String toString() {
		return "LimsExperiment [date=" + date + ", code=" + code
				+" , nbCycles=" + nbCycles
				+ "]";
	}
	
	
}
