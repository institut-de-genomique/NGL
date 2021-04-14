package controllers.readsets.api;

import javax.inject.Inject;

import controllers.NGLController;
import controllers.QueryFieldsForm;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.lfw.Historized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.readsets.FilesAPI;
import fr.cea.ig.ngl.dao.readsets.ReadSetsAPI;
import fr.cea.ig.ngl.dao.runs.RunsAPI;
import fr.cea.ig.ngl.support.NGLForms;
import models.laboratory.run.instance.File;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import play.data.Form;
import play.mvc.Result;

@Historized
public class Files extends NGLController implements NGLForms {

    private final FilesAPI api;
    private final ReadSetsAPI readSetApi;
    private final Form<File> fileForm; 
    private final Form<QueryFieldsForm> updateForm;
    private final RunsAPI runApi;

    @Inject
    public Files(NGLApplication app, FilesAPI api, ReadSetsAPI readSetApi,RunsAPI runApi) {
        super(app);
        this.api        = api;
        this.readSetApi = readSetApi;
        this.runApi     = runApi;
        this.fileForm   = app.formFactory().form(File.class);
        this.updateForm = app.formFactory().form(QueryFieldsForm.class);
    }

    @Authenticated
    @Authorized.Read
    public Result head(String readsetCode, String fullname) {
        return globalExceptionHandler(() -> {
            if (api.checkObjectExist(readsetCode, fullname)){
                return ok();                    
            } else {
                return notFound();
            }  
        });
    }
    
    @Authenticated
    @Authorized.Read
    public Result get(String readsetCode, String fullname) {
        return globalExceptionHandler(() -> {
            if(api.checkObjectExist(readsetCode, fullname)) {
                File file = api.getSubObject(readSetApi.get(readsetCode), fullname);
                if (file != null ) {
                    return okAsJson(file);
                } else {
                    return notFound();
                }
            } else {
                return badRequest(); // EJACOBY: return a value coherent with head()
            }
        });
    }
    
    
    @Authenticated
    @Authorized.Read
    public Result list(String readSetCode){
        return globalExceptionHandler(() -> {
            ReadSet readSet = readSetApi.get(readSetCode);
            if (readSet != null) {
                return okAsJson(api.getSubObjects(readSet));
            } else {
                return badRequest(); // EJACOBY: return notFound()
            }
        });
    }
    
    
    @Authenticated
    @Authorized.Write
    public Result save(String readSetCode) {
        return globalExceptionHandler(() -> {
            try {
                ReadSet readSet = readSetApi.get(readSetCode);
                if (readSet != null) {
                    File file = getFilledForm(fileForm, File.class).get();
                    logger.debug("file " + file);
                    return okAsJson(api.save(readSet, file, getCurrentUser()));
                } else {
                    return badRequest(); // EJACOBY: return a value coherent with head()
                }
            } catch (APIValidationException e) {
                return badRequestLoggingForValidationException(e);
            }
        });
    }
    
    @Authenticated
    @Authorized.Write
    public Result update(String readSetCode, String fullname) {
        return globalExceptionHandler(() -> {
            if (api.checkObjectExist(readSetCode, fullname)) {
                ReadSet readSet = readSetApi.get(readSetCode);
                QueryFieldsForm queryFieldsForm = filledFormQueryString(updateForm, QueryFieldsForm.class).get();
                File file = getFilledForm(fileForm, File.class).get();
                if (queryFieldsForm.fields == null) {
                    if (fullname.equals(file.fullname)) { 
                        try {
                            return okAsJson(api.update(readSet, file, getCurrentUser()));
                        } catch (APIValidationException e) {
                            return badRequestLoggingForValidationException(e);
                        }
                    } else {
                        return badRequestAsJson("fullname are not the same");
                    }
                } else { //update only some authorized properties
                    try {
                        return okAsJson(api.update(readSet, file, getCurrentUser(), queryFieldsForm.fields, fullname));
                    } catch (APIValidationException e) {
                        return badRequestLoggingForValidationException(e);
                    }
                }
            } else {
                return badRequest();
            }
        });
    }

    @Authenticated
    @Authorized.Write
    public Result delete(String readSetCode, String fullname) {
        return globalExceptionHandler(() -> {
            if (api.checkObjectExist(readSetCode, fullname)) {
                try {
                    api.delete(readSetApi.get(readSetCode), fullname, getCurrentUser());
                    return ok();
                } catch (APIException e) {
                    getLogger().error(e.getMessage(), e);
                    return badRequestAsJson(e.getMessage());
                }
            } else {
                return badRequest();
            }
        });
    }


    @Authenticated
    @Authorized.Write
    public Result deleteByReadSetCode(String readsetCode) { 
        return globalExceptionHandler(() -> {
            ReadSet readSet = readSetApi.get(readsetCode);
            if (readSet == null) {
                return badRequest();
            } else {
                api.deleteByReadSetCode(readSet);
                return ok();
            }
        });
    }

    @Authenticated
    @Authorized.Write
    public Result deleteByRunCode(String runCode) { 
        Run run  = runApi.get(runCode);
        if (run == null) {
            return notFound();
        } else {
            api.deleteByRunCode(run);
            return ok();
        }
    }
    
}
