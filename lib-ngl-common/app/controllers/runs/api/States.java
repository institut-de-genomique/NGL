package controllers.runs.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.mongodb.BasicDBObject;

import controllers.NGLController;
import controllers.StateController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.lfw.Historized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.runs.RunsAPI;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TransientState;
import models.laboratory.run.instance.Run;
import play.data.Form;
import play.mvc.Result;

@Historized
public class States extends NGLController implements StateController {

    private final RunsAPI api;
    private final Form<HistoricalStateSearchForm> historicalForm;
    
    @Inject
    public States(NGLApplication app, RunsAPI api) {
        super(app);
        this.api = api;
        this.historicalForm = app.formFactory().form(HistoricalStateSearchForm.class);
    }

    @Override
    public Object updateStateImpl(String code, State state, String currentUser) throws APIException {
        return api.updateState(code, state, currentUser);
    }

    @Authenticated
    @Authorized.Read
    public Result get(String code) {
        BasicDBObject keys = new BasicDBObject();
        keys.append("state", 1);
        Run run = api.getObject(code, keys);
        if (run != null) {
            return okAsJson(run.state);
        } else {
            return notFound();
        }
    }
    
    @Authenticated
    @Authorized.Read
    public Result historical(String code) {
        BasicDBObject keys = new BasicDBObject();
        keys.append("state", 1);
        Run run = api.getObject(code, keys);
        if (run != null) {
            Form<HistoricalStateSearchForm> inputForm = filledFormQueryString(historicalForm, HistoricalStateSearchForm.class);
            Set<TransientState> historical = getHistorical(run.state.historical, inputForm.get());
            return okAsJson(historical);
        } else {
            return notFound();
        }
    }

    private static Set<TransientState> getHistorical(Set<TransientState> historical, HistoricalStateSearchForm form) {
        List<TransientState> values = new ArrayList<>();
        if (StringUtils.isNotBlank(form.stateCode)) {
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
