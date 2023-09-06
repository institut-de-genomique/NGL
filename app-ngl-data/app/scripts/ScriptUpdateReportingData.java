package scripts;

import javax.inject.Inject;

import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.NGLConfig;
import models.Constants;
import play.Logger;
import services.instance.sample.UpdateReportingData;
import validation.ContextValidation;

public class ScriptUpdateReportingData extends Script<ScriptUpdateReportingData.Args>{

	public static class Args {
		
	}

	private final NGLApplication app;

	private final NGLConfig config;

	@Inject
	public ScriptUpdateReportingData(NGLApplication app, NGLConfig config) {
		this.app = app;
		this.config = config;
	}

	@Override
	public void execute(Args args) throws Exception {
		Logger.info("Start ScriptUpdateReportingData");

		ContextValidation contextError = ContextValidation.createUndefinedContext(Constants.NGL_DATA_USER);

		UpdateReportingData update = new UpdateReportingData(app, config);
		update.runImport(contextError);		

		Logger.info("End ScriptUpdateReportingData");
	}
}
