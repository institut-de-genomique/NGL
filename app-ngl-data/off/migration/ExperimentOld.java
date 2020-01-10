package controllers.migration;

import java.util.List;

import models.laboratory.experiment.instance.AtomicTransfertMethod;
import models.laboratory.experiment.instance.Experiment;


public class ExperimentOld extends Experiment {
	public List<AtomicTransfertMethodOld> atomicTransfertMethods; 
}
