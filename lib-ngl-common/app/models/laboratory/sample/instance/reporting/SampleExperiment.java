package models.laboratory.sample.instance.reporting;

import java.util.Map;

import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.Valuation;

public class SampleExperiment {
	
	public String code;
	public String typeCode;
	public String categoryCode;
	public State state;
	public Valuation status;
	public TraceInformation traceInformation;
	public String protocolCode;
	public Map<String,PropertyValue> properties;
	
}
