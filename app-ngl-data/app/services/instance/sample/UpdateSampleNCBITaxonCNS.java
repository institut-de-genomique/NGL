package services.instance.sample;

import javax.inject.Inject;

import fr.cea.ig.ngl.NGLApplication;
import services.taxonomy.TaxonomyServices;

public class UpdateSampleNCBITaxonCNS extends AbstractUpdateSampleNCBITaxon {

//	@Inject
//	public UpdateSampleNCBITaxonCNS(FiniteDuration durationFromStart,
//			FiniteDuration durationFromNextIteration, NGLContext ctx, TaxonomyServices taxonomyServices) {
//		super("UpdateSampleNCBI", durationFromStart, durationFromNextIteration, ctx, taxonomyServices);
//
//	}

//	@Inject
//	public UpdateSampleNCBITaxonCNS(NGLContext ctx, TaxonomyServices taxonomyServices) {
//		super("UpdateSampleNCBI", ctx, taxonomyServices);
//	}
	
	@Inject
	public UpdateSampleNCBITaxonCNS(NGLApplication app, TaxonomyServices taxonomyServices) {
		super("UpdateSampleNCBI", app, taxonomyServices);
	}

}
