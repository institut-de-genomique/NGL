package fr.cea.ig.ngl.dao.readsets;

import java.util.Collection;

import javax.inject.Inject;

import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import org.mongojack.DBUpdate;

import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.GenericAPI;
import fr.cea.ig.ngl.dao.api.SubDocumentGenericAPI;
import models.laboratory.common.description.Level;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Treatment;
import validation.ContextValidation;

public class ReadSetTreatmentsAPI extends SubDocumentGenericAPI<Treatment, ReadSetsDAO, ReadSet> {

    @Inject
    public ReadSetTreatmentsAPI(ReadSetsDAO dao) {
        super(dao);
    }

    @Override
    public Collection<Treatment> getSubObjects(ReadSet objectInDB) {
        return objectInDB.treatments.values();
    }

    @Override
    public Treatment getSubObject(ReadSet objectInDB, String code) {
        return objectInDB.treatments.get(code);
    }

    @Override
    public Iterable<ReadSet> listObjects(String parentCode, Query query) {
        return dao.find(query);
    }

    @Override
    public Treatment save(ReadSet objectInDB, Treatment input, String currentUser) throws APIValidationException {
        ContextValidation ctxVal = ContextValidation.createCreationContext(currentUser);
        ctxVal.putObject("level", Level.CODE.ReadSet);
        ctxVal.putObject("readSet", objectInDB);
//        input.validate(ctxVal);
        input.validate(ctxVal, objectInDB);
        if (!ctxVal.hasErrors()) {
            TraceInformation ti = objectInDB.traceInformation;
            ti.modificationStamp(ctxVal, currentUser);
            dao.update(DBQuery.is("code", objectInDB.code),
                       DBUpdate.set("treatments." + input.code, input)
                               .set("traceInformation",         ti));
            return getSubObject(dao.getObject(objectInDB.code), input.code);
        } else {
            throw new APIValidationException(GenericAPI.INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
        }
    }

    @Override
    public Treatment update(ReadSet objectInDB, Treatment input, String currentUser) throws APIValidationException {
        ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
        ctxVal.putObject("level", Level.CODE.ReadSet);
        ctxVal.putObject("readSet", objectInDB);
//        input.validate(ctxVal);
        input.validate(ctxVal, objectInDB);
        if (!ctxVal.hasErrors()) {
            TraceInformation ti = objectInDB.traceInformation;
            ti.modificationStamp(ctxVal, currentUser);
            dao.update(DBQuery.is("code", objectInDB.code),
                       DBUpdate.set("treatments."+input.code, input)
                               .set("traceInformation",       ti));
            return getSubObject(dao.getObject(objectInDB.code), input.code);
        } else {
            throw new APIValidationException(GenericAPI.INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
        }
    }

    @Override
    public void delete(ReadSet objectInDB, String code, String currentUser) throws APIException {
        TraceInformation ti = objectInDB.traceInformation;
        ti.setTraceInformation(currentUser);
        dao.update(DBQuery.is("code", objectInDB.code), 
                   DBUpdate.unset("treatments."+code).set("traceInformation", ti));        
    }

    public boolean checkObjectExist(String readSetCode, String treatmentCode) {
        return dao.checkObjectExist(DBQuery.and(DBQuery.is("code", readSetCode), 
                                                DBQuery.exists("treatments."+treatmentCode)));
    }

    
    public void deleteAll(ReadSet objectInDB, String currentUser) {
        TraceInformation ti = objectInDB.traceInformation;
        ti.setTraceInformation(currentUser);
        dao.update(DBQuery.is("code", objectInDB.code), 
                   DBUpdate.unset("treatments")
                           .set("traceInformation", ti));
    }

}
