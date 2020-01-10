package services.instance.sample;

import javax.inject.Inject;

import fr.cea.ig.ngl.NGLApplication;
import services.ncbi.TaxonomyServices;

public class UpdateSampleNCBITaxonCNG extends AbstractUpdateSampleNCBITaxon {

	@Inject
	public UpdateSampleNCBITaxonCNG(NGLApplication app, TaxonomyServices taxonomyServices) {
		super("UpdateSampleNCBI", app, taxonomyServices);
	}

}
