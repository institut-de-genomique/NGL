package controllers.sra.submissions.api;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import controllers.DocumentController;
import controllers.QueryFieldsForm;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.ngl.NGLApplication;
import mail.MailServiceException;
import models.laboratory.common.instance.State;
import models.sra.submit.common.instance.Study;
import models.sra.submit.common.instance.Submission;
import models.sra.submit.common.instance.UserCloneType;
import models.sra.submit.util.SraCodeHelper;
import models.sra.submit.util.SraException;
import models.utils.InstanceConstants;
import ngl.refactoring.state.SRASubmissionStateNames;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Result;
import services.FileAcServices;
import services.ReleaseServices;
import services.SubmissionServices;
import services.Tools;
import services.UserCloneTypeParser;
import services.XmlServices;
import validation.ContextValidation;
import views.components.datatable.DatatableResponse;
import workflows.sra.submission.SubmissionWorkflows;

public class Submissions extends DocumentController<Submission> {
	
	private static final play.Logger.ALogger logger = play.Logger.of(Submissions.class);
	
	private final static List<String> authorizedUpdateFields = Arrays.asList("accession","submissionDate", "xmlSubmission", "xmlStudys", "xmlSamples", "xmlExperiments", "xmlRuns");
	
	private Map<String, UserCloneType>          mapUserClones      = new HashMap<>();
	private final Form<QueryFieldsForm>         updateForm;
	private final Form<Submission>              submissionForm;
	private final Form<SubmissionsCreationForm> submissionsCreationForm;
	private final Form<SubmissionsSearchForm>   submissionsSearchForm;
	private final Form<SubmissionsFileForm>     submissionsACForm; 
	private final Form<State>                   stateForm;
	private final SubmissionWorkflows           subWorkflows;
	private final SubmissionServices            submissionServices;
	private final ReleaseServices               releaseServices;
	private final FileAcServices                fileAcServices;
	private final XmlServices                   xmlServices;
	private final SraCodeHelper                 sraCodeHelper;
	
//	@Inject
//	public Submissions(NGLContext ctx, SubmissionWorkflows subWorkflows, 
//			           SubmissionServices submissionServices, 
//			           ReleaseServices    releaseServices,
//			           FileAcServices     fileAcServices,
//			           XmlServices        xmlServices,
//			           SraCodeHelper      sraCodeHelper) {
//		super(ctx,InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class);
//		updateForm              = ctx.form(QueryFieldsForm.class);
//		submissionForm          = ctx.form(Submission.class);
//		submissionsCreationForm = ctx.form(SubmissionsCreationForm.class);
//		submissionsSearchForm   = ctx.form(SubmissionsSearchForm.class);
//		submissionsACForm       = ctx.form(SubmissionsFileForm.class); 
//		stateForm               = ctx.form(State.class);
//		this.subWorkflows       = subWorkflows;
//		this.submissionServices = submissionServices;
//		this.releaseServices    = releaseServices;
//		this.fileAcServices     = fileAcServices;
//		this.xmlServices        = xmlServices;
//		this.sraCodeHelper      = sraCodeHelper;
//	}

	@Inject
	public Submissions(NGLApplication      app, 
			           SubmissionWorkflows subWorkflows, 
			           SubmissionServices  submissionServices, 
			           ReleaseServices     releaseServices,
			           FileAcServices      fileAcServices,
			           XmlServices         xmlServices,
			           SraCodeHelper       sraCodeHelper) {
		super(app,InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class);
		updateForm              = app.form(QueryFieldsForm.class);
		submissionForm          = app.form(Submission.class);
		submissionsCreationForm = app.form(SubmissionsCreationForm.class);
		submissionsSearchForm   = app.form(SubmissionsSearchForm.class);
		submissionsACForm       = app.form(SubmissionsFileForm.class); 
		stateForm               = app.form(State.class);
		this.subWorkflows       = subWorkflows;
		this.submissionServices = submissionServices;
		this.releaseServices    = releaseServices;
		this.fileAcServices     = fileAcServices;
		this.xmlServices        = xmlServices;
		this.sraCodeHelper      = sraCodeHelper;
	}

	// methode appelee avec url suivante :
	//localhost:9000/api/sra/submissions?datatable=true&paginationMode=local&projCode=BCZ&state=N
	// url construite dans services.js 
	//search : function(){
	//	this.datatable.search({projCode:this.form.projCode, state:'N'});
	//},

	public Result list() {	
		Form<SubmissionsSearchForm> submissionsSearchFilledForm = filledFormQueryString(submissionsSearchForm, SubmissionsSearchForm.class);
		SubmissionsSearchForm submissionsSearchForm = submissionsSearchFilledForm.get();
		//modif 		Logger.debug(submissionsSearchForm.state);
		logger.debug(submissionsSearchForm.stateCode);
		Query query = getQuery(submissionsSearchForm);
		MongoDBResult<Submission> results = mongoDBFinder(submissionsSearchForm, query);				
		List<Submission> submissionsList = results.toList();
		if (submissionsSearchForm.datatable) {
			return ok(Json.toJson(new DatatableResponse<>(submissionsList, submissionsList.size())));
		} else {
			return ok(Json.toJson(submissionsList));
		}
	}

	private Query getQuery(SubmissionsSearchForm form) {
		List<Query> queries = new ArrayList<>();
		Query query = null;
		if (CollectionUtils.isNotEmpty(form.projCodes)) { 
			queries.add(DBQuery.in("projectCodes", form.projCodes)); // doit pas marcher car pour state.code
			// C'est une valeur qui peut prendre une valeur autorisee dans le formulaire. Ici on veut que 
			// l'ensemble des valeurs correspondent à l'ensemble des valeurs du formulaire independamment de l'ordre.
		}
		if (CollectionUtils.isNotEmpty(form.stateCodes)) { //all
			queries.add(DBQuery.in("state.code", form.stateCodes));
		}
		if (StringUtils.isNotBlank(form.stateCode)) { //all
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
		Form<Submission> filledForm = getFilledForm(submissionForm, Submission.class);
		Form<QueryFieldsForm> filledQueryFieldsForm = filledFormQueryString(updateForm, QueryFieldsForm.class);
		QueryFieldsForm queryFieldsForm = filledQueryFieldsForm.get();
		
//		ContextValidation ctxVal = new ContextValidation(this.getCurrentUser(), filledForm.errors());
//		ContextValidation ctxVal = new ContextValidation(this.getCurrentUser(), filledForm);
//		ctxVal.setUpdateMode();
		ContextValidation ctxVal = ContextValidation.createUpdateContext(getCurrentUser(), filledForm);
		if (submission == null) {
			//return badRequest("Submission with code "+code+" not exist");
			ctxVal.addError("submission ", " not exist");
			//return badRequest(filledForm.errors-AsJson());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}
		Submission submissionInput = filledForm.get();

		if (queryFieldsForm.fields == null) {
			if (code.equals(submissionInput.code)) {	
//				ctxVal.setUpdateMode();
//				ctxVal.getContextObjects().put("type","sra");
				ctxVal.putObject("type","sra");
				submissionInput.traceInformation.setTraceInformation(getCurrentUser());
				submissionInput.validate(ctxVal);
				if (!ctxVal.hasErrors()) {
					logger.info("Update submission state "+submissionInput.state.code);
					MongoDBDAO.update(InstanceConstants.SRA_SUBMISSION_COLL_NAME, submissionInput);
					return ok(Json.toJson(submissionInput));
				} else {
					//return badRequest(filledForm.errors-AsJson());
					return badRequest(errorsAsJson(ctxVal.getErrors()));
				}
			} else {
				//return badRequest("submission code are not the same");
				ctxVal.addError("submission "+code, "submission code  " +code + " and submissionInput.code "+ submissionInput.code + "are not the same");
				// return badRequest(filledForm.errors-AsJson());
				return badRequest(errorsAsJson(ctxVal.getErrors()));
			}	
		} else { //update only some authorized properties
//			ctxVal = new ContextValidation(getCurrentUser(), filledForm.errors()); 	
//			ctxVal = new ContextValidation(getCurrentUser(), filledForm); 	
//			ctxVal.setUpdateMode();
//			ctxVal = ContextValidation.createUpdateContext(getCurrentUser(), filledForm);
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
	}

	public Result updateState(String code) {
		//Get Submission from DB 
		System.out.println("Dans Submissions.updateState, submission.code="+ code);
		logger.debug("logger Dans Submissions.updateState, submission.code="+ code);
		Submission submission = getSubmission(code); // ou bien Submission submission2 = getObject(code);
		Form<State> filledForm = getFilledForm(stateForm, State.class);
		State state = filledForm.get();
		state.date = new Date();
		state.user = getCurrentUser();
//		ContextValidation ctxVal = new ContextValidation(this.getCurrentUser(), filledForm.errors());
		ContextValidation ctxVal = ContextValidation.createUndefinedContext(this.getCurrentUser(), filledForm);
		if (submission == null) {
			//return badRequest("Submission with code "+code+" not exist");
			ctxVal.addError("submission " + code,  " not exist in database");	
			ctxVal.displayErrors(logger);
			//return badRequest(filledForm.errors-AsJson());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}

		subWorkflows.setState(ctxVal, submission, state);
		
		if (!ctxVal.hasErrors()) {
			return ok(Json.toJson(getObject(code)));
		} else {
			// return badRequest(filledForm.errors-AsJson());
			System.out.println("Error dans updateState:");
			ctxVal.displayErrors(logger);


			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}
	}

	public Result createXml(String code) {
		//Get Submission from DB 
		Submission submission = MongoDBDAO.findByCode(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, code);
		// declaration d'un filledForm uniquement pour utiliser messages.addDetails au niveau des javascript et 
		// pouvoir afficher l'ensemble des erreurs.
		//Form initialise avec l'objet submission car pas d'objet submission dans le body
		//Form<Submission> filledForm = getFilledForm(submissionForm, Submission.class);
		// Form<Submission> filledForm = /*Form.*/form(Submission.class);
		// filledForm.fill(submission);
		Form<Submission> filledForm = getFilledForm(submissionForm, Submission.class);
		ContextValidation ctx = ContextValidation.createUndefinedContext(getCurrentUser(),filledForm);
		if (submission == null) {
			//return badRequest("Submission with code "+code+" not exist");
//			filledForm.reject("Submission " + code," not exist");  // si solution filledForm.reject
			ctx.addError("Submission " + code," not exist");
//			return badRequest(filledForm.errorsAsJson( )); // legit
			return badRequest(errorsAsJson(ctx.getErrors()));
		}
		try {
			submission = xmlServices.writeAllXml(code);
		} catch (IOException e) {
			//return badRequest(e.getMessage());
//			filledForm.reject("Submission " + code, e.getMessage());  // si solution filledForm.reject
//			return badRequest(filledForm.errorsAsJson( )); // legit
			ctx.addError("Submission " + code, e.getMessage());  // si solution filledForm.reject
			return badRequest(errorsAsJson(ctx.getErrors())); 
		} catch (SraException e) {
			//return badRequest(e.getMessage());
//			filledForm.reject("Submission " + code, e.getMessage());  // si solution filledForm.reject
//			return badRequest(filledForm.errorsAsJson( )); // legit
			ctx.addError("Submission " + code, e.getMessage());  // si solution filledForm.reject
			return badRequest(errorsAsJson(ctx.getErrors()));
		}
		return ok(Json.toJson(submission));
	}

	public Result treatmentAc(String code) {
		//Get Submission from DB 
		Submission submission = MongoDBDAO.findByCode(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, code);
		//	Form<File> filledForm = getFilledForm(pathForm, File.class);
		if (submission == null) {
			return badRequest("Submission with code "+code, " not exist");
		}
		Form<SubmissionsFileForm> submissionsACFilledForm = filledFormQueryString(submissionsACForm, SubmissionsFileForm.class);
		SubmissionsFileForm submissionsACForm = submissionsACFilledForm.get();
		//Logger.debug("filledForm "+filledForm);
		File ebiFileAc = new File(submissionsACForm.fileName);
		ContextValidation ctxVal = ContextValidation.createUndefinedContext(getCurrentUser());
		try {
			// submission = FileAcServices.traitementFileAC(ctxVal, code, ebiFileAc);
			submission = fileAcServices.traitementFileAC(ctxVal, code, ebiFileAc);
		} catch (IOException e) {
			//return badRequest(e.getMessage());
			return badRequest("Submission " + code + " et ebiFileAc " +ebiFileAc, e.getMessage());  // si solution filledForm.reject
		} catch (SraException e) {
			//return badRequest(e.getMessage());
			return badRequest("Submission " + code + " et ebiFileAc " +ebiFileAc, e.getMessage());  // si solution filledForm.reject
		} catch (MailServiceException e) {
			//return badRequest(e.getMessage());
			return badRequest("Submission " + code + " et ebiFileAc " +ebiFileAc, e.getMessage());  // si solution filledForm.reject
		}
		return ok(Json.toJson(submission));
	}
	
	public Result treatmentRelease(String code)	{
		//Get Submission from DB 
		Submission submission = MongoDBDAO.findByCode(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, code);
		//	Form<File> filledForm = getFilledForm(pathForm, File.class);
		if (submission == null) {
			return badRequest("Submission with code " + code, " not exist");
		}
		Form<SubmissionsFileForm> submissionsFilledForm = filledFormQueryString(submissionsACForm, SubmissionsFileForm.class);
		SubmissionsFileForm submissionsForm = submissionsFilledForm.get();
		//Logger.debug("filledForm "+filledForm);
		File retourEbiRelease = new File(submissionsForm.fileName);
		ContextValidation ctxVal = ContextValidation.createUndefinedContext(getCurrentUser());
		ctxVal.putObject("fileEbi", retourEbiRelease);
		try {
			// submission = ReleaseServices.traitementRetourRelease(ctxVal, code, retourEbiRelease); 
			submission = releaseServices.traitementRetourRelease(ctxVal, code, retourEbiRelease); 
		} catch (IOException e) {
			//return badRequest(e.getMessage());
			return badRequest("Submission " + code + " et release file " +retourEbiRelease, e.getMessage());  // si solution filledForm.reject
		} catch (SraException e) {
			//return badRequest(e.getMessage());
			return badRequest("Submission " + code + " et release file " +retourEbiRelease, e.getMessage());  // si solution filledForm.reject
		} catch (MailServiceException e) {
			//return badRequest(e.getMessage());
			return badRequest("Submission " + code + " et release file " +retourEbiRelease, e.getMessage());  // si solution filledForm.reject
		}
		return ok(Json.toJson(submission));
	}

	private Submission getSubmission(String code) {
		Submission submission = MongoDBDAO.findByCode(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, code);
		return submission;
	}

	// methode appelée  depuis interface submissions.create-ctrl.js (submissions.create.scala.html)
	// @BodyParser.Of(value = BodyParser.Json.class, maxLength = 15000 * 1024)
	@BodyParser.Of(value = fr.cea.ig.play.IGBodyParsers.Json5MB.class)
	public Result save() throws SraException, IOException {
		Form<SubmissionsCreationForm> filledForm = getFilledForm(submissionsCreationForm, SubmissionsCreationForm.class);
		logger.debug("filledForm "+filledForm);
		SubmissionsCreationForm submissionsCreationForm = filledForm.get();
		logger.debug("readsets "+submissionsCreationForm.readSetCodes);
		String user = getCurrentUser();
//		ContextValidation contextValidation = new ContextValidation(user, filledForm.errors());
//		ContextValidation contextValidation = new ContextValidation(user, filledForm);
//		contextValidation.setCreationMode();
		ContextValidation contextValidation = ContextValidation.createCreationContext(user, filledForm);
//		contextValidation.getContextObjects().put("type", "sra");
		contextValidation.putObject("type", "sra");
		String submissionCode;
		try {
			if (StringUtils.isBlank(submissionsCreationForm.base64UserFileExperiments)) {
				submissionsCreationForm.base64UserFileExperiments = "";
			}
			if (StringUtils.isBlank(submissionsCreationForm.base64UserFileSamples)) {
				submissionsCreationForm.base64UserFileSamples = "";
			}
			if (StringUtils.isBlank(submissionsCreationForm.base64UserFileClonesToAc)) {
				submissionsCreationForm.base64UserFileClonesToAc = "";
			}
			if (StringUtils.isBlank(submissionsCreationForm.base64UserFileReadSet)) {
				submissionsCreationForm.base64UserFileReadSet = "";
			}
			logger.debug("Read base64UserFileExperiments");
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
			logger.debug("Read base64UserFileSamples");
//			InputStream inputStreamUserFileSamples = Tools.decodeBase64(submissionsCreationForm.base64UserFileSamples);
//			UserSampleTypeParser userSamplesParser = new UserSampleTypeParser();		
			logger.debug("Read base64UserFileClonesToAc");
			InputStream inputStreamUserFileClonesToAc = Tools.decodeBase64(submissionsCreationForm.base64UserFileClonesToAc);
			UserCloneTypeParser userClonesParser = new UserCloneTypeParser();
			mapUserClones = userClonesParser.loadMap(inputStreamUserFileClonesToAc);		
			System.out.println("\ntaille de la map des userClone = " + mapUserClones.size());
			/*for (Iterator<Entry<String, UserCloneType>> iterator = mapUserClones.entrySet().iterator(); iterator.hasNext();) {
			  Entry<String, UserCloneType> entry = iterator.next();
			  System.out.println("cle du userClone = '" + entry.getKey() + "'");
			  System.out.println("       study_ac : '" + entry.getValue().getStudyAc()+  "'");
			  System.out.println("       sample_ac : '" + entry.getValue().getSampleAc()+  "'");
			}*/

			List<String> readSetCodes;
			InputStream inputStreamUserFileReadSet = Tools.decodeBase64(submissionsCreationForm.base64UserFileReadSet);
			logger.debug("Read base64UserFileReadSet : "+inputStreamUserFileReadSet);
			Tools tools = new Tools();
			// Recuperer readSetCodes à partir du fichier utilisateur :		
			readSetCodes = tools.loadReadSet(inputStreamUserFileReadSet);	
			if (readSetCodes.isEmpty()) {
				// Recuperer readSetCodes à partir de la selection de readset des utilisateurs			
				readSetCodes = submissionsCreationForm.readSetCodes; 
				// remplir la liste readSetCodes
				// à partir de la selection de readset des utilisateurs
			}
			submissionCode = submissionServices.initPrimarySubmission(readSetCodes, submissionsCreationForm.studyCode, submissionsCreationForm.configurationCode, submissionsCreationForm.acStudy,submissionsCreationForm.acSample, mapUserClones, contextValidation);
			if (contextValidation.hasErrors()) {
				contextValidation.displayErrors(logger);
				// return badRequest(filledForm.errors-AsJson());
				return badRequest(errorsAsJson(contextValidation.getErrors()));
			}	
		} catch (SraException e) {
			contextValidation.addError("save submission ", e.getMessage()); // si solution avec ctxVal
			// return badRequest(filledForm.errors-AsJson());
			return badRequest(errorsAsJson(contextValidation.getErrors()));
		}
		return ok(Json.toJson(submissionCode));
	}


	public Result activate(String submissionCode) throws SraException, IOException {
		// SubmissionServices submissionServices = new SubmissionServices();
		Submission submission = null;
		System.out.println("XXXXXXXXXXXX    Dans controllers.sra.submissions.api.Submissions.activate :");
		logger.debug("Dans controllers.sra.submissions.api.Submissions.activate :");

		// affichage des erreurs via messages.addDetails qui passe par 
		// solution filledForm et reject 
		// ou bien solution ctxVal.addErrors
		String user = getCurrentUser();
		ContextValidation contextValidation = ContextValidation.createUndefinedContext(user);
		Form<Submission> filledForm = getFilledForm(submissionForm, Submission.class); 
		//ContextValidation ctxVal = new ContextValidation(getCurrentUser(), filledForm.errors()); // si solution avec ctxVal
		try {
	
			logger.debug("Dans controllers.sra.submissions.api.Submissions.activate :");
			submissionServices.activatePrimarySubmission(contextValidation, submissionCode);			
			submission = MongoDBDAO.findByCode(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, submissionCode);
		} catch (SraException e) {
			//return badRequest(Json.toJson(e.getMessage()));
			// filledForm.reject("Submission "+submissionCode, e.getMessage());  // si solution filledForm.reject
			contextValidation.addError("Submission "+submissionCode, e.getMessage()); // si solution avec ctxVal
			// Logger.debug("filled form "+filledForm.errors-AsJson());
			logger.debug("filled form "+filledForm.errorsAsJson());
			// return badRequest(filledForm.errors-AsJson());
			logger.debug("filled form "+filledForm.errorsAsJson());

			logger.debug("Error dans activate :");

			contextValidation.displayErrors(logger);
			
			return badRequest(errorsAsJson(contextValidation.getErrors()));
		}
		return ok(Json.toJson(submission));
	}


	/**
	 * Cree une soumission pour la release du study indiqué et active cette soumission en la mettant dans un status IW_SUB_R
	 * @param studyCode study code
	 * @return          ok(Json.toJson(submission))
	 */           
	public Result createFromStudy(String studyCode) {
		logger.info("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		Submission submission = null;
		Date courantDate = new java.util.Date();
		String user = getCurrentUser();

//		ContextValidation contextValidation = new ContextValidation(user);
//			contextValidation.setCreationMode();
		ContextValidation contextValidation = ContextValidation.createCreationContext(user);
		try {
			Study study = MongoDBDAO.findByCode(InstanceConstants.SRA_STUDY_COLL_NAME, Study.class, studyCode);
			if (study == null) {
				contextValidation.addError("createfromStudy appele avec le codeStudy" + studyCode , " qui n'existe pas dans base"); // si solution avec ctxVal
				logger.debug("createfromStudy: " + contextValidation.getErrors());
				return badRequest(errorsAsJson(contextValidation.getErrors()));
			}
			if (study.traceInformation.createUser == null) {
				return unauthorized("le study " + study.code + "n'est pas renseigné pour le createUser" );
			}
			if (user == null) {
				return unauthorized("l'utilisateur n'est pas authentifié" );
			}
			if (!study.traceInformation.createUser.equals(user)) {
				//lcontextValidation.addErrors("createfromStudy appele avec le codeStudy" + studyCode , " qui n'existe pas dans base"); // si solution avec ctxVal
				return unauthorized(user + " n'est pas autorisé à rendre publique le study " + study.code + " crée par " + study.traceInformation.createUser);
			}
			submission = new Submission(user, study.projectCodes);
			submission.code = sraCodeHelper.generateSubmissionCode(study.projectCodes);
			
			submission.creationDate = courantDate;
			logger.debug("************************submissionCode="+ submission.code);
			// mettre à jour l'objet submission pour le cas d'une release de study :
			submission.release = true;
			submission.refStudyCodes.add(study.code);
			submission.studyCode = study.code;		
//			submission.state = new State(SubmissionWorkflows.N_R, contextValidation.getUser());
			submission.state = new State(SRASubmissionStateNames.N_R, contextValidation.getUser());

			// valider et sauver submission
//			contextValidation.getContextObjects().put("type", "sra");
			contextValidation.putObject("type", "sra");
			logger.debug("AVANT submission.validate="+contextValidation.getErrors());
			submission.validate(contextValidation);
			logger.debug("APRES submission.validate="+contextValidation.getErrors());
			if (contextValidation.hasErrors()) {
				logger.debug("createfromStudy: " + contextValidation.getErrors());
				return badRequest(errorsAsJson(contextValidation.getErrors()));
			}
			if (!MongoDBDAO.checkObjectExist(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, "code",submission.code)){	
				MongoDBDAO.save(InstanceConstants.SRA_SUBMISSION_COLL_NAME, submission);
				logger.debug("sauvegarde dans la base du submission " + submission.code);
			}
			subWorkflows.activateSubmissionRelease(contextValidation, submission);
		} catch (SraException e) {
			contextValidation.addError("Submission " + submission.code, e.getMessage()); 
			return badRequest(errorsAsJson(contextValidation.getErrors()));
		}		
		return ok(Json.toJson(submission));
	}

	
}

