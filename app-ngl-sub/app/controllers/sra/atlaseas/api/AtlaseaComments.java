package controllers.sra.atlaseas.api;

import javax.inject.Inject;

import controllers.NGLController;
import controllers.sra.api.AtlaseaAPI;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APISemanticException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.support.NGLForms;
import models.laboratory.common.instance.Comment;
import models.sra.submit.sra.instance.Atlasea;
import play.mvc.Result;

public class AtlaseaComments extends NGLController implements NGLForms {

    private final AtlaseaCommentsAPI api;
    private final AtlaseaAPI parentApi;


    @Inject
    public AtlaseaComments(NGLApplication app, AtlaseaCommentsAPI api, AtlaseaAPI parentApi) {
        super(app);
        this.api = api;
        this.parentApi = parentApi;
    }

    @Authenticated
    @Authorized.Write
    public Result save(String parentCode) {
        return globalExceptionHandler(() -> {
            Atlasea objectInDB = parentApi.get(parentCode);	
            if (objectInDB == null) {
                return notFound();
            } else {
                Comment inputComment = getFilledForm(Comment.class).get();
                try {
                    Comment comment = api.save(objectInDB, inputComment, getCurrentUser());
                    return okAsJson(comment);
                } catch (APIValidationException e) {
                    return badRequestLoggingForValidationException(e);
                } catch (APISemanticException e) {
                    if (getLogger().isDebugEnabled()) getLogger().warn(e.getMessage(), e);
                    return badRequestAsJson("use PUT method to update");
                } 
            }
        });
    }

    @Authenticated
    @Authorized.Write
    public Result update(String parentCode, String code){
        return globalExceptionHandler(() -> {
        	Atlasea objectInDB = parentApi.get(parentCode);
            if (objectInDB == null) {
                return notFound();
            } else {
                Comment input = getFilledForm(Comment.class).get();
                try {
                    Comment comment = api.update(parentApi.get(parentCode), input, getCurrentUser());
                    return okAsJson(comment);
                } catch (APIValidationException e) {
                    return badRequestLoggingForValidationException(e);
                } catch (APIException e) {
                    if (getLogger().isDebugEnabled()) getLogger().warn(e.getMessage(), e);
                    return forbidden();
                }
            }

        });
    }

    @Authenticated
    @Authorized.Write
    public Result delete(String parentCode, String code){
        return globalExceptionHandler(() -> {
        	Atlasea objectInDB = parentApi.get(parentCode);
            if (objectInDB == null) {
                return notFound();
            } else {
                try {
                    api.delete(objectInDB, code, getCurrentUser());
                } catch (APIException e) {
                    if (getLogger().isDebugEnabled()) getLogger().warn(e.getMessage(), e);
                    return forbidden();
                }
                return ok();
            }
        });
    }

}

