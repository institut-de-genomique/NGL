package models.laboratory.processes.instance;

import static validation.common.instance.CommonValidationHelper.FIELD_STATE_CODE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.laboratory.common.instance.Comment;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.utils.InstanceConstants;

import org.mongojack.MongoCollection;

import validation.ContextValidation;
import validation.IValidation;
import validation.common.instance.CommonValidationHelper;
import validation.processes.instance.ProcessValidationHelper;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fr.cea.ig.DBObject;


@MongoCollection(name="Process")
public class Process extends DBObject implements IValidation {

	public String typeCode;
	public String categoryCode;

	public State state;

	public TraceInformation traceInformation;
	public List<Comment> comments = new ArrayList<>(0);

	public Map<String,PropertyValue> properties;

	// Projects ref
	public Set<String> projectCodes;
	// Samples ref
	public Set<String> sampleCodes;

	public String currentExperimentTypeCode;
	public String inputContainerCode;
	public String inputContainerSupportCode;

	public Set<String> outputContainerSupportCodes;
	public Set<String> outputContainerCodes; 
	public Set<String> experimentCodes;

	public SampleOnInputContainer sampleOnInputContainer;

	@JsonIgnore
	@Override
	public void validate(ContextValidation contextValidation) {
		if(contextValidation.getObject(FIELD_STATE_CODE) == null && state != null){
			contextValidation.putObject(FIELD_STATE_CODE , state.code);			
		}
		if (contextValidation.isCreationMode() 
				&& contextValidation.getObject(CommonValidationHelper.FIELD_PROCESS_CREATION_CONTEXT).equals(CommonValidationHelper.VALUE_PROCESS_CREATION_CONTEXT_COMMON)){
			ProcessValidationHelper.validateProcessType(typeCode,properties,contextValidation);
			ProcessValidationHelper.validateProcessCategory(categoryCode,contextValidation);
			ProcessValidationHelper.validateTraceInformation(traceInformation, contextValidation);
			
		}else if(contextValidation.isCreationMode() 
				&& contextValidation.getObject(CommonValidationHelper.FIELD_PROCESS_CREATION_CONTEXT).equals(CommonValidationHelper.VALUE_PROCESS_CREATION_CONTEXT_SPECIFIC)){
			ProcessValidationHelper.validateId(this, contextValidation);
			ProcessValidationHelper.validateCode(this, InstanceConstants.PROCESS_COLL_NAME, contextValidation);
			
			ProcessValidationHelper.validateState(typeCode,state, contextValidation);
			ProcessValidationHelper.validateContainerCode(inputContainerCode, contextValidation, "inputContainerCode");
			ProcessValidationHelper.validateContainerSupportCode(inputContainerSupportCode, contextValidation, "inputContainerSupportCode");
			
			ProcessValidationHelper.validateProjectCodes(projectCodes, contextValidation);
			ProcessValidationHelper.validateSampleCodes(sampleCodes, contextValidation);
			ProcessValidationHelper.validateSampleOnInputContainer(sampleOnInputContainer, contextValidation);
		}else{
			ProcessValidationHelper.validateId(this, contextValidation);
			ProcessValidationHelper.validateCode(this, InstanceConstants.PROCESS_COLL_NAME, contextValidation);
			
			ProcessValidationHelper.validateProcessType(typeCode,properties,contextValidation);
			ProcessValidationHelper.validateProcessCategory(categoryCode,contextValidation);
			ProcessValidationHelper.validateState(typeCode,state, contextValidation);
			ProcessValidationHelper.validateTraceInformation(traceInformation, contextValidation);
			ProcessValidationHelper.validateContainerCode(inputContainerCode, contextValidation, "inputContainerCode");
			ProcessValidationHelper.validateContainerSupportCode(inputContainerSupportCode, contextValidation, "inputContainerSupportCode");
			
			ProcessValidationHelper.validateProjectCodes(projectCodes, contextValidation);
			ProcessValidationHelper.validateSampleCodes(sampleCodes, contextValidation);
			ProcessValidationHelper.validateCurrentExperimentTypeCode(currentExperimentTypeCode,contextValidation);		
			ProcessValidationHelper.validateSampleOnInputContainer(sampleOnInputContainer, contextValidation);

		}
		//ProcessValidationHelper.validateExperimentCodes(experimentCodes, contextValidation);
	}
	
	@JsonIgnore
	public Process cloneCommon() {
		Process p = new Process();
		p.typeCode     = this.typeCode;
		p.categoryCode = this.categoryCode;
		if (this.properties != null && this.properties.size() > 0) {
//			p.properties = new HashMap<String, PropertyValue<?>>(this.properties);
			p.properties = new HashMap<>(this.properties);
		}
		p.sampleCodes = this.sampleCodes;
		p.projectCodes = this.projectCodes;
		p.traceInformation = this.traceInformation;
		p.inputContainerSupportCode = this.inputContainerSupportCode;
		p.inputContainerCode = this.inputContainerCode;
		p.state = this.state;
		p.comments = this.comments;
		return p;
	}

}
