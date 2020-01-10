package models.laboratory.stats;

import java.util.List;

import models.laboratory.common.instance.ITracingAccess;
import models.laboratory.common.instance.TraceInformation;
import validation.ContextValidation;
import validation.IValidation;
import fr.cea.ig.DBObject;

public class StatsConfiguration extends DBObject implements IValidation, ITracingAccess {

	public String name;
	public TraceInformation traceInformation;
	public List<String> pageCodes; //code des pages sur lesquelles on souhaite l'afficher
	public QueryForm queryForm;
	public List<StatsForm> statsForm;
	
	@Override
	public void validate(ContextValidation contextValidation) {
	}
	
	@Override
	public TraceInformation getTraceInformation() {
		if (traceInformation == null)
			traceInformation = new TraceInformation();
		return traceInformation;
	}

}
