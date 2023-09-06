package models.laboratory.run.instance;

import static fr.cea.ig.lfw.utils.Iterables.zen;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fr.cea.ig.DBObject;
import fr.cea.ig.ngl.dao.runs.RunsDAO;
import fr.cea.ig.ngl.utils.GuiceSupplier;
import models.laboratory.common.instance.ITracingAccess;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.Valuation;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.IValidation;
import validation.common.instance.CommonValidationHelper;
import validation.run.instance.LaneValidationHelper;
import validation.run.instance.RunValidationHelper;
import validation.run.instance.TreatmentValidationHelper;

public class Run extends DBObject implements IValidation, ITracingAccess {
        
	public static final Supplier<RunsDAO> find = new GuiceSupplier<>(RunsDAO.class);
	
	public String           typeCode;
	public Date             sequencingStartDate;
	public String           categoryCode;
	public State            state;
	public String           containerSupportCode; //id flowcell
    public Boolean          dispatch         = Boolean.FALSE;
    public Valuation        valuation        = new Valuation();
    public Set<String>      projectCodes     = new TreeSet<>();
    public Set<String>      sampleCodes      = new TreeSet<>();
    public Boolean          keep             = Boolean.FALSE;
    public Boolean          deleted          = Boolean.FALSE;
    public TraceInformation traceInformation;
    public InstrumentUsed   instrumentUsed; // Instrument used to obtain the run
    public Map<String,Treatment> treatments = new HashMap<>();
    public Map<String, PropertyValue> properties = new HashMap<>(); 
    public List<Lane>       lanes;
    
//    @JsonIgnore
//    public Lane getLane(Integer laneNumber) {
//    	if (lanes != null) {
//    		Iterator<Lane> iti = lanes.iterator();
//	    	while (iti.hasNext()) {
//	    		Lane next = iti.next();
//	    		if (next.number.equals(laneNumber)) {
//	    			return next;
//	    		}
//	    	}
//    	}
//    	return null;
//    	//return lanes.stream().filter((Lane l) -> l.number.equals(laneNumber)).findFirst().get();
//    }
    
//    @JsonIgnore
//    public Lane getLane(Integer laneNumber) {
//    	if (lanes == null)
//    		return null;
//   		return filter(lanes, l -> l.number.equals(laneNumber)).first().orElse(null);
//    }
    @JsonIgnore
    public Lane getLane(Integer laneNumber) {
    	return zen(lanes).filter(l -> l.number.equals(laneNumber)).first().orElse(null);
    }
    
    // _CTX_PARAM: use explicit 'external' boolean parameter
    @Override
    public void validate(ContextValidation contextValidation) {
    	CommonValidationHelper   .validateIdPrimary               (contextValidation, this);
    	CommonValidationHelper   .validateCodePrimary             (contextValidation, this, InstanceConstants.RUN_ILLUMINA_COLL_NAME);
    	RunValidationHelper      .validateRunTypeCodeRequired     (contextValidation, typeCode, properties);
    	RunValidationHelper      .validateRunCategoryCodeRequired (contextValidation, categoryCode);
    	CommonValidationHelper   .validateStateRequired           (contextValidation, typeCode, state);
    	CommonValidationHelper   .validateValuationRequired       (contextValidation, typeCode, valuation);
    	CommonValidationHelper   .validateTraceInformationRequired(contextValidation, traceInformation);
    	if (!(contextValidation.containsKey("external")) || (!contextValidation.<Boolean>getTypedObject("external")))
    		CommonValidationHelper.validateContainerSupportCodeRequired(contextValidation, containerSupportCode, "containerSupportCode"); 
    	
    	//TODO NGL-2959 bad validation ReadSet presence add specific Run type rules 
    	//TODO NGL-2959 ReadSet validation with projectCode depends on state of Run 
		RunValidationHelper      .validateRunProjectCodes         (contextValidation, code, projectCodes);
		//TODO NGL-2959 ReadSet validation with sampleCode depends on state of Run 
		RunValidationHelper      .validateRunSampleCodes          (contextValidation, code, sampleCodes);
		// WARN DON'T CHANGE THE ORDER OF VALIDATION
		TreatmentValidationHelper.validateTreatments              (contextValidation, treatments, this);
		LaneValidationHelper     .validateLanes                   (contextValidation, this, lanes);		
    }

    /*
        nbClusterIlluminaFilter
        nbCycle
        nbClusterTotal
        nbBase
        flowcellPosition
        rtaVersion
        flowcellVersion
        controlLane
        mismatch

	    id du depot flowcell ???
	    id du type de sequen
    */
    
    // ----------------------------------------------------------------------
    // some @JsonIgnore are probably superfluous but better safe than sorry.
    
    @JsonIgnore
	public boolean isValuationComplete() {
		if (valuation.valid.equals(TBoolean.UNSET))
			return false;
		if (lanes == null) 
			return true;
		for (Lane lane : lanes) 
			if (lane.valuation.valid.equals(TBoolean.UNSET)) 
				return false;
		return true;
	}
	
    @JsonIgnore
	public boolean atLeastOneValuation() {
		if (!valuation.valid.equals(TBoolean.UNSET)) 
			return true;
		if (lanes == null) 
			return false;
		for (Lane lane : lanes) 
			if (!lane.valuation.valid.equals(TBoolean.UNSET))
				return true;
		return false;
	}

	@Override
	public TraceInformation getTraceInformation() {
		if (traceInformation == null)
			traceInformation = new TraceInformation();
		return traceInformation;
	}

//	public Set<String> getSampleCodes() {
//		return sampleCodes;
//	}
//	
//	public void setSampleCodes(Set<String> s)  {
//		sampleCodes = s;
//	}
	
}

