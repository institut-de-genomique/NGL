package controllers.readsets.api;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import akka.stream.javadsl.Source;
import akka.util.ByteString;
import controllers.NGLAPIController;
import controllers.QueryFieldsForm;
import controllers.StateController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.lfw.utils.Streamer;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.readsets.ReadSetsAPI;
import fr.cea.ig.ngl.dao.readsets.ReadSetsDAO;
import fr.cea.ig.ngl.support.ListFormWrapper;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.run.instance.ReadSet;
import play.data.Form;
import play.i18n.Lang;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import rules.services.IDrools6Actor;
import views.components.datatable.DatatableBatchResponseElement;

public class ReadSets extends NGLAPIController<ReadSetsAPI, ReadSetsDAO, ReadSet> implements StateController {

    private final Form<ReadSet>              form;
    private final Form<ReadSetValuation>     valuationForm;        
    private final Form<ReadSetBatchElement>  batchElementForm; 
//    private final LazyRules6Actor rulesActor;
    private final IDrools6Actor              rulesActor;

    @Inject
    public ReadSets(NGLApplication app, ReadSetsAPI api) {
        super(app, api, ReadSetsSearchForm.class);
        this.form             = app.formFactory().form(ReadSet.class);
        this.valuationForm    = app.formFactory().form(ReadSetValuation.class);         
        this.batchElementForm = app.formFactory().form(ReadSetBatchElement.class); 
        this.rulesActor       = app.rules6Actor();
    }

    
    @Authenticated
    @Authorized.Read
    @Override
    public Result list() {
        return globalExceptionHandler(() -> {
            try {
                Source<ByteString, ?> resultsAsStream = api().list(new ListFormWrapper<>(objectFromRequestQueryString(this.searchFormClass),
                                                                                         form -> generateBasicDBObjectFromKeys(form),
                                                                                         form -> generateJSONKeys(form)));
                return Streamer.okStream(resultsAsStream);
            } catch (APIException e) {
                getLogger().error(e.getMessage(), e);
                return badRequestAsJson(e.getMessage());
            }
        });
    }

    
    @Override
    public Object updateStateImpl(String code, State state, String currentUser) throws APIException {
        return this.api().updateState(code, state, currentUser);
    }

    @Override
    public ReadSet saveImpl() throws APIException {
        ReadSet input = getFilledForm(form, ReadSet.class).get();
        ReadSetsSaveForm readSetsSaveForm = filledFormQueryString(getNGLApplication().formFactory()
                                                                  .form(ReadSetsSaveForm.class), 
                                                                  ReadSetsSaveForm.class).get();
        return api().create(input, getCurrentUser(), readSetsSaveForm.external);
    }

    @Override
    public ReadSet updateImpl(String code) throws Exception, APIException, APIValidationException {
        ReadSet readsetInDB = api().get(code);
        ReadSet input = getFilledForm(form, ReadSet.class).get();
        Form<QueryFieldsForm> filledQueryFieldsForm = filledFormQueryString(updateForm, QueryFieldsForm.class);
        QueryFieldsForm queryFieldsForm = filledQueryFieldsForm.get();
        if (queryFieldsForm.fields == null) {
            if (code.equals(input.code)) {
                if (!readsetInDB.state.code.equals(input.state.code)) throw new Exception("You can not change the state code. Please use the state url ! ");
                return api().update(input, getCurrentUser());
            } else {
                throw new Exception("ReadSet codes are not the same");
            }
        } else {
            if (input.code == null) input.code = code; // in ngl-bi the code is not in input object when only specific fields are updated 
            return api().update(input, getCurrentUser(), queryFieldsForm.fields);
        }
    }

    @Authenticated
    @Authorized.Write
    public Result deleteByRunCode(String runCode) {
        return globalExceptionHandler(() -> {
            try {
                api().deleteByRunCode(runCode);
                return ok();
            } catch (APIException e) {
                return badRequestAsJson(e.getMessage());
            }  
        });
    }

    @Authenticated
    @Authorized.Write
    public Result valuation(String code){
        return globalExceptionHandler(() -> {
            try {
                ReadSetValuation valuations = getFilledForm(valuationForm, ReadSetValuation.class).get();
                ReadSet readSet = api().valuation(code, valuations, getCurrentUser());
                return okAsJson(readSet);
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
            List<Form<ReadSetBatchElement>> filledForms =  getFilledFormList(batchElementForm, ReadSetBatchElement.class);
            final String user = getCurrentUser();
            final Lang lang = Http.Context.Implicit.lang();
            List<DatatableBatchResponseElement> response = filledForms.parallelStream()
                    .map(filledForm->{
                        ReadSetBatchElement element = filledForm.get();
                        try {
                            ReadSetValuation valuations = new ReadSetValuation();
                            valuations.bioinformaticValuation = element.data.bioinformaticValuation;
                            valuations.productionValuation    = element.data.productionValuation;
                            ReadSet readSet = api().valuation(element.data.code, valuations, user);
                            return new DatatableBatchResponseElement(OK, readSet, element.index);
                        } catch (APIValidationException e) {
                            return new DatatableBatchResponseElement(BAD_REQUEST, errorsAsJson(lang, e.getErrors()), element.index);
                        } catch (APIException e) {
                            return new DatatableBatchResponseElement(BAD_REQUEST, element.index);
                        }
                    }).collect(Collectors.toList());
            return ok(Json.toJson(response));
        });
    }

//    @SuppressWarnings("deprecation") // controller asynchronous drools
	@Authenticated
    @Authorized.Write
    public Result applyRules(String code, String codeRules) {
        return globalExceptionHandler(() -> {
            ReadSet readSet = api().get(code);
            if (readSet == null)         
                return badRequestAsJson("ReadSet with code " + code + " not exist");
            rulesActor.tellMessage(codeRules, readSet);
            return ok();
        });
    }

    @Authenticated
    @Authorized.Write
    public Result properties(String code) {
        return globalExceptionHandler(() -> {
            Form<ReadSet> filledForm = getFilledForm(form, ReadSet.class);
            Map<String, PropertyValue> properties = filledForm.get().properties;
            try {
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
            List<Form<ReadSetBatchElement>> filledForms =  getFilledFormList(batchElementForm, ReadSetBatchElement.class);
            final String user = getCurrentUser();
            final Lang lang = Http.Context.Implicit.lang();

            List<DatatableBatchResponseElement> response = filledForms.parallelStream()
                    .map(filledForm -> {
                        ReadSetBatchElement element = filledForm.get();
                        Map<String, PropertyValue> properties = element.data.properties;
                        try {
                            ReadSet readSet = api().updateProperties(element.data.code, properties, user);
                            return new DatatableBatchResponseElement(OK, readSet, element.index);
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
