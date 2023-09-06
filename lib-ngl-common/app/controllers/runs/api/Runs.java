package controllers.runs.api;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.mongojack.DBQuery;

import controllers.NGLAPIController;
import controllers.QueryFieldsForm;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APISemanticException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.runs.RunsAPI;
import fr.cea.ig.ngl.dao.runs.RunsDAO;
import fr.cea.ig.play.IGBodyParsers;
import models.laboratory.common.instance.Valuation;
import models.laboratory.container.instance.Container;
import models.laboratory.run.instance.Run;
import models.utils.InstanceConstants;
import play.data.Form;
import play.mvc.BodyParser;
import play.mvc.Result;
import rules.services.LazyRules6Actor;
import validation.ContextValidation;

public class Runs extends NGLAPIController<RunsAPI, RunsDAO, Run> {

    private final LazyRules6Actor rulesActor;
    private final Form<Run> form;
    private final Form<Valuation> valuationForm;
    
    @Inject
    public Runs(NGLApplication app, RunsAPI api, LazyRules6Actor rules6Actor) {
        super(app, api, RunsSearchForm.class);
        this.rulesActor = rules6Actor;
        this.form = app.formFactory().form(Run.class);
        this.valuationForm = app.formFactory().form(Valuation.class);
    }

    @Override
    public Run saveImpl() throws APIValidationException, APISemanticException {
        Run input = getFilledForm(form, Run.class).get();
        RunsSaveForm runSaveForm = filledFormQueryString(getNGLApplication().formFactory()
                                                         .form(RunsSaveForm.class),
                                                         RunsSaveForm.class).get();
        return api().create(input, getCurrentUser(), runSaveForm.external);
    }

    @Override
    public Run updateImpl(String code) throws Exception, APIException, APIValidationException {
        Run runInDB = api().get(code);
        Run input = getFilledForm(form, Run.class).get();
        Form<QueryFieldsForm> filledQueryFieldsForm = filledFormQueryString(updateForm, QueryFieldsForm.class);
        QueryFieldsForm queryFieldsForm = filledQueryFieldsForm.get();
        if (queryFieldsForm.fields == null) {
            if (code.equals(input.code)) {
                if (!runInDB.state.code.equals(input.state.code)) throw new Exception("You can not change the state code. Please use the state url ! ");
                return api().update(input, getCurrentUser());
            } else {
                throw new Exception("Run codes are not the same");
            }
        } else {
            if (input.code == null) input.code = code; // in ngl-bi the code is not in input object when only specific fields are updated 
            return api().update(input, getCurrentUser(), queryFieldsForm.fields);
        }
    }


    @Authenticated
    @Authorized.Write
    @BodyParser.Of(value = IGBodyParsers.Json15MB.class)
    public Result updateExternal(String code) {
        try {
        	Run run =this.updateExternalImpl(code);
            return okAsJson(run);
        } catch (APIValidationException e) {
            return badRequestLoggingForValidationException(e);
        } catch (APIException e) {
            getLogger().error(e.getMessage(), e);
            return badRequestAsJson(e.getMessage());
        } catch (Exception e) {
            getLogger().error(e.getMessage(), e);
            return nglGlobalBadRequest(e.getMessage());
        }
    }
    
    // NGL-3444
    private Run updateExternalImpl(String code) throws Exception, APIException, APIValidationException  {
        Run runInDB = api().get(code);
        Run input = getFilledForm(form, Run.class).get();
        Form<QueryFieldsForm> filledQueryFieldsForm = filledFormQueryString(updateForm, QueryFieldsForm.class);
        QueryFieldsForm queryFieldsForm = filledQueryFieldsForm.get();
        if (queryFieldsForm.fields == null) {
            if (code.equals(input.code)) {
                if (!runInDB.state.code.equals(input.state.code)) throw new Exception("You can not change the state code. Please use the state url ! ");
                return api().updateExternal(input, getCurrentUser());
            } else {
                throw new Exception("Run codes are not the same");
            }
        } else {
//        	for (String field : queryFieldsForm.fields) {
//				logger.debug(" zzz field = " + field);
//			}
            if (input.code == null) input.code = code; // in ngl-bi the code is not in input object when only specific fields are updated 
            return api().update(input, getCurrentUser(), queryFieldsForm.fields);
        }
	}


	//    @SuppressWarnings("deprecation") // controller asynchronous drools
	@Authenticated
    @Authorized.Write
    public Result applyRules(String code, String rulesCode) {
        return globalExceptionHandler(() -> {
            Run run = api().get(code);
            if (run == null) 
                return badRequestAsJson("Run with code " + code + " not exist");
            ContextValidation ctxVal = ContextValidation.createCreationContext(getCurrentUser());
            ArrayList<Object> facts = new ArrayList<>();
			facts.add(run);
			facts.add(ctxVal);
			//Get container
			List<Container> containers = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.is("support.code", run.containerSupportCode)).toList();
			facts.addAll(containers);
            rulesActor.tellMessage(rulesCode, facts);
            return ok();
        });
    }
    
    @Authenticated
    @Authorized.Write
    public Result valuation(String code){
        return globalExceptionHandler(() -> {
            try {
                Valuation valuation = getFilledForm(valuationForm, Valuation.class).get();
                Run run = api().valuation(code, valuation, getCurrentUser());
                return okAsJson(run);
            } catch (APIValidationException e) {
                return badRequestLoggingForValidationException(e);
            } catch (APIException e) {
                getLogger().error(e.getMessage(), e);
                return badRequestAsJson(e.getMessage());
            }
        });
    }
}
