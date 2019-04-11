package models.laboratory.run.instance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.cea.ig.DBObject;
import models.laboratory.common.description.Level;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.Valuation;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.IValidation;
import validation.run.instance.AnalysisValidationHelper;
import validation.run.instance.FileValidationHelper;
import validation.run.instance.TreatmentValidationHelper;
import validation.utils.ValidationHelper;

public class Analysis extends DBObject implements IValidation {

    public String           typeCode;
	public State            state;
	public Valuation        valuation        = new Valuation();
	public TraceInformation traceInformation;
	public List<String>     masterReadSetCodes;
	public List<String>     readSetCodes;
	public List<String>     projectCodes     = new ArrayList<>();
    public List<String>     sampleCodes      = new ArrayList<>();	
	public String           path;
	public List<File>       files;
	public Map<String, Treatment>     treatments = new HashMap<>();
	public List<ReadSetProperties>    masterReadSetProperties = null; //used to add with drools readset properties need to evaluate analysis
	public Map<String, PropertyValue> properties = new HashMap<>();
	
	@Override
	public void validate(ContextValidation contextValidation) {
		AnalysisValidationHelper.validateIdPrimary               (contextValidation, this);
		AnalysisValidationHelper.validateCodePrimary             (contextValidation, this, InstanceConstants.ANALYSIS_COLL_NAME);
		AnalysisValidationHelper.validateAnalysisTypeRequired    (contextValidation, typeCode, properties);
		AnalysisValidationHelper.validateStateRequired           (contextValidation, typeCode, state);
		AnalysisValidationHelper.validateValuationRequired       (contextValidation, typeCode, valuation);
		AnalysisValidationHelper.validateTraceInformationRequired(contextValidation, traceInformation);
		
		AnalysisValidationHelper.validateReadSetCodes            (contextValidation, this);
		AnalysisValidationHelper.validateProjectCodes            (contextValidation, projectCodes);
		AnalysisValidationHelper.validateSampleCodes             (contextValidation, sampleCodes);
		
		ValidationHelper        .validateNotEmpty                (contextValidation, path, "path");
		
		contextValidation.putObject("analysis",    this);                // CTX: remove
		contextValidation.putObject("objectClass", getClass());          // CTX: remove
		contextValidation.putObject("level",       Level.CODE.Analysis); // CTX: remove
		
//		TreatmentValidationHelper.validationTreatments           (contextValidation, treatments);
		TreatmentValidationHelper.validateTreatments             (contextValidation, treatments, this);
//		FileValidationHelper     .validateFiles                  (contextValidation, files);
		FileValidationHelper     .validateFiles                  (contextValidation, this, files);
	}
	
}

