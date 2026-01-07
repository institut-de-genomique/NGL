package controllers.sra.studies.api;


import java.util.List;

import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import controllers.NGLAPIController;
import controllers.QueryFieldsForm;
import controllers.sra.api.AbstractStudyAPI;
import controllers.sra.submissions.api.SubmissionsCreationFormForUmbrella;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.sra.AbstractStudyDAO;
import models.sra.submit.sra.instance.AbstractStudy;
import models.sra.submit.sra.instance.ExternalStudy;
import models.sra.submit.sra.instance.Study;
import models.sra.submit.sra.instance.Submission;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import services.sra.SraException;
import services.sra.SraValidationException;
import sra.api.SubmissionNewAPI;
import validation.ContextValidation;
import views.components.datatable.DatatableForm;


public class Studies extends  NGLAPIController<AbstractStudyAPI, AbstractStudyDAO, AbstractStudy>  {

	private static final play.Logger.ALogger  logger = play.Logger.of(Studies.class);
	private final Form<AbstractStudy>         abstractStudyForm;
	private final Form<QueryFieldsForm>       updateForm;
	private final NGLApplication              app;
	private final SubmissionNewAPI            submissionNewAPI;

	@Inject
	public Studies(NGLApplication       app,
			       AbstractStudyAPI     api,
			       SubmissionNewAPI     submissionNewApi) {
		super(app, api, StudiesSearchForm.class);
		this.abstractStudyForm      = app.formFactory().form(AbstractStudy.class);
		this.updateForm             = app.form(QueryFieldsForm.class);
		this.app                    = app;
		this.submissionNewAPI       = submissionNewApi;
		
	}

	@Override
	@Authenticated
	@Authorized.Read
	public Result get(String code) {
	    return globalExceptionHandler(() -> {
			DatatableForm form = objectFromRequestQueryString(DatatableForm.class);
			AbstractStudy abstractStudy = api().getObject(code, generateBasicDBObjectFromKeys(form));
			if (abstractStudy == null) {
				return notFound();
			} 
			return okAsJson(abstractStudy);
		});	
	}

	
	// methode list appelee avec url suivante :
	//localhost:9000/api/sra/studies?datatable=true&paginationMode=local&projCode=BCZ
	// url construite dans services.js 
	//search : function(){
	//	this.datatable.search({projCode:this.form.projCode, state:'N'});
	//},
	// Renvoie le Json correspondant à la liste des study ayant le projectCode indique dans la variable du formulaire projectCode et stockee dans
	// l'instance studiesSearchForm	


	
	public<T> T getRequestArgs(Class <T> clazz) {
		Form<T> formTemplate = app.form(clazz);                   // template du formulaire
		Form<T> filledForm = getFilledForm(formTemplate, clazz);  //formulaire rempli grace au template et a la classe
		if (filledForm.hasErrors())
			throw new SraValidationException(ContextValidation.createUndefinedContext(getCurrentUser(), filledForm));
		T args = filledForm.get(); // retourne l'objet correspondant à la classe
		return args;
	}
	
	
	
	
    @Authenticated
    @Authorized.Write
	public Result createFromStudyCreationForm() throws APIException {
		logger.debug("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA    Dans createFromStudyCreationForm du controller des study");
		StudyCreationForm studyCreationForm  = getRequestArgs(StudyCreationForm.class);
		String user                          = getCurrentUser();
		ContextValidation contextValidation  = ContextValidation.createCreationContext(user);
		// liste des champs obligatoires dans studyCreationForm
		List<String>     projectCodes        = studyCreationForm.projectCodes;
		String           existingStudyType   = studyCreationForm.existingStudyType;
		String           centerProjectName   = studyCreationForm.centerProjectName;
		String           title               = studyCreationForm.title;
		String 		     description         = studyCreationForm.description;  
	    String           dropBox             = studyCreationForm.dropBox;

		// liste des champs facultatifs dans studyCreationForm		
		String           strTaxonId          = null; 
	    String           strIdsPubmeds       = null;  
	    String           strLocusTagPrefixs  = null; 
	    String           strParentProjectAccessions  = null; 
	    String           strAttributes       = null; 

		if (StringUtils.isNotBlank(studyCreationForm.strTaxonId)) {
			strTaxonId = studyCreationForm.strTaxonId;
		}
		if (StringUtils.isNotBlank(studyCreationForm.strIdsPubmeds)) {
			strIdsPubmeds = studyCreationForm.strIdsPubmeds;
		}
		if (StringUtils.isNotBlank(studyCreationForm.strLocusTagPrefixs)) {
			strLocusTagPrefixs = studyCreationForm.strLocusTagPrefixs;
		}
		if (StringUtils.isNotBlank(studyCreationForm.strParentProjectAccessions)) {
			strParentProjectAccessions = studyCreationForm.strParentProjectAccessions;
		}
		if (StringUtils.isNotBlank(studyCreationForm.strAttributes)) {
			logger.debug("studyCreationForm.strAttributes not blank='" + studyCreationForm.strAttributes + "'");
			strAttributes = studyCreationForm.strAttributes;
		}
		Study study;
		try {
			//logger.debug("submissionCreationForm.description= " + submissionsCreationFormForUmbrella.description);
			// NGL-4013
			study = submissionNewAPI.createAndSaveStudyEntity(projectCodes, existingStudyType, centerProjectName, title, description, dropBox, strTaxonId, strIdsPubmeds, strLocusTagPrefixs, strParentProjectAccessions, strAttributes, contextValidation);
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

		return ok(Json.toJson(study));
	}

	
	// methode saveImpl appelée par le save de la classe mere, elle meme appelée par la route 
	@Override
	@Authenticated
	@Authorized.Write
	public AbstractStudy saveImpl() throws APIException {
		logger.debug("AAAAAAAAAAAAAAAAAAAAABBBBBBBBBBBBBBBBBBB    Dans saveImpl du controller des study");
		AbstractStudy userAbstractStudy = getFilledForm(abstractStudyForm, AbstractStudy.class).get();
		boolean copy = false;
		StudiesUrlParamForm filledAbstractStudiesUrlParamForm = filledFormQueryString(getNGLApplication().formFactory()
				.form(StudiesUrlParamForm.class),  StudiesUrlParamForm.class).get();

		if (filledAbstractStudiesUrlParamForm != null  && filledAbstractStudiesUrlParamForm.copy != null && filledAbstractStudiesUrlParamForm.copy == true ) {
			copy = true;
		} else {
			copy = false;
		}
		return api().create(userAbstractStudy, getCurrentUser(), copy);	
	}
	
	
	// Met a jour le study  dont le code est indiqué avec les valeurs presentes dans le study recuperé du formulaire (userStudy)
	@Override
	@Authenticated
	@Authorized.Write
	public AbstractStudy updateImpl(String code) throws Exception, APIException, APIValidationException {
		//logger.debug("ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ    Dans updateImpl du controller des study");

		AbstractStudy userAbstractStudy =  getFilledForm(abstractStudyForm, AbstractStudy.class).get();
		QueryFieldsForm queryFieldsForm = filledFormQueryString(updateForm, QueryFieldsForm.class).get();
		if (StringUtils.isBlank(code)) {
			throw new Exception("abstractStudy.code :  valeur nulle  incompatible avec appel de la methode updateImpl");
		}
		if(StringUtils.isNotBlank(userAbstractStudy.code) && ! userAbstractStudy.code.equals(code)) {
			throw new Exception("abstractStudy.code :  valeur " + userAbstractStudy.code + " != du code indiqué dans la route "  + code);
		}
		if (userAbstractStudy instanceof Study) {
			//logger.debug("ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ    Dans updateImpl du controller j'appelle synchronyseValuesInStudy");
			ContextValidation ctxVal = ContextValidation.createUndefinedContext(getCurrentUser());
			submissionNewAPI.synchronyseValuesInStudy((Study)userAbstractStudy, ctxVal);
			if(ctxVal.hasErrors()) {
				throw new APIValidationException("INVALID_INPUT_ERROR_MSG", ctxVal.getErrors());
			}
		}
		// boolean copy = false;
		if (queryFieldsForm.fields == null) {	
			return api().update(userAbstractStudy, getCurrentUser(), false);
		} 
		if(queryFieldsForm.fields.contains("copy")) {
			// copy = true;
			queryFieldsForm.fields.remove("copy");
			// en mode copy, on copie tout l'objet et non pas uniquement les champs indiqués
			return api().update(userAbstractStudy, getCurrentUser(), true);
		} 
		// en mode classique, on copie uniquement les champs indiqués dans le queryFieldsForm, et on met le traceInformation à jour
		return api().update(userAbstractStudy, getCurrentUser(), queryFieldsForm.fields); 
	}


}
