package models.laboratory.parameter;

import validation.ContextValidation;
import validation.IValidation;
import validation.common.instance.CommonValidationHelper;
import validation.utils.ValidationHelper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import models.laboratory.common.instance.ITracingAccess;
import models.laboratory.common.instance.TraceInformation;
import models.utils.InstanceConstants;
import fr.cea.ig.DBObject;

@JsonTypeInfo(use=Id.NAME, include=As.EXISTING_PROPERTY, property="typeCode", defaultImpl=models.laboratory.parameter.index.Index.class)
@JsonSubTypes({
	@JsonSubTypes.Type(value =  models.laboratory.parameter.index.IlluminaIndex       .class, name = "index-illumina-sequencing"),
	@JsonSubTypes.Type(value =  models.laboratory.parameter.index.NanoporeIndex       .class, name = "index-nanopore-sequencing"),
	@JsonSubTypes.Type(value =  models.laboratory.parameter.index.PacBioIndex         .class, name = "index-pacbio-sequencing"),
	@JsonSubTypes.Type(value =  models.laboratory.parameter.index.MGIIndex       .class, name = "index-mgi-sequencing"),
	@JsonSubTypes.Type(value =  models.laboratory.parameter.printer.BBP11             .class, name = "BBP11"),
	@JsonSubTypes.Type(value =  models.laboratory.parameter.map.MapParameter          .class, name = "map-parameter"),
	@JsonSubTypes.Type(value =  models.laboratory.parameter.context.ContextDescription.class, name = "context-description"),
	@JsonSubTypes.Type(value =  models.laboratory.parameter.map.MapQPCR.               class, name = "map-qpcr-parameter"),
	@JsonSubTypes.Type(value =  models.laboratory.parameter.propertyDefinitionsProtocol.PropertyDefinitionsProtocol.   class, name = "map-property-definitions"),

})
public abstract class Parameter extends DBObject implements IValidation, ITracingAccess {
	
	public String           typeCode;
	public TraceInformation traceInformation;
	public String           categoryCode;
	public String           name;
	
	protected Parameter(String typeCode) {
		this.typeCode = typeCode;
	}
	
	@JsonIgnore
	@Override
	public void validate(ContextValidation contextValidation) {
		CommonValidationHelper.validateCodePrimary(contextValidation, this, InstanceConstants.PARAMETER_COLL_NAME);
		ValidationHelper      .validateNotEmpty   (contextValidation, categoryCode, "categoryCode");
		ValidationHelper      .validateNotEmpty   (contextValidation, name,         "name");
	}
	
	@Override
	public TraceInformation getTraceInformation() {
		if (traceInformation == null)
			traceInformation = new TraceInformation(); 
		return traceInformation;
	}
}
