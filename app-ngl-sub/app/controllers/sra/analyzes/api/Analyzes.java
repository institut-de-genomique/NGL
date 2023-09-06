package controllers.sra.analyzes.api;




import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import controllers.NGLAPIController;
import controllers.QueryFieldsForm;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.sra.AnalysisAPI;
import fr.cea.ig.ngl.dao.sra.AnalysisDAO;
import models.sra.submit.sra.instance.Analysis;
import play.data.Form;
import play.mvc.Result;
import views.components.datatable.DatatableForm;

public class Analyzes extends NGLAPIController<AnalysisAPI, AnalysisDAO, Analysis>  { 

	//	private static final play.Logger.ALogger logger = play.Logger.of(Analyzes.class);

	private final Form<Analysis>           analysisForm;
	private final Form<QueryFieldsForm>    updateForm;


	@Inject
	public Analyzes(NGLApplication     app,
			AnalysisAPI        api
			) {
		super(app, api, AnalyzesSearchForm.class);
		this.analysisForm   = app.formFactory().form(Analysis.class);
		this.updateForm     = app.form(QueryFieldsForm.class);
	}


	@Override
	@Authenticated
	@Authorized.Read
	public Result get(String code) {
		return globalExceptionHandler(() -> {
			DatatableForm form = objectFromRequestQueryString(DatatableForm.class);
			Analysis analysis = api().getObject(code, generateBasicDBObjectFromKeys(form));
			if (analysis == null) {
				return notFound();
			} 
			return okAsJson(analysis);
		});	
	}	

	// methode list appelee avec url suivante :
	//localhost:9000/api/sra/analyzes?projCodes=BIL
	// url construite dans services.js 
	//search : function(){
	//	this.datatable.search({projCode:this.form.projCode, state:'N'});
	//},
	// Renvoie le Json correspondant à la liste des analyses ayant le projectCode indique dans la variable du formulaire projectCode et stockee dans
	// l'instance analyzesSearchForm	




	// methode saveImpl appelée par le save de la classe mere, elle meme appelée par la route 
	// methode saveImpl appelée par le save de la classe mere, elle meme appelée par la route 
	@Override
	@Authenticated
	@Authorized.Write
	public Analysis saveImpl() throws APIException {
		Analysis userAnalysis = getFilledForm(analysisForm, Analysis.class).get();
		boolean copy = false;
		AnalyzesUrlParamForm filledAnalysissUrlParamForm = filledFormQueryString(getNGLApplication().formFactory()
				.form(AnalyzesUrlParamForm.class),  AnalyzesUrlParamForm.class).get();

		if (filledAnalysissUrlParamForm != null  && filledAnalysissUrlParamForm.copy != null && filledAnalysissUrlParamForm.copy == true ) {
			copy = true;
		} else {
			copy = false;
		}
		return api().create(userAnalysis, getCurrentUser(), copy);	
	}


	// methode saveImpl appelée par le save de la classe mere, elle meme appelée par la route 
	@Override
	@Authenticated
	@Authorized.Write
	public Analysis updateImpl(String code) throws Exception, APIException, APIValidationException {
		Analysis userAnalysis =  getFilledForm(analysisForm, Analysis.class).get();
		//Form<QueryFieldsForm> filledQueryFieldsForm = filledFormQueryString(updateForm, QueryFieldsForm.class);
		//QueryFieldsForm queryFieldsForm = filledQueryFieldsForm.get();
		QueryFieldsForm queryFieldsForm = filledFormQueryString(updateForm, QueryFieldsForm.class).get();
		if (StringUtils.isBlank(code)) {
			throw new Exception("analysis.code :  valeur nulle  incompatible avec appel de la methode updateImpl");
		}
		if(StringUtils.isNotBlank(userAnalysis.code) && ! userAnalysis.code.equals(code)) {
			throw new Exception("analysis.code :  valeur " + userAnalysis.code + " != du code indiqué dans la route "  + code);
		}
		boolean copy = false;
		if (queryFieldsForm.fields == null) {	
			return api().update(userAnalysis, getCurrentUser(), false);
		} 
		if(queryFieldsForm.fields.contains("copy")) {
			copy = true;
			queryFieldsForm.fields.remove("copy");
			return api().update(userAnalysis, getCurrentUser(), true);
		} 
		return api().update(userAnalysis, getCurrentUser(), queryFieldsForm.fields); 
	}


}
