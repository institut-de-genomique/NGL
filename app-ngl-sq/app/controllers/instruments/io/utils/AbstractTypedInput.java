package controllers.instruments.io.utils;

import java.util.Map;

import fr.cea.ig.lfw.reflect.ReflectionUtils;
import models.laboratory.common.instance.property.PropertyFileValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.experiment.instance.Experiment;
import validation.ContextValidation;

// TODO: migrate AbstractInput subclasses to this class subclasses 
/**
 * Experiment file import base class that provides support for URL parameter
 * enforcement using dumb object based definition.
 * 
 * @author vrd
 *
 * @param <T> URL parameter definition and storage
 */
public abstract class AbstractTypedInput<T> extends AbstractInputHandler {

	/**
	 * Logger.
	 */
	protected play.Logger.ALogger logger;
	
	/**
	 * Construct an instance.
	 */
	public AbstractTypedInput() {
		logger = play.Logger.of(getClass());
	}
	
	/**
	 * Execution with arguments fetched from an HTTP request.
	 * @param contextValidation validation context
	 * @param experiment        experiment
	 * @param pfv               file content
	 * @param args              URL parameters
	 * @return                  experiment
	 * @throws Exception        error
	 */
	public Experiment run(ContextValidation contextValidation, Experiment experiment, PropertyFileValue pfv, Map<String,String[]> args) throws Exception {
		T t = ReflectionUtils.readInstance(ReflectionUtils.getDefiningClassTypeArgument(AbstractTypedInput.class, getClass()), args);
		return importFile(contextValidation, experiment, pfv, t);
	}
	
	/**
	 * File import implementation with URL parameters checked and provided as
	 * an instance of the argument class. 
	 * @param contextValidation validation context
	 * @param experiment        experiment
	 * @param pfv               file content
	 * @param parameters        URL parameters
	 * @return                  experiment
	 * @throws Exception        error
	 */
	public abstract Experiment importFile(ContextValidation contextValidation, Experiment experiment, PropertyFileValue pfv, T parameters) throws Exception;
	
}

/**
 * Replacement of a an old input class by simply extending the NoParameterInput class.
 * 
 * @author vrd
 *
 */
class OldInput extends NoParameterInput {

	@Override
	public Experiment importFile(ContextValidation contextValidation, Experiment experiment, PropertyFileValue pfv) {
		// old code goes here
		return experiment;
	}
	
}

/**
 * Custom URL parameter.
 * 
 * @author vrd
 *
 */
class ExampleInput extends AbstractTypedInput<ExampleInput.Args> {

	/**
	 * Public field names of this class (and super classes) are used as URL parameter
	 * names and field types to parse the values.
	 * <p>
	 * There is no need for this class to be named 'Args' as the actual 
	 * argument class is defined as the generic argument of AbstractTypedInput
	 * which we derive from, it seems to be an good naming scheme though.  
	 */
	public static class Args {
		
		/**
		 * Appropriately comment the fields so the URL usage is clear.
		 */
		public String gamme;
		
		/**
		 * Appropriately comment the fields so the URL usage is clear.
		 */
		public float  concentration;
		
	}

	@Override
	public Experiment importFile(ContextValidation contextValidation, Experiment experiment, PropertyFileValue pfv,	Args parameters) throws Exception {
		switch (parameters.gamme) {
		case "A" : /* do something */ break;
		default  : /* do something */ break;
		}
		experiment.experimentProperties.put("someConcentration", new PropertySingleValue(parameters.concentration));
		return experiment;
	}
	
}
