package models.sra.submit.sra.instance;

public class EbiIdentifiers {
	private String alias = null;
	private String accession = null;
	private String externalId = null;
	
	public EbiIdentifiers() {
	}
	
	public EbiIdentifiers(String alias) {
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

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}
	
	
	
}
