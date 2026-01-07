package scripts.ATSea.model;

import java.util.HashSet;
import java.util.Set;

public class TaxonData {

	public String taxonName;
	public Integer aphiaIDWorms;
	public String goatStatus;
	public String phylum;
	
	//Data for recollect
	public Integer nbIndividuals;
	public Integer nbContainers;
	public Integer nbCollect;
	public Integer totalWeight;
	public Integer meanWeight;
	public Set<String> listCollectDate = new HashSet<String>();
	
	public TaxonData() {
		super();
	}


	public TaxonData(String taxonName, Integer aphiaIDWorms, String goatStatus) {
		super();
		this.taxonName = taxonName;
		this.aphiaIDWorms = aphiaIDWorms;
		this.goatStatus = goatStatus;
	}


	public TaxonData(String taxonName, Integer nbIndividuals, Integer nbContainers, Integer nbCollect,
			Integer totalWeight, Integer meanWeight) {
		super();
		this.taxonName = taxonName;
		this.nbIndividuals = nbIndividuals;
		this.nbContainers = nbContainers;
		this.nbCollect = nbCollect;
		this.totalWeight = totalWeight;
		this.meanWeight = meanWeight;
	}


	public Set<String> getListCollectDate() {
		return listCollectDate;
	}


	public void setListCollectDate(Set<String> listCollectDate) {
		this.listCollectDate = listCollectDate;
	}


	public String getPhylum() {
		return phylum;
	}


	public void setPhylum(String phylum) {
		this.phylum = phylum;
	}
	
	
}
