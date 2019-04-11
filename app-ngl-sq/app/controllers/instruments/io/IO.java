package controllers.instruments.io;

import java.lang.reflect.Constructor;

import javax.inject.Inject;

import controllers.TPLCommonController;
import controllers.authorisation.Permission;
import controllers.instruments.io.utils.AbstractInput;
import controllers.instruments.io.utils.AbstractOutput;
import controllers.instruments.io.utils.File;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.play.IGBodyParsers;
import models.laboratory.common.instance.property.PropertyFileValue;
import models.laboratory.experiment.instance.Experiment;
import models.utils.DescriptionHelper;
import models.utils.InstanceConstants;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Result;
import validation.ContextValidation;
import validation.utils.ValidationHelper;

public class IO extends TPLCommonController {

	private static final play.Logger.ALogger logger = play.Logger.of(IO.class);
	
	private final NGLApplication app;
	private final Form<PropertyFileValue> fileForm;

	@Inject
	public IO(NGLApplication app) {
		this.app      = app;
		this.fileForm = app.formFactory().form(PropertyFileValue.class);
	}
	
	private Experiment getExperiment(String code){
		return MongoDBDAO.findByCode(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, code);
	}
	
	private AbstractOutput getOutputInstance(Experiment experiment, ContextValidation contextValidation) {	
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
	}
	
	/* *
	 FDS 25/10/2017 ajout parametre optionnel 'extraInstrument'
	 permet d'outrepasser l'instrument embarqué par l'experiment...
	 utilisé par exemple pour l'import de fichier MettlerToledo qui n'est pas un "instrument" au sens NGL
	 utilisable aussi pour faire executer par 'hand' du code normalement executé par un intrument officiel...
	 */
	private AbstractInput getInputInstance(Experiment experiment, ContextValidation contextValidation, String extraInstrument) {
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

	private String getClassName(Experiment experiment, String type) {
		String institute = DescriptionHelper.getInstitutes().get(0).toLowerCase();
		return "controllers.instruments.io." + institute + "." + experiment.instrument.typeCode.toLowerCase().replace("-", "") + "." + type;
	}
	
	public Result generateFile(String experimentCode) {
		Experiment experiment = getExperiment(experimentCode);
		if (experiment == null) 
			return badRequest("experiment not exist");
		// GA/FDS 22/07/2016 ajout .bindFromRequest() + context....putAll pour recuperer un parametre de la query string...
//		DynamicForm filledForm = this.context.form().bindFromRequest(); 
		DynamicForm filledForm = app.formFactory().form().bindFromRequest(); 
//        ContextValidation contextValidation = new ContextValidation(getCurrentUser(), filledForm.errors());
		ContextValidation contextValidation = ContextValidation.createUndefinedContext(getCurrentUser());
//        contextValidation.getContextObjects().putAll(filledForm.data());
        contextValidation.getContextObjects().putAll(filledForm.rawData());
		AbstractOutput output = getOutputInstance(experiment, contextValidation);
		
		if (!contextValidation.hasErrors()) {
			try {
				File file = output.generateFile(experiment, contextValidation);
				if (!contextValidation.hasErrors() && null != file) {									
					// response().setContentType("application/x-download");  
					response().setHeader("Content-disposition","attachment; filename=" + file.filename);
					return ok(file.content).as("application/x-download");
				}
			} catch (Throwable e) {
				logger.error("IO Error :", e);
				contextValidation.addError("Error :", e.getMessage()+"");
			}
		}		
		// return badRequest(filledForm.errors-AsJson());
//		return badRequest(NGLContext._errorsAsJson(contextValidation.getErrors()));
		return badRequest(app.errorsAsJson(contextValidation.getErrors()));
	}
	
	// @BodyParser.Of(value = BodyParser.Json.class, maxLength = 5000 * 1024)
	@BodyParser.Of(value = IGBodyParsers.Json5MB.class)
	@Permission(value={"writing"})
	public Result importFile(String experimentCode, String extraInstrument) { // FDS 25/10 ajout param optionnel pour instrument additionnel  (voir apinglsq.routes??)
		Experiment experiment = getExperiment(experimentCode);
		if (experiment == null)
			return badRequest("experiment not exist");
		Form<PropertyFileValue> filledForm = getFilledForm(fileForm, PropertyFileValue.class);
		PropertyFileValue pfv = filledForm.get();
//		ContextValidation contextValidation = new ContextValidation(getCurrentUser(), filledForm.errors());
		ContextValidation contextValidation = ContextValidation.createUndefinedContext(getCurrentUser(), filledForm);
		// This fill the context objects with values from the query string (e.g: put("mode","Dev")).
		
        fillDataWith(contextValidation.getContextObjects(), request().queryString());
		if (pfv != null) {
			AbstractInput input = getInputInstance(experiment, contextValidation, extraInstrument); // FDS 25/10 ajout param optionnel pour instrument additionnel
			if (!contextValidation.hasErrors()) {
				try {
					experiment = input.importFile(experiment, pfv,contextValidation);
					if (!contextValidation.hasErrors()) {	
						return ok(Json.toJson(experiment));
					}
				} catch(Throwable e) {
					logger.error(e.getMessage(),e);
					contextValidation.addError("Error :", e.getMessage()+"");
				}
			}
			// return badRequest(filledForm.errors-AsJson());
//			return badRequest(NGLContext._errorsAsJson(contextValidation.getErrors()));
			return badRequest(app.errorsAsJson(contextValidation.getErrors()));
		} else {
			return badRequest("missing file");
		}		
	}
	
}
