package models.sra.submit.sra.instance;

public class EbiStudyIdentifiersInExperiment {
	private String alias = null;
	private String accession = null;
	private String studyAccession = null;
	private String studyExternalId = null;
	
	public EbiStudyIdentifiersInExperiment() {
	}
	
	public EbiStudyIdentifiersInExperiment(String alias) {
		this.setAlias(alias);
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getAccession() {
		return accession;
	}

	public void setAccession(String accession) {
		this.accession = accession;
	}
	public String getStudyAccession() {
		return studyAccession;
	}

	public void setStudyAccession(String studyAccession) {
		this.studyAccession = studyAccession;
	}
	
	public String getStudyExternalId() {
		return studyExternalId;
	}

	public void setStudyExternalId(String studyExternalId) {
		this.studyExternalId = studyExternalId;
	}
	
}
