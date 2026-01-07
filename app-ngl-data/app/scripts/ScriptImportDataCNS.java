package scripts;

import javax.inject.Inject;

import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.ngl.NGLApplication;
import play.Logger;
import services.instance.balancesheet.UpdateDailyBalanceSheet;
import services.instance.balancesheet.UpdateMonthlyBalanceSheet;
import services.instance.container.TubeImportCNS;
import services.instance.project.ProjectImportCNG;
import services.instance.project.ProjectImportCNS;
import services.instance.sample.UpdateSampleAtlaSea;
import services.instance.sample.UpdateSampleCNS;
import services.instance.sample.UpdateSampleNCBITaxonCNS;
import services.taxonomy.TaxonomyServices;


/**
 * Script qui simule l'execution de NGL DATA séquentiellement
 * 
 * @author ejacoby
 *
 */


public class ScriptImportDataCNS extends Script<ScriptImportDataCNS.Args>{

	public static class Args {
		
	}

	private final NGLApplication app;
	
	@Inject
	public ScriptImportDataCNS(NGLApplication app) {
		super();
		this.app=app;
	}
	
	// Merci de laisser cette methode ici car permet dans execute() d'utiliser les appels suivants :
	// 'app.injector().instanceOf(SubmissionAtlaseaProjectsToEbi.class).run();' ou 
	// 'run(SubmissionAtlaseaProjectsToEbi.class);' qui est beaucoup plus lisible et qui permet de ne pas se préoccuper de l'instanciation des classes passées en paramètre du cron.
	public <T extends Runnable> void run(Class<T> c) {
		app.injector().instanceOf(c).run();
	}
	
	@Override
	public void execute(Args args) throws Exception {
		
		//new UpdateSampleNCBITaxonCNS(app,new TaxonomyServices(app)).run();
		new UpdateSampleAtlaSea(app).run();
//		run(SubmissionAtlaseaProjectsToEbi.class);  //ok marche
	}
}
