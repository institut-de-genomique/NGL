package fr.cea.ig.ngl.dao.runs;

import java.util.Map;

import javax.inject.Inject;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import com.mongodb.BasicDBObject;

import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.GenericAPI;
import fr.cea.ig.ngl.dao.readsets.ReadSetsDAO;
import models.laboratory.common.description.Level;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.run.instance.Run;
import models.laboratory.run.instance.Treatment;
import validation.ContextValidation;

public class LaneTreatmentsAPI extends LanesAPI {
    private static final play.Logger.ALogger logger = play.Logger.of(LaneTreatmentsAPI.class);                  

    @Inject
    public LaneTreatmentsAPI(RunsDAO dao, ReadSetsDAO readSetDao) {
        super(dao, readSetDao);
    }

    /**
     * List all treatments of a lane in a run
     * @param runCode    code of Run
     * @param laneNumber number of lane
     * @return           all treatments of the lane or null if no Run has been found
     * @throws APIException if no lane corresponds to the given number 
     */
    public Map<String, Treatment> list(String runCode, Integer laneNumber) throws APIException {
        Run run  = dao.findOne(DBQuery.and(DBQuery.is("code", runCode), 
                                           DBQuery.elemMatch("lanes", DBQuery.is("number", laneNumber))));
        if (run != null) {
            return get(run, laneNumber).treatments;
        } else {
            return null;
        } 
    }

    /**
     * Get a treatment from a lane
     * @param run           code of Run
     * @param laneNumber    number of lane
     * @param treatmentCode code of treatment
     * @return              the treatment of the lane or null if no Run has been found
     * @throws APIException if no lane corresponds to the given number 
     */
    public Treatment get(Run run, Integer laneNumber, String treatmentCode) throws APIException {
        return get(run, laneNumber).treatments.get(treatmentCode);
    }


    public boolean checkObjectExist(String runCode, Integer laneNumber, String treatmentCode) {
        return dao.checkObjectExist(DBQuery.and(DBQuery.is("code", runCode), 
                                                DBQuery.elemMatch("lanes", DBQuery.and(DBQuery.is("number", laneNumber),
                                                                                       DBQuery.exists("treatments."+treatmentCode)))));
    }
    
    public Run getRun(String runCode, Integer laneNumber, String treatmentCode) {
        return dao.getObject(DBQuery.and(DBQuery.is("code", runCode), 
                                                DBQuery.elemMatch("lanes", DBQuery.and(DBQuery.is("number", laneNumber),
                                                                                       DBQuery.exists("treatments."+treatmentCode)))));
    }

    public Treatment save(Run run, Treatment input, Integer laneNumber, String currentUser) throws APIValidationException, APIException {
        ContextValidation ctxVal = ContextValidation.createCreationContext(currentUser);
        ctxVal.putObject("level", Level.CODE.Lane);
        ctxVal.putObject("run", run);
        ctxVal.putObject("lane", get(run, laneNumber));
//        input.validate(ctxVal);
        input.validate(ctxVal, run, get(run, laneNumber));

        if (!ctxVal.hasErrors()) {
            TraceInformation ti = run.traceInformation;
            if(ti != null){
                ti.modificationStamp(ctxVal, currentUser);
            } else{
                logger.error("traceInformation is null !!");
            }
            dao.updateObject(DBQuery.and(DBQuery.is("code", run.code), 
                                         DBQuery.is("lanes.number", laneNumber)),
                             DBUpdate.set("lanes.$.treatments."+input.code, input).set("traceInformation", ti)); 
            BasicDBObject keys = new BasicDBObject();
            keys.put("lanes", 1);
            return get(dao.getObject(run.code, keys), laneNumber, input.code);
        } else {
            throw new APIValidationException(GenericAPI.INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
        }
    }

    public Treatment update(Run run, Integer laneNumber, Treatment input, String currentUser) throws APIValidationException, APIException { 
        ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser); 
        ctxVal.putObject("level", Level.CODE.Lane);
        ctxVal.putObject("run", run);
        ctxVal.putObject("lane", get(run, laneNumber));
//        input.validate(ctxVal);
        input.validate(ctxVal, run, get(run, laneNumber));
        if (!ctxVal.hasErrors()) {
            TraceInformation ti = run.traceInformation;
            if(ti != null){
                ti.modificationStamp(ctxVal, currentUser);
            } else{
                logger.error("traceInformation is null !!");
            }
            dao.updateObject(DBQuery.and(DBQuery.is("code", run.code), 
                                         DBQuery.is("lanes.number", laneNumber)),
                             DBUpdate.set("lanes.$.treatments."+input.code, input).set("traceInformation", ti));
            return input;
        } else {
            throw new APIValidationException(GenericAPI.INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
        }

    }

    /**
     * Delete a treatment of specific lane into a run 
     * @param run           the run instance from database
     * @param laneNumber    number of lane
     * @param treatmentCode code of treatment of lane
     * @param currentUser   current user
     * @throws APIException no treatment corresponds to the given arguments
     */
    public void delete(Run run,  Integer laneNumber, String treatmentCode, String currentUser) throws APIException {
        Treatment treatment  = get(run, laneNumber, treatmentCode);
        if(treatment == null) {
            throw new APIException("Treatment with code " + treatmentCode + " not exist in Lane number " + laneNumber + "in Run " + run.code);
        } else {
            TraceInformation ti = run.traceInformation;
            if(ti != null){
                ti.modificationStamp(ContextValidation.createUndefinedContext(currentUser), currentUser);
            } else{
                logger.error("traceInformation is null !!");
            }
            dao.updateObject(DBQuery.and(DBQuery.is("code", run.code), 
                                         DBQuery.is("lanes.number", laneNumber)),
                             DBUpdate.unset("lanes.$.treatments." + treatmentCode).set("traceInformation", ti));
        }
    }   


}
