package services.taxonomy;


public class Taxon {
	
	//private static final play.Logger.ALogger logger = play.Logger.of(Taxon.class);
	
	public  String   code;
	public  String   errorMessage;          // l'information renvoyée par le serveur mentionne une erreur (dans le xml ou la string renvoyée)
	public  boolean  error;                 // l'information du taxon existe bien sur le serveur du NCBI ou de l'EBI
	public String    lineage;
	public String    scientificName;
	//public List<String> akaTaxIds;        // alias ou aka de taxonId : plusieurs taxons peuvent correspondrent à la meme donnée
	public String    akaTaxId;              // alias ou aka de taxonId : plusieurs taxons peuvent correspondrent à la meme donnée normalement, mais 
										    // cas jamais rencontré. 
	public  boolean  submittable;           // notion qui existe uniquement à l'EBI
	public String    rank;                  // notion qui existe uniquement au NCBI et qui indique le rang dans la taxonomie
											// on peut utiliser cette notion pour estimer si un taxon est submittable voir script BilanTaxonInCollectionSamples : 
	                                        // les rank species, subspecies, strain, genotype et varietas sont submittable, tandis que les autres rangs ne le sont pas.
										    // la valeur "no rank" ne permet pas de deduire si le taxon est submittable.
												
	
	public Taxon(String code) {
		this.code = code;
		this.error = false;
		this.errorMessage = "";
	}
	
	public String getScientificName() {
		return this.scientificName;
	}
	public String getLineage() {
		return this.lineage;
	}
		
	
}