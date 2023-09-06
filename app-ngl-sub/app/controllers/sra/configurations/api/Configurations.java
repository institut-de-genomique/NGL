package controllers.sra.configurations.api;



import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import controllers.NGLAPIController;
import controllers.QueryFieldsForm;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.sra.ConfigurationAPI;
import models.sra.submit.util.SraCodeHelper;
import models.sra.submit.sra.instance.Configuration;
import play.data.Form;
import play.mvc.Result;
import views.components.datatable.DatatableForm;
import fr.cea.ig.ngl.dao.sra.ConfigurationDAO;


public class Configurations extends NGLAPIController<ConfigurationAPI, ConfigurationDAO, Configuration> {


	private static final play.Logger.ALogger logger = play.Logger.of(Configurations.class);

	private final Form<Configuration>                configurationForm;
	private final Form<ConfigurationsUrlParamForm>   configurationsUrlParamForm;


	@Inject
	public Configurations(NGLApplication app, ConfigurationAPI api, SraCodeHelper sraCodeHelper) {
		super(app, api, ConfigurationsSearchForm.class);
		this.configurationForm          = app.formFactory().form(Configuration.class);
		this.configurationsUrlParamForm = app.formFactory().form(ConfigurationsUrlParamForm.class);
	}	



	@Override
	@Authenticated
	@Authorized.Read
	public Result get(String code) {
		return globalExceptionHandler(() -> {
			DatatableForm form = objectFromRequestQueryString(DatatableForm.class);
			Configuration configuration = api().getObject(code, generateBasicDBObjectFromKeys(form));
			if (configuration == null) {
				return notFound();
			} 
			return okAsJson(configuration);
		});	
	}	

	// methode list de NGLAPIController appelee avec url suivante :
	//localhost:9000/api/sra/configurations?datatable=true&paginationMode=local&projCode=BCZ
	// url construite dans services.js 
	//search : function(){
	//	this.datatable.search({projCode:this.form.projCode, state:'N'});
	//},
	// methode list qui fonctionne avec le getQuery() definie dans ConfigurationSearchForm.



	// methode saveImpl appelée par le save de la classe mere, elle meme appelée par la route 
	@Override
	@Authenticated
	@Authorized.Write
	public Configuration saveImpl() throws APIException {
		Configuration userConfiguration = getFilledForm(configurationForm, Configuration.class).get();

		boolean copy = false;
		//      ancienne ecriture si on herite de DocumentController	
		//		ConfigurationsUrlParamForm configurationsUrlParamForm = filledFormQueryString(this.app.formFactory().form(ConfigurationsUrlParamForm.class), 
		//		ConfigurationsUrlParamForm.class).get();
		ConfigurationsUrlParamForm filledConfigurationsUrlParamForm = filledFormQueryString(getNGLApplication().formFactory()
				.form(ConfigurationsUrlParamForm.class),  ConfigurationsUrlParamForm.class).get();

		if (filledConfigurationsUrlParamForm != null  && filledConfigurationsUrlParamForm.copy != null && filledConfigurationsUrlParamForm.copy == true ) {
			copy = true;
		} else {
			copy = false;
		}
		return api().create(userConfiguration, getCurrentUser(), copy);	
	}


	// methode updateImpl appelée par le update de la classe mere, elle meme appelée par la route
	@Override
	@Authenticated
	@Authorized.Write
	public Configuration updateImpl(String code) throws Exception, APIException, APIValidationException {
		Configuration userConfiguration =  getFilledForm(configurationForm, Configuration.class).get();
//		Form<QueryFieldsForm> filledQueryFieldsForm = filledFormQueryString(updateForm, QueryFieldsForm.class);
//		SraQueryFieldsForm queryFieldsForm = (SraQueryFieldsForm) filledQueryFieldsForm.get();

		QueryFieldsForm queryFieldsForm = filledFormQueryString(updateForm, QueryFieldsForm.class).get();
		if (StringUtils.isBlank(code)) {
			throw new Exception("configuration.code :  valeur nulle  incompatible avec appel de la methode updateImpl");
		}
		if(StringUtils.isNotBlank(userConfiguration.code) && ! userConfiguration.code.equals(code)) {
			throw new Exception("configuration.code :  valeur " + userConfiguration.code + " != du code indiqué dans la route "  + code);
		}

//		ConfigurationsUrlParamForm filledConfigurationsUrlParamForm = filledFormQueryString(getNGLApplication().formFactory()
//				.form(ConfigurationsUrlParamForm.class),  ConfigurationsUrlParamForm.class).get();
//		if (filledConfigurationsUrlParamForm != null  && filledConfigurationsUrlParamForm.copy != null && filledConfigurationsUrlParamForm.copy == true ) {
//			copy = true;
//		} else {
//			copy = false;
//		}
		
		// l'utilisation de queryFieldsForm ne permet pas de recuperer un parametre sur l'url meme avec ConfigurationsUrlParamForm
		// On est alors obligé de passer le parametre avec fields=copy
		//On aura url de la forme :
		// http://appuat.genoscope.cns.fr:9005/api/sra/configurations/CONF_BIL_77SE36N3M?fields=copy
		// ou bien 
		//http://appuat.genoscope.cns.fr:9005/api/sra/configurations/CONF_BIL_77SE36N3M?fields=strategyStudy
		// et si on a http://appuat.genoscope.cns.fr:9005/api/sra/configurations/CONF_BIL_77SE36N3M?fields=strategyStudy&fields=copy alors
		//fields=strategyStudy sera ignoré car on est en mode copy et tous les champs de l'input seront recopiés.
		//En mode copy, on veut que tous les champs de l'object input soient recopiés, on passe donc par un update sans
		// queryFieldsForm

		boolean copy = false;
		if (queryFieldsForm.fields == null) {	
			return api().update(userConfiguration, getCurrentUser(), false);
		} 
		if(queryFieldsForm.fields.contains("copy")) {
			copy = true;
			queryFieldsForm.fields.remove("copy");
			return api().update(userConfiguration, getCurrentUser(), true);
		} 
		return api().update(userConfiguration, getCurrentUser(), queryFieldsForm.fields); 
	}
	
}
