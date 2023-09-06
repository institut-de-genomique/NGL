package scripts;

import javax.inject.Inject;

import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.NGLConfig;
import models.Constants;
import play.Logger;
import services.instance.sample.UpdateFlagForReportingData;
import validation.ContextValidation;

/**
 * Script permettant de détecter les objets récemments modifiés pour qu'ils soient pris en charge par le reporting nocturne de data.
 * 
 * @author jcharpen - Jordi CHARPENTIER - jcharpen@genoscope.cns.fr
 */
public class ScriptUpdateFlagForReportingData extends Script<ScriptUpdateFlagForReportingData.Args> {
	public static class Args {

	}

	private NGLApplication app;

	private NGLConfig config;

	@Inject
	public ScriptUpdateFlagForReportingData(NGLApplication app, NGLConfig config) {
		this.app = app;
		this.config = config;
	}

	@Override
	public void execute(Args args) throws Exception {
		Logger.info("Start ScriptUpdateFlagForReportingData");

		ContextValidation contextError = ContextValidation.createUndefinedContext(Constants.NGL_DATA_USER);
		UpdateFlagForReportingData update = new UpdateFlagForReportingData(app, config);

		update.runImport(contextError);
				
		Logger.info("End ScriptUpdateFlagForReportingData");
	}
}