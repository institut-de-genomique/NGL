package controllers.authorisation;

import javax.inject.Inject;

import controllers.NGLController;
import fr.cea.ig.ngl.NGLApplication;
import play.mvc.Result;

public class User extends NGLController{

	@Inject
	public User(NGLApplication app) {
		super(app);
	}

	public Result get() {
		return okAsJson(getCurrentUser());
	}
	
}
