package fr.cea.ig.ngl.dao.runs;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.mongojack.DBQuery;

import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APISemanticException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.GenericAPI;
import fr.cea.ig.ngl.dao.readsets.ReadSetsDAO;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.Valuation;
import models.laboratory.run.description.RunCategory;
import models.laboratory.run.instance.Run;
import ngl.refactoring.state.RunStateNames;
import validation.ContextValidation;
import validation.run.instance.RunValidationHelper;
import workflows.run.RunWorkflows;

@Singleton
public class RunsAPI extends GenericAPI<RunsDAO, Run> {

    private static final String INVALID_RUN_OBJECT = "Invalid Run object";

    private static final play.Logger.ALogger logger = play.Logger.of(RunsAPI.class);

    private static final List<String> AUTHORIZED_UPDATE_FIELDS = Arrays.asList("keep", "deleted");
    private static final List<String> DEFAULT_KEYS =  Arrays.asList("code", 
                                                                    "typeCode", 
                                                                    "sequencingStartDate", 
                                                                    "state", 
            "valuation");
    private final RunWorkflows workflows;
    private final ReadSetsDAO readSetDao;

    @Inject
    public RunsAPI(RunsDAO dao, RunWorkflows workflows, ReadSetsDAO readSetDao) {
        super(dao);
        this.workflows  = workflows;  
        this.readSetDao = readSetDao;
    }

    @Override
    protected List<String> authorizedUpdateFields() {
        return AUTHORIZED_UPDATE_FIELDS;
    }


    @Override
    protected List<String> defaultKeys() {
        return DEFAULT_KEYS;
    }

    public Run create(Run input, String currentUser, Boolean external) throws APIValidationException, APISemanticException {
        if (input._id == null) { 
            input.traceInformation = new TraceInformation();
            input.traceInformation.setTraceInformation(currentUser);

            if (input.state == null) input.state = new State();
            input.state.code = RunStateNames.N;
            input.state.user = currentUser;
            input.state.date = new Date();

            if(null == input.categoryCode && null != input.typeCode){
                input.categoryCode = RunCategory.find.get().findByTypeCode(input.typeCode).code;
            }

            ContextValidation ctxVal = ContextValidation.createCreationContext(currentUser); 
            if (external != null) {
                ctxVal.putObject("external", external);
            } else {
                ctxVal.putObject("external", false);
            }
            input.validate(ctxVal);
            if (!ctxVal.hasErrors()) {
                return dao.save(input);
            } else {
                throw new APIValidationException(INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
            }
        } else {
            throw new APISemanticException(CREATE_SEMANTIC_ERROR_MSG); 
        }
    }

    @Override
    public Run update(Run input, String currentUser) throws APIException, APIValidationException {
        Run runInDB = get(input.code);
        if (runInDB == null) {
            throw new APIException(dao.getElementClass().getSimpleName() + " with code " + input.code + " not exist");
        } else {
            ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
            if(input.traceInformation != null){
                input.traceInformation.modificationStamp(ctxVal, currentUser);
            } else {
                logger.warn("traceInformation is null !!");
            }
            input.validate(ctxVal);
            if (!ctxVal.hasErrors()) {
                dao.updateObject(input);
                return input;
            } else {
                throw new APIValidationException(INVALID_RUN_OBJECT, ctxVal.getErrors());
            }
        }
    }

    @Override
    public Run update(Run input, String currentUser, List<String> fields) throws APIException, APIValidationException {
        Run runInDB = get(input.code);
        if (runInDB == null) {
            throw new APIException(dao.getElementClass().getSimpleName() + " with code " + input.code + " not exist");
        } else {
            ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);     
            checkAuthorizedUpdateFields(ctxVal, fields);
            checkIfFieldsAreDefined(ctxVal, fields, input);
            if (!ctxVal.hasErrors()) {
                TraceInformation ti = runInDB.traceInformation;
                ti.modificationStamp(ctxVal, currentUser);
                dao.updateObject(DBQuery.and(DBQuery.is("code", input.code)), dao.getBuilder(input, fields).set("traceInformation", ti));
                return get(input.code);
            } else {
                throw new APIValidationException(INVALID_RUN_OBJECT, ctxVal.getErrors());
            }
        }
    }

    public Run valuation(String code, Valuation valuation, String currentUser) throws APIException, APIValidationException {
        Run runInDB = get(code);
        if (runInDB == null) {
            throw new APIException(dao.getElementClass().getSimpleName() + " with code " + code + " not exist");
        } else {
            ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
//            RunValidationHelper.validateValuation(runInDB.typeCode, valuation, ctxVal);
            RunValidationHelper.validateValuationRequired(ctxVal, runInDB.typeCode, valuation);
            if (!ctxVal.hasErrors()) { 
                Run input = new Run();
                input.valuation = valuation;
                TraceInformation ti = runInDB.traceInformation;
                if(ti != null){
                    ti.modificationStamp(ctxVal, currentUser);
                } else{
                    logger.warn("traceInformation is null !!");
                }
                dao.updateObject(DBQuery.and(DBQuery.is("code", code)), dao.getBuilder(input, Arrays.asList("valuation")).set("traceInformation", ti));
                runInDB = get(code);
                workflows.nextState(ctxVal, runInDB);
                if (!ctxVal.hasErrors()) { 
                    return get(code);
                } else {
                    throw new APIValidationException("Invalid state modification after valuation modification", ctxVal.getErrors());
                }
            } else {
                throw new APIValidationException("Invalid valuation modification", ctxVal.getErrors());
            }
        }
    }

    @Override
    public void delete(String code) throws APIException {
        Run runInDB = get(code);
        if(runInDB == null) {
            throw new APIException(dao.getElementClass().getSimpleName() + " with code " + code + " not exist");
        } else {
            dao.deleteObject(code);
            logger.debug("delete readset(s) linked to the Run " + code);
            readSetDao.deleteObject(DBQuery.is("runCode", code));  
            //TODO delete analysis (not implemented yet)
        }
    }

    public Run get(String code, Integer laneNumber, String treatmentCode) {
        return dao.findOne(DBQuery.and(DBQuery.is("code", code), 
                                       DBQuery.elemMatch("lanes", DBQuery.and(DBQuery.is("number", laneNumber),
                                                                              DBQuery.exists("treatments."+treatmentCode)))));
    }

    /**
     * Call {@link #create(Run, String, Boolean)} with null as value for Boolean argument
     * @see fr.cea.ig.ngl.dao.api.GenericAPI#create(fr.cea.ig.DBObject, java.lang.String)
     */
    @Override
    public Run create(Run input, String currentUser) throws APIValidationException, APIException {
        return create(input, currentUser, null);
    }

    public Run updateState(String code, State state, String currentUser) throws APIValidationException, APIException {
        Run run = get(code);
        if (run == null) {
            throw new APIException(dao.getElementClass().getSimpleName() + " with code " + code + " not exist");
        } else {
            state.date = new Date();
            state.user = currentUser;
            ContextValidation ctxVal = ContextValidation.createUndefinedContext(currentUser);
            workflows.setState(ctxVal, run, state);
            if (!ctxVal.hasErrors()) {
                return get(code);
            } else {
                throw new APIValidationException(INVALID_STATE_ERROR_MSG, ctxVal.getErrors());
            }
        }
    }
}
