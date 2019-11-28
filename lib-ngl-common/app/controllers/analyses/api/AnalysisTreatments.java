package controllers.analyses.api;

import javax.inject.Inject;

import controllers.NGLController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.lfw.Historized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.analyses.AnalysesAPI;
import fr.cea.ig.ngl.dao.analyses.AnalysisTreatmentsAPI;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.support.NGLForms;
import fr.cea.ig.play.IGBodyParsers;
import models.laboratory.run.instance.Analysis;
import models.laboratory.run.instance.Treatment;
import play.data.Form;
import play.mvc.BodyParser;
import play.mvc.Result;

@Historized
public class AnalysisTreatments extends NGLController implements NGLForms {

    private final AnalysisTreatmentsAPI api;
    private final AnalysesAPI analysesApi;
    private final Form<Treatment> form;
    @Inject
    public AnalysisTreatments(NGLApplication app, AnalysisTreatmentsAPI api, AnalysesAPI analysesApi) {
        super(app);
        this.api         = api;
        this.analysesApi = analysesApi;
        this.form        = app.formFactory().form(Treatment.class);
    }
    
    @Authenticated
    @Authorized.Read
    public Result head(String parentCode, String code) {
        return globalExceptionHandler(() -> {
            if (api.checkObjectExist(parentCode, code)) {
                return ok();
            } else {
                return notFound();
            }
        });
    }

    @Authenticated
    @Authorized.Read
    public Result get(String parentCode, String code) {
        return globalExceptionHandler(() -> {
            if (api.checkObjectExist(parentCode, code)) {
                return okAsJson(analysesApi.get(parentCode));
            } else {
                return notFound();
            }
        });
    }

    @Authenticated
    @Authorized.Read
    public Result list(String parentCode) {
        return globalExceptionHandler(() -> {
            Analysis analysis = analysesApi.get(parentCode);
            if (analysis == null) {
                return notFound();
            } else {
                return okAsJson(api.getSubObjects(analysis));
            }
        });
    }

    @Authenticated
    @Authorized.Write
    @BodyParser.Of(value = IGBodyParsers.Json5MB.class)
    public Result save(String parentCode) {
        return globalExceptionHandler(() -> {
            Analysis objectInDB = analysesApi.get(parentCode);
            if (objectInDB == null) {
                return notFound();
            } else {
                Treatment inputTreatment = getFilledForm(form, Treatment.class).get();
                try {
                    return okAsJson(api.save(objectInDB, inputTreatment, getCurrentUser()));
                } catch (APIValidationException e) {
                    return badRequestLoggingForValidationException(e);
                }
            }
        });
    }
    
    @Authenticated
    @Authorized.Write
    @BodyParser.Of(value = IGBodyParsers.Json5MB.class)
    public Result update(String parentCode, String code){
        return globalExceptionHandler(() -> {
            if(api.checkObjectExist(parentCode, code)) {
                Analysis objectInDB = analysesApi.get(parentCode);
                Treatment inputTreatment = getFilledForm(form, Treatment.class).get();
                if (code.equals(inputTreatment.code)) {
                    try {
                        return okAsJson(api.update(objectInDB, inputTreatment, getCurrentUser()));
                    } catch (APIValidationException e) {
                        return badRequestLoggingForValidationException(e);
                    }
                } else {
                    return badRequestAsJson("treatment code are not the same"); 
                }
            } else {
                return notFound();
            }
        });
    }
    
    @Authenticated
    @Authorized.Write
    public Result delete(String parentCode, String code) {
        return globalExceptionHandler(() -> {
            Analysis objectInDB = analysesApi.get(parentCode);
            if (objectInDB == null) {
                return notFound();
            } else {
                try {
                    api.delete(objectInDB, code, getCurrentUser());
                    return ok();
                } catch (APIException e) {
                    getLogger().error(e.getMessage(), e);
                    return badRequestAsJson(e.getMessage());
                } 
            }
        });
    }
    
    @Authenticated
    @Authorized.Write
    public Result deleteAll(String parentCode) {
        return globalExceptionHandler(() -> {
            Analysis objectInDB = analysesApi.get(parentCode);
            if (objectInDB == null) {
                return notFound();
            } else {
                try {
                    api.deleteAll(objectInDB, getCurrentUser());
                    return ok();
                } catch (APIException e) {
                    getLogger().error(e.getMessage(), e);
                    return badRequestAsJson(e.getMessage());
                } 
            }
        });
    }
}
