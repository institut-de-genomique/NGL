package controllers.instruments.io.utils;

import models.laboratory.common.instance.property.PropertyFileValue;
import models.laboratory.experiment.instance.Experiment;
import validation.ContextValidation;

/**
 * No URL parameter enforced. This behaves like the {@link AbstractInput} class
 * but no URL parameters are allowed so this can be considered a safe replacement.
 * 
 * @author vrd
 *
 */
public abstract class NoParameterInput extends AbstractTypedInput<Object> {

	@Override
	public Experiment importFile(ContextValidation contextValidation, Experiment experiment, PropertyFileValue pfv,	Object parameters) throws Exception {
		return importFile(contextValidation, experiment, pfv);
	}

	public abstract Experiment importFile(ContextValidation contextValidation, Experiment experiment, PropertyFileValue pfv) throws Exception;

}
