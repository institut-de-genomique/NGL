package controllers.instruments.io.utils;

import models.laboratory.common.instance.property.PropertyFileValue;
import models.laboratory.experiment.instance.Experiment;
import validation.ContextValidation;

public abstract class AbstractInput extends AbstractInputHandler {
	
	protected final play.Logger.ALogger logger;
	
	public AbstractInput() {
		logger = play.Logger.of(getClass());
	}
	
	public abstract Experiment importFile(Experiment experiment, PropertyFileValue pfv, ContextValidation contextValidation) throws Exception;
	
}
