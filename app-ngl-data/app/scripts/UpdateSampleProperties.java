package scripts;

import javax.inject.Inject;

import fr.cea.ig.lfw.controllers.scripts.Script;
import fr.cea.ig.ngl.NGLApplication;
import services.instance.sample.UpdateSamplePropertiesCNS;

public class UpdateSampleProperties extends Script<UpdateSampleProperties.Args>{

	public class Args {

	}

	private final NGLApplication app;
	
	@Inject
	public UpdateSampleProperties(NGLApplication app) {
		super();
		this.app=app;
	}

	@Override
	public void execute(Args args) throws Exception {
		UpdateSamplePropertiesCNS update = new UpdateSamplePropertiesCNS(app);
		update.run();
	}

}
