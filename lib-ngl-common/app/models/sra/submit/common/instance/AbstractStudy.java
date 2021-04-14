package models.sra.submit.common.instance;

import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import validation.IValidation;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import fr.cea.ig.DBObject;

@JsonTypeInfo(use=Id.NAME, include=As.EXTERNAL_PROPERTY, property="_type", defaultImpl=models.sra.submit.common.instance.Study.class)
@JsonSubTypes({
	@JsonSubTypes.Type(value =  models.sra.submit.common.instance.Study        .class, name = AbstractStudy.studyType),
	@JsonSubTypes.Type(value =  models.sra.submit.common.instance.ExternalStudy.class, name = AbstractStudy.externalStudyType),
})
public abstract class AbstractStudy extends DBObject implements IValidation, IStateReference {

	public static final String studyType         = "Study";
	public static final String externalStudyType = "ExternalStudy";
	
	public String _type;
	public String accession;       // numeros d'accession attribué par ebi 
	public State state = new State();// Reference sur "models.laboratory.common.instance.state" 
	 // pour gerer les differents etats de l'objet.(new, inwaiting, inprogress, submitted)
	
	public String adminComment; // commentaire privé "reprise historique"
	public TraceInformation traceInformation = new TraceInformation();
	
	public AbstractStudy(String _type) {
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
