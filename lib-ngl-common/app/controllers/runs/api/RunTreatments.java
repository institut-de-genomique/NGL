package controllers.runs.api;

import javax.inject.Inject;

import controllers.NGLController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.lfw.Historized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.runs.RunsAPI;
import fr.cea.ig.ngl.dao.runs.TreatmentsAPI;
import fr.cea.ig.ngl.support.NGLForms;
import fr.cea.ig.play.IGBodyParsers;
import models.laboratory.run.instance.Run;
import models.laboratory.run.instance.Treatment;
import play.data.Form;
import play.mvc.BodyParser;
import play.mvc.Result;

@Historized
public class RunTreatments extends NGLController implements NGLForms {

    private final Form<Treatment> treatmentForm;
    private final TreatmentsAPI   api;
    private final RunsAPI         runApi;

    @Inject
    public RunTreatments(NGLApplication app, TreatmentsAPI api, RunsAPI runApi) {
        super(app);
        this.treatmentForm = app.formFactory().form(Treatment.class);
        this.api           = api;
        this.runApi        = runApi;
    }

    @Authenticated
    @Authorized.Read
    public Result head(String runCode, String treatmentCode) {
        return globalExceptionHandler(() -> {
            if(api.checkObjectExist(runCode, treatmentCode)) {
                return ok();
            } else {
                return notFound();
            }
        });
    }

    @Authenticated
    @Authorized.Read
    public Result get(String runCode, String treatmentCode){
        return globalExceptionHandler(() -> {
            Run run = api.get(runCode, treatmentCode); 
            if (run == null) {
                return notFound();
            } else {
                return okAsJson(api.getSubObject(run, treatmentCode));
            }
        });
    }

    @Authenticated
    @Authorized.Write
    public Result delete(String runCode, String treatmentCode){
        return globalExceptionHandler(() -> {
            Run run  = api.get(runCode, treatmentCode); 
            if (run == null) {
                return badRequestAsJson("Run with code " + runCode + " not exist");
            } else {
                try {
                    api.delete(run, treatmentCode, getCurrentUser());
                    return ok();
                } catch (APIException e) {
                    logger.error(e.getMessage(), e);
                    return badRequestAsJson(e.getMessage());
                }
            }   
        });
    }

    @Authenticated
    @Authorized.Read
    public Result list(String runCode){
        return globalExceptionHandler(() -> {
            try {
                return okAsJson(api.list(runCode));
            } catch (APIException e) {
                logger.trace(e.getMessage(), e);
                return notFoundAsJson(e.getMessage());
            } 
        });
    }   

    @Authenticated
    @Authorized.Write
    @BodyParser.Of(value = IGBodyParsers.Json5MB.class)
    public Result save(String runCode){
        return globalExceptionHandler(() -> {
            Run run  = runApi.get(runCode);
            if (run == null) {
                return badRequestAsJson("Run with code " + runCode + " not exist");
            } else {
                Treatment input = getFilledForm(treatmentForm, Treatment.class).get();
                try {
                    return okAsJson(api.save(run, input, getCurrentUser()));
                } catch (APIValidationException e) {
                    return badRequestLoggingForValidationException(e);
                }    
            }
        });
    }

    @Authenticated
    @Authorized.Write
    @BodyParser.Of(value = IGBodyParsers.Json5MB.class)
    public Result update(String runCode, String treatmentCode){
        return globalExceptionHandler(() -> {
            Run run  = runApi.get(runCode);
            if (run == null) {
                return badRequestAsJson("Run with code " + runCode + " not exist");
            } else {
                Treatment treatment = getFilledForm(treatmentForm, Treatment.class).get();
                if (!treatmentCode.equals(treatment.code)) {
                    return badRequestAsJson("treatment code are not the same");
                } else {
                    try {
                        return okAsJson(api.update(run, treatment, getCurrentUser()));
                    } catch (APIValidationException e) {
                        return badRequestLoggingForValidationException(e);
                    }
                }
            }   
        });
    }
}
