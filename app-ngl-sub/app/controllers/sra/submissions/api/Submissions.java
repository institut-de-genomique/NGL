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
import java.util.function.Predicate;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;

import controllers.NGLAPIController;
import controllers.QueryFieldsForm;
import controllers.sra.api.SubmissionAPI;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.sra.SubmissionDAO;
import fr.cea.ig.play.IGBodyParsers;
import models.laboratory.common.instance.State;
import models.sra.submit.sra.instance.AbstractSample;
import models.sra.submit.sra.instance.Analysis;
import models.sra.submit.sra.instance.Attribute;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.Project;
import models.sra.submit.sra.instance.Readset;
import models.sra.submit.sra.instance.Sample;
import models.sra.submit.sra.instance.Study;
import models.sra.submit.sra.instance.Submission;
import models.sra.submit.sra.instance.UserRefCollabType;
import models.utils.InstanceConstants;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Result;
import services.sra.SraException;
import services.sra.SraValidationException;
import sra.api.SubmissionNewAPI;
import sra.api.SubmissionWorkflowsAPI;
import sra.parser.UserRefCollabTypeParser;
import sra.services.Tools;
import sra.workflows.SubmissionWorkflows;
import validation.ContextValidation;
import sra.api.SubmissionNewAPITools;
import static ngl.refactoring.state.SRASubmissionStateNames.SUB_V;
import static ngl.refactoring.state.SRASubmissionStateNames.SUB_SRD_IW;


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
	private final SubmissionWorkflowsAPI        submissionWorkflowsAPI;
	private final SubmissionNewAPI              submissionNewAPI;
	private final SubmissionNewAPITools         submissionNewAPITools;
	private final Form<Submission>              submissionForm;
	private final Form<QueryFieldsForm>         updateForm;
	private final Form<Sample>                  submissionsCreationFormForSample;


	

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
	public Submissions(NGLApplication          app,
					   SubmissionAPI           api,
					   SubmissionWorkflows     subWorkflows,
					   SubmissionWorkflowsAPI  submissionWorkflowsAPI,
					   SubmissionNewAPI        submissionNewAPI,
		               SubmissionNewAPITools   submissionNewAPITools) {


		super(app, api, SubmissionsSearchForm.class);
		this.api                     = api;
		this.app                     = app;
		this.submissionForm          = app.formFactory().form(Submission.class);
		this.submissionsCreationFormForSample = app.formFactory().form(Sample.class);

		this.subWorkflows            = subWorkflows;
		this.submissionWorkflowsAPI  = submissionWorkflowsAPI;
		this.submissionNewAPITools   = submissionNewAPITools;
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

		//boolean copy = false;
		if (queryFieldsForm.fields == null) {	
			return api().update(userSubmission, getCurrentUser(), false);
		} 
		if(queryFieldsForm.fields.contains("copy")) {
			//copy = true;
			queryFieldsForm.fields.remove("copy");
			return api().update(userSubmission, getCurrentUser(), true);
		} 
		return api().update(userSubmission, getCurrentUser(), queryFieldsForm.fields); 
	}
	
	public void validateSubmissionAndAllObjects(Submission submission, ContextValidation contextValidation) {
		// Probleme avec la validation qui doit etre en contexte update et des objets qui ne contiennent pas toujours 
		// le modifyUser à ce stade
		// on ajoute le modifyUser et le modifyDate à chaque objet de la soumission
		submission.traceInformation.modificationStamp(contextValidation.getUser());
		submission.validate(contextValidation);
		//contextValidation.displayErrors(logger, ContextValidation.LOG_LEVEL_DEBUG);
		
		if(submission.umbrellaCode != null) {
			if (!MongoDBDAO.checkObjectExist(InstanceConstants.SRA_PROJECT_COLL_NAME, Project.class, "code", submission.umbrellaCode)) {
				contextValidation.addError("umbrella." + submission.umbrellaCode, "absent de la base de données");
			} else {
				Project umbrella = MongoDBDAO.findByCode(InstanceConstants.SRA_PROJECT_COLL_NAME, Project.class, submission.umbrellaCode);
				umbrella.traceInformation.modificationStamp(contextValidation.getUser());
				umbrella.validate(contextValidation);
			}
		}
		if(submission.studyCode != null) {
			if (!MongoDBDAO.checkObjectExist(InstanceConstants.SRA_STUDY_COLL_NAME, Study.class, "code", submission.studyCode)) {
				contextValidation.addError("study." + submission.studyCode, "absent de la base de données");
			} else {
				Study study = MongoDBDAO.findByCode(InstanceConstants.SRA_STUDY_COLL_NAME, Study.class, submission.studyCode);
				study.traceInformation.modificationStamp(contextValidation.getUser());
				study.validate(contextValidation);
			}
		}
		if(submission.analysisCode != null) {
			if (!MongoDBDAO.checkObjectExist(InstanceConstants.SRA_ANALYSIS_COLL_NAME, Analysis.class, "code", submission.analysisCode)) {
				contextValidation.addError("analysis." + submission.analysisCode, "absent de la base de données");
			} else {
				Analysis analysis = MongoDBDAO.findByCode(InstanceConstants.SRA_ANALYSIS_COLL_NAME, Analysis.class, submission.analysisCode);
				analysis.traceInformation.modificationStamp(contextValidation.getUser());
				analysis.validate(contextValidation);
			}
			List<Readset> list_rs =  MongoDBDAO.find(InstanceConstants.SRA_READSET_COLL_NAME, Readset.class, DBQuery.in("analysisCode", submission.analysisCode)).toList();
			for (Readset rs : list_rs) {
				rs.traceInformation.modificationStamp(contextValidation.getUser());
				rs.validate(contextValidation);		
			}
		}	
		if(submission.sampleCodes != null) {
			for (String sampleCode : submission.sampleCodes) {
				if (!MongoDBDAO.checkObjectExist(InstanceConstants.SRA_SAMPLE_COLL_NAME, Sample.class, "code", sampleCode)) {
					contextValidation.addError("sample." + sampleCode, "absent de la base de données");
					continue;
				}
				Sample sample = MongoDBDAO.findByCode(InstanceConstants.SRA_SAMPLE_COLL_NAME, Sample.class, sampleCode);
				sample.traceInformation.modificationStamp(contextValidation.getUser());
				sample.validate(contextValidation);
			}
		}	
		if(submission.experimentCodes != null) {
			for (String experimentCode : submission.experimentCodes) {
				if (!MongoDBDAO.checkObjectExist(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class, "code", experimentCode)) {
					contextValidation.addError("experiment." + experimentCode, "absent de la base de données");
					continue;
				}
				Experiment experiment = MongoDBDAO.findByCode(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class, experimentCode);
				experiment.traceInformation.modificationStamp(contextValidation.getUser());
				experiment.validate(contextValidation);
				if (!MongoDBDAO.checkObjectExist(InstanceConstants.SRA_READSET_COLL_NAME, Readset.class, "experimentCode", experimentCode)) {
					contextValidation.addError("readset.experimentCode." + experimentCode, "absent de la base de données");
					continue;
				}
				List<Readset> list_rs =  MongoDBDAO.find(InstanceConstants.SRA_READSET_COLL_NAME, Readset.class, DBQuery.in("experimentCode", experimentCode)).toList();
				for (Readset rs : list_rs) {
					rs.traceInformation.modificationStamp(contextValidation.getUser());
					rs.validate(contextValidation);		
				}
			}
		}	
		// Autre solution, si on n'a pas ajouté le modifyUser et le modifyDate aux objets de la soumission avant validation :
		// Enlever les erreurs de modifyTrace car on cherche juste à valider les données et on n'a pas toujours
		// modifies les données en base:
//		if(contextValidation.hasErrors()) {
//			contextValidation.removeErrorsByKey(cle->cle.contains("traceInformation"));
//		}
	}
	
	
	@Authenticated
	@Authorized.Write
	public Result validateAndUpdateState(String code) {
		//logger.debug("ZZZZZZZZZZZZZZZZZZZZZZ dans validateAndUpdateState");
		//Get Submission from DB 
		//logger.debug("Dans Submissions.updateState, submission.code="+ code);
		Submission submission = this.api.get(code); // ou bien Submission submission2 = getObject(code);
		//		Form<State> filledForm = getFilledForm(stateForm, State.class);
		//		State state = filledForm.get();import play.mvc.BodyParser;

		State state = new State(SUB_V, getCurrentUser(), new Date());
		state.date = new Date();
		state.user = getCurrentUser();
		//		ContextValidation ctxVal = new ContextValidation(this.getCurrentUser(), filledForm.errors());
		//		ContextValidation ctxVal = ContextValidation.createUndefinedContext(this.getCurrentUser(), filledForm);
		ContextValidation contextValidation = ContextValidation.createUpdateContext(this.getCurrentUser());
		//logger.debug("ZZZZZZZZZZZZZZZZZZZZZZ dans validateAndUpdateState");

		if (submission == null) {
			contextValidation.addError("submission " + code,  " is existe dans la base ? ");	
			return badRequest(errorsAsJson(contextValidation.getErrors()));
		}
		//logger.debug("ZZZZZZZZZZZZZZZZZZZZZZ dans validateAndUpdateState, submission existe dans base");

		validateSubmissionAndAllObjects(submission, contextValidation);
		
		if(contextValidation.hasErrors()) {
			//logger.debug("ZZZZZZZZZZZZZZZZZZZZZZ dans validateAndUpdateState,  debut displayErrors ");
			contextValidation.displayErrors(logger, "error");
			//logger.debug("ZZZZZZZZZZZZZZZZZZZZZZ dans validateAndUpdateState,  fin displayErrors ");

			return badRequest(errorsAsJson(contextValidation.getErrors()));
		}
		
		
		
		ContextValidation ctxVal = ContextValidation.createUndefinedContext(this.getCurrentUser());

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
	
	@Authenticated
	@Authorized.Write
	public Result activate(String code) {
		//logger.debug("ZZZZZZZZZZZZZZZZZZZZZZ dans validateAndUpdateState");
		//Get Submission from DB 
		//logger.debug("Dans Submissions.updateState, submission.code="+ code);
		Submission submission = this.api.get(code); // ou bien Submission submission2 = getObject(code);
		//		Form<State> filledForm = getFilledForm(stateForm, State.class);
		//		State state = filledForm.get();import play.mvc.BodyParser;

		State state = new State(SUB_SRD_IW, getCurrentUser(), new Date());
		//		ContextValidation ctxVal = new ContextValidation(this.getCurrentUser(), filledForm.errors());
		//		ContextValidation ctxVal = ContextValidation.createUndefinedContext(this.getCurrentUser(), filledForm);
		ContextValidation contextValidation = ContextValidation.createUpdateContext(this.getCurrentUser());
		//logger.debug("ZZZZZZZZZZZZZZZZZZZZZZ dans validateAndUpdateState");

		if (submission == null) {
			contextValidation.addError("submission " + code,  " n'existe pas dans la base ? ");	
			return badRequest(errorsAsJson(contextValidation.getErrors()));
		}
		//logger.debug("ZZZZZZZZZZZZZZZZZZZZZZ dans validateAndUpdateState, submission existe dans base");

		validateSubmissionAndAllObjects(submission, contextValidation);
		
		if(contextValidation.hasErrors()) {
			//logger.debug("ZZZZZZZZZZZZZZZZZZZZZZ dans validateAndUpdateState,  debut displayErrors ");
			contextValidation.displayErrors(logger, "error");
			//logger.debug("ZZZZZZZZZZZZZZZZZZZZZZ dans validateAndUpdateState,  fin displayErrors ");

			return badRequest(errorsAsJson(contextValidation.getErrors()));
		}
		
		
		
		ContextValidation ctxVal = ContextValidation.createUndefinedContext(this.getCurrentUser());

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
		String dropBox = null;
		List <String> childrenProjectAccessions = null;
		List <String> parentProjectAccessions = null;

		List <String> idsPubmed = null;
		
		String taxonId = null;
		// NGL-4708
		//List <Attribute> attributes = null;
		String attributes = null;

		if (submissionsCreationFormForUmbrella.title != null) {
			title = submissionsCreationFormForUmbrella.title;
		}
		if (submissionsCreationFormForUmbrella.description != null) {
			description = submissionsCreationFormForUmbrella.description;
		} 				
		if (submissionsCreationFormForUmbrella.childrenProjectAccessions != null) {
			childrenProjectAccessions = submissionsCreationFormForUmbrella.childrenProjectAccessions;
		} 		
		// NGL-4013
		if (submissionsCreationFormForUmbrella.parentProjectAccessions != null) {
			parentProjectAccessions = submissionsCreationFormForUmbrella.parentProjectAccessions;
		} 		
		//logger.debug("dans submissionsCtrlAPI");
		if (submissionsCreationFormForUmbrella.idsPubmed != null) {
			idsPubmed = submissionsCreationFormForUmbrella.idsPubmed;
		} 
		if (StringUtils.isNotBlank(submissionsCreationFormForUmbrella.taxonId)) {
			taxonId = submissionsCreationFormForUmbrella.taxonId;
		} 
		if (submissionsCreationFormForUmbrella.attributes != null) {
			attributes = submissionsCreationFormForUmbrella.attributes;
		}
		if (StringUtils.isNotBlank(submissionsCreationFormForUmbrella.dropBox)) {
			dropBox = submissionsCreationFormForUmbrella.dropBox;
		}
		try {
			//logger.debug("submissionCreationForm.description= " + submissionsCreationFormForUmbrella.description);
			// NGL-4013
			submission = submissionNewAPI.initPrimarySubmissionForUmbrella(contextValidation, title, description, parentProjectAccessions, childrenProjectAccessions, idsPubmed, taxonId, attributes, dropBox);
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

	// methode appelée  depuis interface study.create-ctrl.js (study.scala.html)
	// @BodyParser.Of(value = BodyParser.Json.class, maxLength = 15000 * 1024)
	//@BodyParser.Of(value = fr.cea.ig.play.IGBodyParsers.Json5MB.class)
    @Authenticated
    @Authorized.Write
	public Result createFromStudy() throws SraException, IOException {
    	
		SubmissionsCreationFormForStudy submissionsCreationFormForStudy = getRequestArgs(SubmissionsCreationFormForStudy.class);
		String user = getCurrentUser();
		//		ContextValidation contextValidation = ContextValidation.createCreationContext(user, filledForm);
		ContextValidation contextValidation = ContextValidation.createCreationContext(user);
		Submission submission = null;
		List<String>     projectCodes = null;
		String existingStudyType = null;
		String centerProjectName = null;
		String description = null;
		String title = null;
		String strTaxonId = null;
		String strIdsPubmeds = null;
		String strLocusTagPrefixs = null;
		String strParentProjectAccessions = null;
		String strAttributes = null;
		String dropBox = null;

		// NGL-4708
		//List <Attribute> attributes = null;
		if (submissionsCreationFormForStudy.existingStudyType != null) {
			existingStudyType = submissionsCreationFormForStudy.existingStudyType;
		}
		if (submissionsCreationFormForStudy.centerProjectName != null) {
			centerProjectName = submissionsCreationFormForStudy.centerProjectName;
		}
		if (submissionsCreationFormForStudy.projectCodes != null) {
			projectCodes = submissionsCreationFormForStudy.projectCodes;
		}
		if (submissionsCreationFormForStudy.title != null) {
			title = submissionsCreationFormForStudy.title;
		}
		if (submissionsCreationFormForStudy.description != null) {
			description = submissionsCreationFormForStudy.description;
		} 				
		if (submissionsCreationFormForStudy.strTaxonId != null) {
			strTaxonId = submissionsCreationFormForStudy.strTaxonId;
		} 		
		if (submissionsCreationFormForStudy.strIdsPubmeds != null) {
			strIdsPubmeds = submissionsCreationFormForStudy.strIdsPubmeds;
		} 
		if (submissionsCreationFormForStudy.strLocusTagPrefixs != null) {
			strLocusTagPrefixs = submissionsCreationFormForStudy.strLocusTagPrefixs;
		} 
		if (StringUtils.isNotBlank(submissionsCreationFormForStudy.strParentProjectAccessions)) {
			strParentProjectAccessions = submissionsCreationFormForStudy.strParentProjectAccessions;
		} 
		if (submissionsCreationFormForStudy.strAttributes != null) {
			strAttributes = submissionsCreationFormForStudy.strAttributes;
		}
		if (StringUtils.isNotBlank(submissionsCreationFormForStudy.dropBox)) {
			dropBox = submissionsCreationFormForStudy.dropBox;
		}
		try {
			//logger.debug("submissionCreationForm.description= " + submissionsCreationFormForUmbrella.description);
			// NGL-4013
			submission = submissionNewAPI.initPrimarySubmissionForStudy(projectCodes, 
		            													existingStudyType, 
		            													centerProjectName, 
		            													title, 
		            													description, 
		            													dropBox, 
		            													strTaxonId, 
		            													strIdsPubmeds, 
		            													strLocusTagPrefixs, 
		            													strParentProjectAccessions,
		            													strAttributes, 
		            													contextValidation);
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

 
    
	// methode appelée  depuis interface sample.create-ctrl.js (study.scala.html)
	// @BodyParser.Of(value = BodyParser.Json.class, maxLength = 15000 * 1024)
	//@BodyParser.Of(value = fr.cea.ig.play.IGBodyParsers.Json5MB.class)
    @Authenticated
    @Authorized.Write
	public Result createFromSample() throws SraException, IOException {
    	
		//SubmissionsCreationFormForSample submissionsCreationFormForSample = getRequestArgs(SubmissionsCreationFormForSample.class);
		String user = getCurrentUser();
		//		ContextValidation contextValidation = ContextValidation.createCreationContext(user, filledForm);
		ContextValidation contextValidation = ContextValidation.createCreationContext(user);
		Submission submission     = null;
		// NGL-4708
		try {
			//logger.debug("submissionCreationForm.description= " + submissionsCreationFormForUmbrella.description);
			// NGL-4013
			Sample sample = getFilledForm(submissionsCreationFormForSample, Sample.class).get();
			//logger.debug("dans controller, projectCode={}, dropBox={}, title={}, anonymizedName={}, taxonId={}", sample.projectCode, sample.dropBox, sample.title, sample.anonymizedName, sample.taxonId);
			submission = submissionNewAPI.initPrimarySubmissionForSample(sample.projectCode, 
		            													 sample.taxonId, 
		            													 sample.attributes, 
		            													 sample.dropBox, 
		            													 sample.title, 
		            													 sample.anonymizedName, 
		            													 contextValidation);
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

        
    
	// methode appelée  depuis interface submissions.create-ctrl.js (submissions.create.scala.html dans cas atlaseaCheck)
	// @BodyParser.Of(value = BodyParser.Json.class, maxLength = 15000 * 1024)
	@BodyParser.Of(value = fr.cea.ig.play.IGBodyParsers.Json5MB.class)
	@Authenticated
    @Authorized.Write
	public Result createFromTolid() throws SraException, IOException, APIValidationException {
		SubmissionsCreationForm submissionsCreationForm = getRequestArgs(SubmissionsCreationForm.class);
		String user = getCurrentUser();
		ContextValidation contextValidation = ContextValidation.createCreationContext(user);
		ContextValidation contextSuccess = ContextValidation.createUndefinedContext(user);
		String submissionsAtlaseaMessage = "";
		
		try {	
			if (! submissionsCreationForm.atlaseaCheck) {
				contextValidation.addError("createFromTolid ", "atlaseaCheck à false"); // si solution avec ctxVal
				return badRequest(errorsAsJson(contextValidation.getErrors()));
			} 
			submissionWorkflowsAPI.processSubmissionsForAtlaseaRawData(submissionsCreationForm.tolId, contextValidation, contextSuccess);
			logger.debug("dans controler, submissionsAtlaseaMessage=" + submissionsAtlaseaMessage);
			if (contextValidation.hasErrors()) {
				logger.debug("error=KKKKKKKKK");
				contextValidation.displayErrors(logger, "debug");
				logger.debug("end error=KKKKKKKKK");
				return badRequest(errorsAsJson(contextValidation.getErrors()));
			}
		} catch (SraValidationException e) { // probleme lors de la validation ok marche bien
			//logger.debug("FFFFFFFFFFFFFFFFFF   Dans controller Submissions, je catch bien SraValidationException");
			e.getContext().displayErrors(logger, "debug");
			//logger.debug("END           FFFFFFFFFFFFFFFFFF   Dans controller Submissions, je catch bien SraValidationException");
			return badRequest(errorsAsJson(e.getContext().getErrors()));
			//throw new APIValidationException("SraValidationException", contextValidation.getErrors());
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
		return ok(Json.toJson(errorsAsJson(contextSuccess.getErrors())));
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
				if (StringUtils.isBlank(submissionsCreationForm.base64UserFileRefCollabToEbiIdentifier)) {
					submissionsCreationForm.base64UserFileRefCollabToEbiIdentifier = "";
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
				InputStream inputStreamUserFileRefCollabToEbiIdentifier = Tools.decodeBase64(submissionsCreationForm.base64UserFileRefCollabToEbiIdentifier);
				UserRefCollabTypeParser userRefCollabToEbiIdentifierParser = new UserRefCollabTypeParser();
				mapUserRefCollab = userRefCollabToEbiIdentifierParser.loadMap(inputStreamUserFileRefCollabToEbiIdentifier);
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
				//Laisser le controle dans methode la plus profonde car on veut bloquer toutes les soumissions de plus de 1000 readsets et pas uniquement 
				//celles provenant de l'interface utilisateur.
				if (readSetCodes == null || readSetCodes.isEmpty()) {
					// Recuperer readSetCodes à partir de la selection de readset des utilisateurs
					readSetCodes = submissionsCreationForm.readSetCodes;
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
					//logger.debug("999999999999999999  Dans crateViaServices submissionsCreationForm.acSample={}", submissionsCreationForm.acSample);
					logger.debug("999999999999999999  Dans crateViaServices noCheckTaxonSampleCollab={}", submissionsCreationForm.noCheckTaxonSampleCollab);
					submissionCode = submissionNewAPI.initPrimarySubmission(
							readSetCodes, 
							submissionsCreationForm.studyCode,
							submissionsCreationForm.configurationCode, 
							submissionsCreationForm.acStudy,
							submissionsCreationForm.acSample, 
							mapUserRefCollab, 
							submissionsCreationForm.noCheckTaxonSampleCollab,
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
			//logger.debug("Dans controller Submissions, je catch bien SraValidationException");
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
		return api.get(submissionCode);
		
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
	 * Cree une soumission pour la release du study indiqué, ecrit les metadonnées et les envoie à l'EBI,
	 * recupere le rapport de soumission et met la base de données à jour. La soumission est dans le status SUB-F 
	 * si la soumission est ok, ou en erreur sinon.
	 * @param studyCode study code
	 * @return          ok(Json.toJson(submission))
	 */             
	@Authenticated
	@Authorized.Write
	public Result createFromStudyRelease(String studyCode) {
		String user = getCurrentUser();
		ContextValidation ctxVal = ContextValidation.createCreationContext(user);
		try {
			Submission submission = submissionWorkflowsAPI.processSubmissionForStudyRelease(ctxVal, studyCode);
			if (ctxVal.hasErrors()) {
				return badRequest(errorsAsJson(ctxVal.getErrors()));
			}
			return ok(Json.toJson(submission));
		} catch (SraValidationException e) { // probleme lors de la validation
			return badRequest(errorsAsJson(e.getContext().getErrors()));
		} catch (SraException e) { // probleme lors du processus
			ctxVal.addError(e.getKey(), e.getMessage()); // si solution avec ctxVal
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		} catch (Exception e) { // probleme non identifié 
			ctxVal.addError("Error", e.getMessage()); 
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}
	}

	
	/**
	 * Cree une soumission pour la release du sample indiqué, ecrit les metadonnées et les envoie à l'EBI,
	 * recupere le rapport de soumission et met la base de données à jour. La soumission est dans le status SUB-F 
	 * si la soumission est ok, ou en erreur sinon.
	 * @param sampleCode sample code
	 * @return          ok(Json.toJson(submission))
	 */         
	@Authenticated
	@Authorized.Write
	public Result createFromSampleRelease(String sampleCode) {
		String user = getCurrentUser();
		ContextValidation ctxVal = ContextValidation.createCreationContext(user);
		try {
			Submission submission = submissionWorkflowsAPI.processSubmissionForSampleRelease(ctxVal, sampleCode);
			if (ctxVal.hasErrors()) {
				return badRequest(errorsAsJson(ctxVal.getErrors()));
			}
			return ok(Json.toJson(submission));
		} catch (SraValidationException e) { // probleme lors de la validation
			return badRequest(errorsAsJson(e.getContext().getErrors()));
		} catch (SraException e) { // probleme lors du processus
			//		contextValidation.addError("save submission ", e.getMessage()); // si solution avec ctxVal
			ctxVal.addError(e.getKey(), e.getMessage()); // si solution avec ctxVal
			// return badRequest(filledForm.errors-AsJson());
			logger.debug("yyyyyyyyyyyyyyy   Dans createFromSampleRelease,    SraException=", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		} catch (Exception e) { // probleme non identifié 
			ctxVal.addError(e); 
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}
	}

	
	
	@Authenticated
	@Authorized.Write
	public static class CreateForUpdateArgs {
		public boolean       noCheckTaxonSampleCollab;
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
	 * et active cette soumission, ecrit fichiers xml, envoie metadonnées à l'EBI, recupere et charge rapport de soumission et met la 
	 * base de données à jour.
	 * @return          ok(Json.toJson(submission))
	 */  
	// Met a jour l'experiment dont le code est indiqué avec les valeurs presentes dans l'experiment recuperé du formulaire (userExperiment)
	@BodyParser.Of(value = IGBodyParsers.Json15MB.class)
	@Authenticated
    @Authorized.Write
	public Result createForUpdate() {	
		String user = getCurrentUser();
		ContextValidation ctxVal = ContextValidation.createCreationContext(user);
		try {
			//logger.debug("XXXXXXXXXXXXXXXXXXX   submissions.api.createForUpdate");

			//	    	Form<CreateForUpdateArgs> createForUpdateArgsForm = app.form(CreateForUpdateArgs.class);
			// 			Form<CreateForUpdateArgs> createForUpdateArgsFilledForm = getFilledForm(createForUpdateArgsForm, CreateForUpdateArgs.class);
			//			CreateForUpdateArgs args = createForUpdateArgsFilledForm.get();
			CreateForUpdateArgs args = getRequestArgs(CreateForUpdateArgs.class);
			boolean noCheckTaxonSampleCollab = args.noCheckTaxonSampleCollab;
			logger.debug("YYYYYYYYYYY         noCheckTaxonSampleCollab='{}'", noCheckTaxonSampleCollab);
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

			Submission submission = submissionWorkflowsAPI.processSubmissionForUpdate(ctxVal, umbrella, study, list_samples, list_experiments, noCheckTaxonSampleCollab);
			//Submission submission = submissionNewAPI.createSubmissionForUpdate(ctxVal, umbrella, study, list_samples, list_experiments);
			//logger.debug("submissions.api.createForUpdate :: submissionCode = " + submission.code);
			//logger.debug("submissions.api.createForUpdate :: submissionCreationUser = " + submission.creationUser);

			if (ctxVal.hasErrors()) {
				return badRequest(errorsAsJson(ctxVal.getErrors()));
			}
			ctxVal.setUpdateMode();
			submission.validate(ctxVal);
			if (ctxVal.hasErrors()) {
				return badRequest(errorsAsJson(ctxVal.getErrors()));
			} 
			//logger.debug("aaaaaaaaaaa   Aucune erreur, la soumission existe en base :");
			return ok(Json.toJson(submission));
		} catch (SraValidationException e) { // probleme lors de la validation
			return badRequest(errorsAsJson(e.getContext().getErrors()));
		} catch (SraException e) { // probleme lors du processus 
			ctxVal.addError(e.getKey(), e.getMessage()); 		
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		} catch (Exception e) { // probleme non identifié 
			if (e.getMessage() != null) {
				ctxVal.addError("Error", e.getMessage()); 
			} else {
				ctxVal.addError("Error", "Erreur non identifié"); 
			}
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}		
	}
	
	public Result delete(String submissionCode) {
		String user = getCurrentUser();
		ContextValidation ctxVal = ContextValidation.createCreationContext(user);
		if( StringUtils.isBlank(submissionCode) || submissionCode.equals("undefined")) {
			logger.debug("submissionCode='"+submissionCode+"'");
			ctxVal.addError("SubmissionCode", "Aucun caracteres visibles transmis");
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}
		Submission submission = api.get(submissionCode);
		if (submission == null) {
			ctxVal.addError("Submissioncode", submissionCode + " absent de la base");
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		} 		
		try {
			submissionNewAPITools.rollbackSubmission(submission, user);
			return ok(Json.toJson(submissionCode));
		} catch (SraValidationException e) { // probleme lors de la validation
			return badRequest(errorsAsJson(e.getContext().getErrors()));
		} catch (SraException e) { // probleme lors du processus
			//		contextValidation.addError("save submission ", e.getMessage()); // si solution avec ctxVal
			ctxVal.addError(e.getKey(), e.getMessage()); // si solution avec ctxVal
			// return badRequest(filledForm.errors-AsJson());
			logger.debug("yyyyyyyyyyyyyyy   Dans createFromSampleRelease,    SraException=", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		} catch (Exception e) { // probleme non identifié 
			ctxVal.addError(e); 
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}
	}
	  

}

