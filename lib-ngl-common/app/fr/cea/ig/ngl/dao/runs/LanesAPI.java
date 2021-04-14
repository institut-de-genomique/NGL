package fr.cea.ig.ngl.dao.runs;

import java.util.Collection;
import java.util.Date;

import javax.inject.Inject;

import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import org.mongojack.DBUpdate;

import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.GenericAPI;
import fr.cea.ig.ngl.dao.api.SubDocumentGenericAPI;
import fr.cea.ig.ngl.dao.readsets.ReadSetsDAO;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.Valuation;
import models.laboratory.run.instance.Lane;
import models.laboratory.run.instance.Run;
import models.utils.dao.DAOException;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;

public class LanesAPI extends SubDocumentGenericAPI<Lane, RunsDAO, Run> {

    private static final play.Logger.ALogger logger = play.Logger.of(LanesAPI.class);

    private final ReadSetsDAO readSetDao;

    @Inject
    public LanesAPI(RunsDAO dao, ReadSetsDAO readSetDao) {
        super(dao);
        this.readSetDao = readSetDao;
    }

    @Override
    public Collection<Lane> getSubObjects(Run objectInDB) {
        return objectInDB.lanes;
    }

    public Run getRun(String code, Integer laneNumber) throws APIException {
        try {
            Run run = dao.findOne(DBQuery.and(DBQuery.is("code", code), DBQuery.is("lanes.number", laneNumber)));
            return run;
        } catch (DAOException e) {
            logger.warn(e.getMessage());
            throw new APIException(e.getMessage(), e);
        }
    }

    /**
     * The run instance is retrieve from the database before getting the lane object. 
     * @param code       code of Run
     * @param laneNumber number of lane
     * @return           lane object
     * @throws APIException exception raised if no lane corresponds to the number of lane
     *                      or if no run is found
     */
    public Lane get(String code, Integer laneNumber) throws APIException {
        Run run = getRun(code, laneNumber);
        if (run == null)
            throw new APIException("Run with code " + code + " not exist with lane " + laneNumber);
        return get(run, laneNumber);
    }

    /**
     * @param objectInDB    run instance from database
     * @param laneNumber    number of lane
     * @return              lane object
     * @throws APIException exception raised if no lane corresponds to the number of lane
     */
    public Lane get(Run objectInDB, Integer laneNumber) throws APIException {
        String message = "Lane does not exist " + objectInDB.code + " / " + laneNumber;
        if (objectInDB.lanes == null) 
            throw new APIException(message);
        for (Lane l : objectInDB.lanes) 
        	if (l.number.equals(laneNumber))
        		return l;
        throw new APIException(message);
    }

    /*
     * Prefer use {@link #getRun(Run, String)} instead. <br>
     * {@link #getSubObject(Run, String)} calls {@link #get(Run, Integer)} 
     * and handles exceptions by logging them and return null pointer.
     * @param objectInDB object instance from db
     * @param code       the lane number
     * @return           the lane or null if not found
     */
    @Override
    public Lane getSubObject(Run objectInDB, String code) {
        try {
            return get(objectInDB, new Integer(code));
        } catch (APIException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Iterable<Run> listObjects(String parentCode, Query query) {
        return dao.findAsList(query);
    }

    @Override
    public Lane save(Run objectInDB, Lane input, String currentUser) throws APIValidationException, APIException {
        ContextValidation ctxVal = ContextValidation.createCreationContext(currentUser);
        ctxVal.putObject("run", objectInDB);
//        input.validate(ctxVal);
        input.validate(ctxVal, objectInDB);
        if (ctxVal.hasErrors())
            throw new APIValidationException(GenericAPI.INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
        TraceInformation ti = objectInDB.traceInformation;
        if (ti != null) {
        	ti.modificationStamp(ctxVal, currentUser);
        } else {
        	logger.error("traceInformation is null !!");
        }
        dao.updateObject(DBQuery.and(DBQuery.is("code", objectInDB.code)), DBUpdate.push("lanes", input).set("traceInformation", ti));
        return get(dao.findByCode(objectInDB.code), input.number);  
    }

    @Override
    public Lane update(Run objectInDB, Lane input, String currentUser) throws APIValidationException, APIException {
        ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
        ctxVal.putObject("run", objectInDB);
//        input.validate(ctxVal);
        input.validate(ctxVal, objectInDB);
        if (ctxVal.hasErrors()) 
            throw new APIValidationException(GenericAPI.INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());        	
        TraceInformation ti = objectInDB.traceInformation;
        if (ti != null) {
        	ti.modificationStamp(null, currentUser);
        } else {
        	logger.error("traceInformation is null !!");
        }
        dao.update(DBQuery.and(DBQuery.is("code", objectInDB.code), 
        		               DBQuery.is("lanes.number", input.number)),
        		DBUpdate.set("lanes.$", input).set("traceInformation", ti)); 
        return get(objectInDB.code, input.number);
    }

    @Override
    public void delete(Run objectInDB, String laneNumber, String currentUser) throws APIException {
        delete(objectInDB, new Integer(laneNumber), currentUser);
    }

    public void delete(Run objectInDB, Integer laneNumber, String currentUser) {
        dao.updateObject(DBQuery.and(DBQuery.is("code",         objectInDB.code),
                                     DBQuery.is("lanes.number", laneNumber)), 
                         DBUpdate.unset("lanes.$"));
        TraceInformation ti = objectInDB.traceInformation;
        if (ti != null) {
            ti.modificationStamp(null, currentUser);
        } else {
            logger.error("traceInformation is null !!");
        }
        dao.updateObject(DBQuery.is("code",objectInDB.code), 
                         DBUpdate.pull("lanes", null).set("traceInformation", ti));
        readSetDao.deleteObject(DBQuery.and(DBQuery.is("runCode",    objectInDB.code), 
                                            DBQuery.is("laneNumber", laneNumber)));
    }

    public void delete(String runCode, Integer laneNumber, String currentUser) throws APIException {
        delete(getRun(runCode, laneNumber), laneNumber, currentUser);
    }

    /**
     * Delete all lanes in the run and all readsets linked to the run
     * @param objectInDB    run instance
     * @param currentUser   current user
     */
    public void deleteAllLanes(Run objectInDB, String currentUser) {
        TraceInformation ti = objectInDB.traceInformation;
        if (ti != null) {
            ti.modificationStamp(null, currentUser);
        } else {
            logger.error("traceInformation is null !!");
        }
        dao.updateObject(DBQuery.is("code", objectInDB.code), 
                         DBUpdate.unset("lanes").set("traceInformation", ti));
        readSetDao.deleteObject(DBQuery.and(DBQuery.is("runCode", objectInDB.code)));
    }

    public Lane valuation(Run objectInDB, Integer laneNumber, Valuation valuation, String currentUser) throws APIValidationException, APIException {
        ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
        ctxVal.putObject("run", objectInDB);
        manageValidation(objectInDB, laneNumber, valuation, ctxVal);
        if (ctxVal.hasErrors()) 
            throw new APIValidationException(GenericAPI.INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
        TraceInformation ti = objectInDB.traceInformation;
        if (ti != null) {
        	ti.modificationStamp(ctxVal, currentUser);
        } else {
        	logger.error("traceInformation is null !!");
        }
        dao.updateObject(DBQuery.and(DBQuery.is("code", objectInDB.code), 
        		                     DBQuery.is("lanes.number", laneNumber)),
        				 DBUpdate.set("lanes.$.valuation", valuation)
        				         .set("traceInformation", ti)); 
        return get(objectInDB.code, laneNumber);
    }
    
    private void manageValidation(Run run, Integer laneNumber, Valuation valuation, ContextValidation ctxVal) {
    	Lane lane = run.lanes.get(laneNumber - 1);
        if (lane.valuation.valid == null ? valuation.valid != null : !lane.valuation.valid.equals(valuation.valid)) {
        	valuation.date = new Date();
        	valuation.user = ctxVal.getUser();
            CommonValidationHelper.validateValuationRequired(ctxVal, run.typeCode, valuation);
        }
    }

    public boolean checkObjectExist(String code, Integer laneNumber) {
        return dao.checkObjectExist(DBQuery.and(DBQuery.is("code", code), 
                                                DBQuery.is("lanes.number", laneNumber)));
    }
    
}
