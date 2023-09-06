package controllers.sra.studies.api;

import javax.inject.Inject;

import controllers.NGLController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APISemanticException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.sra.AbstractStudyAPI;
import fr.cea.ig.ngl.dao.api.sra.StudyCommentsAPI;
import fr.cea.ig.ngl.support.NGLForms;
import models.laboratory.common.instance.Comment;
import models.sra.submit.sra.instance.AbstractStudy;
import play.mvc.Result;

public class StudyComments extends NGLController implements NGLForms {

    private final StudyCommentsAPI api;
    private final AbstractStudyAPI parentApi;


    @Inject
    public StudyComments(NGLApplication app, StudyCommentsAPI api, AbstractStudyAPI parentApi) {
        super(app);
        this.api = api;
        this.parentApi = parentApi;
    }

    @Authenticated
    @Authorized.Write
    public Result save(String parentCode) {
        return globalExceptionHandler(() -> {
            AbstractStudy objectInDB = parentApi.get(parentCode);	
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
        	AbstractStudy objectInDB = parentApi.get(parentCode);
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
        	AbstractStudy objectInDB = parentApi.get(parentCode);
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
