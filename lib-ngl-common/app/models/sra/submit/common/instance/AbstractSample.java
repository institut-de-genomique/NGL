package models.sra.submit.common.instance;

import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import validation.IValidation;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import fr.cea.ig.DBObject;

@JsonTypeInfo(use=Id.NAME, include=As.EXTERNAL_PROPERTY, property="_type", defaultImpl=models.sra.submit.common.instance.Sample.class)
@JsonSubTypes({
	@JsonSubTypes.Type(value =  models.sra.submit.common.instance.Sample.class, name = AbstractSample.sampleType),
	@JsonSubTypes.Type(value =  models.sra.submit.common.instance.ExternalSample.class, name = AbstractSample.externalSampleType),
})
public abstract class AbstractSample extends DBObject implements IValidation {
	public static final String sampleType = "Sample";
	public static final String externalSampleType = "ExternalSample";
	
	public String _type;
	public String accession;       // numeros d'accession attribué par ebi 
	public State state = new State();// Reference sur "models.laboratory.common.instance.state" 
	 // pour gerer les differents etats de l'objet.(new, inwaiting, inprogress, submitted)
	
	public String adminComment; // commentaire privé "reprise historique"
	public TraceInformation traceInformation = new TraceInformation();
	
	public AbstractSample(String _type) {
//		super();
		this._type=_type;
	} 
}
