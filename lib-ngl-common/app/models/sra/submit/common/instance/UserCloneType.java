package models.sra.submit.common.instance;

public class UserCloneType {
	private String alias = null;
	private String studyAc = null;
	private String sampleAc = null;
	
	public UserCloneType() {
	}
	
	public UserCloneType(String alias) {
		this.setAlias(alias);
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
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
	
	
	
}
