package models.sra.submit.sra.instance;

import models.laboratory.common.instance.Comment;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import validation.IValidation;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import fr.cea.ig.DBObject;

@JsonTypeInfo(use=Id.NAME, include=As.EXTERNAL_PROPERTY, property="_type", defaultImpl=models.sra.submit.sra.instance.Sample.class)
@JsonSubTypes({
	@JsonSubTypes.Type(value =  models.sra.submit.sra.instance.Sample        .class, name = AbstractSample.sampleType),
	@JsonSubTypes.Type(value =  models.sra.submit.sra.instance.ExternalSample.class, name = AbstractSample.externalSampleType),
})
public abstract class AbstractSample extends DBObject implements IValidation, IStateReference {

	public static final String sampleType         = "Sample";
	public static final String externalSampleType = "ExternalSample";
	
	public String _type;
	public String accession;       // numeros d'accession attribué par ebi de la forme ERS
	public String externalId;      // numeros d'accession attribué par la base BioSample de la forme SAM
	public State state = new State();// Reference sur "models.laboratory.common.instance.state" 
	 // pour gerer les differents etats de l'objet.(new, inwaiting, inprogress, submitted)
	
	public String adminComment; // commentaire privé "reprise historique"
	public TraceInformation traceInformation = new TraceInformation();
    public List<Comment>comments = new ArrayList<Comment>();// commentaires des utilisateurs.

	public AbstractSample(String _type) {
		this._type=_type;
	} 


	@Override
	public State getState() {
		return state;
	}

	@Override
	public void setState(State state) {
		this.state = state;
	}

	
	@Override
	public TraceInformation getTraceInformation() {
		return traceInformation;
	}
	
	
}
