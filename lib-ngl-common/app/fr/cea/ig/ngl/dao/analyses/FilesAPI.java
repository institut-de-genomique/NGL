package fr.cea.ig.ngl.dao.analyses;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;

import javax.inject.Inject;

import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import org.mongojack.DBUpdate;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import fr.cea.ig.ngl.dao.GenericMongoDAO;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.GenericAPI;
import fr.cea.ig.ngl.dao.api.SubDocumentGenericAPI;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.run.instance.Analysis;
import models.laboratory.run.instance.File;
import validation.ContextValidation;
import validation.run.instance.FileValidationHelper;

public class FilesAPI extends SubDocumentGenericAPI<File, FilesDao, Analysis> {
    
    private static final play.Logger.ALogger logger = play.Logger.of(FilesAPI.class);
    private final static List<String> authorizedUpdateFields = Arrays.asList("fullname");
    
    @Inject
    public FilesAPI(FilesDao dao) {
        super(dao);
    }

    public boolean checkObjectExist(String parentCode, String fullname) {
        return dao.checkObjectExist(DBQuery.and(DBQuery.is("code", parentCode), 
                                                DBQuery.is("files.fullname", fullname)));
    }


    @Override
    public Collection<File> getSubObjects(Analysis objectInDB) {
        return objectInDB.files;
    }

    @Override
    public File getSubObject(Analysis objectInDB, String fullname) {
        for (File file : objectInDB.files) {
            if (file.fullname.equals(fullname)) {
                return file;
            }
        }
        return null;
    }

    @Override
    public Iterable<Analysis> listObjects(String parentCode, Query query) {
        return dao.find(query);
    }

    @Override
    public File save(Analysis objectInDB, File input, String currentUser) throws APIValidationException {
        ContextValidation ctxVal = ContextValidation.createCreationContext(currentUser);
        ctxVal.putObject("analysis", objectInDB);
        ctxVal.putObject("objectClass", objectInDB.getClass());
        input.validate(ctxVal, objectInDB);

        if (!ctxVal.hasErrors()) {
            TraceInformation ti = objectInDB.traceInformation;
            ti.modificationStamp(ctxVal, currentUser);
            dao.updateObject(DBQuery.is("code", objectInDB.code), 
                             DBUpdate.push("files", input).set("traceInformation", ti));
            return getSubObject(dao.getObject(objectInDB.code), input.fullname);
        } else {
            throw new APIValidationException(GenericAPI.INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
        }
    }

    @Override
    public File update(Analysis objectInDB, File fileInput, String currentUser) throws APIValidationException {
        ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
        ctxVal.putObject("analysis", objectInDB);
        ctxVal.putObject("objectClass", objectInDB.getClass());
        fileInput.validate(ctxVal, objectInDB);
        if (!ctxVal.hasErrors()) {
            TraceInformation ti = objectInDB.traceInformation;
            ti.modificationStamp(ctxVal, currentUser);
            dao.updateObject(getSubObjectQuery(objectInDB.code, fileInput.fullname), 
                             DBUpdate.set("files.$", fileInput).set("traceInformation", ti));
            return getSubObject(dao.getObject(objectInDB.code), fileInput.fullname);
        } else {
            throw new APIValidationException(GenericAPI.INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
        }
    }

    public File update(Analysis objectInDB, File fileInput, String currentUser, List<String> fields, String olderFullname) throws APIValidationException {
        ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser); 
        ctxVal.putObject("analysis", objectInDB);
        ctxVal.putObject("objectClass", objectInDB.getClass());
        checkAuthorizedUpdateFields(ctxVal, fields);
        checkIfFieldsAreDefined(ctxVal, fields, fileInput);
        if(fields.contains("fullname")){
            ctxVal.setCreationMode();
//            FileValidationHelper.validateFileFullName(fileInput.fullname, ctxVal);
            FileValidationHelper.validateFileFullName(ctxVal, fileInput.fullname, objectInDB);
        }
        if (!ctxVal.hasErrors()) {
            TraceInformation ti = objectInDB.traceInformation;
            ti.modificationStamp(ctxVal, currentUser);
            dao.updateObject(getSubObjectQuery(objectInDB.code, fileInput.fullname), 
                       dao.getBuilder(fileInput, fields, "files.$").set("traceInformation", ti)); 

            if (fields.contains("fullname") && fileInput.fullname != null) {
                olderFullname = fileInput.fullname;
            }
            return getSubObject(dao.getObject(objectInDB.code), olderFullname);
        } else {
            throw new APIValidationException(GenericAPI.INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
        }
    }


    @Override
    public void delete(Analysis objectInDB, String fullname, String currentUser) throws APIException {
        TraceInformation ti = objectInDB.traceInformation;
        ti.setTraceInformation(currentUser);
        dao.updateObject(getSubObjectQuery(objectInDB.code, fullname), 
                         DBUpdate.unset("files.$").set("traceInformation", ti));
        dao.updateObject(DBQuery.is("code", objectInDB.code),
                         DBUpdate.pull("files", null)); 
    }

    public void delete(Analysis objectInDB, String currentUser) {
        TraceInformation ti = objectInDB.traceInformation;
        ti.setTraceInformation(currentUser);
        dao.updateObject(DBQuery.is("code", objectInDB.code), 
                         DBUpdate.unset("files").set("traceInformation", ti));       
    }

    private Query getSubObjectQuery(String parentCode, String fullname) {
        return DBQuery.and(DBQuery.is("code",           parentCode), 
                           DBQuery.is("files.fullname", fullname));
    }
    
    private void checkAuthorizedUpdateFields(ContextValidation ctxVal, List<String> fields) {
        for (String field: fields) {
            if (!authorizedUpdateFields.contains(field)) {
                ctxVal.addError("fields", "error.valuenotauthorized", field);   
            }
        }
    }
    
    private void checkIfFieldsAreDefined(ContextValidation ctxVal, List<String> fields, File file) {
        try {
            for (String field: fields) {
                Matcher matcher = GenericMongoDAO.FIELD_PATTERN.matcher(field);
                if(matcher.matches()){
                    String nPrefix = matcher.group(1);
                    String nField = matcher.group(2);
                    BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(file);
                    Object o = wrapper.getPropertyValue(nPrefix);
                    if (wrapper.getPropertyType(nPrefix).getField(nField).get(o) == null) {
                        ctxVal.addError(field, "error.notdefined");
                    }
                } else {
                    if (file.getClass().getField(field).get(file) == null) {
                        ctxVal.addError(field, "error.notdefined");
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
