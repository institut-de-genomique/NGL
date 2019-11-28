package fr.cea.ig.ngl.dao.runs;

import java.util.Collection;
import java.util.Map;

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
import models.laboratory.run.instance.Run;
import models.laboratory.run.instance.Treatment;
import validation.ContextValidation;

public class TreatmentsAPI extends SubDocumentGenericAPI<Treatment, RunsDAO, Run> {

    private static final play.Logger.ALogger logger = play.Logger.of(TreatmentsAPI.class);

    @Inject
    public TreatmentsAPI(RunsDAO dao) {
        super(dao);
    }

    public Run get(String code, String treatmentCode) {
        return dao.findOne(DBQuery.and(DBQuery.is("code", code), 
                                       DBQuery.exists("treatments."+treatmentCode)));
    }

    @Override
    public Collection<Treatment> getSubObjects(Run objectInDB) {
        return objectInDB.treatments.values();
    }

    @Override
    public Treatment getSubObject(Run objectInDB, String code) {        
        return objectInDB.treatments.get(code);
    }

    @Override
    public Iterable<Run> listObjects(String parentCode, Query query) {
        return dao.find(query);
    }

    public Iterable<Run> listObjects(Query query) {
        return listObjects(null, query);
    }

    /**
     * @param runCode code of run
     * @return treatment of the run instance
     * @throws APIException if no run is found in the database
     */
    public Map<String, Treatment> list(String runCode) throws APIException {
        Run run = dao.getObject(DBQuery.is("code", runCode));
        if (run == null) {
            throw new APIException("Run with code " + runCode + " not exist");
        } else {
            return run.treatments;
        }
    }

    @Override
    public Treatment save(Run objectInDB, Treatment input, String currentUser) throws APIValidationException {        
        ContextValidation ctxVal = ContextValidation.createCreationContext(currentUser); 
        ctxVal.putObject("level", Level.CODE.Run);
        ctxVal.putObject("run", objectInDB);
//        input.validate(ctxVal);
        input.validate(ctxVal, objectInDB);
        if (! ctxVal.hasErrors()) {
            TraceInformation ti = objectInDB.traceInformation;
            if(ti != null){
                ti.modificationStamp(ctxVal, currentUser);
            } else{
                logger.error("traceInformation is null !!");
            }
            dao.updateObject(DBQuery.is("code", objectInDB.code),
                             DBUpdate.set("treatments."+input.code, input).set("traceInformation", ti));
            return getSubObject(get(objectInDB.code, input.code), input.code);
        } else {
            throw new APIValidationException(GenericAPI.INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
        }
    }

    @Override
    public Treatment update(Run objectInDB, Treatment input, String currentUser) throws APIValidationException {
        ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
        ctxVal.putObject("level", Level.CODE.Run);
        ctxVal.putObject("run", objectInDB);
//        input.validate(ctxVal);
        input.validate(ctxVal, objectInDB);
        if (! ctxVal.hasErrors()) {
            TraceInformation ti = objectInDB.traceInformation;
            if(ti != null){
                ti.modificationStamp(ctxVal, currentUser);
            } else{
                logger.error("traceInformation is null !!");
            }
            dao.updateObject(DBQuery.is("code", objectInDB.code),
                             DBUpdate.set("treatments."+input.code, input).set("traceInformation", ti));     
            return getSubObject(get(objectInDB.code, input.code), input.code);  
        } else {
            throw new APIValidationException(GenericAPI.INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
        }
    }

    @Override
    public void delete(Run objectInDB, String code, String currentUser) throws APIException {
        if (getSubObject(objectInDB, code) == null) {
            throw new APIException("Treatment to delete not found: " + code);
        } else {
            TraceInformation ti = objectInDB.traceInformation;
            if(ti != null){
                ti.modificationStamp(null, currentUser);
            } else{
                logger.error("traceInformation is null !!");
            }
            dao.update(DBQuery.is("code", objectInDB.code), 
                       DBUpdate.unset("treatments."+code).set("traceInformation", ti));
        }
    }

    public boolean checkObjectExist(String runCode, String treatmentCode) {
        return dao.checkObjectExist(DBQuery.and(DBQuery.is("code", runCode), 
                                                DBQuery.exists("treatments."+treatmentCode)));
    }

}
