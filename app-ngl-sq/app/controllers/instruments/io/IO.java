package controllers.instruments.io;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;

import controllers.TPLCommonController;
import controllers.authorisation.Permission;
import controllers.instruments.io.utils.AbstractInput;
import controllers.instruments.io.utils.AbstractInputHandler;
import controllers.instruments.io.utils.AbstractMultiInput;
import controllers.instruments.io.utils.AbstractOutput;
import controllers.instruments.io.utils.AbstractTypedInput;
import controllers.instruments.io.utils.AbstractTypedOutput;
import controllers.instruments.io.utils.File;
import controllers.instruments.io.utils.MultiInputHelper;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.experiments.ExperimentsAPI;
import fr.cea.ig.play.IGBodyParsers;
import models.laboratory.common.instance.property.PropertyFileValue;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.InputContainerUsed;
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
	
	private final Form<PropertyFileList> fileForms;

	/**
	 * Experiment API.
	 */
	private final ExperimentsAPI experimentsAPI;
	
	@Inject
	public IO(NGLApplication app, ExperimentsAPI experimentsAPI) {
		this.app            = app;
		this.fileForm       = app.formFactory().form(PropertyFileValue.class);
		this.fileForms		= app.formFactory().form(PropertyFileList.class);
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
			String className = this.getClassName(experiment, "Output");
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
	 * for the class resolution).</br></br>
	 * 
	 * FDS 25/10/2017 ajout parametre optionnel 'extraInstrument'</br>
	 * permet d'outrepasser l'instrument embarqué par l'experiment...</br>
	 * utilisé par exemple pour l'import de fichier MettlerToledo qui n'est pas un "instrument" au sens NGL</br>
	 * utilisable aussi pour faire executer par 'hand' du code normalement executé par un intrument officiel...</br></br>
	 * 
	 * InputType: permet de sélectionner un type d'input, exemple: 'Input' ou 'MultiInput'.
	 * @param contextValidation  validation context
	 * @param experiment         experiment
	 * @param extraInstrument	 extraInstrument
	 * @param inputType			 inputType
	 * @return					 input instance
	 */
	private AbstractInputHandler getInputInstance(ContextValidation contextValidation, Experiment experiment, String extraInstrument, String inputType) {
		//JV BUG 
		//return getIntrumentSpecificInstance(contextValidation, experiment, "Input", instrumentTypeCode);
		if (ValidationHelper.validateNotEmpty(contextValidation, experiment.instrument, "instrument") 
				&& ValidationHelper.validateNotEmpty(contextValidation, experiment.instrument.typeCode, "instrument.code")) {
			String className; 
			
			if (StringUtils.isEmpty(extraInstrument)) {
				// ancien comportement
				className = this.getClassName(experiment, inputType);
			} else {
				// Class Input de extraInstrument
				String institute = DescriptionHelper.getInstitutes().get(0).toLowerCase();
				className ="controllers.instruments.io." + institute + "." + extraInstrument + "." + inputType;
			}
			try {
				@SuppressWarnings("unchecked") // Uncheckable reflection
				Class<? extends AbstractInputHandler>       clazz       = (Class<? extends AbstractInputHandler>) Class.forName(className);
				Constructor<? extends AbstractInputHandler> constructor = clazz.getConstructor();
				AbstractInputHandler                        instance    = constructor.newInstance();
				return instance;
			} catch (Exception e) {
				logger.error(e.getMessage());
				contextValidation.addError("inputClass", "io.error.instance.notexist", className);
			}
		}
		return null;	
	}
	
	private AbstractInput getInputInstance(ContextValidation contextValidation, Experiment experiment, String extraInstrument) {
		return (AbstractInput) this.getInputInstance(contextValidation, experiment, extraInstrument, "Input");
	}
	
	private AbstractMultiInput getMultiInputInstance(ContextValidation contextValidation, Experiment experiment, String extraInstrument) {
		return (AbstractMultiInput) this.getInputInstance(contextValidation, experiment, extraInstrument, "MultiInput");
	}
	
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
		if (experiment == null) {
			return badRequest("experiment not found " + experimentCode);
		}
		// GA/FDS 22/07/2016 ajout .bindFromRequest() + context....putAll pour recuperer un parametre de la query string...
		DynamicForm filledForm = app.formFactory().form().bindFromRequest(); 
		ContextValidation contextValidation = ContextValidation.createUndefinedContext(this.getCurrentUser());
        contextValidation.getContextObjects().putAll(filledForm.rawData());
        Object output = this.getOutputInstance(contextValidation, experiment);
        if (!contextValidation.hasErrors()) {
        	try {
        		File file = null;
        		if (output instanceof AbstractOutput) {
					file = ((AbstractOutput)output).generateFile(experiment, contextValidation);
				} else if (output instanceof AbstractTypedOutput) {
					file = ((AbstractTypedOutput<?>)output).run(contextValidation, experiment, request().queryString());
				} else {
					throw new RuntimeException("invalid output handler " + output);
				}
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
		if (experiment == null) {
			return badRequest("experiment not found " + experimentCode);
		}
		Form<PropertyFileValue> filledForm = this.getFilledForm(fileForm, PropertyFileValue.class);
		PropertyFileValue pfv = filledForm.get();
		if (pfv == null) {
			return badRequest("missing file");
		}
		ContextValidation contextValidation = ContextValidation.createUndefinedContext(this.getCurrentUser(), filledForm);
		// FDS 25/10 ajout param optionnel pour instrument additionnel
		Object inputHandler = this.getInputInstance(contextValidation, experiment, extraInstrument);
        if (!contextValidation.hasErrors()) {
        	try {
        		if (inputHandler instanceof AbstractInput) {
        			this.fillDataWith(contextValidation.getContextObjects(), request().queryString());
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
		if (!contextValidation.hasErrors()) {
			return ok(Json.toJson(experiment));
		}
		return badRequest(app.errorsAsJson(contextValidation.getErrors()));
	}
	
	@BodyParser.Of(value = IGBodyParsers.Json5MB.class)
	@Permission(value={"writing"})
	public Result importFiles(String experimentCode, String extraInstrument) {
		Experiment experiment = experimentsAPI.get(experimentCode);
		if (experiment == null) {
			return badRequest("experiment not found " + experimentCode);
		}
		Form<PropertyFileList> filledForms = this.getFilledForm(fileForms, PropertyFileList.class);
		ContextValidation contextValidation = ContextValidation.createUndefinedContext(this.getCurrentUser(), filledForms);
		
		try {
			List<PropertyFileValue> pfvs = filledForms.get().files;
			if(pfvs.stream().anyMatch((PropertyFileValue pfv) -> pfv == null)) {
				throw new IllegalStateException("missing files");
			}
			
			Object inputHandler = this.getMultiInputInstance(contextValidation, experiment, extraInstrument);
		
			if(inputHandler == null) {
				throw new RuntimeException("invalid input handler: null");
			} else if (!(inputHandler instanceof AbstractMultiInput)) {
				throw new RuntimeException("invalid input handler: " + inputHandler.getClass().getName());
			}
			
			AbstractMultiInput multiInputHandler = (AbstractMultiInput) inputHandler;
			
			// get global files from list
			Map<String, PropertyFileValue> globalFilesMap = multiInputHandler
					.getGlobalFilesMap(experiment, pfvs, contextValidation);
			// remove global files from list
			if(!globalFilesMap.isEmpty()) {
				pfvs.removeAll(globalFilesMap.values());
			}
			
			// get positions files
			Map<String, PropertyFileValue> filePositionsMap = multiInputHandler
					.getPositionsMap(experiment, pfvs, contextValidation);
			
			// get experiment inputContainerUseds
			Map<String, InputContainerUsed> icuPositionsMap = MultiInputHelper.getIcuPositionsMap(experiment);
			
			MultiInputHelper.handleMissingPositions(icuPositionsMap.keySet(), filePositionsMap.keySet(), multiInputHandler, contextValidation);
			
			MultiInputHelper.handleGlobalFiles(experiment, multiInputHandler, globalFilesMap, contextValidation);
			
			if (!contextValidation.hasErrors()) {			
				for(String position : filePositionsMap.keySet()) {
					if(icuPositionsMap.containsKey(position)) {
						this.fillDataWith(contextValidation.getContextObjects(), request().queryString());
		    			experiment = multiInputHandler.importPartialFile(experiment, 
		    					filePositionsMap.get(position), icuPositionsMap.get(position), contextValidation);
					} else {
						// if valid position but not inputContainers
						contextValidation.addError("Erreurs fichier", "experiments.msg.import.multi.no.container", String.valueOf(position));
					}
				}
				
				if (!contextValidation.hasErrors()) {
					// call some postProcessing if needed
					experiment = multiInputHandler.postProcessing(experiment, contextValidation);
				}
				
				if (!contextValidation.hasErrors()) {
					return ok(Json.toJson(experiment));
				}
			}
			
		} catch (Throwable e) {
    		logger.error(e.getMessage(), e);
    		contextValidation.addError("Error :", String.valueOf(e.getMessage()));
    	}
		return badRequest(app.errorsAsJson(contextValidation.getErrors()));
	}
	
}




