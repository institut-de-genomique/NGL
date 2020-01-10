package controllers.runs.api;


import java.util.Map;

import javax.inject.Inject;

import controllers.NGLController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.lfw.Historized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.runs.LaneTreatmentsAPI;
import fr.cea.ig.ngl.support.NGLForms;
import fr.cea.ig.play.IGBodyParsers;
import models.laboratory.run.instance.Run;
import models.laboratory.run.instance.Treatment;
import play.data.Form;
import play.mvc.BodyParser;
import play.mvc.Result;

@Historized
public class LaneTreatments extends NGLController implements NGLForms {

    private final Form<Treatment>   treatmentForm;
    private final LaneTreatmentsAPI api;

    @Inject
    public LaneTreatments(NGLApplication app, LaneTreatmentsAPI api) {
        super(app);
        treatmentForm = app.formFactory().form(Treatment.class);
        this.api      = api;
    }

    @Authenticated
    @Authorized.Read
    public Result list(String runCode, Integer laneNumber){
        return globalExceptionHandler(() -> {
            try {
                Map<String, Treatment> treatments = api.list(runCode, laneNumber);
                if (treatments == null) {
                    return notFoundAsJson("Run with code " + runCode + " not exist"); 
                } else {
                    return okAsJson(treatments);
                }
            } catch (APIException e) {
                return notFoundAsJson(e.getMessage());
            }	
        });
    }

    @Authenticated
    @Authorized.Read
    public Result get(String runCode, Integer laneNumber, String treatmentCode){
        return globalExceptionHandler(() -> {
            try {
                return okAsJson(api.get(api.getRun(runCode, laneNumber), laneNumber, treatmentCode));
            } catch (APIException e) {
                return notFoundAsJson(e.getMessage());
            }
        });
    }

    @Authenticated
    @Authorized.Read
    public Result head(String runCode, Integer laneNumber, String treatmentCode) {
        return globalExceptionHandler(() -> {
            if (api.checkObjectExist(runCode, laneNumber, treatmentCode)){
                return ok();
            } else {
                return notFound();
            }
        });
    }

    @Authenticated
    @Authorized.Write
    @BodyParser.Of(value = IGBodyParsers.Json5MB.class)
    public Result save(String runCode, Integer laneNumber) {
        return globalExceptionHandler(() -> {
            try {
                Run run = api.getRun(runCode, laneNumber);
                if (run == null) {
                    return badRequestAsJson("Run with code " + runCode + " not exist"); 
                } else {
                    Treatment input = getFilledForm(treatmentForm, Treatment.class).get();
                    Treatment t = api.save(run, input, laneNumber, getCurrentUser());
                    return okAsJson(t);
                }
            } catch (APIValidationException e) {
                return badRequestLoggingForValidationException(e);
            } catch (APIException e) {
                return badRequestAsJson(e.getMessage());
            }
        });
    }

    @Authenticated
    @Authorized.Write
    @BodyParser.Of(value = IGBodyParsers.Json5MB.class)
    public Result update(String runCode, Integer laneNumber, String treatmentCode){
        return globalExceptionHandler(() -> {
            Run run = api.getRun(runCode, laneNumber, treatmentCode);
            if (run == null) {
                return badRequestAsJson("Run with code " + runCode + " not exist"); 
            } else {
                Treatment input = getFilledForm(treatmentForm, Treatment.class).get();
                if (treatmentCode.equals(input.code)) {
                    try {
                        return okAsJson(api.update(run, laneNumber, input, getCurrentUser()));
                    } catch (APIValidationException e) {
                        return badRequestLoggingForValidationException(e);
                    } catch (APIException e) {
                        return badRequestAsJson(e.getMessage());
                    }
                } else {
                    return badRequestAsJson("treatment code are not the same");
                }
            }
        });
    }

    @Authenticated
    @Authorized.Write
    public Result delete(String runCode,  Integer laneNumber, String treatmentCode){
        return globalExceptionHandler(() -> {
            Run run = api.getRun(runCode, laneNumber, treatmentCode);
            if (run == null) {
                return notFoundAsJson("Run with code " + runCode + " not exist");
            } else {
                try {
                    api.delete(run, laneNumber, treatmentCode, getCurrentUser());
                    return ok();
                } catch (APIException e) {
                    return badRequestAsJson(e.getMessage());
                }
            }
        });	
    }	

}
