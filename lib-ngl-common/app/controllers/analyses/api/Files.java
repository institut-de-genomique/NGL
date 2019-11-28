package controllers.analyses.api;

import javax.inject.Inject;

import controllers.NGLController;
import controllers.QueryFieldsForm;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.lfw.Historized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.analyses.AnalysesAPI;
import fr.cea.ig.ngl.dao.analyses.FilesAPI;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.support.NGLForms;
import models.laboratory.run.instance.Analysis;
import models.laboratory.run.instance.File;
import play.data.Form;
import play.mvc.Result;

@Historized
public class Files extends NGLController implements NGLForms {

    private final FilesAPI              api;
    private final AnalysesAPI           analysesApi;
    private final Form<File>            fileForm;
    private final Form<QueryFieldsForm> updateForm;

    @Inject
    public Files(NGLApplication app, FilesAPI api, AnalysesAPI analysisApi) {
        super(app);
        this.api        = api;
        this.analysesApi = analysisApi;
        this.fileForm   = app.formFactory().form(File.class);
        this.updateForm = app.formFactory().form(QueryFieldsForm.class);
    }

    @Authenticated
    @Authorized.Read
    public Result head(String parentCode, String fullname) {
        if (api.checkObjectExist(parentCode, fullname)) {
            return ok();
        } else {
            return notFound();
        }
    }

    @Authenticated
    @Authorized.Read
    public Result get(String parentCode, String fullname) {
        return globalExceptionHandler(() -> {
            if (api.checkObjectExist(parentCode, fullname)) {
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
            Analysis objectInDB = analysesApi.get(parentCode);
            if (objectInDB != null) {
                return okAsJson(api.getSubObjects(objectInDB));
            } else {
                return notFound();
            }
        });
    }

    @Authenticated
    @Authorized.Write
    public Result save(String parentCode) {
        return globalExceptionHandler(() -> {
            Analysis objectInDB = analysesApi.get(parentCode);
            if (objectInDB == null) {
                return notFound();
            } else {
                File inputFile = getFilledForm(fileForm, File.class).get();
                try {
                    return okAsJson(api.save(objectInDB, inputFile, getCurrentUser()));
                } catch (APIValidationException e) {
                    return badRequestLoggingForValidationException(e);
                }
            }
        });
    }

    @Authenticated
    @Authorized.Write
    public Result update(String parentCode, String fullname){
        return globalExceptionHandler(() -> {
            if (api.checkObjectExist(parentCode, fullname)) {
                Analysis objectInDB = analysesApi.get(parentCode);
                QueryFieldsForm queryFieldsForm = filledFormQueryString(updateForm, QueryFieldsForm.class).get();
                File inputFile = getFilledForm(fileForm, File.class).get();
                if (queryFieldsForm.fields == null) {
                    if (fullname.equals(inputFile.fullname)) {  
                        try {
                            return okAsJson(api.update(objectInDB, inputFile, getCurrentUser()));
                        } catch (APIValidationException e) {
                            return badRequestLoggingForValidationException(e);
                        }
                    } else {
                        return badRequest("fullname are not the same");
                    }
                } else { //update only some authorized properties
                    try {
                        return okAsJson(api.update(objectInDB, inputFile, getCurrentUser(), queryFieldsForm.fields, fullname));
                    } catch (APIValidationException e) {
                        return badRequestLoggingForValidationException(e);
                    }
                }
            } else {
                return notFound();
            }
        });
    }

    @Authenticated
    @Authorized.Write
    public Result delete(String parentCode, String fullname) {
        return globalExceptionHandler(() -> {
            if (api.checkObjectExist(parentCode, fullname)) {
                try {
                    api.delete(analysesApi.get(parentCode), fullname, getCurrentUser());
                } catch (APIException e) {
                    return badRequestAsJson(e.getMessage());
                }
                return ok();
            } else {
                return notFound();
            }
        });
    }

    @Authenticated
    @Authorized.Write
    public Result deleteByParentCode(String parentCode) {
        return globalExceptionHandler(() -> {
            Analysis objectInDB = analysesApi.get(parentCode);
            if (objectInDB != null) {
                api.delete(objectInDB, getCurrentUser());
                return ok();
            } else {
                return notFound();
            }
        });
    }
}
