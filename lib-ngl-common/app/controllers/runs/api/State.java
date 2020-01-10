package controllers.runs.api;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.lfw.Historized;
import fr.cea.ig.play.migration.NGLContext;
import models.laboratory.common.instance.TransientState;
import models.laboratory.run.instance.Run;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import validation.ContextValidation;
import views.components.datatable.DatatableBatchResponseElement;
import workflows.run.RunWorkflows;

public class State extends RunsController {
	
    private final Form<models.laboratory.common.instance.State> stateForm;        
    private final Form<HistoricalStateSearchForm>               historicalForm;  
    private final Form<RunBatchElement>                         batchElementForm; 
    private final RunWorkflows workflows;
	
    @Inject
    public State(NGLContext ctx, RunWorkflows workflows) {
    	stateForm        = ctx.form(models.laboratory.common.instance.State.class);
    	historicalForm   = ctx.form(HistoricalStateSearchForm.class);
    	batchElementForm = ctx.form(RunBatchElement.class);
    	this.workflows   = workflows;
	}
    
//    @Permission(value={"reading"})
    @Authenticated
    @Historized
    @Authorized.Read
    public Result get(String code) {
    	Run runValue = getRun(code, "state");
    	if (runValue != null) {
    		return ok(Json.toJson(runValue.state));
    	} else {
    		return notFound();
    	}
    }

//    @Permission(value={"writing"})
    @Authenticated
    @Historized
    @Authorized.Write
    // @Permission(value={"workflow_run_lane"})
    public Result update(String code) {
		Run run = getRun(code);
		if (run == null)
		    return badRequest(); // TODO: probably a not found
		Form<models.laboratory.common.instance.State> filledForm = getFilledForm(stateForm, models.laboratory.common.instance.State.class);
		models.laboratory.common.instance.State state = filledForm.get();
		state.date = new Date();
		state.user = getCurrentUser();
//		ContextValidation ctxVal = new ContextValidation(getCurrentUser(), filledForm.errors());
		ContextValidation ctxVal = new ContextValidation(getCurrentUser(), filledForm);
		workflows.setState(ctxVal, run, state);
		if (!ctxVal.hasErrors()) {
		    return ok(Json.toJson(getRun(code)));
		} else {
			return badRequest(NGLContext._errorsAsJson(ctxVal.getErrors()));
		}
    }
    
//    @Permission(value={"writing"})
    @Authenticated
    @Historized
    @Authorized.Write
    public Result updateBatch() {
    	List<Form<RunBatchElement>> filledForms =  getFilledFormList(batchElementForm, RunBatchElement.class);
		
		List<DatatableBatchResponseElement> response = new ArrayList<>(filledForms.size());
		for(Form<RunBatchElement> filledForm: filledForms){
			RunBatchElement element = filledForm.get();
			Run run = getRun(element.data.code);
			if (run != null) {
				models.laboratory.common.instance.State state = element.data.state;
				state.date = new Date();
				state.user = getCurrentUser();
//				ContextValidation ctxVal = new ContextValidation(getCurrentUser(), filledForm.errors());
				ContextValidation ctxVal = new ContextValidation(getCurrentUser(), filledForm);
				workflows.setState(ctxVal, run, state);
				if (!ctxVal.hasErrors()) {
					response.add(new DatatableBatchResponseElement(OK, getRun(run.code), element.index));
				} else {
					response.add(new DatatableBatchResponseElement(BAD_REQUEST, NGLContext._errorsAsJson(ctxVal.getErrors()), element.index));
				}
			} else {
				response.add(new DatatableBatchResponseElement(BAD_REQUEST, element.index));
			}
		}		
		return ok(Json.toJson(response));
    }
    
//    @Permission(value={"reading"})
    @Authenticated
    @Historized
    @Authorized.Read
    public Result historical(String code) {
		Run runValue = getRun(code, "state");
		if (runValue != null) {
		    Form<HistoricalStateSearchForm> inputForm = filledFormQueryString(historicalForm, HistoricalStateSearchForm.class);
		    Set<TransientState> historical = getHistorical(runValue.state.historical, inputForm.get());
		    return ok(Json.toJson(historical));
		} else {
		    return notFound();
		}
    }

    private static Set<TransientState> getHistorical(Set<TransientState> historical, HistoricalStateSearchForm form) {
		List<TransientState> values = new ArrayList<>();
		if (StringUtils.isNotBlank(form.stateCode)) {
//			Iterator<TransientState> iterator = historical.iterator();
//			while (iterator.hasNext()) {
//				TransientState ts = iterator.next();
//				if (form.stateCode.equals(ts.code)) {
//				    values.add(ts);
//				    if (form.last)
//				    	break;
//				}				
//			}
			for (TransientState ts : historical) {
				if (form.stateCode.equals(ts.code)) {
				    values.add(ts);
				    if (form.last)
				    	break;
				}				
			}
			
		    Collections.reverse(values);
		    return new HashSet<>(values);
		} else {
		    return historical;
		}
    }
    
}
