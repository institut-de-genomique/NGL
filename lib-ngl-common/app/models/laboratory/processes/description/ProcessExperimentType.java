package models.laboratory.processes.description;

import models.laboratory.experiment.description.ExperimentType;

public class ProcessExperimentType {
	
	public ExperimentType experimentType;
	public Integer        positionInProcess;
	public String         experimentTypeCode;
	
	public ProcessExperimentType() {
	}

	public ProcessExperimentType(ExperimentType experimentType,	Integer processOrder) {
		this.experimentType    = experimentType;
		this.positionInProcess = processOrder;		
	}
		
}
