package models.laboratory.sample.instance.reporting;

import java.util.List;
import java.util.Map;

import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.processes.instance.SampleOnInputContainer;

/**
 * Projection of a {@link models.laboratory.processes.instance.Process} 
 * and of a Experiment that is 
 * stored in a {@link models.laboratory.sample.instance.Sample}.
 * 
 * The trace information means that this object is user editable, this is not 
 * a real projection, if any at all.
 *
 */
public class SampleProcess {
	
	/**
	 * Process code, see {@link models.laboratory.processes.instance.Process#code}.
	 */
	public String code;
	
	/**
	 * Process type code, see {@link models.laboratory.processes.instance.Process#typeCode}.
	 */
	public String typeCode;
	
	/**
	 * Category code, see {@link models.laboratory.processes.instance.Process#categoryCode}.
	 */
	public String categoryCode;
	/**
	 * State, see {@link models.laboratory.processes.instance.Process#state}.
	 */
	public State state;
	
	/**
	 * System maintained access trace.
	 */
	public TraceInformation traceInformation;
	
	public SampleOnInputContainer sampleOnInputContainer;
	
	public Map<String,PropertyValue> properties;
	
	public String currentExperimentTypeCode;
	
	public List<SampleExperiment> experiments;
	
	public List<SampleReadSet> readsets;
	
	public Integer progressInPercent;
	
}
