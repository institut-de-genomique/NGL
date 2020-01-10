package fr.cea.ig.ngl.dao.readsets;

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
import models.laboratory.run.instance.File;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import validation.ContextValidation;
import validation.run.instance.FileValidationHelper;

public class FilesAPI extends SubDocumentGenericAPI<File, FilesDao, ReadSet> {

    private static final play.Logger.ALogger logger = play.Logger.of(FilesAPI.class);

    private static final List<String> authorizedUpdateFields = Arrays.asList("fullname");
    
    @Inject
    public FilesAPI(FilesDao dao) {
        super(dao);
    }

    @Override
    public Collection<File> getSubObjects(ReadSet objectInDB) {
        return objectInDB.files;
    }

    @Override
    public File getSubObject(ReadSet objectInDB, String fullname) {
        for (File file : objectInDB.files) {
            if (file != null) { // see NGL-2222
                if (file.fullname != null) { // see NGL-2222
                    if (file.fullname.equals(fullname)) return file;
                } else{
                    logger.error("NGL-2222 Résolution Erreur File Null PointerException : file.fullname null "+objectInDB.code);
                }
            } else{
                logger.error("NGL-2222 Résolution Erreur File Null PointerException : file null "+objectInDB.code);
            }
        }
        return null;
    }

    @Override
    public Iterable<ReadSet> listObjects(String parentCode, Query query) {
        logger.warn("not implemented");
        return null;
    }

    @Override
    public File save(ReadSet objectInDB, File input, String currentUser) throws APIValidationException {
        ContextValidation ctxVal = ContextValidation.createCreationContext(currentUser);
        ctxVal.putObject("readSet",     objectInDB);
        ctxVal.putObject("objectClass", objectInDB.getClass());
//        input.validate(ctxVal);
        input.validate(ctxVal, objectInDB);
        if (!ctxVal.hasErrors()) {
            TraceInformation ti = objectInDB.traceInformation;
            ti.modificationStamp(ctxVal, currentUser);
            dao.update(DBQuery.is("code", objectInDB.code),
                       DBUpdate.push("files", input).set("traceInformation", ti)); 
            return getSubObject(dao.getObject(objectInDB.code), input.fullname);
        } else {
            throw new APIValidationException(GenericAPI.INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
        }
    }

    @Override
    public File update(ReadSet objectInDB, File input, String currentUser) throws APIValidationException {
        ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
        ctxVal.putObject("readSet", objectInDB);
        ctxVal.putObject("objectClass", objectInDB.getClass());
//        input.validate(ctxVal);
        input.validate(ctxVal, objectInDB);
        if (!ctxVal.hasErrors()) {
            TraceInformation ti = objectInDB.traceInformation;
            ti.modificationStamp(ctxVal, currentUser);
            dao.update(DBQuery.and(DBQuery.is("code", objectInDB.code), 
                                   DBQuery.is("files.fullname", input.fullname)),
                       DBUpdate.set("files.$", input).set("traceInformation", ti)); 
            return getSubObject(dao.getObject(objectInDB.code), input.fullname);
        } else {
            throw new APIValidationException(GenericAPI.INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
        }
    }
    
    public File update(ReadSet objectInDB, File input, String currentUser, List<String> fields, String olderFullname) throws APIValidationException {
        ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser); 
        ctxVal.putObject("readSet", objectInDB);
        ctxVal.putObject("objectClass", objectInDB.getClass());
        checkAuthorizedUpdateFields(ctxVal, fields);
        checkIfFieldsAreDefined(ctxVal, fields, input);
        if(fields.contains("fullname")){
            ctxVal.setCreationMode();
//            FileValidationHelper.validateFileFullName(input.fullname, ctxVal);
            FileValidationHelper.validateFileFullName(ctxVal, input.fullname, objectInDB);
        }
        if (!ctxVal.hasErrors()) {
            TraceInformation ti = objectInDB.traceInformation;
            ti.modificationStamp(ctxVal, currentUser);
            dao.update(DBQuery.and(DBQuery.is("code",           objectInDB.code), 
                                   DBQuery.is("files.fullname", olderFullname)),
                       dao.getBuilder(input, fields, "files.$").set("traceInformation", ti)); 

            if (fields.contains("fullname") && input.fullname != null) {
                olderFullname = input.fullname;
            }
            return getSubObject(dao.getObject(objectInDB.code), olderFullname);
        } else {
            throw new APIValidationException(GenericAPI.INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
        }
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
    
    @Override
    public void delete(ReadSet objectInDB, String fullname, String currentUser) throws APIException {
        // EJACOBY Doit marcher {$pull : {"files" : {"fullname" : {$regex : "trim"}}}}
        TraceInformation ti = objectInDB.traceInformation;
        ti.modificationStamp(ContextValidation.createUndefinedContext(currentUser), currentUser);
        dao.update(DBQuery.and(DBQuery.is("code",           objectInDB.code), 
                               DBQuery.is("files.fullname", fullname)), 
                   DBUpdate.unset("files.$").set("traceInformation", ti));
        dao.update(DBQuery.is("code",objectInDB.code), DBUpdate.pull("files", null));
    }

    public boolean checkObjectExist(String readsetCode, String fullname) {
        return dao.checkObjectExist(DBQuery.and(DBQuery.is("code",           readsetCode), 
                                                DBQuery.is("files.fullname", fullname)));
    }

    public void deleteByReadSetCode(ReadSet objectInDB) {
        dao.update(DBQuery.is("code", objectInDB.code), DBUpdate.unset("files"));
    }

    public void deleteByRunCode(Run run) {
        dao.update(DBQuery.and(DBQuery.is("runCode", run.code)), DBUpdate.unset("files"));
    }

}
