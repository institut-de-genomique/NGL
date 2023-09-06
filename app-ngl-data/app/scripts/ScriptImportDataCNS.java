package scripts;

import javax.inject.Inject;

import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.ngl.NGLApplication;
import play.Logger;
import services.instance.project.ProjectImportCNS;


/**
 * Script qui simule l'execution de NGL DATA séquentiellement
 * Uniquement CNG
 * 
 * @author ejacoby
 *
 */
public class ScriptImportDataCNS extends Script<ScriptImportDataCNS.Args>{

	//TODO prevoir de mettre en argument l'institut CNS/CNG pour un script commun
	public static class Args {

	}

	private final NGLApplication app;
	
	@Inject
	public ScriptImportDataCNS(NGLApplication app) {
		super();
		this.app=app;
	}

	@Override
	public void execute(Args args) throws Exception {
		
		Logger.debug("Start projectImport");
	//	new ProjectImportCNG(app).run();
		/* Logger.debug("Start projectImport"); */
		new ProjectImportCNS(app).run();
		/** Logger.debug("Start tubeImport");
		new TubeImportCNS(app).run();
		Logger.debug("Start updateSample");
		new UpdateSampleCNS(app).run();
		/*Logger.debug("Start BanqueAmpliImport");
		new BanqueAmpliImportCNS(app).run();
		Logger.debug("Start sizingImport");
		new SizingImportCNS(app).run();
		Logger.debug("Start updateSampleNCBITaxon");
		new UpdateSampleNCBITaxonCNS(app, new TaxonomyServices(app)).run();
		Logger.debug("Start updateSampleProperties");
		new UpdateSamplePropertiesCNS(app).run();*/
		
	}

}
