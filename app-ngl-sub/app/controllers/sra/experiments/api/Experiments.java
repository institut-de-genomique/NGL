package controllers.sra.experiments.api;



import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;

import controllers.NGLAPIController;
import controllers.QueryFieldsForm;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import fr.cea.ig.ngl.dao.sra.ExperimentDAO;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.UserExperimentExtendedType;
import models.sra.submit.sra.instance.UserExperimentType;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import services.Tools;
import sra.parser.UserExperimentExtendedTypeParser;
import sra.parser.UserExperimentTypeParser;
import validation.ContextValidation;
import views.components.datatable.DatatableForm;

public class Experiments extends NGLAPIController<ExperimentAPI, ExperimentDAO, Experiment>  {

	private static final play.Logger.ALogger         logger                     = play.Logger.of(Experiments.class);
	private Map<String, UserExperimentType>          mapUserExperiments         = new HashMap<>();
	private Map<String, UserExperimentExtendedType>  mapUserExperimentsExtended = new HashMap<>();
	private final Form<Experiment>                   experimentForm;
	private final Form<QueryFieldsForm>              updateForm;
	private final Form<UserFileExperimentsForm>      userFileExperimentsForm;

	//NGL-3666:
	//	private AbstractSampleAPI abstractSampleAPI; utilisé dans getQuery deportée dans ExperimentSearchForm
	//	private AbstractStudyAPI abstractStudyAPI;

	@Inject
	public Experiments(NGLApplication app,
					   ExperimentAPI  api) {

		super(app, api, ExperimentsSearchForm.class);
		this.userFileExperimentsForm = app.formFactory().form(UserFileExperimentsForm.class);
		this.experimentForm          = app.formFactory().form(Experiment.class);
		this.updateForm              = app.form(QueryFieldsForm.class);
	}




	@Override
	@Authenticated
	@Authorized.Read
	public Result get(String code) {
		return globalExceptionHandler(() -> {
			DatatableForm form = objectFromRequestQueryString(DatatableForm.class);
			Experiment experiment = api().getObject(code, generateBasicDBObjectFromKeys(form));
			if (experiment == null) {
				return notFound();
			} 
			return okAsJson(experiment);
		});	
	}	




	// methode saveImpl appelée par le save de la classe mere, elle meme appelée par la route 
	@Override
	@Authenticated
	@Authorized.Write
	public Experiment saveImpl() throws APIException {
		Experiment userExperiment = getFilledForm(experimentForm, Experiment.class).get();
		boolean copy = false;
		ExperimentsUrlParamForm filledExperimentsUrlParamForm = filledFormQueryString(getNGLApplication().formFactory()
				.form(ExperimentsUrlParamForm.class),  ExperimentsUrlParamForm.class).get();

		if (filledExperimentsUrlParamForm != null  && filledExperimentsUrlParamForm.copy != null && filledExperimentsUrlParamForm.copy == true ) {
			copy = true;
		} else {
			copy = false;
		}
		return api().create(userExperiment, getCurrentUser(), copy);	
	}

	
	

	@Override
	@Authenticated
	@Authorized.Write
	public Experiment updateImpl(String code) throws Exception, APIException, APIValidationException {
		Experiment userExperiment =  getFilledForm(experimentForm, Experiment.class).get();
		//Form<QueryFieldsForm> filledQueryFieldsForm = filledFormQueryString(updateForm, QueryFieldsForm.class);
		//QueryFieldsForm queryFieldsForm = filledQueryFieldsForm.get();
		QueryFieldsForm queryFieldsForm = filledFormQueryString(updateForm, QueryFieldsForm.class).get();
		if (StringUtils.isBlank(code)) {
			throw new Exception("experiment.code :  valeur nulle  incompatible avec appel de la methode updateImpl");
		}
		if(StringUtils.isNotBlank(userExperiment.code) && ! userExperiment.code.equals(code)) {
			throw new Exception("experiment.code :  valeur " + userExperiment.code + " != du code indiqué dans la route "  + code);
		}
		boolean copy = false;
		if (queryFieldsForm.fields == null) {	
			return api().update(userExperiment, getCurrentUser(), false);
		} 
		if(queryFieldsForm.fields.contains("copy")) {
			copy = true;
			queryFieldsForm.fields.remove("copy");
			return api().update(userExperiment, getCurrentUser(), true);
		} 
		return api().update(userExperiment, getCurrentUser(), queryFieldsForm.fields); 
	}


	

	// appelé par javascripts.experiments.services.js via la route
	@Authenticated
	@Authorized.Write
	public Result loadUserFileExperiment(String typeParser) {
		UserFileExperimentsForm form = getFilledForm(userFileExperimentsForm, UserFileExperimentsForm.class).get();
		ContextValidation ctxVal = ContextValidation.createUndefinedContext(this.getCurrentUser());
		if (typeParser == null ) {
			String message = "typeParser autorises: userExperiment ou userExperimentExtented et ici aucun typeParser indique";
			ctxVal.addError("RuntimeException", message);
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}
		
		logger.debug("ZZZZZZZZZZZZZZZZZZZ           typeParser=" + typeParser);
		try {
			//logger.debug("form.base64UserFileExperiment", form.base64UserFileExperiment);
			//logger.debug("Experiments::list::list.size = " + list.size());

			if (StringUtils.isBlank(form.base64UserFileExperiment)) {
				//logger.debug("Pas de fichier utilisateur  dans experimentSearchForm.base64 XXXXXXXXX ");
				form.base64UserFileExperiment = "";
			} else {
				//logger.debug("ok pour fichier utilisateur dans experimentSearchForm");
			}
			//logger.debug("Read base64UserFileExperiment");
			InputStream inputStreamUserFileExperiments = Tools.decodeBase64(form.base64UserFileExperiment);

			if (typeParser.equalsIgnoreCase("userExperiment")) {

				UserExperimentTypeParser userExperimentsParser = new UserExperimentTypeParser();
				mapUserExperiments = userExperimentsParser.loadMap(inputStreamUserFileExperiments);
				for (Iterator<Entry<String, UserExperimentType>> iterator = mapUserExperiments.entrySet().iterator(); iterator.hasNext();) {
					Entry<String, UserExperimentType> entry = iterator.next();
					//						logger.debug("  cle de exp = '" + entry.getKey() + "'");
					//						logger.debug("       nominal_length : '" + entry.getValue().getNominalLength()+ "'");
					//						logger.debug("Appel de validate");
					entry.getValue().validate(ctxVal);
				}

				// Renvoyer message d'erreur si le fichier utilisateur n'est pas bon:
				if (ctxVal.hasErrors()) {
					//logger.debug("Erreur dans le fichier utilisateur :");
					//ctxVal.displayErrors(logger, "debug");
					return badRequest(errorsAsJson(ctxVal.getErrors()));
				} 
				return ok(Json.toJson(mapUserExperiments));
			} else if (typeParser.equalsIgnoreCase("userExperimentExtended")) {
				UserExperimentExtendedTypeParser userExperimentsExtendedParser = new UserExperimentExtendedTypeParser();
				mapUserExperimentsExtended = userExperimentsExtendedParser.loadMap(inputStreamUserFileExperiments);
				for (Iterator<Entry<String, UserExperimentExtendedType>> iterator = mapUserExperimentsExtended.entrySet().iterator(); iterator.hasNext();) {
					Entry<String, UserExperimentExtendedType> entry = iterator.next();
					//logger.debug("  XXXXXXXXXXXX    cle de exp = '" + entry.getKey() + "'");
					//logger.debug("       study_ac : '" + entry.getValue().getStudyAccession()+ "'");
					//logger.debug("Appel de validate");
					entry.getValue().validate(ctxVal);
				}
				// Renvoyer message d'erreur si le fichier utilisateur n'est pas bon:
				if (ctxVal.hasErrors()) {
					//logger.debug("Erreur dans le fichier utilisateur :");
					//ctxVal.displayErrors(logger, "debug");
					return badRequest(errorsAsJson(ctxVal.getErrors()));
				} 
				return ok(Json.toJson(mapUserExperimentsExtended));
			} else {
				String message = "typeParser autorises: userExperiment ou userExperimentExtented et ici typeParser passe en arg = " + typeParser;
				ctxVal.addError("RuntimeException", message);
				return badRequest(errorsAsJson(ctxVal.getErrors()));
			}
		} catch(RuntimeException e) {
			ctxVal.addError("RuntimeException", e.getMessage());
			e.printStackTrace();
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		} catch(Exception e) {
			ctxVal.addError("Exception", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}
	}



}
