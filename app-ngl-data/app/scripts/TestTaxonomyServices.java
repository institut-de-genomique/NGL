package scripts;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import services.taxonomy.Taxon;
import services.taxonomy.TaxonomyServices;

//http://localhost:9000/scripts/run/scripts.TestTaxonomyServices?taxonId=344338
public class TestTaxonomyServices extends Script<TestTaxonomyServices.Args>{

	public static class Args {
		public String taxonId;
	}

	private TaxonomyServices taxonomyServices;
	private static final play.Logger.ALogger logger = play.Logger.of(TestTaxonomyServices.class);

	@Inject
	public TestTaxonomyServices(TaxonomyServices taxonomyServices) {
		this.taxonomyServices = taxonomyServices;
	}

	@Override
	public void execute(Args args) throws Exception {
		String taxonId = args.taxonId;

		printfln("taxonId=%s", taxonId);
		logger.debug("taxonId = " + taxonId);	

		Taxon taxon = taxonomyServices.getTaxon(taxonId);
		if (taxon == null) {
			logger.debug("Aucun taxon retourne par le service");
			return;
		}
		if(StringUtils.isNotBlank(taxon.errorMessage)) {
			printfln("Pour le taxonId=%s, taxon.error = %s, taxon.errorMessage=%s", taxon.code, taxon.error, taxon.errorMessage); 
		} else {
			printfln("Pour le taxonId=%s, taxon.error = %s",taxon.code,taxon.error); 
		}
		if(StringUtils.isNotBlank(taxon.scientificName)) {
			printfln("Pour le taxonId=%s, taxon.scientificName = %s",taxon.code, taxon.scientificName); 
		}
		if(StringUtils.isNotBlank(taxon.lineage)) {
			printfln("Pour le taxonId=%s, taxon.lineage = %s",taxon.code, taxon.lineage); 
		}
		if(taxon.submittable) {
			printfln("Pour le taxonId=%s, taxon.submittable = true ",taxon.code); 
		} else {
			printfln("Pour le taxonId=%s, taxon.submittable = false ",taxon.code); 
		}
	}
}
