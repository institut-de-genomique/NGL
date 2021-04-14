package controllers.sra.submissions.api;


//import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import controllers.DocumentController;
import controllers.QueryFieldsForm;
import controllers.sra.studies.api.StudiesSearchForm;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.play.IGBodyParsers;
import models.laboratory.common.instance.State;
import models.sra.submit.common.instance.Project;
import models.sra.submit.common.instance.Sample;
import models.sra.submit.common.instance.Study;
import models.sra.submit.common.instance.Submission;
import models.sra.submit.common.instance.UserRefCollabType;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.util.SraException;
import models.sra.submit.util.SraValidationException;
import models.utils.InstanceConstants;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Result;
import services.Tools;
import sra.api.submission.SubmissionNewAPI;
import sra.parser.UserRefCollabTypeParser;
import validation.ContextValidation;
import views.components.datatable.DatatableResponse;
import workflows.sra.submission.SubmissionWorkflows;

public class Submissions extends DocumentController<Submission> {

	private static final play.Logger.ALogger logger = play.Logger.of(Submissions.class);

	private final static List<String> authorizedUpdateFields = Arrays.asList("accession","submissionDate", "xmlSubmission", "xmlStudys", "xmlSamples", "xmlExperiments", "xmlRuns");

	private Map<String, UserRefCollabType>          mapUserRefCollab      = new HashMap<>();
	//	private final Form<QueryFieldsForm>         updateForm;
	//	private final Form<Submission>              submissionForm;
	//	private final Form<SubmissionsCreationForm> submissionsCreationForm;
	//	private final Form<SubmissionsSearchForm>   submissionsSearchForm;
	////	private final Form<SubmissionsFileForm>     submissionsACForm; 
	//	private final Form<State>                   stateForm;
	private final SubmissionWorkflows           subWorkflows;

	private final SubmissionNewAPI              submissionNewAPI;

	@Inject
	public Submissions(NGLApplication      app, 
			SubmissionWorkflows subWorkflows, 
			SubmissionNewAPI   submissionNewAPI
			) {
		super(app,InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class);
		//		updateForm              = app.form(QueryFieldsForm.class);
		//		submissionForm          = app.form(Submission.class);
		//		submissionsCreationForm = app.form(SubmissionsCreationForm.class);
		//		submissionsSearchForm   = app.form(SubmissionsSearchForm.class);
		////		submissionsACForm       = app.form(SubmissionsFileForm.class); 
		//		stateForm               = app.form(State.class);
		this.subWorkflows       = subWorkflows;

		this.submissionNewAPI   = submissionNewAPI;
	}

	// methode appelee avec url suivante :
	//localhost:9000/api/sra/submissions?datatable=true&paginationMode=local&projCode=BCZ&state=N
	// url construite dans services.js 
	//search : function(){
	//	this.datatable.search({projCode:this.form.projCode, state:'N'});
	//},

	public Result list() {	
		ContextValidation ctxVal = ContextValidation.createUndefinedContext(this.getCurrentUser());
		try {		
			//		Form<SubmissionsSearchForm> submissionsSearchFilledForm = filledFormQueryString(submissionsSearchForm, SubmissionsSearchForm.class);
			//		SubmissionsSearchForm submissionsSearchForm = submissionsSearchFilledForm.get();
			SubmissionsSearchForm submissionsSearchForm = getURLArgs(SubmissionsSearchForm.class);
			logger.debug(submissionsSearchForm.stateCode);
			Query query = getQuery(submissionsSearchForm);
			MongoDBResult<Submission> results = mongoDBFinder(submissionsSearchForm, query);
			List<Submission> submissionsList = results.toList();
			if (submissionsSearchForm.datatable) {
				return ok(Json.toJson(new DatatableResponse<>(submissionsList, submissionsList.size())));
			} else {
				return ok(Json.toJson(submissionsList));
			}
		} catch(RuntimeException e) {
			ctxVal.addError("RuntimeException", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		} catch(Exception e) {
			ctxVal.addError("Exception", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}
	}


	private Query getQuery(SubmissionsSearchForm form) {
		List<Query> queries = new ArrayList<>();
		Query query = null;
		SubmissionsSearchForm.copyPseudoStateCodesToStateCodesInFormulaire(form);

		if (CollectionUtils.isNotEmpty(form.projCodes)) { 
			queries.add(DBQuery.in("projectCodes", form.projCodes)); // doit pas marcher car pour state.code
			// C'est une valeur qui peut prendre une valeur autorisee dans le formulaire. Ici on veut que 
			// l'ensemble des valeurs correspondent à l'ensemble des valeurs du formulaire independamment de l'ordre.
		}
		if(StringUtils.isNotBlank(form.createUser)) {
			queries.add(DBQuery.in("traceInformation.createUser", form.createUser));
		}
		if(StringUtils.isNotBlank(form.type)) {
			queries.add(DBQuery.in("type", form.type));
		}
		if (CollectionUtils.isNotEmpty(form.stateCodes)) { //all
			queries.add(DBQuery.in("state.code", form.stateCodes));
		} else if (StringUtils.isNotBlank(form.stateCode)) { //all
			queries.add(DBQuery.in("state.code", form.stateCode));
		}
		if (CollectionUtils.isNotEmpty(form.accessions)) { //all
			queries.add(DBQuery.in("accession", form.accessions));
		}	
		if (CollectionUtils.isNotEmpty(form.accessions)) {
			queries.add(DBQuery.in("accession", form.accessions));
		} else if(StringUtils.isNotBlank(form.accessionRegex)) {
			queries.add(DBQuery.regex("accession", Pattern.compile(form.accessionRegex)));
		}
		if (CollectionUtils.isNotEmpty(form.codes)) { //all
			queries.add(DBQuery.in("code", form.codes));
		}
		if (CollectionUtils.isNotEmpty(form.codes)) {
			queries.add(DBQuery.in("code", form.codes));
		} else if(StringUtils.isNotBlank(form.codeRegex)) {
			queries.add(DBQuery.regex("code", Pattern.compile(form.codeRegex)));
		}
		if (queries.size() > 0) {
			query = DBQuery.and(queries.toArray(new Query[queries.size()]));
		}
		return query;
	}

	public Result update(String code) {
		//Get Submission from DB 
		Submission submission = getSubmission(code);
		Form<Submission> submissionForm = app.form(Submission.class);
		Form<Submission> filledForm = getFilledForm(submissionForm, Submission.class);
		//		Form<QueryFieldsForm> filledQueryFieldsForm = filledFormQueryString(updateForm, QueryFieldsForm.class);
		//		QueryFieldsForm queryFieldsForm = filledQueryFieldsForm.get();
		QueryFieldsForm queryFieldsForm = getURLArgs(QueryFieldsForm.class); 
		//		ContextValidation ctxVal = ContextValidation.createUpdateContext(getCurrentUser(), filledForm);
		ContextValidation ctxVal = ContextValidation.createUpdateContext(getCurrentUser());
		try {
			if (submission == null) {
				ctxVal.addError("submission ", " not exist");
				return badRequest(errorsAsJson(ctxVal.getErrors()));
			}
			Submission submissionInput = filledForm.get();
			//		Submission submissionInput = getRequestArgs(Submission.class); 
			if (queryFieldsForm.fields == null) {
				if (code.equals(submissionInput.code)) {	
					submissionInput.traceInformation.setTraceInformation(getCurrentUser());
					submissionInput.validate(ctxVal);
					if (!ctxVal.hasErrors()) {
						logger.info("Update submission state "+submissionInput.state.code);
						MongoDBDAO.update(InstanceConstants.SRA_SUBMISSION_COLL_NAME, submissionInput);
						return ok(Json.toJson(submissionInput));
					} else {
						return badRequest(errorsAsJson(ctxVal.getErrors()));
					}
				} else {
					ctxVal.addError("submission "+code, "submission code  " + code + " and submissionInput.code "+ submissionInput.code + "are not the same");
					return badRequest(errorsAsJson(ctxVal.getErrors()));
				}	
			} else { //update only some authorized properties
				validateAuthorizedUpdateFields(ctxVal, queryFieldsForm.fields, authorizedUpdateFields);
				validateIfFieldsArePresentInForm(ctxVal, queryFieldsForm.fields, filledForm);

				if (!ctxVal.hasErrors()) {
					updateObject(DBQuery.and(DBQuery.is("code", code)), 
							getBuilder(submissionInput, queryFieldsForm.fields).set("traceInformation", getUpdateTraceInformation(submission.traceInformation)));

					return ok(Json.toJson(getObject(code)));
				} else {
					//return badRequest(filledForm.errors-AsJson());
					return badRequest(errorsAsJson(ctxVal.getErrors()));
				}			
			}
		} catch(RuntimeException e) {
			ctxVal.addError("RuntimeException", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		} catch(Exception e) {
			ctxVal.addError("Exception", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}
	}



	public Result updateState(String code) {
		//Get Submission from DB 


		logger.debug("Dans Submissions.updateState, submission.code="+ code);


		Submission submission = getSubmission(code); // ou bien Submission submission2 = getObject(code);
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
				logger.error("Erreur installation dans submission " + submission.code + " du stateCode " + state.code);
				ctxVal.displayErrors(logger, "debug");				
				return badRequest(errorsAsJson(ctxVal.getErrors()));
			}	
			return ok(Json.toJson(getObject(code)));
		} catch (SraValidationException e) { // probleme lors de la validation
			logger.info("SraValidationException", e);
			return badRequest(errorsAsJson(e.getContext().getErrors()));
		} catch (SraException e) { // probleme lors du processus
			ctxVal.addError(e.getKey(), e.getMessage()); // si solution avec ctxVal
			logger.debug("SraException", e);
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		} catch(RuntimeException e) {
			ctxVal.addError("RuntimeException", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		} catch (Exception e) { // probleme non identifié 
			ctxVal.addError("Error", e.getMessage()); 
			logger.debug("Error", e);
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}
	}


	private Submission getSubmission(String code) {
		Submission submission = MongoDBDAO.findByCode(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, code);
		return submission;
	}



	// methode appelée  depuis interface submissions.create-ctrl.js (submissions.create.scala.html)
	// @BodyParser.Of(value = BodyParser.Json.class, maxLength = 15000 * 1024)
	@BodyParser.Of(value = fr.cea.ig.play.IGBodyParsers.Json5MB.class)
	public Result save() throws SraException, IOException {
		//		Form<SubmissionsCreationForm> filledForm = getFilledForm(submissionsCreationForm, SubmissionsCreationForm.class);
		//		logger.debug("filledForm "+filledForm);
		//		SubmissionsCreationForm submissionsCreationForm = filledForm.get();
		//		logger.debug("readsets "+submissionsCreationForm.readSetCodes);
		SubmissionsCreationForm submissionsCreationForm = getRequestArgs(SubmissionsCreationForm.class);
		String user = getCurrentUser();
		//		ContextValidation contextValidation = ContextValidation.createCreationContext(user, filledForm);
		ContextValidation contextValidation = ContextValidation.createCreationContext(user);
		String submissionCode;
		try {
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
			//logger.debug("Read base64UserFileExperiments");
			//			InputStream inputStreamUserFileExperiments = Tools.decodeBase64(submissionsCreationForm.base64UserFileExperiments);
			//			UserExperimentTypeParser userExperimentsParser = new UserExperimentTypeParser();
			//mapUserExperiments = userExperimentsParser.loadMap(inputStreamUserFileExperiments);		
			/*for (Iterator<Entry<String, UserExperimentType>> iterator = mapUserExperiments.entrySet().iterator(); iterator.hasNext();) {
				Entry<String, UserExperimentType> entry = iterator.next();
				System.out.println("  cle de exp = '" + entry.getKey() + "'");
				System.out.println("       nominal_length : '" + entry.getValue().getNominalLength()+  "'");
				System.out.println("       title : '" + entry.getValue().getTitle()+  "'");
				System.out.println("       lib_name : '" + entry.getValue().getLibraryName()+  "'");
				System.out.println("       lib_source : '" + entry.getValue().getLibrarySource()+  "'");
			}*/
			//logger.debug("Read base64UserFileSamples");
			//			InputStream inputStreamUserFileSamples = Tools.decodeBase64(submissionsCreationForm.base64UserFileSamples);
			//			UserSampleTypeParser userSamplesParser = new UserSampleTypeParser();		
			//logger.debug("Read base64UserRefCollabToAc");
			InputStream inputStreamUserFileRefCollabToAc = Tools.decodeBase64(submissionsCreationForm.base64UserFileRefCollabToAc);
			UserRefCollabTypeParser userRefCollabToAc = new UserRefCollabTypeParser();
			mapUserRefCollab = userRefCollabToAc.loadMap(inputStreamUserFileRefCollabToAc);		
			logger.debug("\ntaille de la map des refCollab = " + mapUserRefCollab.size());
			for (Iterator<Entry<String, UserRefCollabType>> iterator = mapUserRefCollab.entrySet().iterator(); iterator.hasNext();) {
			  Entry<String, UserRefCollabType> entry = iterator.next();
			  logger.debug("cle du userRefCollab = '" + entry.getKey() + "'");
			  logger.debug("       study_ac : '" + entry.getValue().getStudyAc()+  "'");
			  logger.debug("       sample_ac : '" + entry.getValue().getSampleAc()+  "'");
			}
			
			List<String> readSetCodes;
			
			InputStream inputStreamUserFileReadSet = Tools.decodeBase64(submissionsCreationForm.base64UserFileReadSet);
			//logger.debug("Read base64UserFileReadSet : " + inputStreamUserFileReadSet);
			//Tools tools = new Tools();
			// Recuperer readSetCodes à partir du fichier utilisateur :		
			readSetCodes = Tools.loadReadSet(inputStreamUserFileReadSet);	
			if (readSetCodes.isEmpty()) {
				// Recuperer readSetCodes à partir de la selection de readset des utilisateurs			
				readSetCodes = submissionsCreationForm.readSetCodes; 
				// remplir la liste readSetCodes
				// à partir de la selection de readset des utilisateurs
			}
			logger.debug("submissionCreationForm.studyCode= " + submissionsCreationForm.studyCode);
			logger.debug("submissionCreationForm.configurationCode= " + submissionsCreationForm.configurationCode);

			submissionCode = submissionNewAPI.initPrimarySubmission(readSetCodes, submissionsCreationForm.studyCode, submissionsCreationForm.configurationCode, submissionsCreationForm.acStudy,submissionsCreationForm.acSample, mapUserRefCollab, contextValidation);
			if (contextValidation.hasErrors()) {
				contextValidation.displayErrors(logger, "debug");
				// return badRequest(filledForm.errors-AsJson());
				return badRequest(errorsAsJson(contextValidation.getErrors()));
			}	
		} catch (SraValidationException e) { // probleme lors de la validation
			logger.info("Dans controller Submissions, je catch bien SraValidationException");
			e.getContext().displayErrors(logger, "debug");
			return badRequest(errorsAsJson(e.getContext().getErrors()));
		} catch (SraException e) { // probleme lors du processus
			//logger.info("Dans controller Submissions, je catch bien SraException");
			//logger.info("Dans controller Submissions, e.getkey()='" + e.getKey() + "' et e.getValue()=" + e.getMessage() +"'"); 

			//			contextValidation.addError("save submission ", e.getMessage()); // si solution avec ctxVal
			// retour chariot dans message qui est ignoré dans javascript et si on tente de remplacer \n par <br/> pour affichage dans javascript, ca ne marche pas.
			//contextValidation.addError(e.getKey(), e.getMessage().replaceAll("\n", "<br/>")); // si solution avec ctxVal
			
			contextValidation.addError(e.getKey(), e.getMessage()); // si solution avec ctxVal
			logger.debug("contextValidation.displayErrors");
			contextValidation.displayErrors(logger, "debug");
			logger.debug("end contextValidation.displayErrors");

			// return badRequest(filledForm.errors-AsJson());
			//logger.debug("Error !!!!!", e);
			return badRequest(errorsAsJson(contextValidation.getErrors()));	
		} catch (Exception e) { // probleme non identifié 
			logger.debug(contextValidation.toString());
			logger.debug("e.message = " + e.getMessage());
			contextValidation.addError("Error", e.getMessage() + " xxx"); 
			logger.debug("Error", e);
			return badRequest(errorsAsJson(contextValidation.getErrors()));
		}

		return ok(Json.toJson(submissionCode));
	}


	/**
	 * Cree une soumission pour la release du study indiqué et active cette soumission en 
	 * la mettant dans un status {@link ngl.refactoring.state.States#SUBR_SMD_IW}.
	 * @param studyCode study code
	 * @return          ok(Json.toJson(submission))
	 */           
	public Result createFromStudyRelease(String studyCode) {

		String user = getCurrentUser();
		ContextValidation contextValidation = ContextValidation.createCreationContext(user);
		try {
			Submission submission = submissionNewAPI.createSubmissionFromStudy(contextValidation, studyCode, user);
			if (contextValidation.hasErrors()) {
				logger.debug("createfromStudyRelease: " + contextValidation.getErrors());
				return badRequest(errorsAsJson(contextValidation.getErrors()));
			}
			return ok(Json.toJson(submission));
		} catch (SraValidationException e) { // probleme lors de la validation
			logger.info("Validation", e);
			return badRequest(errorsAsJson(e.getContext().getErrors()));
		} catch (SraException e) { // probleme lors du processus
			//		contextValidation.addError("save submission ", e.getMessage()); // si solution avec ctxVal
			contextValidation.addError(e.getKey(), e.getMessage()); // si solution avec ctxVal
			// return badRequest(filledForm.errors-AsJson());
			logger.debug("Error", e);
			return badRequest(errorsAsJson(contextValidation.getErrors()));
		} catch (Exception e) { // probleme non identifié 
			contextValidation.addError("Error", e.getMessage()); 
			logger.debug("Error", e);

			return badRequest(errorsAsJson(contextValidation.getErrors()));
		}
	}

	public static class CreateForUpdateArgs {
		public Project       project;
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

	//SGAS
	// a verifier
	//comme pour scripts
	public<T> T getURLArgs(Class <T> clazz) {
		Form<T> formTemplate = app.form(clazz);    // template du formulaire
		Form<T> filledForm = filledFormQueryString(formTemplate, clazz);  //formulaire rempli grace au template et a la classe
		if (filledForm.hasErrors())
			throw new SraValidationException(ContextValidation.createUndefinedContext(getCurrentUser(), filledForm));
		T args = filledForm.get(); // retourne l'objet correspondant à la classe
		return args;
	}

	/**
	 * Cree une soumission pour l'update à l'EBI du study indiqué et active cette soumission en 
	 * la mettant dans un status {@link ngl.refactoring.state.States#SUBU_SMD_IW}.
	 * @return          ok(Json.toJson(submission))
	 */  
	// Met a jour l'experiment dont le code est indiqué avec les valeurs presentes dans l'experiment recuperé du formulaire (userExperiment)
	@BodyParser.Of(value = IGBodyParsers.Json15MB.class)
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
			Project project = args.project;
			
			if (project != null && StringUtils.isNotBlank(project.code)) {
				logger.debug("CreateForUpdate:: args.projectCode = "+ project.code);
			} 	
			if (study != null && StringUtils.isNotBlank(study.code)) {
				logger.debug("CreateForUpdate:: args.studyCode = "+ study.code);
			} 			
			for (Sample sample : samples) {
				logger.debug("args.sampleCodes =" + sample.code);
			}
			for (Experiment experiment : experiments) {
				logger.debug("args.experimentCodes=" + experiment.code);
			}		
			
			ArrayList<Sample> list_samples = new ArrayList<>();
			if (samples != null && samples.length !=0) {
				list_samples = new ArrayList<>(Arrays.asList(samples));
			}
			ArrayList<Experiment> list_experiments = new ArrayList<>();
			if (experiments !=null && experiments.length!=0) {
				list_experiments = new ArrayList<>(Arrays.asList(experiments));
			}

			//logger.debug("XXXXXXXXXXXXXXXXXXX   submissions.api.createForUpdate:: avant appel submissionNewAPI.createSubmissionForUpdate");

			Submission submission = submissionNewAPI.createSubmissionForUpdate(contextValidation, project, study, list_samples, list_experiments);
			logger.debug("submissions.api.createForUpdate :: submissionCode = " + submission.code);
			//logger.debug("submissions.api.createForUpdate :: submissionCreationUser = " + submission.creationUser);

			if (contextValidation.hasErrors()) {
				logger.debug("RRRRRRRRRRRRRRRRRRRRR    createSubmissionForUpdate: " + contextValidation.getErrors());
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
			logger.info("ZZZZZZZZZZZZ     Validation", e);
			return badRequest(errorsAsJson(e.getContext().getErrors()));
		} catch (SraException e) { // probleme lors du processus
			logger.debug("ZZZZZZZZZZZ  SRA_EXCEPTION");
			//contextValidation.addError("strategy_study", " STRATEGY_AC_STUDY incompatible avec studyCode renseigne : '" + studyCode +"'");
			contextValidation.addError(e.getKey(), e.getMessage()); 		
			//logger.debug("cle= " + e.getKey() + " message.toString()=" + e.getMessage().toString());		
			return badRequest(errorsAsJson(contextValidation.getErrors()));
		} catch (Exception e) { // probleme non identifié 
			contextValidation.addError("Error", e.getMessage()); 
			logger.debug("Error", e);
			return badRequest(errorsAsJson(contextValidation.getErrors()));
		}		
	}


}

