package models.laboratory.resolutions.instance;

import java.util.List;

import fr.cea.ig.DBObject;
import models.laboratory.common.instance.TraceInformation;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.IValidation;
import validation.common.instance.CommonValidationHelper;
import validation.resolution.instance.ResolutionValidationHelper;
import validation.utils.ValidationHelper;

public class ResolutionConfiguration extends DBObject implements IValidation {
	
    public String           objectTypeCode;
	public List<String>     typeCodes;
    public List<Resolution> resolutions;
    public TraceInformation traceInformation;
    
    /**
     * Validate this resolution configuration.
     */
	@Override
	public void validate(ContextValidation contextValidation) {		
    	CommonValidationHelper    .validateCodePrimary             (contextValidation, this, InstanceConstants.RESOLUTION_COLL_NAME);
    	// GA: validate objectTypeCode & typeCodes
    	ValidationHelper          .validateNotEmpty                (contextValidation, objectTypeCode, "type");    	
    	ResolutionValidationHelper.validateResolutions             (contextValidation, resolutions);
		// manage traceInformation
    	// LOGIC: why does the validation always overwrite the trace information ?
    	//        This means that the current method must be called in creation mode.
		TraceInformation t = new TraceInformation();
		t.setTraceInformation("ngsrg");
		traceInformation = t;
		CommonValidationHelper    .validateTraceInformationRequired(contextValidation, traceInformation);
	}

}
