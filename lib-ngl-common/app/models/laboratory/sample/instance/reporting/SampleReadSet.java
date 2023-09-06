package models.laboratory.sample.instance.reporting;

import java.util.Date;
import java.util.Map;

import models.laboratory.common.instance.State;
import models.laboratory.common.instance.Valuation;
import models.laboratory.run.instance.SampleOnContainer;
import models.laboratory.run.instance.Treatment;

public class SampleReadSet {
	public String code;
	public String typeCode;
	public State state;
	
	public String runCode;
	public String runTypeCode;
	public Date runSequencingStartDate;
	
	public Valuation productionValuation;    // GA: rename to QCValuation
	public Valuation bioinformaticValuation; // GA: rename to bioinformaticUsable
	public SampleOnContainer sampleOnContainer;
	public Map<String,Treatment> treatments;
	
}
