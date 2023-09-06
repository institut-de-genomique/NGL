package controllers.sra.submissions.api;



//import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import controllers.NGLAPIController;
import controllers.QueryFieldsForm;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import fr.cea.ig.ngl.dao.sra.SubmissionDAO;
import fr.cea.ig.play.IGBodyParsers;
import models.laboratory.common.instance.State;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.Project;
import models.sra.submit.sra.instance.Sample;
import models.sra.submit.sra.instance.Study;
import models.sra.submit.sra.instance.Submission;
import models.sra.submit.sra.instance.UserRefCollabType;
import models.sra.submit.util.SraException;
import models.sra.submit.util.SraValidationException;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Result;
import services.Tools;
import sra.api.submission.SubmissionNewAPI;
import sra.parser.UserRefCollabTypeParser;
import validation.ContextValidation;
import workflows.sra.submission.SubmissionWorkflows;

public class Submissions extends NGLAPIController<SubmissionAPI, SubmissionDAO, Submission> {

	private static final play.Logger.ALogger logger = play.Logger.of(Submissions.class);


	private Map<String, UserRefCollabType>          mapUserRefCollab      = new HashMap<>();
	//	private final Form<QueryFieldsForm>         updateForm;
	//	private final Form<Submission>              submissionForm;
	//	private final Form<SubmissionsCreationForm> submissionsCreationForm;
	//	private final Form<SubmissionsSearchForm>   submissionsSearchForm;
	////	private final Form<SubmissionsFileForm>     submissionsACForm; 
	//	private final Form<State>                   stateForm;
	private final SubmissionAPI                 api;
	private final NGLApplication                app;
	private final SubmissionWorkflows           subWorkflows;
	private final SubmissionNewAPI              submissionNewAPI;
	private final Form<Submission>              submissionForm;
	private final Form<QueryFieldsForm>         updateForm;
	

//	@Inject
//	public Submissions(NGLApplication      app, 
//			SubmissionWorkflows subWorkflows, 
//			SubmissionNewAPI   submissionNewAPI
//			) {
//		super(app,InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class);
//		
//		//		updateForm              = app.form(QueryFieldsForm.class);
//		//		submissionForm          = app.form(Submission.class);
//		//		submissionsCreationForm = app.form(SubmissionsCreationForm.class);
//		//		submissionsSearchForm   = app.form(SubmissionsSearchForm.class);
//		////		submissionsACForm       = app.form(SubmissionsFileForm.class); 
//		//		stateForm               = app.form(State.class);
//		this.subWorkflows       = subWorkflows;
//		this.submissionNewAPI   = submissionNewAPI;
//		this.submissionForm     = app.form(Submission.class);
//	}

	@Inject
	public Submissions(NGLApplication      app,
					   SubmissionAPI       api,
					   SubmissionWorkflows subWorkflows,
					   SubmissionNewAPI    submissionNewAPI) {

		super(app, api, SubmissionsSearchForm.class);
		this.api                     = api;
		this.app                     = app;
		this.submissionForm          = app.formFactory().form(Submission.class);
		this.subWorkflows            = subWorkflows;
		this.submissionNewAPI        = submissionNewAPI;
		this.updateForm              = app.form(QueryFieldsForm.class);

	}

	
	// methode appelee avec url suivante :
	//localhost:9000/api/sra/submissions?datatable=true&paginationMode=local&projCode=BCZ&state=N
	// url construite dans services.js 
	//search : function(){
	//	this.datatable.search({projCode:this.form.projCode, state:'N'});
	//},
//
//	public Result list() {	
//		ContextValidation ctxVal = ContextValidation.createUndefinedContext(this.getCurrentUser());
//		try {		
//			//		Form<SubmissionsSearchForm> submissionsSearchFilledForm = filledFormQueryString(submissionsSearchForm, SubmissionsSearchForm.class);
//			//		SubmissionsSearchForm submissionsSearchForm = submissionsSearchFilledForm.get();
//			SubmissionsSearchForm submissionsSearchForm = getURLArgs(SubmissionsSearchForm.class);
//			//logger.debug(submissionsSearchForm.stateCode);
//			Query query = getQuery(submissionsSearchForm);
//			MongoDBResult<Submission> results = mongoDBFinder(submissionsSearchForm, query);
//			List<Submission> submissionsList = results.toList();
//			if (submissionsSearchForm.datatable) {
//				return ok(Json.toJson(new DatatableResponse<>(submissionsList, submissionsList.size())));
//			} else {
//				return ok(Json.toJson(submissionsList));
//			}
//		} catch(RuntimeException e) {
//			ctxVal.addError("RuntimeException", e.getMessage());
//			return badRequest(errorsAsJson(ctxVal.getErrors()));
//		} catch(Exception e) {
//			ctxVal.addError("Exception", e.getMessage());
//			return badRequest(errorsAsJson(ctxVal.getErrors()));
//		}
//	}




	@Override
	@Authenticated
	@Authorized.Write
	public Submission updateImpl(String code) throws Exception, APIException, APIValidationException {
		Submission userSubmission =  getFilledForm(submissionForm, Submission.class).get();
		//Form<QueryFieldsForm> filledQueryFieldsForm = filledFormQueryString(updateForm, QueryFieldsForm.class);
		//QueryFieldsForm queryFieldsForm = filledQueryFieldsForm.get();
		QueryFieldsForm queryFieldsForm = filledFormQueryString(updateForm, QueryFieldsForm.class).get();
		if (StringUtils.isBlank(code)) {
			throw new Exception("submission.code :  valeur nulle  incompatible avec appel de la methode updateImpl");
		}
		if(StringUtils.isNotBlank(userSubmission.code) && ! userSubmission.code.equals(code)) {
			throw new Exception("submission.code :  valeur " + userSubmission.code + " != du code indiqué dans la route "  + code);
		}

		boolean copy = false;
		if (queryFieldsForm.fields == null) {	
			return api().update(userSubmission, getCurrentUser(), false);
		} 
		if(queryFieldsForm.fields.contains("copy")) {
			copy = true;
			queryFieldsForm.fields.remove("copy");
			return api().update(userSubmission, getCurrentUser(), true);
		} 
		return api().update(userSubmission, getCurrentUser(), queryFieldsForm.fields); 
	}
	
	


	@Authenticated
	@Authorized.Write
	public Result updateState(String code) {
		//Get Submission from DB 
		//logger.debug("Dans Submissions.updateState, submission.code="+ code);
		Submission submission = this.api.get(code); // ou bien Submission submission2 = getObject(code);
		//		Form<State> filledForm = getFilledForm(stateForm, State.class);
		//		State state = filledForm.get();import play.mvc.BodyParser;

		State state = getRequestArgs(State.class);
		state.date = new Date();
		state.user = getCurrentUser();
		//		ContextValidation ctxVal = new ContextValidation(this.getCurrentUser(), filledForm.errors());
		//		ContextValidation ctxVal = ContextValidation.createUndefinedContext(this.getCurrentUser(), filledForm);
		ContextValidation ctxVal = ContextValidation.createUndefinedContext(this.getCurrentUser());

		if (submission == null) {
			ctxVal.addError("submission " + code,  " n'existe pas dans la base");	
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}
		try {
			subWorkflows.setState(ctxVal, submission, state);
			if (ctxVal.hasErrors()) {
				//logger.debug("Erreur installation dans submission " + submission.code + " du stateCode " + state.code);
				//ctxVal.displayErrors(logger, "debug");				
				return badRequest(errorsAsJson(ctxVal.getErrors()));
			}	
			return ok(Json.toJson(this.api.get(code)));
		} catch (SraValidationException e) { // probleme lors de la validation
			//logger.info("SraValidationException", e);
			return badRequest(errorsAsJson(e.getContext().getErrors()));
		} catch (SraException e) { // probleme lors du processus
			ctxVal.addError(e.getKey(), e.getMessage()); // si solution avec ctxVal
			//logger.debug("SraException", e);
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		} catch(RuntimeException e) {
			ctxVal.addError("RuntimeException", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		} catch (Exception e) { // probleme non identifié 
			ctxVal.addError("Error", e.getMessage()); 
			//logger.debug("Error", e);
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}
	}

	
//	 la methode getObject(code) heritée correspond a la methode get ici appelée getSubmission
//	private Submission getSubmission(String code) {
//		Submission submission = MongoDBDAO.findByCode(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, code);
//		return submission;
//	}



	// methode appelée  depuis interface submissions.create-ctrl.js (submissions.create.scala.html)
	// @BodyParser.Of(value = BodyParser.Json.class, maxLength = 15000 * 1024)
	//@BodyParser.Of(value = fr.cea.ig.play.IGBodyParsers.Json5MB.class)
    @Authenticated
    @Authorized.Write
	public Result createFromUmbrella() throws SraException, IOException {
		SubmissionsCreationFormForUmbrella submissionsCreationFormForUmbrella = getRequestArgs(SubmissionsCreationFormForUmbrella.class);
		String user = getCurrentUser();
		//		ContextValidation contextValidation = ContextValidation.createCreationContext(user, filledForm);
		ContextValidation contextValidation = ContextValidation.createCreationContext(user);
		Submission submission = null;
		String description = null;
		String title = null;
		List <String> childrenProjectAccessions = null;
		List <String> idsPubmed = null;
		String strTaxonId = null;
		if (submissionsCreationFormForUmbrella.title != null) {
			title = submissionsCreationFormForUmbrella.title;
		}
		if (submissionsCreationFormForUmbrella.description != null) {
			description = submissionsCreationFormForUmbrella.description;
		} 				
		if (submissionsCreationFormForUmbrella.childrenProjectAccessions != null) {
			childrenProjectAccessions = submissionsCreationFormForUmbrella.childrenProjectAccessions;
		} 		
		//logger.debug("dans submissionsCtrlAPI");
		if (submissionsCreationFormForUmbrella.idsPubmed != null) {
			idsPubmed = submissionsCreationFormForUmbrella.idsPubmed;
		} 
		if (StringUtils.isNotBlank(submissionsCreationFormForUmbrella.strTaxonId)) {
			strTaxonId = submissionsCreationFormForUmbrella.strTaxonId;
		} 
		try {
			//logger.debug("submissionCreationForm.description= " + submissionsCreationFormForUmbrella.description);
			submission = submissionNewAPI.initPrimarySubmissionForUmbrella(title, description, childrenProjectAccessions, idsPubmed, strTaxonId, contextValidation);
			//logger.debug("dans controller API, submission.code="+ submission.code);
			if (contextValidation.hasErrors()) {
				contextValidation.displayErrors(logger, "debug");
				// return badRequest(filledForm.errors-AsJson());
				return badRequest(errorsAsJson(contextValidation.getErrors()));
			}	
			
		} catch (SraValidationException e) { // probleme lors de la validation
			//logger.info("Dans controller Submissions, je  catch bien SraValidationException");
			e.getContext().displayErrors(logger, "debug");
			return badRequest(errorsAsJson(e.getContext().getErrors()));
		} catch (SraException e) { // probleme lors du processus
			contextValidation.addError(e.getKey(), e.getMessage()); // si solution avec ctxVal
			//logger.debug("contextValidation.displayErrors");
			contextValidation.displayErrors(logger, "debug");
			//logger.debug("end contextValidation.displayErrors");
			return badRequest(errorsAsJson(contextValidation.getErrors()));	
		} catch (Exception e) { // probleme non identifié 
			//logger.debug(contextValidation.toString());
			//logger.debug("e.message = " + e.getMessage());
			contextValidation.addError("Error", e.getMessage() + " xxx"); 
			//logger.debug("Error", e);
			return badRequest(errorsAsJson(contextValidation.getErrors()));
		}

		return ok(Json.toJson(submission));
	}

	// methode appelée  depuis interface submissions.create-ctrl.js (submissions.create.scala.html)
	// @BodyParser.Of(value = BodyParser.Json.class, maxLength = 15000 * 1024)
	@BodyParser.Of(value = fr.cea.ig.play.IGBodyParsers.Json5MB.class)
	@Authenticated
    @Authorized.Write
	public Submission createViaServices() throws APIValidationException  {
		//Form<Submission> filledForm = getFilledForm(submissionForm, Submission.class);
		SubmissionsCreationForm submissionsCreationForm = getRequestArgs(SubmissionsCreationForm.class);
		String user = getCurrentUser();
		ContextValidation contextValidation = ContextValidation.createCreationContext(user);
		boolean copy = false; 
		contextValidation.putObject("copy", copy);
	
		String submissionCode = null;
		try {
			// preparation donnée :
		//	if (!copy) { // En mode classic, on recupere les elements à partir de SubmissionsCreationForm, et on construit la soumission en passant 
				// par la submission est sans code et sans id, il faut generer
						// traceInformation, code et mettre state, et preparer donnees

				if (StringUtils.isBlank(submissionsCreationForm.base64UserFileExperiments)) {
					submissionsCreationForm.base64UserFileExperiments = "";
				}
				if (StringUtils.isBlank(submissionsCreationForm.base64UserFileSamples)) {
					submissionsCreationForm.base64UserFileSamples = "";
				}
				if (StringUtils.isBlank(submissionsCreationForm.base64UserFileRefCollabToAc)) {
					submissionsCreationForm.base64UserFileRefCollabToAc = "";
				}
				if (StringUtils.isBlank(submissionsCreationForm.base64UserFileReadSet)) {
					submissionsCreationForm.base64UserFileReadSet = "";
				}
				if (StringUtils.isBlank(submissionsCreationForm.base64UserFilePathcmap)) {
					submissionsCreationForm.base64UserFilePathcmap = "";
				}

				// logger.debug("Read base64UserFileExperiments");
				// InputStream inputStreamUserFileExperiments =
				// Tools.decodeBase64(submissionsCreationForm.base64UserFileExperiments);
				// UserExperimentTypeParser userExperimentsParser = new UserExperimentTypeParser();
				// mapUserExperiments = userExperimentsParser.loadMap(inputStreamUserFileExperiments);
				/*
				 * for (Iterator<Entry<String, UserExperimentType>> iterator =
				 * mapUserExperiments.entrySet().iterator(); iterator.hasNext();) {
				 * Entry<String, UserExperimentType> entry = iterator.next();
				 * System.out.println("  cle de exp = '" + entry.getKey() + "'");
				 * System.out.println("       nominal_length : '" +
				 * entry.getValue().getNominalLength()+ "'");
				 * System.out.println("       title : '" + entry.getValue().getTitle()+ "'");
				 * System.out.println("       lib_name : '" + entry.getValue().getLibraryName()+
				 * "'"); System.out.println("       lib_source : '" +
				 * entry.getValue().getLibrarySource()+ "'"); }
				 */
				// logger.debug("Read base64UserFileSamples");
				// InputStream inputStreamUserFileSamples =
				// Tools.decodeBase64(submissionsCreationForm.base64UserFileSamples);
				// UserSampleTypeParser userSamplesParser = new UserSampleTypeParser();
				// logger.debug("Read base64UserRefCollabToAc");
				InputStream inputStreamUserFileRefCollabToAc = Tools.decodeBase64(submissionsCreationForm.base64UserFileRefCollabToAc);
				UserRefCollabTypeParser userRefCollabToAc = new UserRefCollabTypeParser();
				mapUserRefCollab = userRefCollabToAc.loadMap(inputStreamUserFileRefCollabToAc);
				//logger.debug("\ntaille de la map des refCollab = " + mapUserRefCollab.size());
//				for (Iterator<Entry<String, UserRefCollabType>> iterator = mapUserRefCollab.entrySet()
//						.iterator(); iterator.hasNext();) {
//					Entry<String, UserRefCollabType> entry = iterator.next();
//					logger.debug("cle du userRefCollab = '" + entry.getKey() + "'");
//					logger.debug("       study_ac : '" + entry.getValue().getStudyAc() + "'");
//					logger.debug("       sample_ac : '" + entry.getValue().getSampleAc() + "'");
//				}

				List<String> readSetCodes;

				InputStream inputStreamUserFileReadSet = Tools.decodeBase64(submissionsCreationForm.base64UserFileReadSet);
				// logger.debug("Read base64UserFileReadSet : " + inputStreamUserFileReadSet);
				// Tools tools = new Tools();
				// Recuperer readSetCodes à partir du fichier utilisateur :
				readSetCodes = Tools.loadReadSet(inputStreamUserFileReadSet);
				if (readSetCodes.isEmpty()) {
					// Recuperer readSetCodes à partir de la selection de readset des utilisateurs
					readSetCodes = submissionsCreationForm.readSetCodes;
					// remplir la liste readSetCodes
					// à partir de la selection de readset des utilisateurs
				}

				InputStream inputStreamUserFilePathcmap = Tools.decodeBase64(submissionsCreationForm.base64UserFilePathcmap);
				
				List<String> pathcmaps;
				pathcmaps = Tools.loadFirstword(inputStreamUserFilePathcmap);
				//logger.debug("submissionCreationForm.studyCode= " + submissionsCreationForm.studyCode);
				//logger.debug("submissionCreationForm.configurationCode= " + submissionsCreationForm.configurationCode);
				//logger.debug("XXXXXXXXXXXXXXXXXXXXXXX submissionCreationForm.acSample= " + submissionsCreationForm.acSample);
//				for (String pathcmap : pathcmaps) {
//					logger.debug("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx                    "+ pathcmap);
//				}
	
				if(submissionsCreationForm.bionanoCheck) {
					//logger.debug("appel submissionNewAPI.initPrimarySubmissionBionano");
					submissionCode = submissionNewAPI.initPrimarySubmissionBionano(
							readSetCodes, 
							pathcmaps, 
							submissionsCreationForm.analysisCode, 
							contextValidation);
				} else if(submissionsCreationForm.defaultCheck) {
					// validation et sauvagarde donnée (si initPrimarySubmission se passe mal, rallback de la base):
					//logger.debug("appel submissionNewAPI.initPrimarySubmission");
					submissionCode = submissionNewAPI.initPrimarySubmission(
							readSetCodes, 
							submissionsCreationForm.studyCode,
							submissionsCreationForm.configurationCode, 
							submissionsCreationForm.acStudy,
							submissionsCreationForm.acSample, 
							mapUserRefCollab, 
							contextValidation);
				} else if (submissionsCreationForm.metadataCheck) { 
					//logger.debug("appel submissionNewAPI.initPrimarySubmissionWithoutRawData");
					submissionCode = submissionNewAPI.initPrimarySubmissionWithoutRawData(submissionsCreationForm.studyCode, submissionsCreationForm.sampleCodes, contextValidation);
				} else {
					contextValidation.addError("save submission ", "type de soumission non reconnu"); // si solution avec ctxVal
					//return badRequest(errorsAsJson(contextValidation.getErrors()));
				}
				
				
				if (contextValidation.hasErrors()) {
					//contextValidation.displayErrors(logger, "debug");
					throw new APIValidationException("INVALID_INPUT_ERROR_MSG", contextValidation.getErrors());
				}
		} catch (SraValidationException e) { // probleme lors de la validation
			//logger.info("Dans controller Submissions, je catch bien SraValidationException");
			e.getContext().displayErrors(logger, "debug");
			//return badRequest(errorsAsJson(e.getContext().getErrors()));
			throw new APIValidationException("SraValidationException", contextValidation.getErrors());
		} catch (SraException e) { // probleme lors du processus
			//logger.info("Dans controller Submissions, je catch bien SraException");
			//logger.info("Dans controller Submissions, e.getkey()='" + e.getKey() + "' et e.getValue()=" + e.getMessage() +"'"); 

			//			contextValidation.addError("save submission ", e.getMessage()); // si solution avec ctxVal
			// retour chariot dans message qui est ignoré dans javascript et si on tente de remplacer \n par <br/> pour affichage dans javascript, ca ne marche pas.
			//contextValidation.addError(e.getKey(), e.getMessage().replaceAll("\n", "<br/>")); // si solution avec ctxVal
			
			contextValidation.addError(e.getKey(), e.getMessage()); // si solution avec ctxVal
			//logger.debug("contextValidation.displayErrors");
			contextValidation.displayErrors(logger, "debug");
			//logger.debug("end contextValidation.displayErrors");

			// return badRequest(filledForm.errors-AsJson());
			//logger.debug("Error !!!!!", e);
			//return badRequest(errorsAsJson(contextValidation.getErrors()));	
			throw new APIValidationException("SraException", contextValidation.getErrors());

		} catch (Exception e) { // probleme non identifié 
			//logger.debug(contextValidation.toString());
			//logger.debug("e.message = " + e.getMessage());
			contextValidation.addError("Error", e.getMessage() + " xxx"); 
			//logger.debug("Error", e);
			//return badRequest(errorsAsJson(contextValidation.getErrors()));
			throw new APIValidationException("Exception", contextValidation.getErrors());

		}
		return(api.get(submissionCode));	
	}

	

	// methode saveImpl appelée par le save de la classe mere, elle meme appelée par la route 
	@Override
	@Authenticated
	@Authorized.Write
	public Submission saveImpl() throws APIException {
		Submission userSubmission = getFilledForm(submissionForm, Submission.class).get();
		boolean copy = false;
		SubmissionsUrlParamForm filledSubmissionsUrlParamForm = filledFormQueryString(getNGLApplication().formFactory()
				.form(SubmissionsUrlParamForm.class),  SubmissionsUrlParamForm.class).get();

		if (filledSubmissionsUrlParamForm != null  && filledSubmissionsUrlParamForm.copy != null && filledSubmissionsUrlParamForm.copy == true ) {
			copy = true;
			return api().create(userSubmission, getCurrentUser(), copy);
		} else {
			copy = false;
			return(createViaServices());
		}
	}

	
	
	/**
	 * Cree une soumission pour la release du study indiqué et active cette soumission en 
	 * la mettant dans un status {@link ngl.refactoring.state.States#SUBR_SMD_IW}.
	 * @param studyCode study code
	 * @return          ok(Json.toJson(submission))
	 */         
	@Authenticated
	@Authorized.Write
	public Result createFromStudyRelease(String studyCode) {

		String user = getCurrentUser();
		ContextValidation contextValidation = ContextValidation.createCreationContext(user);
		try {
			Submission submission = submissionNewAPI.createSubmissionFromStudy(contextValidation, studyCode, user);
			if (contextValidation.hasErrors()) {
				//logger.debug("createfromStudyRelease: " + contextValidation.getErrors());
				return badRequest(errorsAsJson(contextValidation.getErrors()));
			}
			return ok(Json.toJson(submission));
		} catch (SraValidationException e) { // probleme lors de la validation
			//logger.info("Validation", e);
			return badRequest(errorsAsJson(e.getContext().getErrors()));
		} catch (SraException e) { // probleme lors du processus
			//		contextValidation.addError("save submission ", e.getMessage()); // si solution avec ctxVal
			contextValidation.addError(e.getKey(), e.getMessage()); // si solution avec ctxVal
			// return badRequest(filledForm.errors-AsJson());
			//logger.debug("Error", e);
			return badRequest(errorsAsJson(contextValidation.getErrors()));
		} catch (Exception e) { // probleme non identifié 
			contextValidation.addError("Error", e.getMessage()); 
			//logger.debug("Error", e);

			return badRequest(errorsAsJson(contextValidation.getErrors()));
		}
	}

	@Authenticated
	@Authorized.Write
	public static class CreateForUpdateArgs {
		public Project       umbrella; 
		public Study         study;
		public Sample []     samples;
		public Experiment [] experiments;
	}


	
	public<T> T getRequestArgs(Class <T> clazz) {
		Form<T> formTemplate = app.form(clazz);                   // template du formulaire
		Form<T> filledForm = getFilledForm(formTemplate, clazz);  //formulaire rempli grace au template et a la classe
		if (filledForm.hasErrors())
			throw new SraValidationException(ContextValidation.createUndefinedContext(getCurrentUser(), filledForm));
		T args = filledForm.get(); // retourne l'objet correspondant à la classe
		return args;
	}
	
	
//
//	//SGAS
//	// a verifier
//	//comme pour scripts
//	public<T> T getURLArgs(Class <T> clazz) {
//		Form<T> formTemplate = app.form(clazz);    // template du formulaire
//		Form<T> filledForm = filledFormQueryString(formTemplate, clazz);  //formulaire rempli grace au template et a la classe
//		if (filledForm.hasErrors())
//			throw new SraValidationException(ContextValidation.createUndefinedContext(getCurrentUser(), filledForm));
//		T args = filledForm.get(); // retourne l'objet correspondant à la classe
//		return args;
//	}

	/**
	 * Cree une soumission pour l'update à l'EBI du study indiqué (ou des experiments ou des samples ou du project de type SEQUENCING)  
	 * et active cette soumission en 
	 * la mettant dans un status {@link ngl.refactoring.state.States#SUBU_SMD_IW}.
	 * @return          ok(Json.toJson(submission))
	 */  
	// Met a jour l'experiment dont le code est indiqué avec les valeurs presentes dans l'experiment recuperé du formulaire (userExperiment)
	@BodyParser.Of(value = IGBodyParsers.Json15MB.class)
	@Authenticated
    @Authorized.Write
	public Result createForUpdate() {	
		String user = getCurrentUser();
		ContextValidation contextValidation = ContextValidation.createCreationContext(user);
		try {
			//logger.debug("XXXXXXXXXXXXXXXXXXX   submissions.api.createForUpdate");

			//	    	Form<CreateForUpdateArgs> createForUpdateArgsForm = app.form(CreateForUpdateArgs.class);
			// 			Form<CreateForUpdateArgs> createForUpdateArgsFilledForm = getFilledForm(createForUpdateArgsForm, CreateForUpdateArgs.class);
			//			CreateForUpdateArgs args = createForUpdateArgsFilledForm.get();
			CreateForUpdateArgs args = getRequestArgs(CreateForUpdateArgs.class);
			//logger.debug("XXXXXXXXXXXXXXXXXXX   submissions.api.createForUpdate:: apres CreateForUpdateArgs");

			Sample [] samples = args.samples;
			Experiment [] experiments = args.experiments;
			Study study = args.study;
			Project umbrella = args.umbrella;
			

			ArrayList<Sample> list_samples = new ArrayList<>();
			if (samples != null && samples.length !=0) {
				list_samples = new ArrayList<>(Arrays.asList(samples));
			}
			ArrayList<Experiment> list_experiments = new ArrayList<>();
			if (experiments !=null && experiments.length!=0) {
				list_experiments = new ArrayList<>(Arrays.asList(experiments));
			}

			//logger.debug("XXXXXXXXXXXXXXXXXXX   submissions.api.createForUpdate:: avant appel submissionNewAPI.createSubmissionForUpdate");
			//logger.debug("nbre samples = "+ list_samples.size());
			//logger.debug("nbre experiments = "+ list_experiments.size());

			Submission submission = submissionNewAPI.createSubmissionForUpdate(contextValidation, umbrella, study, list_samples, list_experiments);
			//logger.debug("submissions.api.createForUpdate :: submissionCode = " + submission.code);
			//logger.debug("submissions.api.createForUpdate :: submissionCreationUser = " + submission.creationUser);

			if (contextValidation.hasErrors()) {
				//logger.debug("RRRRRRRRRRRRRRRRRRRRR    createSubmissionForUpdate: " + contextValidation.getErrors());
				return badRequest(errorsAsJson(contextValidation.getErrors()));
			}
			//logger.debug("XXXXXXXXXXXXXXXXXXX   submissions.api.createForUpdate:: avant retour ok json, submission.code = " + submission.code);
			contextValidation.setUpdateMode();
			submission.validate(contextValidation);
			if (contextValidation.hasErrors()) {
				//logger.debug("aaaaaaaaaaa   les erreur :");
				contextValidation.displayErrors(logger, "debug");
				return badRequest(errorsAsJson(contextValidation.getErrors()));
			} 
			//logger.debug("aaaaaaaaaaa   Aucune erreur, la soumission existe en base :");
			return ok(Json.toJson(submission));
		} catch (SraValidationException e) { // probleme lors de la validation
			//logger.info("ZZZZZZZZZZZZ     Validation", e);
			return badRequest(errorsAsJson(e.getContext().getErrors()));
		} catch (SraException e) { // probleme lors du processus
			//logger.debug("ZZZZZZZZZZZ  SRA_EXCEPTION");
			//contextValidation.addError("strategy_study", " STRATEGY_AC_STUDY incompatible avec studyCode renseigne : '" + studyCode +"'");
			contextValidation.addError(e.getKey(), e.getMessage()); 		
			//logger.debug("cle= " + e.getKey() + " message.toString()=" + e.getMessage().toString());		
			return badRequest(errorsAsJson(contextValidation.getErrors()));
		} catch (Exception e) { // probleme non identifié 
			logger.debug("contextValidation : " + contextValidation); 
			logger.debug("e == null : "+ (e==null));
			logger.debug("e.getMessage() : "+ (e.getMessage()));
			logger.debug("contextValidation.getErrors() : " + contextValidation.getErrors());
			if (e.getMessage() != null) {
				contextValidation.addError("Error", e.getMessage()); 
			} else {
				contextValidation.addError("Error", "Erreur non identifié"); 
			}
			return badRequest(errorsAsJson(contextValidation.getErrors()));
		}		
	}


}

