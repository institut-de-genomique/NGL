package scripts.ATSea.model;

public class TaxonInfo {

	
	public String taxonName;
	public Integer taxonId;
	public Long genomeSize;
	
	
	
	
	public TaxonInfo(String taxonName, Integer taxonId, Long taxonSize) {
		this.taxonName=taxonName;
		this.taxonId=taxonId;
		this.genomeSize=taxonSize;
	}
}
