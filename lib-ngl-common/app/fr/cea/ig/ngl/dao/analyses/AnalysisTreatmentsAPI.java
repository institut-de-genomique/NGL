package fr.cea.ig.ngl.dao.analyses;

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
import models.laboratory.run.instance.Analysis;
import models.laboratory.run.instance.Treatment;
import validation.ContextValidation;

public class AnalysisTreatmentsAPI extends SubDocumentGenericAPI<Treatment, AnalysesDAO, Analysis> {

    @Inject
    public AnalysisTreatmentsAPI(AnalysesDAO dao) {
        super(dao);
    }

    public boolean checkObjectExist(String analysisCode, String treatmentCode) {
        return dao.checkObjectExist(DBQuery.and(DBQuery.is("code", analysisCode), 
                                                DBQuery.exists("treatments."+treatmentCode)));
    }
    
    @Override
    public Collection<Treatment> getSubObjects(Analysis objectInDB) {
        return objectInDB.treatments.values();
    }

    @Override
    public Treatment getSubObject(Analysis objectInDB, String code) {
        return objectInDB.treatments.get(code);
    }

    @Override
    public Iterable<Analysis> listObjects(String parentCode, Query query) {
        return dao.find(query);
    }

    @Override
    public Treatment save(Analysis objectInDB, Treatment input, String currentUser) throws APIValidationException {
        ContextValidation ctxVal = ContextValidation.createCreationContext(currentUser);
        ctxVal.putObject("level", Level.CODE.Analysis);
        ctxVal.putObject("analysis", objectInDB);
        input.validate(ctxVal, objectInDB);
        if (!ctxVal.hasErrors()) {
            TraceInformation ti = objectInDB.traceInformation;
            ti.modificationStamp(ctxVal, currentUser);
            dao.updateObject(DBQuery.is("code", objectInDB.code), 
                             DBUpdate.set("treatments."+input.code, input).set("traceInformation", ti));
            return getSubObject(dao.getObject(objectInDB.code), input.code);
        } else {
            throw new APIValidationException(GenericAPI.INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
        }
    }

    @Override
    public Treatment update(Analysis objectInDB, Treatment input, String currentUser) throws APIValidationException {
        ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser); 
        ctxVal.putObject("level", Level.CODE.Analysis);
        ctxVal.putObject("analysis", objectInDB);
        input.validate(ctxVal, objectInDB);
        if (!ctxVal.hasErrors()) { 
            TraceInformation ti = objectInDB.traceInformation;
            ti.modificationStamp(ctxVal, currentUser);
            dao.updateObject(DBQuery.is("code", objectInDB.code), 
                             DBUpdate.set("treatments."+input.code, input).set("traceInformation", ti));
            return getSubObject(dao.getObject(objectInDB.code), input.code);
        } else {
            throw new APIValidationException(GenericAPI.INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
        }
    }

    @Override
    public void delete(Analysis objectInDB, String code, String currentUser) throws APIException {
        TraceInformation ti = objectInDB.traceInformation;
        ti.setTraceInformation(currentUser);
        dao.updateObject(DBQuery.is("code", objectInDB.code), 
                DBUpdate.unset("treatments."+code).set("traceInformation", ti));
    }
    
    public void deleteAll(Analysis objectInDB, String currentUser) throws APIException {
        TraceInformation ti = objectInDB.traceInformation;
        ti.setTraceInformation(currentUser);
        dao.updateObject(DBQuery.is("code", objectInDB.code), 
                DBUpdate.unset("treatments").set("traceInformation", ti));
    }     
}
