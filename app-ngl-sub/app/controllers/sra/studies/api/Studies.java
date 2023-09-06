package controllers.sra.studies.api;


import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import controllers.NGLAPIController;
import controllers.QueryFieldsForm;
import controllers.sra.samples.api.Samples;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.sra.AbstractStudyAPI;
import fr.cea.ig.ngl.dao.sra.AbstractStudyDAO;
import models.sra.submit.sra.instance.AbstractStudy;
import play.data.Form;
import play.mvc.Result;

import views.components.datatable.DatatableForm;


public class Studies extends  NGLAPIController<AbstractStudyAPI, AbstractStudyDAO, AbstractStudy>  {

	private static final play.Logger.ALogger  logger = play.Logger.of(Samples.class);
	private final Form<AbstractStudy>         abstractStudyForm;
	private final Form<QueryFieldsForm>       updateForm;

	@Inject
	public Studies(NGLApplication        app,
			       AbstractStudyAPI     api) {
		super(app, api, StudiesSearchForm.class);
		this.abstractStudyForm      = app.formFactory().form(AbstractStudy.class);
		this.updateForm             = app.form(QueryFieldsForm.class);
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

	

	
	// methode saveImpl appelée par le save de la classe mere, elle meme appelée par la route 
	@Override
	@Authenticated
	@Authorized.Write
	public AbstractStudy saveImpl() throws APIException {
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


	// Met a jour le sample dont le code est indiqué avec les valeurs presentes dans le sample recuperé du formulaire (userSample)
	@Override
	@Authenticated
	@Authorized.Write
	public AbstractStudy updateImpl(String code) throws Exception, APIException, APIValidationException {
		AbstractStudy userAbstractStudy =  getFilledForm(abstractStudyForm, AbstractStudy.class).get();
		QueryFieldsForm queryFieldsForm = filledFormQueryString(updateForm, QueryFieldsForm.class).get();
		if (StringUtils.isBlank(code)) {
			throw new Exception("abstractStudy.code :  valeur nulle  incompatible avec appel de la methode updateImpl");
		}
		if(StringUtils.isNotBlank(userAbstractStudy.code) && ! userAbstractStudy.code.equals(code)) {
			throw new Exception("abstractStudy.code :  valeur " + userAbstractStudy.code + " != du code indiqué dans la route "  + code);
		}
		boolean copy = false;
		if (queryFieldsForm.fields == null) {	
			return api().update(userAbstractStudy, getCurrentUser(), false);
		} 
		if(queryFieldsForm.fields.contains("copy")) {
			copy = true;
			queryFieldsForm.fields.remove("copy");
			// en mode copy, on copie tout l'objet et non pas uniquement les champs indiqués
			return api().update(userAbstractStudy, getCurrentUser(), true);
		} 
		// en mode classique, on copie uniquement les champs indiqués dans le queryFieldsForm, et on met le traceInformation à jour
		return api().update(userAbstractStudy, getCurrentUser(), queryFieldsForm.fields); 
	}
	

	


}
