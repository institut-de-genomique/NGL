package scripts;

import javax.inject.Inject;

import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.ngl.NGLApplication;
import play.Logger;
import services.instance.sample.UpdateSampleNCBITaxonCNS;
import services.taxonomy.TaxonomyServices;

// http://localhost:9000/scripts/run/scripts.ScriptUpdateSampleNCBITaxonCNS
public class ScriptUpdateSampleNCBITaxonCNS extends Script<ScriptUpdateSampleNCBITaxonCNS.Args> {
	
	private final NGLApplication app;
	public static class Args {
	}

	@Inject
	public ScriptUpdateSampleNCBITaxonCNS( NGLApplication app) {
		this.app=app;
	}

	@Override
	public void execute(Args args) throws Exception {
		Logger.debug("Start updateSampleNCBITaxon");
		new UpdateSampleNCBITaxonCNS(app, new TaxonomyServices(app)).run();
		Logger.debug("End of updateSampleNCBITaxon");	
	}
}
