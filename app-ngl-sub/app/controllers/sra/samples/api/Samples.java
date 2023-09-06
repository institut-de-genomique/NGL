package controllers.sra.samples.api;


import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import controllers.NGLAPIController;
import controllers.QueryFieldsForm;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.sra.AbstractSampleAPI;
import fr.cea.ig.ngl.dao.sra.AbstractSampleDAO;
import models.sra.submit.sra.instance.AbstractSample;
import models.sra.submit.sra.instance.UserSampleType;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import services.Tools;
import sra.parser.UserSampleTypeParser;
import validation.ContextValidation;
import views.components.datatable.DatatableForm;


public class Samples extends  NGLAPIController<AbstractSampleAPI, AbstractSampleDAO, AbstractSample>  {

	private static final play.Logger.ALogger  logger = play.Logger.of(Samples.class);
	private final Form<AbstractSample>        abstractSampleForm;
	private final Form<UserFileSamplesForm>   userFileSamplesForm;
	private final Form<QueryFieldsForm>       updateForm;
	private Map<String, UserSampleType>       mapUserSamples = new HashMap<>();


	@Inject
	public Samples(NGLApplication        app,
			       AbstractSampleAPI     api) {
		super(app, api, SamplesSearchForm.class);
		this.abstractSampleForm     = app.formFactory().form(AbstractSample.class);
		this.updateForm             = app.form(QueryFieldsForm.class);
		this.userFileSamplesForm    = app.formFactory().form(UserFileSamplesForm.class);
	}


	@Override
	@Authenticated
	@Authorized.Read
	public Result get(String code) {
	    return globalExceptionHandler(() -> {
			DatatableForm form = objectFromRequestQueryString(DatatableForm.class);
			AbstractSample abstractSample = api().getObject(code, generateBasicDBObjectFromKeys(form));
			if (abstractSample == null) {
				return notFound();
			} 
			return okAsJson(abstractSample);
		});	
	}

	

	// methode list appelee avec url suivante :
	//localhost:9000/api/sra/samples?datatable=true&paginationMode=local&projCode=BCZ
	// url construite dans services.js 
	//search : function(){
	//	this.datatable.search({projCode:this.form.projCode, state:'N'});
	//},
	// Renvoie le Json correspondant à la liste des samples ayant le projectCode indique dans la variable du formulaire projectCode et stockee dans
	// l'instance samplesSearchForm	

	
	// methode saveImpl appelée par le save de la classe mere, elle meme appelée par la route 
	@Override
	@Authenticated
	@Authorized.Write
	public AbstractSample saveImpl() throws APIException {
		AbstractSample userAbstractSample = getFilledForm(abstractSampleForm, AbstractSample.class).get();
		boolean copy = false;
		SamplesUrlParamForm filledAbstractSamplesUrlParamForm = filledFormQueryString(getNGLApplication().formFactory()
				.form(SamplesUrlParamForm.class),  SamplesUrlParamForm.class).get();

		if (filledAbstractSamplesUrlParamForm != null  && filledAbstractSamplesUrlParamForm.copy != null && filledAbstractSamplesUrlParamForm.copy == true ) {
			copy = true;
		} else {
			copy = false;
		}
		logger.debug("AAAAAAAAAAAAAAA                   Dans controlleur des samples, dans saveImp, appel de api.create");
		return api().create(userAbstractSample, getCurrentUser(), copy);	
	}
	
	


	// Met a jour le sample dont le code est indiqué avec les valeurs presentes dans le sample recuperé du formulaire (userSample)
	@Override
	@Authenticated
	@Authorized.Write
	public AbstractSample updateImpl(String code) throws Exception, APIException, APIValidationException {
		AbstractSample userAbstractSample =  getFilledForm(abstractSampleForm, AbstractSample.class).get();
		QueryFieldsForm queryFieldsForm   = filledFormQueryString(updateForm, QueryFieldsForm.class).get();
		if (StringUtils.isBlank(code)) {
			throw new Exception("abstractSample.code :  valeur nulle  incompatible avec appel de la methode updateImpl");
		}
		if(StringUtils.isNotBlank(userAbstractSample.code) && ! userAbstractSample.code.equals(code)) {
			throw new Exception("abstractSample.code :  valeur " + userAbstractSample.code + " != du code indiqué dans la route "  + code);
		}
		boolean copy = false;
		if (queryFieldsForm.fields == null) {	
			return api().update(userAbstractSample, getCurrentUser(), false);
		} 
		if(queryFieldsForm.fields.contains("copy")) {
			copy = true;
			queryFieldsForm.fields.remove("copy");
			// en mode copy, on copie tout l'objet et non pas uniquement les champs indiqués
			return api().update(userAbstractSample, getCurrentUser(), true);
		} 
		// en mode classique, on copie uniquement les champs indiqués dans le queryFieldsForm, et on met le traceInformation à jour
		return api().update(userAbstractSample, getCurrentUser(), queryFieldsForm.fields); 
	}
	

	
	@Authenticated
	@Authorized.Write
	public Result loadUserFileSample() {
		UserFileSamplesForm form = getFilledForm(userFileSamplesForm, UserFileSamplesForm.class).get();
		//UserFileSamplesForm form = getRequestArgs(UserFileSamplesForm.class);
		ContextValidation ctxVal = ContextValidation.createUndefinedContext(this.getCurrentUser());
		
		try {
			//logger.debug("form.base64UserFileSample", form.base64UserFileSample);
			//logger.debug("Samples::list::list.size = " + list.size());

			if (StringUtils.isBlank(form.base64UserFileSample)) {
				//logger.debug("Pas de fichier utilisateur  dans sampleSearchForm.base64 XXXXXXXXX ");
				form.base64UserFileSample = "";
			} else {
				logger.debug("ok pour fichier utilisateur dans sampleSearchForm");
			}
			//logger.debug("Read base64UserFileSample");
			InputStream inputStreamUserFileSamples = Tools.decodeBase64(form.base64UserFileSample);
			UserSampleTypeParser userSamplesParser = new UserSampleTypeParser();
			mapUserSamples = userSamplesParser.loadMap(inputStreamUserFileSamples);
			for (Iterator<Entry<String, UserSampleType>> iterator = mapUserSamples.entrySet().iterator(); iterator.hasNext();) {
				Entry<String, UserSampleType> entry = iterator.next();
				logger.debug("  cle de sample = '" + entry.getKey() + "'");
				logger.debug("       description : '" + entry.getValue().getDescription()+ "'");
				logger.debug("Appel de validate");
				entry.getValue().validate(ctxVal);
			}
			// Renvoyer message d'erreur si le fichier utilisateur n'est pas bon:
			if (ctxVal.hasErrors()) {
				//logger.debug("Erreur dans le fichier utilisateur :");
				//ctxVal.displayErrors(logger, "debug");
				return badRequest(errorsAsJson(ctxVal.getErrors()));
			} 
			return ok(Json.toJson(mapUserSamples));
		} catch(RuntimeException e) {
			ctxVal.addError("RuntimeException", e.getMessage());
			e.printStackTrace();
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		} catch(Exception e) {
			ctxVal.addError("Exception", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}
	}
		

}
