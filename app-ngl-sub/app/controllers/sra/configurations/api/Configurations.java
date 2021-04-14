package controllers.sra.configurations.api;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import controllers.DocumentController;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.mongo.DBQueryBuilder;
import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.sra.submit.sra.instance.Configuration;
import models.sra.submit.util.SraCodeHelper;
import models.sra.submit.util.SraException;
import models.utils.InstanceConstants;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import validation.ContextValidation;
import views.components.datatable.DatatableResponse;

public class Configurations extends DocumentController<Configuration> {

	private static final play.Logger.ALogger logger = play.Logger.of(Configurations.class);

	private final Form<Configuration>            configurationForm;
	private final Form<ConfigurationsSearchForm> configurationsSearchForm;
	private final SraCodeHelper     sraCodeHelper;

	//	@Inject
	//	public Configurations(NGLContext ctx,
	//            SraCodeHelper     sraCodeHelper) {
	//		super(ctx,InstanceConstants.SRA_CONFIGURATION_COLL_NAME, Configuration.class);
	//		configurationForm        = ctx.form(Configuration.class);
	//		configurationsSearchForm = ctx.form(ConfigurationsSearchForm.class);
	//		this.sraCodeHelper       = sraCodeHelper;
	//		// submissionsSearchForm    = ctx.form(SubmissionsSearchForm.class);
	//		// this.configWorkflows     = configWorkflows;
	//	}

	@Inject
	public Configurations(NGLApplication app, SraCodeHelper     sraCodeHelper) {
		super(app,InstanceConstants.SRA_CONFIGURATION_COLL_NAME, Configuration.class);
		configurationForm        = app.form(Configuration.class);
		configurationsSearchForm = app.form(ConfigurationsSearchForm.class);
		this.sraCodeHelper       = sraCodeHelper;
	}

	public Result save() {

		Form<Configuration> filledForm = getFilledForm(configurationForm, Configuration.class);
		Configuration userConfiguration = filledForm.get();

		//		ContextValidation contextValidation = new ContextValidation(getCurrentUser(), filledForm.errors());
		//		ContextValidation contextValidation = new ContextValidation(getCurrentUser(), filledForm);
		//		contextValidation.setCreationMode();
		ContextValidation ctxVal = ContextValidation.createCreationContext(getCurrentUser(), filledForm);
		try {
			if (userConfiguration._id == null) {
				userConfiguration.traceInformation = new TraceInformation(); 
				userConfiguration.traceInformation.setTraceInformation(getCurrentUser());
				State state = new State("NONE", getCurrentUser());
				// Ne pas passer par configWorkflows ici car setState possible si mode update si object existe deja
				//configWorkflows.setState(contextValidation, userConfiguration, state);
				userConfiguration.state = state;
				try {
					userConfiguration.code = sraCodeHelper.generateConfigurationCode(userConfiguration.projectCodes);
				} catch (SraException e) {
					// return badRequest(filledForm.errors-AsJson());
					return badRequest(errorsAsJson(ctxVal.getErrors()));
				}
				//			System.out.println (" !!!!!!!!!!! userConf.code = " + userConfiguration.code);
				logger.debug(" !!!!!!!!!!! userConf.code = " + userConfiguration.code);
				userConfiguration.validate(ctxVal);
				// if(contextValidation.errors.size()==0) {
				if (!ctxVal.hasErrors()) {
					MongoDBDAO.save(InstanceConstants.SRA_CONFIGURATION_COLL_NAME, userConfiguration);
				} else {
					// return badRequest(filledForm.errors-AsJson());
					return badRequest(errorsAsJson(ctxVal.getErrors()));
				}
			} else {
				//			filledForm.reject("configuration with id "+userConfiguration._id ," already exist");
				//			return badRequest(filledForm.errorsAsJson( )); // legit, at least does seem
				ctxVal.addError("configuration with id "+userConfiguration._id ," already exist");
				return badRequest(errorsAsJson(ctxVal.getErrors()));
			}
			return ok(Json.toJson(userConfiguration.code));
		} catch(RuntimeException e) {
			ctxVal.addError("RuntimeException", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		} catch(Exception e) {
			ctxVal.addError("Exception", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}
	}

	// methode list appelee avec url suivante :
	//localhost:9000/api/sra/configurations?datatable=true&paginationMode=local&projCode=BCZ
	// url construite dans services.js 
	//search : function(){
	//	this.datatable.search({projCode:this.form.projCode, state:'N'});
	//},
	public Result list(){	
		ContextValidation ctxVal = ContextValidation.createUndefinedContext(getCurrentUser());
		try {
		Form<ConfigurationsSearchForm> configurationsSearchFilledForm = filledFormQueryString(configurationsSearchForm, ConfigurationsSearchForm.class);
		ConfigurationsSearchForm configurationsSearchForm = configurationsSearchFilledForm.get();
		//Logger.debug(submissionsSearchForm.state);
		Query query = getQuery(configurationsSearchForm);
		MongoDBResult<Configuration> results = mongoDBFinder(configurationsSearchForm, query);				
		List<Configuration> configurationsList = results.toList();
		if (configurationsSearchForm.datatable)
			return ok(Json.toJson(new DatatableResponse<>(configurationsList, configurationsList.size())));
		return ok(Json.toJson(configurationsList));
		} catch(RuntimeException e) {
			ctxVal.addError("RuntimeException", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		} catch(Exception e) {
			ctxVal.addError("Exception", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}
	}

	
	private Query getQuery(ConfigurationsSearchForm form) {
		List<Query> queries = new ArrayList<>();

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

		if (CollectionUtils.isNotEmpty(form.codes)) { //all
			queries.add(DBQuery.in("code", form.codes));
		}

		if (CollectionUtils.isNotEmpty(form.codes)) {
			queries.add(DBQuery.in("code", form.codes));
		} else if(StringUtils.isNotBlank(form.codeRegex)) {
			queries.add(DBQuery.regex("code", Pattern.compile(form.codeRegex)));
		}

		//		Query query = null;
		//		if (queries.size() > 0)
		//			query = DBQuery.and(queries.toArray(new Query[queries.size()]));
		//		return query;
		return DBQueryBuilder.query(DBQueryBuilder.and(queries));
	}

	private Configuration getConfiguration(String code) {
		Configuration configuration = MongoDBDAO.findByCode(InstanceConstants.SRA_CONFIGURATION_COLL_NAME, Configuration.class, code);
		return configuration;
	}

	public Result update(String code) {
		//Get Submission from DB 
		Configuration configuration = getConfiguration(code);
		Form<Configuration> filledForm = getFilledForm(configurationForm, Configuration.class);
		//		ContextValidation ctxVal = new ContextValidation(getCurrentUser(), filledForm.errors()); 	
		//		ContextValidation ctxVal = new ContextValidation(getCurrentUser(), filledForm); 	
		//			ctxVal.setUpdateMode();
		ContextValidation ctxVal = ContextValidation.createUpdateContext(getCurrentUser(), filledForm); 
		try {
			if (configuration == null) {
				//return badRequest("Configuration with code "+code+" not exist");
				ctxVal.addError("configuration ", " not exist");
				// return badRequest(filledForm.errors-AsJson( ));
				return badRequest(errorsAsJson(ctxVal.getErrors()));
			}
			Configuration configurationInput = filledForm.get();
			if (code.equals(configurationInput.code)) {	
				//			ctxVal.getContextObjects().put("type","sra");
				ctxVal.putObject("type","sra");
				configurationInput.traceInformation.setTraceInformation(getCurrentUser());
				configurationInput.validate(ctxVal);
				if (!ctxVal.hasErrors()) {
					logger.info("Update configuration state " + configurationInput.state.code);
					MongoDBDAO.update(InstanceConstants.SRA_CONFIGURATION_COLL_NAME, configurationInput);
					return ok(Json.toJson(configurationInput));
				} else {
					//return badRequest(filledForm.errors-AsJson());
					return badRequest(errorsAsJson(ctxVal.getErrors()));
				}
			} else {
				//return badRequest("configuration code are not the same");
				ctxVal.addError("configuration " + code, "configuration code  " + code + " and configurationInput.code "+ configurationInput.code + "are not the same");
				// return badRequest(filledForm.errors-AsJson());
				return badRequest(errorsAsJson(ctxVal.getErrors()));
			}	
		} catch(RuntimeException e) {
			ctxVal.addError("RuntimeException", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		} catch(Exception e) {
			ctxVal.addError("Exception", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}

	}

}
