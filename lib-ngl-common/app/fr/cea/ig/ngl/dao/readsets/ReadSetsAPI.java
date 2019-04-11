package fr.cea.ig.ngl.dao.readsets;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import org.mongojack.DBUpdate;
import org.mongojack.DBUpdate.Builder;

import com.mongodb.BasicDBObject;

import controllers.readsets.api.ReadSetValuation;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APISemanticException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.GenericAPI;
import fr.cea.ig.ngl.dao.runs.RunsDAO;
import fr.cea.ig.ngl.support.ListFormWrapper;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.Valuation;
import models.laboratory.run.instance.Lane;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import ngl.refactoring.state.ReadSetStateNames;
import ngl.refactoring.state.SRASubmissionStateNames;
import validation.ContextValidation;
import validation.run.instance.ReadSetValidationHelper;
import workflows.readset.ReadSetWorkflows;

@Singleton
public class ReadSetsAPI extends GenericAPI<ReadSetsDAO, ReadSet> {

	private static final play.Logger.ALogger logger = play.Logger.of(ReadSetsAPI.class);
	
    private final static List<String> authorizedUpdateFields = Arrays.asList("code", "path", "location", "properties");
    private final static List<String> defaultKeys            = Arrays.asList("code",
                                                                             "typeCode",
                                                                             "runCode",
                                                                             "runTypeCode",
                                                                             "laneNumber",
                                                                             "projectCode",
                                                                             "sampleCode",
                                                                             "runSequencingStartDate",
                                                                             "state",
                                                                             "productionValuation",
                                                                             "bioinformaticValuation",
                                                                             "properties",
                                                                             "location");
    private final RunsDAO runDao;
    private final ReadSetWorkflows workflows;
    
	@Inject
	public ReadSetsAPI(ReadSetsDAO dao, RunsDAO runDao, ReadSetWorkflows workflows) {
		super(dao);
		this.runDao = runDao;
		this.workflows = workflows;
	}
	
	@Override
    protected List<String> authorizedUpdateFields() {
        return authorizedUpdateFields;
    }


    @Override
    protected List<String> defaultKeys() {
        return defaultKeys;
    }
	
	/* (non-Javadoc)
	 * Override the default behavior of reporting mode (add a projection)
	 * @see fr.cea.ig.ngl.dao.api.GenericAPI#listObjects(fr.cea.ig.ngl.support.ListFormWrapper)
	 */
	@Override
    public Iterable<ReadSet> listObjects(ListFormWrapper<ReadSet> wrapper) throws APIException {
	    if (wrapper.isReportingMode()) {
            return dao.findByQueryWithProjection(wrapper.reportingQuery(), wrapper.getJsonKeys(defaultKeys()));
        } else {
            return super.listObjects(wrapper);
        }
    }

	public ReadSet dao_getObject(String readSetCode) {
		return dao.getObject(readSetCode);
	}
		
	public void dao_update(Query query, Builder set) {
		dao.update(query, set);
	}	
	
	/**
     * Call {@link #create(ReadSet, String, Boolean)} with null as value for Boolean argument
     * @see fr.cea.ig.ngl.dao.api.GenericAPI#create(fr.cea.ig.DBObject, java.lang.String)
     */
	@Override
	public ReadSet create(ReadSet input, String currentUser) throws APIValidationException, APISemanticException {
	    return create(input, currentUser, null);
	}
	
	public ReadSet create(ReadSet input, String currentUser, Boolean external) throws APIValidationException, APISemanticException {
	    if (input._id == null) { 
	        input.traceInformation = new TraceInformation();
	        input.traceInformation.setTraceInformation(currentUser);

	        if (input.state == null) {
	            input.state = new State();
	        }
	        input.state.code = ReadSetStateNames.N;
	        input.state.user = currentUser;
	        input.state.date = new Date();   
	        input.submissionState = new State(SRASubmissionStateNames.NONE, currentUser);
	        input.submissionState.date = new Date(); 

	        //TODO AJ EJACOBY voir si amélioration à faire ou faisable 
	        //hack to simplify ngsrg => move to workflow but workflow not call here !!!
	        if (input.runCode != null && (input.runSequencingStartDate == null || input.runTypeCode == null)) {
	            BasicDBObject keys = new BasicDBObject();
	            keys.put("_id", 0);//Don't need the _id field
	            keys.put("sequencingStartDate", 1);
	            keys.put("typeCode", 1);
	            Run run = runDao.findByCode(input.runCode, keys);
	            //Run run = MongoDBDAO.find(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, DBQuery.is("code", input.runCode), keys).toList().get(0); 
	            input.runSequencingStartDate = run.sequencingStartDate;
	            input.runTypeCode = run.typeCode;
	        }

	        ContextValidation ctxVal = ContextValidation.createUndefinedContext(currentUser);
	        if (external != null) {
	            ctxVal.putObject("external", external);
	        } else {
	            ctxVal.putObject("external", false);
	        }
	        // Apply rules before validation
	        workflows.applyPreStateRules(ctxVal, input, input.state);
	        ctxVal.setCreationMode();
	        input.validate(ctxVal);
	        if (!ctxVal.hasErrors()) {
	            ReadSet readset = dao.save(input);
	            Query query = DBQuery.and(DBQuery.is("code", input.runCode),
	                                      DBQuery.elemMatch("lanes", 
	                                                        DBQuery.and(DBQuery.is("number", input.laneNumber), 
	                                                                    DBQuery.notIn("readSetCodes", input.code))));
	            runDao.update(query, 
	                          DBUpdate.push("lanes.$.readSetCodes", input.code));

	            // To avoid redundant values
	            query = DBQuery.and(DBQuery.is("code", input.runCode), 
	                                DBQuery.notIn("projectCodes", input.projectCode));
	            runDao.update(query, 
	                          DBUpdate.push("projectCodes", input.projectCode));

	            query = DBQuery.and(DBQuery.is("code", input.runCode), 
	                                DBQuery.notIn("sampleCodes", input.sampleCode));
	            runDao.update(query, 
	                          DBUpdate.push("sampleCodes", input.sampleCode));
	            return readset;
	        } else {
	            throw new APIValidationException(INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
	        }
	    } else {
	        throw new APISemanticException(CREATE_SEMANTIC_ERROR_MSG);
	    }
	}

	@Override
	public ReadSet update(ReadSet input, String currentUser) throws APIException, APIValidationException {
	    ReadSet readset = get(input.code);
	    if (readset == null) {
	        throw new APIException(dao.getElementClass().getSimpleName() + " with code " + input.code + " not exist"); 
	    } else {
	        ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser); 
	        if (input.traceInformation != null) {
	            input.traceInformation.modificationStamp(ctxVal, currentUser);
	        } else {
	            logger.warn("traceInformation is null !!");
	        }
	        input.validate(ctxVal);
	        if (!ctxVal.hasErrors()) {
	            dao.update(input);
	            runDao.update(DBQuery.and(DBQuery.is   ("code",         input.runCode), 
	                                      DBQuery.notIn("projectCodes", input.projectCode)), 
	                          DBUpdate.push("projectCodes", input.projectCode));

	            runDao.update(DBQuery.and(DBQuery.is   ("code",        input.runCode), 
	                                      DBQuery.notIn("sampleCodes", input.sampleCode)),
	                          DBUpdate.push("sampleCodes", input.sampleCode));
	            return get(input.code);
	        } else {
	            throw new APIValidationException(INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
	        }
	    }
	}

	@Override
	public ReadSet update(ReadSet input, String currentUser, List<String> fields) throws APIException, APIValidationException {
	    ReadSet readset = get(input.code);
	    if (readset == null) {
	        throw new APIException(dao.getElementClass().getSimpleName() + " with code " + input.code + " not exist"); 
	    } else {
	        String readSetCode = readset.code;
	        ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
	        checkAuthorizedUpdateFields(ctxVal, fields);
	        checkIfFieldsAreDefined(ctxVal, fields, input);
	        if (fields.contains("code")) {
	            ctxVal.setCreationMode();
	            ReadSetValidationHelper.validateCodePrimary(ctxVal, input, dao.getCollectionName());
	        } 
	        if (fields.contains("properties")) return updateProperties(input.code, input.properties, currentUser);
	        if (!ctxVal.hasErrors()) {
	            TraceInformation ti = readset.traceInformation;
	            ti.modificationStamp(ctxVal, currentUser);
	            dao.update(DBQuery.and(DBQuery.is("code", readSetCode)), 
	                       dao.getBuilder(input, fields).set("traceInformation", ti));

	            if (fields.contains("code")) { // here input.code is never null this attribute is mandatory
	                runDao.update(DBQuery.and(DBQuery.is("code",         readset.runCode),
	                                          DBQuery.is("lanes.number", readset.laneNumber)), 
	                              DBUpdate.pull("lanes.$.readSetCodes", readSetCode));
	                runDao.update(DBQuery.and(DBQuery.is       ("code",  readset.runCode), 
	                                          DBQuery.elemMatch("lanes", DBQuery.and(DBQuery.is   ("number",       readset.laneNumber), 
	                                                                                 DBQuery.notIn("readSetCodes", readset.code)))), 
	                              DBUpdate.push("lanes.$.readSetCodes", input.code));
	            }
	            return get(input.code);
	        } else {
	            throw new APIValidationException(INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
	        }
	    }
	} 
	
	public ReadSet updateProperties(String code, Map<String, PropertyValue> properties, String currentUser) throws APIException, APIValidationException {
	    ReadSet readSet = get(code);
        if (readSet == null) {
            throw new APIException(dao.getElementClass().getSimpleName() + " with code " + code + " not exist"); 
        } else {
            ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
//            ReadSetValidationHelper.validateReadSetType(readSet.typeCode, properties, ctxVal);
            ReadSetValidationHelper.validateReadSetTypeRequired(ctxVal, readSet.typeCode, properties);
            if (!ctxVal.hasErrors()) {
                TraceInformation ti = readSet.traceInformation;
                ti.modificationStamp(ctxVal, currentUser);
                dao.update(DBQuery.and(DBQuery.is("code", code)),
                           DBUpdate.set("properties", properties).set("traceInformation", ti));                              
                return get(code);       
            } else {
                throw new APIValidationException(INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
            }
        }
    }

    public ReadSet updateState(String code, State state, String currentUser) throws APIValidationException, APIException {
	    ReadSet readset = get(code);
        if (readset == null) {
            throw new APIException(dao.getElementClass().getSimpleName() + " with code " + code + " not exist");
        } else {
            state.date = new Date();
            state.user = currentUser;
            ContextValidation ctxVal = ContextValidation.createUndefinedContext(currentUser);
            workflows.setState(ctxVal, readset, state);
            if (!ctxVal.hasErrors()) {
                return get(code);
            } else {
                throw new APIValidationException(INVALID_STATE_ERROR_MSG, ctxVal.getErrors());
            }
        }
    }
	
	@Override
	public void delete(String code) throws APIException {
	    if (dao.checkObjectExistByCode(code)) {
	        ReadSet readSet = get(code);
	        runDao.update(DBQuery.and(DBQuery.is("code",         readSet.runCode),
	                                  DBQuery.is("lanes.number", readSet.laneNumber)), 
	                      DBUpdate.pull("lanes.$.readSetCodes", readSet.code));
	        dao.deleteByCode(code);

	        if ((readSet.projectCode!= null) && ! dao.checkObjectExist(DBQuery.and(DBQuery.is("runCode",     readSet.runCode), 
	                                                                               DBQuery.is("projectCode", readSet.projectCode)))) {
	            runDao.update(DBQuery.is("code",readSet.runCode), DBUpdate.pull("projectCodes", readSet.projectCode));
	        }
	        if ((readSet.sampleCode!= null) && ! dao.checkObjectExist(DBQuery.and(DBQuery.is("runCode",    readSet.runCode), 
	                                                                              DBQuery.is("sampleCode", readSet.sampleCode)))) {
	            runDao.update(DBQuery.is("code",readSet.runCode), DBUpdate.pull("sampleCodes", readSet.sampleCode));
	        }

	        // EJACOBY commentaire présent avant refactoring
	        // TODO delete analysis
	    } else {
	        throw new APIException(dao.getElementClass().getSimpleName() + " with code " + code + " not exist");
	    }
	}

    public void deleteByRunCode(String code) throws APIException {
        Run run  = runDao.findByCode(code);
        if (run == null) throw new APIException(runDao.getElementClass().getSimpleName() + " with code " + code + " not exist");
        
        if(run.lanes != null){
            for(Lane lane: run.lanes){
                runDao.update(DBQuery.and(DBQuery.is("code",         code),
                                          DBQuery.is("lanes.number", lane.number)), 
                              DBUpdate.unset("lanes.$.readSetCodes"));     
            }
        }
        runDao.update(DBQuery.is("code",code), 
                      DBUpdate.unset("projectCodes").unset("sampleCodes"));
        dao.deleteObject(DBQuery.and(DBQuery.is("runCode", code)));
    }
    
    public ReadSet valuation(String code, ReadSetValuation valuations, String currentUser) throws APIException {
        ReadSet readset = get(code);
        if (readset == null) {
            throw new APIException(dao.getElementClass().getSimpleName() + " with code " + code + " not exist");
        } else {
            ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
            manageValidation(readset, valuations.productionValuation, valuations.bioinformaticValuation, ctxVal);
            if (!ctxVal.hasErrors()) {
                TraceInformation ti = readset.traceInformation;
                ti.modificationStamp(ctxVal, currentUser);
                dao.update(DBQuery.and(DBQuery.is("code", code)),
                           DBUpdate.set("productionValuation",    valuations.productionValuation)
                                   .set("bioinformaticValuation", valuations.bioinformaticValuation)
                                   .set("traceInformation",       ti));
                workflows.nextState(ctxVal, get(code));
                return get(code);
            } else {
                throw new APIValidationException(INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
            }
        }
    }
    
    private void manageValidation(ReadSet readSet, Valuation productionVal, Valuation bioinfoVal, ContextValidation ctxVal) {
        if (productionVal.valid != readSet.productionValuation.valid) {
            productionVal.date = new Date();
            productionVal.user = ctxVal.getUser();
            ReadSetValidationHelper.validateValuationRequired(ctxVal, readSet.typeCode, productionVal);
        }
        if (bioinfoVal.valid != readSet.bioinformaticValuation.valid) {
            bioinfoVal.date = new Date();
            bioinfoVal.user = ctxVal.getUser();
            ReadSetValidationHelper.validateValuationRequired(ctxVal, readSet.typeCode, bioinfoVal);
        }
    }
}
