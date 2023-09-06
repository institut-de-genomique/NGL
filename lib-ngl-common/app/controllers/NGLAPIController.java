package controllers;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import akka.stream.javadsl.Source;
import akka.util.ByteString;
import fr.cea.ig.DBObject;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.lfw.Historized;
import fr.cea.ig.lfw.utils.Streamer;
import fr.cea.ig.mongo.DBObjectConvertor;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.GenericMongoDAO;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APISemanticException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.GenericAPI;
import fr.cea.ig.ngl.support.ListFormWrapper;
import fr.cea.ig.ngl.support.NGLForms;
import fr.cea.ig.play.IGBodyParsers;
import play.Logger;
import play.data.Form;
import play.data.validation.ValidationError;
import play.mvc.BodyParser;
import play.mvc.Result;
import views.components.datatable.DatatableForm;

/**
 * Contains Generic Methods of API controllers 
 * like head()
 * @author ajosso
 *
 * @param <T> GenericAPI
 * @param <U> GenericMongoDAO
 * @param <V> DBObject
 */
@Historized
public abstract class NGLAPIController<T extends GenericAPI<U, V>, U extends GenericMongoDAO<V>, V extends DBObject>
        extends NGLController implements NGLForms, DBObjectConvertor {

    protected final Form<QueryFieldsForm> updateForm;
    protected final Class<? extends DBObjectListForm<V>> searchFormClass;

    private final T api;

    public T api() {
        return api;
    }

    public NGLAPIController(NGLApplication app, T api, Class<? extends DBObjectListForm<V>> searchFormClass) {
        super(app);
        this.api = api;
        this.updateForm = app.formFactory().form(QueryFieldsForm.class);
        this.searchFormClass = searchFormClass;
    }

    /**
     * If object exists returns Status 200 OK <br>
     * else returns Status 404 NOT FOUND.
     * @param code String 
     * @return     Result HTTP result (200 or 404)
     */
    @Authenticated
    @Authorized.Read
    public Result head(String code) {
        return globalExceptionHandler(() -> {
            if(! api().isObjectExist(code)) {
                return notFound();
            } else {
                return ok();
            }
        });
    }


    @Authenticated
    @Authorized.Read
    public Result list() {
        return globalExceptionHandler(() -> {
            try {
                Source<ByteString, ?> resultsAsStream = api().list(new ListFormWrapper<>(objectFromRequestQueryString(this.searchFormClass), form -> generateBasicDBObjectFromKeys(form)));
                return Streamer.okStream(resultsAsStream);
            } catch (APIException e) {
                getLogger().error(e.getMessage(), e);
                return badRequestAsJson(e.getMessage());
            }
        });
    }



    @Authenticated
    @Authorized.Read
    public Result get(String code) {
        return globalExceptionHandler(() -> {
            DatatableForm form = objectFromRequestQueryString(DatatableForm.class);
            V obj = api().getObject(code, generateBasicDBObjectFromKeys(form));
            if (obj == null) {
                return notFound();
            } 
            return okAsJson(obj);
        });	
    }


    /**
     * These method defines the specific creation behavior for each resource. 
     * {@link NGLAPIController#save()} wraps the call of this method. <br>
     * We do not check here if form has errors because the API validates data. 
     * 
     * @return V the DBObject created
     * @throws APIException exceptions during creation
     */
    public abstract V saveImpl() throws APIException ;

    @Authenticated
    @Authorized.Write
    @BodyParser.Of(value = IGBodyParsers.Json15MB.class)
    public Result save() {
    	//Logger.debug("QQQQQQQQQQQQQQQQQQQQ                                         Dans save de NGLAPIController");
        return globalExceptionHandler(() -> {
            try {
                V object = saveImpl();
                return okAsJson(object);
            } catch (APIValidationException e) {
                return badRequestLoggingForValidationException(e);
            } catch (APISemanticException e) {
                if (getLogger().isDebugEnabled()) getLogger().warn(e.getMessage(), e);
                return badRequestAsJson("use PUT method to update");
            } catch (APIException e) {
                getLogger().error(e.getMessage(), e);
                return badRequestAsJson(e.getMessage());
            }
        });
    }

//    /**
//     * Log exception and return a bad request response using {@link #badRequestAsJson(Object)}
//     * @param e validation exception
//     * @return bad request response in json format
//     */
//    protected Result badRequestLoggingForValidationException(APIValidationException e) {
//        if (getLogger().isDebugEnabled()) getLogger().warn(e.getMessage(), e);
//        if (e.getErrors() != null) {
//            return badRequestAsJson(errorsAsJson(e.getErrors()));
//        } else {
//            return badRequestAsJson(e.getMessage());
//        }
//    }

    /**
     * These method defines the specific update behavior for each resource. 
     * {@link #update(String)} wraps the call of this method. <br>
     * We do not check here if form has errors because the API validates data. 
     * 
     * @param code String
     * @return     V the DBObject created
     * @throws Exception              global exception
     * @throws APIException           exception from API
     * @throws APIValidationException exception from API
     */
    public abstract V updateImpl(String code) throws Exception, APIException, APIValidationException;

    @Authenticated
    @Authorized.Write
    @BodyParser.Of(value = IGBodyParsers.Json15MB.class)
    public Result update(String code) {
        try {
        	Logger.debug("Update controller "+code);
            V object = updateImpl(code);
            return okAsJson(object);
        } catch (APIValidationException e) {
            return badRequestLoggingForValidationException(e);
        } catch (APIException e) {
            getLogger().error(e.getMessage(), e);
            return badRequestAsJson(e.getMessage());
        } catch (Exception e) {
            getLogger().error(e.getMessage(), e);
            return nglGlobalBadRequest(e.getMessage());
        }
    }

    public Map<String, List<ValidationError>> mapErrors(List<ValidationError> formErrors) {
        Map<String, List<ValidationError>> map = new TreeMap<>(); 
        formErrors.forEach(ve -> {
            if(map.containsKey(ve.key())) {
                map.get(ve.key()).add(ve);
            } else {
                map.put(ve.key(), Arrays.asList(ve));
            }
        });
        return map;
    }

    @Authenticated
    @Authorized.Write
    public Result delete(String code){ 
        return globalExceptionHandler(() -> {
            try {
                api().delete(code);
                return ok();
            } catch (APIValidationException e) {
                return badRequestLoggingForValidationException(e);
            } catch (APIException e) {
                getLogger().error(e.getMessage(), e);
                return badRequestAsJson(e.getMessage());
            } 
        });
    }
}
