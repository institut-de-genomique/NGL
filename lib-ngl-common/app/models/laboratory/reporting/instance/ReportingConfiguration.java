package models.laboratory.reporting.instance;

import java.util.List;

import models.laboratory.common.instance.ITracingAccess;
import models.laboratory.common.instance.TraceInformation;

import validation.ContextValidation;
import validation.IValidation;
import fr.cea.ig.DBObject;

public class ReportingConfiguration extends DBObject implements IValidation, ITracingAccess {

	public String name;
	public TraceInformation traceInformation;
	public List<String> pageCodes; //code des pages sur lesquelles on souhaite l'afficher
	public List<Column> columns;
	
	public List<Filter> filters; 
	
	public QueryConfiguration queryConfiguration;
	
	
	
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
