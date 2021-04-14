package controllers.sra.projects.api;

import java.util.ArrayList;
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
import models.sra.submit.common.instance.Project;
import models.utils.InstanceConstants;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import validation.ContextValidation;
import views.components.datatable.DatatableResponse;

public class Projects extends DocumentController<Project> {

	//	private static final play.Logger.ALogger logger = play.Logger.of(Studies.class);

	private final Form<Project>     projectForm;
	private final Form<ProjectsSearchForm> projectsSearchForm;
	private static final play.Logger.ALogger logger = play.Logger.of(Projects.class);

	@Inject
	public Projects(NGLApplication app) {
		super(app,InstanceConstants.SRA_PROJECT_COLL_NAME, Project.class);
		projectForm               = app.form(Project.class);
		projectsSearchForm        = app.form(ProjectsSearchForm.class);
	}

//
//	//	 la methode getObject(code) heritée correspond a la methode get ici appelée getStudy
//		Project getProject(String code) {
//			Project project = MongoDBDAO.findByCode(InstanceConstants.SRA_PROJECT_COLL_NAME, Project.class, code);
//			return project;
//		}


	// methode list appelee avec url suivante :
	//localhost:9000/api/sra/projects?datatable=true&paginationMode=local&projCode=BCZ
	// url construite dans services.js 
	//search : function(){
	//	this.datatable.search({projCode:this.form.projCode, state:'N'});
	//},
	// Renvoie le Json correspondant à la liste des study ayant le projectCode indique dans la variable du formulaire projectCode et stockee dans
	// l'instance studiesSearchForm	
	public Result list(){	
		Form<ProjectsSearchForm> projectsSearchFilledForm = filledFormQueryString(projectsSearchForm, ProjectsSearchForm.class);
		ProjectsSearchForm projectsSearchForm = projectsSearchFilledForm.get();
		//Logger.debug(studiesSearchForm.state);
		Query query = getQuery(projectsSearchForm);
		MongoDBResult<Project> results = mongoDBFinder(projectsSearchForm, query);				
		List<Project> projectsList = results.toList();
		if (projectsSearchForm.datatable)
			return ok(Json.toJson(new DatatableResponse<>(projectsList, projectsList.size())));
		return ok(Json.toJson(projectsList));
	}	

	private Query getQuery(ProjectsSearchForm form) {
		List<Query> queries = new ArrayList<>();
		Query query = null;
		ProjectsSearchForm.copyPseudoStateCodesToStateCodesInFormulaire(form);

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
		Project project = getObject(code);
		Form<Project> filledForm = getFilledForm(projectForm, Project.class);
		ContextValidation ctxVal = ContextValidation.createUpdateContext(getCurrentUser(), filledForm);
		if (project == null) {
			ctxVal.addError("project", code + " n'existe pas dans la base");
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}
		Project projectInput = filledForm.get();

		if (code.equals(projectInput.code)) {	
			projectInput.traceInformation.setTraceInformation(getCurrentUser());
			projectInput.validate(ctxVal);
			if (!ctxVal.hasErrors()) {
				MongoDBDAO.update(InstanceConstants.SRA_PROJECT_COLL_NAME, projectInput);
				return ok(Json.toJson(projectInput));
			} else {
				//				logger.debug("\ndisplayErrors dans projects.api.update ::");
				//				ctxVal.displayErrors(logger, "debug");
				//				logger.debug("\n end displayErrors dans projects.api.update ::");
				return badRequest(errorsAsJson(ctxVal.getErrors()));
			}
		} else {
			ctxVal.addError("project " + code, "project code  " + code + " and projectInput.code "+ projectInput.code + " are not the same");
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}	

	}
}
