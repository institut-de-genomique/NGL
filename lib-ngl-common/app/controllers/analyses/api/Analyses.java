package controllers.analyses.api;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import controllers.NGLAPIController;
import controllers.QueryFieldsForm;
import controllers.StateController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.analyses.AnalysesAPI;
import fr.cea.ig.ngl.dao.analyses.AnalysesDAO;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.Valuation;
import models.laboratory.run.instance.Analysis;
import play.data.Form;
import play.i18n.Lang;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import rules.services.IDrools6Actor;
import views.components.datatable.DatatableBatchResponseElement;

public class Analyses extends NGLAPIController<AnalysesAPI, AnalysesDAO, Analysis> implements StateController {

    private final Form<Analysis>             form;
    private final Form<Valuation>            valuationForm;
    private final Form<AnalysesBatchElement> batchElementForm;
    private final IDrools6Actor              rulesActor;

    @Inject
    public Analyses(NGLApplication app, AnalysesAPI api) {
        super(app, api, AnalysesSearchForm.class);
        this.rulesActor       = app.rules6Actor();
        this.form             = app.formFactory().form(Analysis.class);
        this.valuationForm    = app.formFactory().form(Valuation.class);
        this.batchElementForm = app.formFactory().form(AnalysesBatchElement.class);
    }

//    @SuppressWarnings("deprecation") // controller asynchronous drools
	@Authenticated
    @Authorized.Write
    public Result applyRules(String code, String rulesCode) {
        return globalExceptionHandler(() -> {
            Analysis objectInDB = api().get(code);
            if (objectInDB == null) 
                return notFoundAsJson("Analysis " + code + " not found");
            rulesActor.tellMessage(rulesCode, objectInDB);
            return ok();
        });
    }

    @Authenticated
    @Authorized.Write
    public Result properties(String code) {
        return globalExceptionHandler(() -> {
            Analysis objectInDB = api().get(code);
            if (objectInDB == null) 
                return notFoundAsJson("Analysis " + code + " not found");
            try {
            	Form<Analysis> filledForm = getFilledForm(form, Analysis.class);
            	Map<String, PropertyValue> properties = filledForm.get().properties;
            	return okAsJson(api().updateProperties(code, properties, getCurrentUser()));
            } catch (APIValidationException e) {
            	return badRequestLoggingForValidationException(e);
            } catch (APIException e) {
            	return badRequestAsJson(e.getMessage());
            }   
        });
    }
    
    @Authenticated
    @Authorized.Write
    public Result propertiesBatch() {
        return globalExceptionHandler(() -> {
            List<Form<AnalysesBatchElement>> filledForms =  getFilledFormList(batchElementForm, AnalysesBatchElement.class);
            final String user = getCurrentUser();
            final Lang lang = Http.Context.Implicit.lang();

            List<DatatableBatchResponseElement> response = filledForms.stream()
                    .map(filledForm -> {
                        AnalysesBatchElement element = filledForm.get();
                        Map<String, PropertyValue> properties = element.data.properties;
                        try {
                            Analysis analysis = api().updateProperties(element.data.code, properties, user);
                            return new DatatableBatchResponseElement(OK, analysis, element.index);
                        } catch (APIValidationException e) {
                            return new DatatableBatchResponseElement(BAD_REQUEST, errorsAsJson(lang, e.getErrors()), element.index);
                        } catch (APIException e) {
                            return new DatatableBatchResponseElement(BAD_REQUEST, element.index);
                        }
                    }).collect(Collectors.toList());
            return ok(Json.toJson(response));
        });
    }
    
    @Override
    public Analysis updateStateImpl(String code, State state, String currentUser) throws APIException {
        return api().updateState(code, state, currentUser);
    }

    @Override
    public Analysis saveImpl() throws APIException {
        Analysis input = getFilledForm(form, Analysis.class).get();
        return api().create(input, getCurrentUser());
    }

    @Override
    public Analysis updateImpl(String code) throws Exception, APIException, APIValidationException {
        Analysis objectInDB =  api().get(code);
        QueryFieldsForm queryFieldsForm = filledFormQueryString(updateForm, QueryFieldsForm.class).get();
        Analysis input = getFilledForm(form, Analysis.class).get();
        if (queryFieldsForm.fields == null) {
            if (input.code.equals(code)) {
                if (!objectInDB.state.code.equals(input.state.code)) throw new Exception("You can not change the state code. Please use the state url ! ");
                return api().update(input, getCurrentUser());
            } else {
                throw new Exception("Analysis codes are not the same");
            }
        } else {
            if (input.code == null) 
            	input.code = code; // in ngl-bi the code is not in input object when only specific fields are updated 
            return api().update(input, getCurrentUser(), queryFieldsForm.fields);
        }
    }

    @Authenticated
    @Authorized.Write
    public Result valuation(String code) {
        return globalExceptionHandler(() -> {
            try {
                Valuation valuations = getFilledForm(valuationForm, Valuation.class).get();
                Analysis analysis = api().valuation(code, valuations, getCurrentUser());
                return okAsJson(analysis);
            } catch (APIValidationException e) {
                return badRequestLoggingForValidationException(e);
            } catch (APIException e) {
                return badRequestAsJson(e.getMessage());
            }
        });
    }
    
    @Authenticated
    @Authorized.Write
    public Result valuationBatch(){
        return globalExceptionHandler(() -> {
            List<Form<AnalysesBatchElement>> filledForms =  getFilledFormList(batchElementForm, AnalysesBatchElement.class);
            final String user = getCurrentUser();
            final Lang lang = Http.Context.Implicit.lang();
            List<DatatableBatchResponseElement> response = filledForms.parallelStream()
                    .map(filledForm -> {
                        AnalysesBatchElement element = filledForm.get();
                        try {
                            Analysis analysis = api().valuation(element.data.code, element.data.valuation, user);
                            return new DatatableBatchResponseElement(OK, analysis, element.index);
                        } catch (APIValidationException e) {
                            return new DatatableBatchResponseElement(BAD_REQUEST, errorsAsJson(lang, e.getErrors()), element.index);
                        } catch (APIException e) {
                            return new DatatableBatchResponseElement(BAD_REQUEST, element.index);
                        }
                    }).collect(Collectors.toList());
            return ok(Json.toJson(response));
        });
    }
}
