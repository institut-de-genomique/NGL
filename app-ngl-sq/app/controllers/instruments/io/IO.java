package controllers.instruments.io;

import java.lang.reflect.Constructor;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import controllers.TPLCommonController;
import controllers.authorisation.Permission;
import controllers.instruments.io.utils.AbstractInput;
import controllers.instruments.io.utils.AbstractOutput;
import controllers.instruments.io.utils.AbstractTypedInput;
import controllers.instruments.io.utils.AbstractTypedOutput;
import controllers.instruments.io.utils.File;
import fr.cea.ig.lfw.reflect.LFWInjector;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.experiments.ExperimentsAPI;
import fr.cea.ig.play.IGBodyParsers;
import models.laboratory.common.instance.property.PropertyFileValue;
import models.laboratory.experiment.instance.Experiment;
import models.utils.DescriptionHelper;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Result;
import validation.ContextValidation;
import validation.utils.ValidationHelper;

public class IO extends TPLCommonController {
	
	/**
	 * Logger.
	 */
	private static final play.Logger.ALogger logger = play.Logger.of(IO.class);

	/**
	 * Application.
	 */
	private final NGLApplication app;
	
	/**
	 * File form.
	 */
	private final Form<PropertyFileValue> fileForm;

	/**
	 * Experiment API.
	 */
	private final ExperimentsAPI experimentsAPI;
	
	@Inject
	public IO(NGLApplication app, ExperimentsAPI experimentsAPI) {
		this.app            = app;
		this.fileForm       = app.formFactory().form(PropertyFileValue.class);
		this.experimentsAPI = experimentsAPI;
	}
	
	/**
	 * Output instance for an instrument (of an experiment) (see {@link #getClassName(String, String)}
	 * for the class resolution).
	 * @param contextValidation validation context
	 * @param experiment        experiment
	 * @return                  output instance
	 */
	private AbstractOutput getOutputInstance(ContextValidation contextValidation, Experiment experiment) {
		if (ValidationHelper.validateNotEmpty(contextValidation, experiment.instrument, "instrument") 
				&& ValidationHelper.validateNotEmpty(contextValidation, experiment.instrument.typeCode, "instrument.code")) {
			String className = getClassName(experiment, "Output");
			try {
				@SuppressWarnings("unchecked") // Uncheckable reflection 
				Class<? extends AbstractOutput>       clazz       = (Class<? extends AbstractOutput>) Class.forName(className);
				Constructor<? extends AbstractOutput> constructor = clazz.getConstructor();
				AbstractOutput                        instance    = constructor.newInstance();
				return instance;
			} catch (Exception e) {
				// There is more than one way to fail and this does not mean that
				// the class does not exist (see the full exception list in getInputInstance).
				contextValidation.addError("outputClass", "io.error.instance.notexist",className);
			}			
		}
		return null;
		//JV BUG
		//return getIntrumentSpecificInstance(contextValidation, experiment, "Output", null);		
	}
	
	/**
	 * Input instance for an instrument (of an experiment) (see {@link #getClassName(String, String)}
	 * for the class resolution).
	 * @param contextValidation  validation context
	 * @param experiment         experiment
	 * @param instrumentTypeCode instrument type code override if not null
	 * @return                   input instance
	 */
	/* *
	 FDS 25/10/2017 ajout parametre optionnel 'extraInstrument'
	 permet d'outrepasser l'instrument embarqué par l'experiment...
	 utilisé par exemple pour l'import de fichier MettlerToledo qui n'est pas un "instrument" au sens NGL
	 utilisable aussi pour faire executer par 'hand' du code normalement executé par un intrument officiel...
	 */
	private AbstractInput getInputInstance(ContextValidation contextValidation, Experiment experiment, String extraInstrument) {
		//JV BUG 
		//return getIntrumentSpecificInstance(contextValidation, experiment, "Input", instrumentTypeCode);
		if (ValidationHelper.validateNotEmpty(contextValidation, experiment.instrument, "instrument") 
				&& ValidationHelper.validateNotEmpty(contextValidation, experiment.instrument.typeCode, "instrument.code")) {
			String className; 
			
			if (extraInstrument == null || extraInstrument.equals("")) {
				// ancien comportement
				className = getClassName(experiment, "Input");
			} else {
				// Class Input de extraInstrument
				String institute = DescriptionHelper.getInstitutes().get(0).toLowerCase();
				className ="controllers.instruments.io." + institute + "." + extraInstrument + ".Input";
			}
			try {
				@SuppressWarnings("unchecked") // Uncheckable reflection
				Class<? extends AbstractInput>       clazz       = (Class<? extends AbstractInput>) Class.forName(className);
				Constructor<? extends AbstractInput> constructor = clazz.getConstructor();
				AbstractInput                        instance    = constructor.newInstance();
				return instance;
			} catch (Exception e) {
				// There is more than one way to fail and this does not mean that
				// the class does not exist (see the full exception list below).
				contextValidation.addError("outputClass", "io.error.instance.notexist", className);
//			} catch (InstantiationException 
//					| IllegalAccessException 
//					| IllegalArgumentException
//					| InvocationTargetException 
//					| NoSuchMethodException 
//					| SecurityException 
//					| ClassNotFoundException e) {
//				contextValidation.addError("outputClass", "io.error.instance.notexist", className);
			}
		}
		return null;	
	}

	/**
	 * @deprecated don't work too generic
	 * Instrument specific instance (see {@link #getClassName(String, String)}
	 * for the class resolution).
	 * @param contextValidation  validation context
	 * @param experiment         experiment
	 * @param type               class type ("Input", "Output")
	 * @param extraInstrument    instrument type code override if not null
	 * @return                   input instance
	 */	
	/*private Object getIntrumentSpecificInstance(ContextValidation contextValidation, Experiment experiment, String type, String extraInstrument) {
		if (ValidationHelper.validateNotEmpty(contextValidation, experiment.instrument, "instrument"))
			return null;
		if (ValidationHelper.validateNotEmpty(contextValidation, experiment.instrument.typeCode, "instrument.code")) 
			return null;
		String className = StringUtils.isEmpty(extraInstrument) ? getClassName(experiment.instrument.typeCode, type)
		                                                        : getClassName(extraInstrument,                type);
		try {
			
			//JV BUG 
			return app.injector().instanceOf(LFWInjector.class).newInstance(className);
		} catch (Exception e) {
			// There is more than one way to fail and this does not mean that
			// the class does not exist (see the full exception list below).
			contextValidation.addError("outputClass", "io.error.instance.notexist", className);
			return null;
		}		
	}*/
	
	/**
	 * Get class for an instrument type code.
	 * @param instrumentTypeCode instrument type code
	 * @param type               class type ("Input" or "Output")
	 * @return                   class name
	 */
	private String getClassName(Experiment experiment, String type) {
		String institute = DescriptionHelper.getInstitutes().get(0).toLowerCase();
		return "controllers.instruments.io." + institute + "." + experiment.instrument.typeCode.toLowerCase().replace("-", "") + "." + type;
	}
	
	public Result generateFile(String experimentCode) {
		Experiment experiment = experimentsAPI.get(experimentCode);
		if (experiment == null) 
			return badRequest("experiment not found " + experimentCode);
		// GA/FDS 22/07/2016 ajout .bindFromRequest() + context....putAll pour recuperer un parametre de la query string...
		DynamicForm filledForm = app.formFactory().form().bindFromRequest(); 
		ContextValidation contextValidation = ContextValidation.createUndefinedContext(getCurrentUser());
        contextValidation.getContextObjects().putAll(filledForm.rawData());
        Object output = getOutputInstance(contextValidation, experiment);
        if (!contextValidation.hasErrors()) {
        	try {
        		File file = null;
        		if (output instanceof AbstractOutput)
        			file = ((AbstractOutput)output).generateFile(experiment, contextValidation);
        		else if (output instanceof AbstractTypedOutput)
        			file = ((AbstractTypedOutput<?>)output).run(contextValidation, experiment, request().queryString());
        		else
        			throw new RuntimeException("invalid output handler " + output);
        		if (!contextValidation.hasErrors() && file != null) {									
        			response().setHeader("Content-disposition","attachment; filename=" + file.filename);
        			return ok(file.content).as("application/x-download");
        		}
        	} catch (Throwable e) {
        		logger.error("IO Error :", e);
        		contextValidation.addError("Error :", "" + e.getMessage());
        	}
        }
		return badRequest(app.errorsAsJson(contextValidation.getErrors()));
	}
	
	@BodyParser.Of(value = IGBodyParsers.Json5MB.class)
	@Permission(value={"writing"})
	// FDS 25/10 ajout param optionnel pour instrument additionnel  (voir apinglsq.routes??)
	public Result importFile(String experimentCode, String extraInstrument) {
		Experiment experiment = experimentsAPI.get(experimentCode);
		if (experiment == null)
			return badRequest("experiment not found " + experimentCode);
		Form<PropertyFileValue> filledForm = getFilledForm(fileForm, PropertyFileValue.class);
		PropertyFileValue pfv = filledForm.get();
		if (pfv == null) 
			return badRequest("missing file");
		ContextValidation contextValidation = ContextValidation.createUndefinedContext(getCurrentUser(), filledForm);
		// FDS 25/10 ajout param optionnel pour instrument additionnel
		Object inputHandler = getInputInstance(contextValidation, experiment, extraInstrument);
        if (!contextValidation.hasErrors()) {
        	try {
        		if (inputHandler instanceof AbstractInput) {
        			fillDataWith(contextValidation.getContextObjects(), request().queryString());
        			experiment = ((AbstractInput)inputHandler).importFile(experiment, pfv, contextValidation);
        		} else if (inputHandler instanceof AbstractTypedInput) {
        			experiment = ((AbstractTypedInput<?>)inputHandler).run(contextValidation, experiment, pfv, request().queryString());
        		} else {
        			throw new RuntimeException("invalid input handler " + inputHandler);
        		}
        	} catch (Throwable e) {
        		logger.error(e.getMessage(), e);
        		contextValidation.addError("Error :", "" + e.getMessage());
        	}
        }
		if (!contextValidation.hasErrors())
			return ok(Json.toJson(experiment));
		return badRequest(app.errorsAsJson(contextValidation.getErrors()));
	}
	
}




