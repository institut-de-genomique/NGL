package controllers.sra.studies.api;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import controllers.DocumentController;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.sra.submit.common.instance.AbstractStudy;
import models.sra.submit.common.instance.Study;
import models.sra.submit.util.SraCodeHelper;
import models.sra.submit.util.SraException;
import models.sra.submit.util.VariableSRA;
import models.utils.InstanceConstants;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import services.SubmissionServices;
import validation.ContextValidation;
import views.components.datatable.DatatableResponse;

public class Studies extends DocumentController<AbstractStudy> {
	
	private static final play.Logger.ALogger logger = play.Logger.of(Studies.class);
	
	private final Form<AbstractStudy>     studyForm;
	private final Form<StudiesSearchForm> studiesSearchForm;
	private final SubmissionServices      submissionServices;
	private final SraCodeHelper     sraCodeHelper;

//	@Inject
//	public Studies(NGLContext ctx, SubmissionServices submissionServices, SraCodeHelper     sraCodeHelper) {
//		super(ctx,InstanceConstants.SRA_STUDY_COLL_NAME, AbstractStudy.class);
//		studyForm               = ctx.form(AbstractStudy.class);
//		studiesSearchForm       = ctx.form(StudiesSearchForm.class);
////		stateForm               = ctx.form(State.class);		
////		this.studyWorkflows     = studyWorkflows;
//		this.submissionServices = submissionServices;
//		this.sraCodeHelper      = sraCodeHelper;
//	}

	@Inject
	public Studies(NGLApplication app, SubmissionServices submissionServices, SraCodeHelper sraCodeHelper) {
		super(app,InstanceConstants.SRA_STUDY_COLL_NAME, AbstractStudy.class);
		studyForm               = app.form(AbstractStudy.class);
		studiesSearchForm       = app.form(StudiesSearchForm.class);
		this.submissionServices = submissionServices;
		this.sraCodeHelper      = sraCodeHelper;
	}
	
	public Result release(String studyCode) throws SraException {
		String user = this.getCurrentUser();
		System.out.println("Dans Studies.java.release ");
		ContextValidation contextValidation = ContextValidation.createUndefinedContext(user);
		// SubmissionServices submissionServices = new SubmissionServices();
//		Form<AbstractStudy> filledForm = getFilledForm(studyForm, AbstractStudy.class);
//		AbstractStudy userStudy = filledForm.get();	
		Study study = null;
		try {
			String submissionCode = submissionServices.initReleaseSubmission(studyCode, contextValidation);
			System.out.println("Dans Studies.java.release submissionCode="+ submissionCode);
			// creer le repertoire de soumission
			study = MongoDBDAO.findByCode(InstanceConstants.SRA_STUDY_COLL_NAME, Study.class, studyCode);
		} catch (SraException e) {
//			filledForm.reject("release pour studyCode : "+ studyCode, e.getMessage());
			contextValidation.addError("release pour studyCode : "+ studyCode, e.getMessage());
			// Logger.debug("filled form "+filledForm.errors-AsJson());
			logger.debug("filled form "+errorsAsJson(contextValidation.getErrors()));
			//return badRequest(filledForm.errors-AsJson());
			return badRequest(errorsAsJson(contextValidation.getErrors()));
		}
		return ok(Json.toJson(study));
	}

	public Result save() {
		Form<AbstractStudy> filledForm = getFilledForm(studyForm, AbstractStudy.class);
		AbstractStudy userStudy = filledForm.get();

//		ContextValidation contextValidation = new ContextValidation(getCurrentUser(), filledForm.errors());
//		ContextValidation contextValidation = new ContextValidation(getCurrentUser(), filledForm);
//		contextValidation.setCreationMode();
		ContextValidation contextValidation = ContextValidation.createCreationContext(getCurrentUser(), filledForm);
		if (userStudy._id == null) {
			userStudy.traceInformation = new TraceInformation(); 
			userStudy.traceInformation.setTraceInformation(getCurrentUser());
			userStudy.state = new State("NONE", getCurrentUser());
			if (userStudy instanceof Study) {
				((Study)userStudy).centerName=VariableSRA.centerName;
				((Study)userStudy).centerProjectName = "";
				for (String projectCode: ((Study)userStudy).projectCodes) {
					if (StringUtils.isNotBlank(projectCode)) {
						System.out.println("projectCode= '"+ projectCode +"'");
						((Study)userStudy).centerProjectName += "_" + projectCode;
					}
				}
				if (StringUtils.isNotBlank(((Study)userStudy).centerProjectName)){
					((Study)userStudy).centerProjectName = ((Study)userStudy).centerProjectName.replaceFirst("_", "");
				}
				try {
					((Study)userStudy).code = sraCodeHelper.generateStudyCode(((Study)userStudy).projectCodes);
				} catch (SraException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
			}
//			contextValidation.getContextObjects().put("type", "sra");
			contextValidation.putObject("type", "sra");
			userStudy.validate(contextValidation);
			//Logger.info("utilisateur = "+getCurrentUser());	
			// if(contextValidation.errors.size()==0) {
			if (!contextValidation.hasErrors()) {
				MongoDBDAO.save(InstanceConstants.SRA_STUDY_COLL_NAME, userStudy);
			} else {
				// return badRequest(filledForm.errors-AsJson());
				return badRequest(errorsAsJson(contextValidation.getErrors()));
			}
		} else {
			//return badRequest("study with id " + userStudy._id + " already exist");
//			filledForm.reject("Study_id "+userStudy._id, "study with id " + userStudy._id + " already exist");  // si solution filledForm.reject
//			return badRequest(filledForm.errorsAsJson( )); // legit
			contextValidation.addError("Study_id "+userStudy._id, "study with id " + userStudy._id + " already exist");  // si solution filledForm.reject
			return badRequest(errorsAsJson(contextValidation.getErrors()));
		}
		return ok(Json.toJson(userStudy.code));
	}

	// methode list appelee avec url suivante :
	//localhost:9000/api/sra/studies?datatable=true&paginationMode=local&projCode=BCZ
	// url construite dans services.js 
	//search : function(){
	//	this.datatable.search({projCode:this.form.projCode, state:'N'});
	//},
	// Renvoie le Json correspondant à la liste des study ayant le projectCode indique dans la variable du formulaire projectCode et stockee dans
	// l'instance studiesSearchForm	
	public Result list(){	
		Form<StudiesSearchForm> studiesSearchFilledForm = filledFormQueryString(studiesSearchForm, StudiesSearchForm.class);
		StudiesSearchForm studiesSearchForm = studiesSearchFilledForm.get();
		//Logger.debug(studiesSearchForm.state);
		Query query = getQuery(studiesSearchForm);
		MongoDBResult<AbstractStudy> results = mongoDBFinder(studiesSearchForm, query);				
		List<AbstractStudy> studiesList = results.toList();
		if (studiesSearchForm.datatable)
			return ok(Json.toJson(new DatatableResponse<>(studiesList, studiesList.size())));
		return ok(Json.toJson(studiesList));
	}	

	private Query getQuery(StudiesSearchForm form) {
		List<Query> queries = new ArrayList<>();
		Query query = null;
		
		if (CollectionUtils.isNotEmpty(form.projCodes)) { //
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
	
		if(CollectionUtils.isNotEmpty(form.accessions)){
			queries.add(DBQuery.in("accession", form.accessions));
		}else if(StringUtils.isNotBlank(form.accessionRegex)){
			queries.add(DBQuery.regex("accession", Pattern.compile(form.accessionRegex)));
		}

		if(CollectionUtils.isNotEmpty(form.externalIds)){
			queries.add(DBQuery.in("externalId", form.externalIds));
		}else if(StringUtils.isNotBlank(form.externalIdRegex)){
			queries.add(DBQuery.regex("externalId", Pattern.compile(form.externalIdRegex)));
		}
		
		if (CollectionUtils.isNotEmpty(form.codes)) { //all
			queries.add(DBQuery.in("code", form.codes));
		}

		if(CollectionUtils.isNotEmpty(form.codes)){
			queries.add(DBQuery.in("code", form.codes));
		}else if(StringUtils.isNotBlank(form.codeRegex)){
			queries.add(DBQuery.regex("code", Pattern.compile(form.codeRegex)));
		}

		if ((form.confidential != null) && (form.confidential==true)) {
			Calendar calendar = Calendar.getInstance();
			Date date_courante  = calendar.getTime();
			queries.add(DBQuery.greaterThan("releaseDate", date_courante));
		}

		if(queries.size() > 0){
			query = DBQuery.and(queries.toArray(new Query[queries.size()]));
		}
		return query;
	}

	/* la methode getObject(code) heritée correspond a la methode get ici appelée getStudy
	private AbstractStudy getStudy(String code) {
		AbstractStudy study = MongoDBDAO.findByCode(InstanceConstants.SRA_STUDY_COLL_NAME, AbstractStudy.class, code);
		return study;
	}*/


	public Result update(String code) {
		AbstractStudy study = getObject(code);
		Form<AbstractStudy> filledForm = getFilledForm(studyForm, AbstractStudy.class);
//		ContextValidation ctxVal = new ContextValidation(getCurrentUser(), filledForm.errors()); 	
//		ContextValidation ctxVal = new ContextValidation(getCurrentUser(), filledForm); 	
//		ctxVal.setUpdateMode();
		ContextValidation ctxVal = ContextValidation.createUpdateContext(getCurrentUser(), filledForm);
		if (study == null) {
			//return badRequest("Study with code "+code+" not exist");
			ctxVal.addError("study ", " not exist");
			// return badRequest(filledForm.errors-AsJson());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}
		AbstractStudy studyInput = filledForm.get();

		if (code.equals(studyInput.code)) {	
//			ctxVal.getContextObjects().put("type","sra");
			ctxVal.putObject("type","sra");
			studyInput.traceInformation.setTraceInformation(getCurrentUser());
			studyInput.validate(ctxVal);
			if (!ctxVal.hasErrors()) {
				logger.info("Update study state "+studyInput.state.code);
				MongoDBDAO.update(InstanceConstants.SRA_STUDY_COLL_NAME, studyInput);
				return ok(Json.toJson(studyInput));
			} else {
				// return badRequest(filledForm.errors-AsJson());
				return badRequest(errorsAsJson(ctxVal.getErrors()));
			}
		} else {
			//return badRequest("study code are not the same");
			ctxVal.addError("study " + code, "study code  " + code + " and studyInput.code "+ studyInput.code + " are not the same");
			// return badRequest(filledForm.errors-AsJson());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}	

	}


//	public Result updateState(String code) {
//		//Get Submission from DB 
//		Study study = getStudy(code); 
//		Form<State> filledForm = getFilledForm(stateForm, State.class);
//		State state = filledForm.get();
//		state.date = new Date();
//		state.user = getCurrentUser();
//		ContextValidation ctxVal = new ContextValidation(getCurrentUser(), filledForm.errors());
//		if (study == null) {
//			//return badRequest("Submission with code "+code+" not exist");
//			ctxVal.addErrors("study " + code,  " not exist in database");	
//			// return badRequest(filledForm.errors-AsJson());
//			return badRequest(errorsAsJson(ctxVal.getErrors()));
//		}
//		logger.debug("Controller studies set state for "+study.code);
//		studyWorkflows.setState(ctxVal, study, state);
//		if (!ctxVal.hasErrors()) {
//			return ok(Json.toJson(getObject(code)));
//		} else {
//			//return badRequest(filledForm.errors-AsJson());
//			return badRequest(errorsAsJson(ctxVal.getErrors()));
//		}
//	}

//	private Study getStudy(String code)	{
//		Study study = MongoDBDAO.findByCode(InstanceConstants.SRA_STUDY_COLL_NAME, Study.class, code);
//		return study;
//	}

	/*	public Result release(String code) {
		//Get Submission from DB 
		AbstractStudy study = getStudy(code);		
		Form<AbstractStudy> filledForm = getFilledForm(studyForm, AbstractStudy.class);		
		ContextValidation ctxVal = new ContextValidation(getCurrentUser(), filledForm.errors()); 	

		if (study == null) {
			//return badRequest("Study with code "+code+" not exist");
			ctxVal.addErrors("study ", " not exist");
			return badRequest(filledForm.errors-AsJson());
		}
		AbstractStudy studyInput = filledForm.get();
		if (code.equals(studyInput.code)) {	
			ctxVal.setUpdateMode();
			ctxVal.getContextObjects().put("type","sra");
			studyInput.traceInformation.setTraceInformation(getCurrentUser());
			studyInput.validate(ctxVal);
			if (!ctxVal.hasErrors()) {
				logger.info("Update study state "+studyInput.state.code);
				MongoDBDAO.update(InstanceConstants.SRA_STUDY_COLL_NAME, studyInput);
				return ok(Json.toJson(studyInput));
			}else {
				return badRequest(filledForm.errors-AsJson());
			}
		} else {
			//return badRequest("study code are not the same");
			ctxVal.addErrors("study " + code, "study code  " + code + " and studyInput.code "+ studyInput.code + "are not the same");
			return badRequest(filledForm.errors-AsJson());
		}	
	}
	*/
}
