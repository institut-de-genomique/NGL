package controllers.instruments.io.utils;

import java.util.Map;

import fr.cea.ig.lfw.reflect.ReflectionUtils;
import models.laboratory.experiment.instance.Experiment;
import validation.ContextValidation;

public abstract class AbstractTypedOutput<T> {

	/**
	 * Logger.
	 */
	protected play.Logger.ALogger logger;
	
	/**
	 * Construct an instance.
	 */
	public AbstractTypedOutput() {
		logger = play.Logger.of(getClass());
	}
	
	public File run(ContextValidation contextValidation, Experiment experiment, Map<String,String[]> args) throws Exception {
		T t = ReflectionUtils.readInstance(ReflectionUtils.getDefiningClassTypeArgument(AbstractTypedOutput.class, getClass()), args);
		return generateFile(contextValidation, experiment, t);
	}

	public abstract File generateFile(ContextValidation contextValidation, Experiment experiment, T parameters) throws Exception;

}

abstract class NoParameterOutput extends AbstractTypedOutput<Object> {

	@Override
	public File generateFile(ContextValidation contextValidation, Experiment experiment, Object parameters) throws Exception {
		return generateFile(contextValidation, experiment);
	}
	
	public abstract File generateFile(ContextValidation contextValidation, Experiment experiment) throws Exception;
	
}
