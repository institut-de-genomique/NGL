package controllers.runs.api;

import java.util.Collection;

import javax.inject.Inject;

import controllers.NGLController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.lfw.Historized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.runs.LanesAPI;
import fr.cea.ig.ngl.dao.runs.RunsAPI;
import fr.cea.ig.ngl.support.NGLForms;
import models.laboratory.common.instance.Valuation;
import models.laboratory.run.instance.Lane;
import models.laboratory.run.instance.Run;
import play.data.Form;
import play.mvc.Result;

@Historized
public class Lanes extends NGLController implements NGLForms {

    private final Form<Lane>      laneForm;
    private final Form<Valuation> valuationForm;
    private final LanesAPI        api;
    private final RunsAPI         runApi;

    @Inject
    public Lanes(NGLApplication app, LanesAPI api, RunsAPI runApi) {
        super(app);
        this.laneForm      = app.formFactory().form(Lane.class);
        this.valuationForm = app.formFactory().form(Valuation.class);
        this.api           = api;
        this.runApi        = runApi;
    }

    @Authenticated
    @Authorized.Read
    public Result list(String code) {
        return globalExceptionHandler(() -> {
            Run run = runApi.get(code);
            if (run == null) {
                return notFoundAsJson("Run with code " + code + " not exist"); 
            } else {
                Collection<Lane> lanes = api.getSubObjects(run);
                if (lanes == null) {
                    return notFound();
                } else {
                    return okAsJson(lanes);
                }
            }
        });
    }

    @Authenticated
    @Authorized.Read
    public Result get(String code, Integer laneNumber) {
        return globalExceptionHandler(() -> {
            try {
                return okAsJson(api.get(code, laneNumber));
            } catch (APIException e) {
                return notFoundAsJson(e.getMessage());
            }
        });
    }

    @Authenticated
    @Authorized.Read
    public Result head(String code, Integer laneNumber){
        return globalExceptionHandler(() -> {
            if(api.checkObjectExist(code, laneNumber)) {
                return ok();
            } else {
                return notFound();
            } 
        });
    }


    @Authenticated
    @Authorized.Write
    public Result save(String code) {
        return globalExceptionHandler(() -> {
            Run run = runApi.get(code);
            if (run == null) {
                return badRequestAsJson("Run with code " + code + " not exist");
            } else {
                Lane input = getFilledForm(laneForm, Lane.class).get();
                try {
                    return okAsJson(api.save(run, input, getCurrentUser()));
                } catch (APIValidationException e) {
                    return badRequestLoggingForValidationException(e);
                } catch (APIException e) {
                    logger.error(e.getMessage(), e);
                    return badRequestAsJson(e.getMessage());
                }
            }	
        });
    }

    @Authenticated
    @Authorized.Write
    public Result update(String code, Integer laneNumber){
        return globalExceptionHandler(() -> {
            Run run = runApi.get(code);
            if (run == null) {
                return badRequestAsJson("Run with code " + code + " not exist");
            } else {
                Lane input = getFilledForm(laneForm, Lane.class).get();
                if (laneNumber.equals(input.number)) {  
                    try {
                        return okAsJson(api.update(run, input, getCurrentUser()));
                    } catch (APIValidationException e) {
                        return badRequestLoggingForValidationException(e);
                    } catch (APIException e) {
                        logger.error(e.getMessage(), e);
                        return badRequestAsJson(e.getMessage());
                    }
                } else {
                    return badRequestAsJson("lane number are not the same"); 
                }
            }
        });
    }

    @Authenticated
    @Authorized.Write
    public Result delete(String code, Integer laneNumber) { 
        return globalExceptionHandler(() -> {
            try {
                api.delete(code, laneNumber, getCurrentUser());
                return ok();
            } catch (APIException e) {
                logger.trace(e.getMessage(), e);
                return badRequestAsJson(e.getMessage());
            }
        });
    }

    @Authenticated
    @Authorized.Write
    public Result deleteByRunCode(String code) {
        return globalExceptionHandler(() -> {
            Run run = runApi.get(code);
            if (run == null) {
                return badRequestAsJson("Run with code " + code + " not exist");
            } else {
                api.deleteAllLanes(run, getCurrentUser());
                return ok();
            }
        });
    }

    @Authenticated
    @Authorized.Write
    public Result valuation(String code, Integer laneNumber){
        return globalExceptionHandler(() -> {
            Run run;
            try {
                run = api.getRun(code, laneNumber);
                if (run == null) {
                    return notFoundAsJson("Run with code " + code + " not exist");
                } else {
                    Valuation valuation = getFilledForm(valuationForm, Valuation.class).get();
                    return okAsJson(api.valuation(run, laneNumber, valuation, getCurrentUser()));
                }
            } catch (APIValidationException e) {
                return badRequestLoggingForValidationException(e);
            } catch (APIException e) {
                logger.error(e.getMessage(), e);
                return badRequestAsJson(e.getMessage());
            }
        });
    }	

}
