package fr.cea.ig.ngl;

import javax.inject.Inject;

import fr.cea.ig.lfw.LFWController;
import play.mvc.Result;

public class NGLController extends LFWController implements NGLApplicationHolder {

	private final NGLApplication app;
	
	@Inject
	public NGLController(NGLApplication app) {
		super(app);
		this.app = app;
	}
	
	@Override
	public NGLApplication getNGLApplication() {
		return app; 
	}
	
	public NGLConfig getConfig() {
		return app.nglConfig();
	}
	
	/**
	 * @return badRequest Result with standard message
	 */
	public Result nglGlobalBadRequest() {
		return badRequestAsJson("Error on server: contact support for more details");
	}
	
}
