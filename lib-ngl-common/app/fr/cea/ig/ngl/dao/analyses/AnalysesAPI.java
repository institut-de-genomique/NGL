package fr.cea.ig.ngl.dao.analyses;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APISemanticException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.api.GenericAPI;
import fr.cea.ig.ngl.dao.readsets.ReadSetsDAO;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.Valuation;
import models.laboratory.run.instance.Analysis;
import models.laboratory.run.instance.ReadSet;
import ngl.refactoring.state.AnalysisStateNames;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import workflows.analyses.AnalysisWorkflows;

public class AnalysesAPI extends GenericAPI<AnalysesDAO, Analysis> {
    
    private final static play.Logger.ALogger logger = play.Logger.of(AnalysesAPI.class);
    private final static List<String> authorizedUpdateFields = Arrays.asList("code",
                                                                             "masterReadSetCodes",
                                                                             "readSetCodes");
    
    private final AnalysisWorkflows workflows;
    private final ReadSetsDAO readSetDao;
    
    @Inject
    public AnalysesAPI(AnalysesDAO dao, AnalysisWorkflows workflows, ReadSetsDAO readSetDao) {
        super(dao);
        this.workflows  = workflows;
        this.readSetDao = readSetDao;
    }

    @Override
    protected List<String> authorizedUpdateFields() {
        return authorizedUpdateFields;
    }

    @Override
    protected List<String> defaultKeys() {
        // return Arrays.asList("*");
        return null;
    }

    private Analysis addCodesToAnalysis(Analysis input) {
        for (String code: input.masterReadSetCodes) {
            ReadSet readSet = readSetDao.findByCode(code);
            input.projectCodes.add(readSet.projectCode);
            input.sampleCodes.add(readSet.sampleCode);
        }
        return input;
    }
    
    @Override
    public Analysis create(Analysis input, String currentUser) throws APIValidationException, APIException {
        if (input._id != null) throw new APISemanticException(CREATE_SEMANTIC_ERROR_MSG);

        input.traceInformation = new TraceInformation();
        input.traceInformation.setTraceInformation(currentUser);

        if (input.state == null) input.state = new State();
        input.state.code = AnalysisStateNames.N;
        input.state.user = currentUser;
        input.state.date = new Date();
        
        if (input.masterReadSetCodes != null && input.masterReadSetCodes.size() > 0) input = addCodesToAnalysis(input);
        ContextValidation ctxVal = ContextValidation.createCreationContext(currentUser);
        input.validate(ctxVal); 
        
        if (! ctxVal.hasErrors()) {
            // EJACOBY: Update ReadSet
            return dao.save(input);
        } else {
            ctxVal.getErrors().keySet().forEach((item) -> logger.warn("######################"+item + "--" + ctxVal.getErrors().get(item).toString()));
            throw new APIValidationException(INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
        }
    }

    @Override
    public Analysis update(Analysis input, String currentUser) throws APIException, APIValidationException {
        Analysis analysis = get(input.code);
        if (analysis == null) {
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
                // EJACOBY: Update READSET
                return get(input.code);
            } else {
                throw new APIValidationException(INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
            }
        }
    }


//  } else { //update only some authorized properties
////    ContextValidation ctxVal = new ContextValidation(getCurrentUser(), filledForm.errors());    
////    ValidationContext ctxVal = new ContextValidation(getCurrentUser(), filledForm);     
////    ctxVal.setUpdateMode();
//      ContextValidation ctxVal = ContextValidation.createUpdateContext(getCurrentUser(), filledForm); 
//      validateAuthorizedUpdateFields(ctxVal, queryFieldsForm.fields, authorizedUpdateFields);
//      validateIfFieldsArePresentInForm(ctxVal, queryFieldsForm.fields, filledForm);
//      
//      if (!ctxVal.hasErrors() && queryFieldsForm.fields.contains("code")) {
//          ctxVal.setCreationMode();
//          CommonValidationHelper.validateCodePrimary(ctxVal, input, collectionName);
//          // TO DO Update READSET
//      }
//      if (!ctxVal.hasErrors()) {
//          updateObject(DBQuery.and(DBQuery.is("code", code)), 
//                  getBuilder(input, queryFieldsForm.fields).set("traceInformation", getUpdateTraceInformation(objectInDB.traceInformation)));
//          if (queryFieldsForm.fields.contains("code") && null != input.code) {
//              code = input.code;
//          }
////        return ok(Json.toJson(getObject(code)));
//          return ok(Json.toJson(api.getObject(code)));
//      } else {
//          return badRequest(errorsAsJson(ctxVal.getErrors()));
//      }           
//  }
//}
    
    @Override
    public Analysis update(Analysis input, String currentUser, List<String> fields) throws APIException, APIValidationException {
        Analysis analysis = get(input.code);
        if (analysis == null) {
            throw new APIException(dao.getElementClass().getSimpleName() + " with code " + input.code + " not exist"); 
        } else {
            String analysisCode = analysis.code;
            ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
            checkAuthorizedUpdateFields(ctxVal, fields);
            checkIfFieldsAreDefined(ctxVal, fields, input);
            if (!ctxVal.hasErrors() && fields.contains("code")) {
                ctxVal.setCreationMode();
                CommonValidationHelper.validateCodePrimary(ctxVal, input, dao.getCollectionName());
                // EJACOBY: Update READSET
            } // no else
            if (!ctxVal.hasErrors()) {
                TraceInformation ti = analysis.traceInformation;
                ti.modificationStamp(ctxVal, currentUser);
                dao.updateObject(DBQuery.and(DBQuery.is("code", analysisCode)), 
                                 dao.getBuilder(input, fields).set("traceInformation", ti));
                if (fields.contains("code")) analysisCode = input.code; // here input.code is never null this attribute is mandatory
                return get(analysisCode);
            } else {
                throw new APIValidationException(INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
            }
        }
    }

    public Analysis updateProperties(String code, Map<String, PropertyValue> properties, String currentUser) throws APIException, APIValidationException {
        Analysis analysis = get(code);
        if (analysis == null) {
            throw new APIException(dao.getElementClass().getSimpleName() + " with code " + code + " not exist"); 
        } else {
            ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser); 
            // GA: AnalysisValidationHelper.validateAnalysisType(objectInDB.typeCode, properties, ctxVal);
            if (!ctxVal.hasErrors()) {
                TraceInformation ti = analysis.traceInformation;
                ti.modificationStamp(ctxVal, currentUser);
                dao.updateObject(DBQuery.and(DBQuery.is("code", analysis.code)), 
                                 DBUpdate.set("properties", properties).set("traceInformation", ti));
                return get(analysis.code);
            } else {
                throw new APIValidationException(GenericAPI.INVALID_INPUT_ERROR_MSG, ctxVal.getErrors());
            }
        }
    }

    public Analysis updateState(String code, State state, String currentUser) throws APIValidationException, APIException {
        Analysis analysis = get(code);
        if (analysis == null) {
            throw new APIException(dao.getElementClass().getSimpleName() + " with code " + code + " not exist");
        } else {
            state.date = new Date();
            state.user = currentUser;
            ContextValidation ctxVal = ContextValidation.createUndefinedContext(currentUser);
            workflows.setState(ctxVal, analysis, state);
            if (!ctxVal.hasErrors()) {
                return get(code);
            } else {
                throw new APIValidationException(INVALID_STATE_ERROR_MSG, ctxVal.getErrors());
            }
        }
    }

    public Analysis valuation(String code, Valuation input, String currentUser) throws APIValidationException, APIException {
        Analysis objectInDB = get(code);
        if (objectInDB == null) {
            throw new APIException(dao.getElementClass().getSimpleName() + " with code " + code + " not exist");
        } else {
            ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
            input.createHistoryFrom(objectInDB.valuation, currentUser);
            CommonValidationHelper.validateValuationRequired(ctxVal, objectInDB.typeCode, input);
            if (!ctxVal.hasErrors()) {
                TraceInformation ti = objectInDB.traceInformation;
                ti.modificationStamp(ctxVal, currentUser);
                dao.updateObject(DBQuery.and(DBQuery.is("code", code)), 
                                 DBUpdate.set("valuation", input).set("traceInformation", ti));
                objectInDB = get(code);
                workflows.nextState(ctxVal, objectInDB);
                return get(code);
            } else {
                throw new APIValidationException(INVALID_STATE_ERROR_MSG, ctxVal.getErrors());
            }
        }
    }
}
