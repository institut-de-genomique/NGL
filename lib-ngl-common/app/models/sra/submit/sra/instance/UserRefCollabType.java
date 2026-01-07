package models.sra.submit.sra.instance;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserRefCollabType {
	private String refCollabName = null;
	private String studyAc = null;
	private String sampleAc = null;
	
	public UserRefCollabType() {
	}
	
	@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
	public UserRefCollabType(@JsonProperty("alias") String alias, @JsonProperty("studyAc") String studyAc, @JsonProperty("sampleAc") String sampleAc) {
		this.refCollabName    = alias;
		this.studyAc  = studyAc;
		this.sampleAc = sampleAc;
	}	
	
	public UserRefCollabType(String alias) {
		this.setRefCollabName(alias);
	}

	public String getRefCollabName() {
		return refCollabName;
	}

	public void setRefCollabName(String alias) {
		this.refCollabName = alias;
	}

	public String getStudyAc() {
		return studyAc;
	}

	public void setStudyAc(String studyAc) {
		this.studyAc = studyAc;
	}

	public String getSampleAc() {
		return sampleAc;
	}

	public void setSampleAc(String sampleAc) {
		this.sampleAc = sampleAc;
	}
	
	@Override
	public boolean equals(Object autre) {
		if (autre == null) {
			return false;
		}
		if (this.getClass() != autre.getClass()) { 
			return false;
		}
		UserRefCollabType autreUserRefCollabType = (UserRefCollabType) autre;
		return this.refCollabName.equals(autreUserRefCollabType.refCollabName);
	}
	
	@Override
	public int hashCode() {
		return refCollabName.hashCode();
	}
	
}
