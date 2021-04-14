package controllers.sra.studies.api;

import static ngl.refactoring.state.SRASubmissionStateNames.SUB_F;

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
import models.sra.submit.common.instance.ExternalStudy;
import models.sra.submit.common.instance.Study;
import models.sra.submit.util.SraCodeHelper;
import models.sra.submit.util.SraException;
import models.sra.submit.util.VariableSRA;
import models.utils.InstanceConstants;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
//import services.SubmissionServices;
//import sra.api.submission.SubmissionNewAPI;
import validation.ContextValidation;
import views.components.datatable.DatatableResponse;

public class Studies extends DocumentController<AbstractStudy> {

	//	private static final play.Logger.ALogger logger = play.Logger.of(Studies.class);

	private final Form<AbstractStudy>     abstractStudyForm;
	private final Form<StudiesSearchForm> studiesSearchForm;
	private final SraCodeHelper           sraCodeHelper;
	private static final play.Logger.ALogger logger = play.Logger.of(Studies.class);

	@Inject
	public Studies(NGLApplication app, 
			SraCodeHelper sraCodeHelper) {
		super(app,InstanceConstants.SRA_STUDY_COLL_NAME, AbstractStudy.class);
		abstractStudyForm               = app.form(AbstractStudy.class);
		studiesSearchForm       = app.form(StudiesSearchForm.class);
		this.sraCodeHelper      = sraCodeHelper;
	}

	public Result save() {
		
		//Form<StudiesSearchForm> studiesSearchFilledForm = filledFormQueryString(studiesSearchForm, StudiesSearchForm.class);
		
		Form<AbstractStudy> filledForm = getFilledForm(abstractStudyForm, AbstractStudy.class);
		AbstractStudy userStudy = filledForm.get();

		ContextValidation ctxVal = ContextValidation.createCreationContext(getCurrentUser(), filledForm);
		try {
			logger.debug("Dans controller api.save(), study.type = "+ userStudy._type);
			logger.debug("filledForm.get()._type=" + filledForm.get()._type);
			if (userStudy._id == null) {
				userStudy.traceInformation = new TraceInformation(); 
				userStudy.traceInformation.setTraceInformation(getCurrentUser());
				if (userStudy instanceof Study) {
					userStudy.state = new State("NONE", getCurrentUser());
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
//						((Study)userStudy).code = sraCodeHelper.generateStudyCode(((Study)userStudy).projectCodes);
						((Study)userStudy).code = sraCodeHelper.generateStudyCode();
					} catch (SraException e) {
						throw new RuntimeException(e);
					}	
				} else {
					String externalStudyCode = sraCodeHelper.generateExternalStudyCode(userStudy.accession);
					//userStudy = new ExternalStudy(); // objet avec state.code = submitted
					userStudy.code = externalStudyCode;
					userStudy.traceInformation.setTraceInformation(getCurrentUser());	
					userStudy.state = new State(SUB_F, getCurrentUser());
				}
				userStudy.validate(ctxVal);

				if (!ctxVal.hasErrors()) {
					MongoDBDAO.save(InstanceConstants.SRA_STUDY_COLL_NAME, userStudy);
				} else {
					return badRequest(errorsAsJson(ctxVal.getErrors()));
				}
			} else {
				ctxVal.addError("Study_id " + userStudy._id, "study with id " + userStudy._id + " already exist");  // si solution filledForm.reject
				return badRequest(errorsAsJson(ctxVal.getErrors()));
			}
			//return ok(Json.toJson(userStudy.code));
			return ok(Json.toJson(userStudy));

		} catch(RuntimeException e) {
			ctxVal.addError("RuntimeException", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		} catch(Exception e) {
			ctxVal.addError("Exception", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}
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
		StudiesSearchForm.copyPseudoStateCodesToStateCodesInFormulaire(form);

		//logger.debug("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX   Dans getQuery");
		if (CollectionUtils.isNotEmpty(form.projCodes)) { //
			queries.add(DBQuery.in("projectCodes", form.projCodes)); // doit pas marcher car pour state.code
			// C'est une valeur qui peut prendre une valeur autorisee dans le formulaire. Ici on veut que 
			// l'ensemble des valeurs correspondent à l'ensemble des valeurs du formulaire independamment de l'ordre.
		}
		if(StringUtils.isNotBlank(form.createUser)) {
			queries.add(DBQuery.in("traceInformation.createUser", form.createUser));
		}
		if (CollectionUtils.isNotEmpty(form.stateCodes)) { //all
			queries.add(DBQuery.in("state.code", form.stateCodes));
		} else if (StringUtils.isNotBlank(form.stateCode)) { //all
			queries.add(DBQuery.in("state.code", form.stateCode));
		}
		if (StringUtils.isNotBlank(form.accession)) {
			queries.add(DBQuery.in("accession", form.accession));
		} else if (CollectionUtils.isNotEmpty(form.accessions)) {
			queries.add(DBQuery.in("accession", form.accessions));
		} else if(StringUtils.isNotBlank(form.accessionRegex)) {
			queries.add(DBQuery.regex("accession", Pattern.compile(form.accessionRegex)));
		}

		if (StringUtils.isNotBlank(form.externalId)) {
			queries.add(DBQuery.in("externalId", form.externalId));
		} else if(CollectionUtils.isNotEmpty(form.externalIds)){
			queries.add(DBQuery.in("externalId", form.externalIds));
		} else if(StringUtils.isNotBlank(form.externalIdRegex)){
			queries.add(DBQuery.regex("externalId", Pattern.compile(form.externalIdRegex)));
		}

		if (CollectionUtils.isNotEmpty(form.codes)) { //all
			queries.add(DBQuery.in("code", form.codes));
		}

		if (CollectionUtils.isNotEmpty(form.codes)) {
			queries.add(DBQuery.in("code", form.codes));
		} else if(StringUtils.isNotBlank(form.codeRegex)){
			queries.add(DBQuery.regex("code", Pattern.compile(form.codeRegex)));
		}

		if ((form.confidential != null) && (form.confidential==true)) {
			Calendar calendar = Calendar.getInstance();
			Date date_courante  = calendar.getTime();
			queries.add(DBQuery.greaterThan("releaseDate", date_courante));
		}
		if (StringUtils.isNotBlank(form.type)) { //all
			queries.add(DBQuery.in("_type", form.type));
		}	
		if(queries.size() > 0){
			query = DBQuery.and(queries.toArray(new Query[queries.size()]));
		}
		return query;
	}


	//	 la methode getObject(code) heritée correspond a la methode get ici appelée getStudy
	//	private AbstractStudy getStudy(String code) {
	//		AbstractStudy study = MongoDBDAO.findByCode(InstanceConstants.SRA_STUDY_COLL_NAME, AbstractStudy.class, code);
	//		return study;
	//	}

	public Result update(String code) {
		AbstractStudy study = getObject(code);
		Form<AbstractStudy> filledForm = getFilledForm(abstractStudyForm, AbstractStudy.class);
		ContextValidation ctxVal = ContextValidation.createUpdateContext(getCurrentUser(), filledForm);
		if (study == null) {
			ctxVal.addError("study", code + " n'existe pas dans la base");
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}
		AbstractStudy studyInput = filledForm.get();

		if (code.equals(studyInput.code)) {	
			studyInput.traceInformation.setTraceInformation(getCurrentUser());
			studyInput.validate(ctxVal);
			if (!ctxVal.hasErrors()) {
				MongoDBDAO.update(InstanceConstants.SRA_STUDY_COLL_NAME, studyInput);
				return ok(Json.toJson(studyInput));
			} else {
				//				logger.debug("\ndisplayErrors dans studies.api.update ::");
				//				ctxVal.displayErrors(logger, "debug");
				//				logger.debug("\n end displayErrors dans studies.api.update ::");
				return badRequest(errorsAsJson(ctxVal.getErrors()));
			}
		} else {
			ctxVal.addError("study " + code, "study code  " + code + " and studyInput.code "+ studyInput.code + " are not the same");
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}	

	}
}
