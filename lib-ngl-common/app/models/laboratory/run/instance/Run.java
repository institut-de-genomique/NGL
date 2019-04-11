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
import models.laboratory.common.description.Level;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.Valuation;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.IValidation;
import validation.run.instance.LaneValidationHelper;
import validation.run.instance.RunValidationHelper;
import validation.run.instance.TreatmentValidationHelper;

public class Run extends DBObject implements IValidation {
        
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
    
    @Override
    public void validate(ContextValidation contextValidation) {
    	contextValidation.putObject("run", this);
    	RunValidationHelper.validateIdPrimary               (contextValidation, this);
    	RunValidationHelper.validateCodePrimary             (contextValidation, this, InstanceConstants.RUN_ILLUMINA_COLL_NAME);
    	RunValidationHelper.validateRunType                 (typeCode, properties, contextValidation);
    	RunValidationHelper.validationRunCategoryCode(categoryCode, contextValidation);
    	// TODO ValidationHelper.required(contextValidation, sequencingStartDate, "sequencingStartDate");
    	RunValidationHelper.validateStateRequired(contextValidation, this.typeCode, this.state);
    	RunValidationHelper.validateValuationRequired(contextValidation, this.typeCode, this.valuation);
    	RunValidationHelper.validateTraceInformationRequired(contextValidation, this.traceInformation);
//    	if(!(contextValidation.getContextObjects().containsKey("external")) || 
//    			(contextValidation.getContextObjects().containsKey("external") && !(Boolean)contextValidation.getContextObjects().get("external")))
    	if (!(contextValidation.containsKey("external")) || (!contextValidation.<Boolean>getTypedObject("external")))
    		RunValidationHelper.validateContainerSupportCodeRequired(contextValidation, this.containerSupportCode, "containerSupportCode"); 
    	RunValidationHelper.validateRunInstrumentUsed(this.instrumentUsed, contextValidation);		
		contextValidation.putObject("level", Level.CODE.Run);
		RunValidationHelper.validateRunProjectCodes(this.code, this.projectCodes, contextValidation);
		RunValidationHelper.validateRunSampleCodes(this.code, this.sampleCodes, contextValidation);
		// WARN DON'T CHANGE THE ORDER OF VALIDATION
//		TreatmentValidationHelper.validationTreatments(contextValidation, this.treatments);
		TreatmentValidationHelper.validateTreatments(contextValidation, treatments, this);
//		LaneValidationHelper.validationLanes(this.lanes, contextValidation);		
		LaneValidationHelper.validateLanes(contextValidation, this, lanes);		
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
    
}

