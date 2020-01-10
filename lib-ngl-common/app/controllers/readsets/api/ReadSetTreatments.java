package controllers.readsets.api;

import javax.inject.Inject;

import controllers.NGLController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.lfw.Historized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.readsets.ReadSetTreatmentsAPI;
import fr.cea.ig.ngl.dao.readsets.ReadSetsAPI;
import fr.cea.ig.ngl.support.NGLForms;
import fr.cea.ig.play.IGBodyParsers;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Treatment;
import play.data.Form;
import play.mvc.BodyParser;
import play.mvc.Result;

@Historized
public class ReadSetTreatments  extends NGLController implements NGLForms {

    private final ReadSetTreatmentsAPI api;
    private final ReadSetsAPI readSetApi;
    private final Form<Treatment> treatmentForm; 

    @Inject
    public ReadSetTreatments(NGLApplication app, ReadSetTreatmentsAPI api, ReadSetsAPI readSetApi) {
        super(app);
        this.api           = api;
        this.readSetApi    = readSetApi;
        this.treatmentForm = app.formFactory().form(Treatment.class);
    }

    @Authenticated
    @Authorized.Read
    public Result list(String readSetCode){
        return globalExceptionHandler(() -> {
            ReadSet readSet = readSetApi.get(readSetCode);
            if (readSet != null) {
                return okAsJson(api.getSubObjects(readSet));
            } else {
                return notFound();
            }
        });
    }

    @Authenticated
    @Authorized.Read
    public Result head(String readSetCode, String treatmentCode) {
        return globalExceptionHandler(() -> {
            if(api.checkObjectExist(readSetCode, treatmentCode)) {
                return ok();
            } else {
                return notFound();
            }
        });
    }

    @Authenticated
    @Authorized.Read
    public Result get(String readSetCode, String treatmentCode){
        return globalExceptionHandler(() -> {
            if(api.checkObjectExist(readSetCode, treatmentCode)) {
                return okAsJson(api.getSubObject(readSetApi.get(readSetCode), treatmentCode));
            } else {
                return notFound();
            }     
        });
    }

    @Authenticated
    @Authorized.Write
    @BodyParser.Of(value = IGBodyParsers.Json5MB.class)
    public Result save(String readSetCode){
        return globalExceptionHandler(() -> {
            try {
                ReadSet readSet = readSetApi.get(readSetCode);
                if (readSet != null) {
                    Treatment treatment = getFilledForm(treatmentForm, Treatment.class).get();
                    return okAsJson(api.save(readSet, treatment, getCurrentUser()));
                } else {
                    return badRequestAsJson("ReadSet with code " + readSetCode + " not exist");
                }
            } catch (APIValidationException e) {
                return badRequestLoggingForValidationException(e);
            }
        });
    }
    
    @Authenticated
    @Authorized.Write
    @BodyParser.Of(value = IGBodyParsers.Json5MB.class)
    public Result update(String readSetCode, String treatmentCode){
        return globalExceptionHandler(() -> {
            try {
                if (api.checkObjectExist(readSetCode, treatmentCode)) {
                    ReadSet readSet = readSetApi.get(readSetCode);
                    Treatment treatment = getFilledForm(treatmentForm, Treatment.class).get();
                    if (!treatmentCode.equals(treatment.code)) {
                        return badRequestAsJson("treatment code are not the same");
                    } else {
                        return okAsJson(api.update(readSet, treatment, getCurrentUser()));
                    }
                } else {
                    return badRequest();
                }
            } catch (APIValidationException e) {
                return badRequestLoggingForValidationException(e);
            }
        });
    }
    
    
    
    @Authenticated
    @Authorized.Write
    public Result delete(String readSetCode, String treatmentCode){
        return globalExceptionHandler(() -> {
            if (api.checkObjectExist(readSetCode, treatmentCode)) {
                try {
                    api.delete(readSetApi.get(readSetCode), treatmentCode, getCurrentUser());
                    return ok();
                } catch (APIException e) {
                    // no management of exception here
                    throw new RuntimeException(e);
                }
            } else {
                return badRequest();
            }
        });  
    }
    
    @Authenticated
    @Authorized.Write
    public Result deleteAll(String readSetCode){
        return globalExceptionHandler(() -> {
            ReadSet readSet = readSetApi.get(readSetCode);
            if (readSet != null) {
                api.deleteAll(readSet, getCurrentUser());
                return ok();
            } else {
                return badRequest();
            }
        });  
    }
}
