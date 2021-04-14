package controllers.experiments.api;

import java.util.ArrayList;

import javax.inject.Inject;

import controllers.NGLAPIController;
import controllers.QueryFieldsForm;
import controllers.StateController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APISemanticException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.experiments.ExperimentsAPI;
import fr.cea.ig.ngl.dao.experiments.ExperimentsDAO;
import models.laboratory.common.instance.State;
import models.laboratory.experiment.instance.AtomicTransfertMethod;
import models.laboratory.experiment.instance.Experiment;
import play.data.Form;
import play.mvc.Result;
import rules.services.IDrools6Actor;
import validation.ContextValidation;

public class Experiments extends NGLAPIController<ExperimentsAPI, ExperimentsDAO, Experiment> implements StateController {

    private final Form<Experiment> form;
    private final IDrools6Actor    rulesActor;

    @Inject
    public Experiments(NGLApplication app, ExperimentsAPI api) {
        super(app, api, ExperimentSearchForm.class);
        this.form = app.formFactory().form(Experiment.class);
        this.rulesActor = app.rules6Actor();
    }

    @Override
    public Experiment saveImpl() throws APIValidationException, APISemanticException {
        Experiment input = getFilledForm(form, Experiment.class).get();
        Experiment exp = api().create(input, getCurrentUser());
        return exp;
    }

    @Override
    public Experiment updateImpl(String code) throws Exception, APIException, APIValidationException {
        Experiment input = getFilledForm(form, Experiment.class).get();
        if(code.equals(input.code)) { 
            Experiment expInDB = api().get(code);
            if (!expInDB.state.code.equals(input.state.code)) throw new Exception("You can not change the state code. Please use the state url ! ");

            QueryFieldsForm queryFieldsForm = filledFormQueryString(updateForm, QueryFieldsForm.class).get();
            Experiment exp = null;
            final boolean userIsAdmin = getNGLApplication().authorizator().authorize(getCurrentUser(), new String[] {fr.cea.ig.authorization.Permission.Admin.getAlias()});
            if(queryFieldsForm.fields == null) {
                exp = api().update(input, getCurrentUser());
            } else if(queryFieldsForm.fields.contains("all") && queryFieldsForm.fields.contains("updateContentProperties") && userIsAdmin) {
                getLogger().info("update with cascading content properties (admin user)");
                exp = api().updateWithCascadingContentProperties(input, getCurrentUser());
            } else {
                exp = api().update(input, getCurrentUser(), queryFieldsForm.fields);
            }
            return exp;
        } else {
            throw new Exception("Experiment codes are not the same");
        }
    }

    @Override
    public Object updateStateImpl(String code, State state, String currentUser) throws APIValidationException, APIException {
        return api().updateState(code, state, currentUser);
    }
	
    @Authenticated
    @Authorized.Write
    @Override
    public Result delete(String code){
        return globalExceptionHandler(() -> {
            try {
                api().delete(code, getCurrentUser());
                return ok();
            } catch (APIValidationException e) {
                return badRequestLoggingForValidationException(e);
            } catch (APIException e) {
                getLogger().error(e.getMessage(), e);
                return badRequestAsJson(e.getMessage());
            } 
        });
    }
    
    @Authenticated
    @Authorized.Write
    public Result applyRules(String code, String codeRules) {
        return globalExceptionHandler(() -> {
            Experiment experiment = api().get(code);
            if (experiment == null)         
                return badRequestAsJson("Experiment with code " + code + " not exist");
            ContextValidation ctxVal = ContextValidation.createUndefinedContext(getCurrentUser());
            //ADD ATM for some rules
            ArrayList<Object> facts = new ArrayList<>();
            for(int i=0; i<experiment.atomicTransfertMethods.size(); i++) {
    			AtomicTransfertMethod atomic = experiment.atomicTransfertMethods.get(i);
    			if(atomic.viewIndex == null)atomic.viewIndex = i+1; //used to have the position in the list
    			facts.add(atomic);
    		}
            facts.add(experiment);
            facts.add(ctxVal);
            rulesActor.tellMessage(codeRules, facts);
            return ok();
        });
    }
}
