package controllers;

import java.util.function.Supplier;

import javax.inject.Inject;

import fr.cea.ig.lfw.LFWController;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.NGLApplicationHolder;
import fr.cea.ig.ngl.NGLConfig;
import fr.cea.ig.ngl.dao.api.APIValidationException;
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
	 * Standard bad request result.
	 * @param  message    message
	 * @return badRequest Result with standard message
	 */
	public Result nglGlobalBadRequest(String message) {
		if (message == null) 
			message = "no message";
		return badRequestAsJson("Error on server: contact support for more details ("
								+ message
								+ ")");
	}
	
	/**
	 * These method wraps the execution of the given supplier 
	 * to handle global exceptions (ie. RunTimeException)
	 * @param sup Supplier which produces the Result object
	 * @return    the final Result
	 */
	public Result globalExceptionHandler(Supplier<Result> sup) {
	    try {
	        return sup.get();
	    } catch (Exception e) {
	        getLogger().error(e.getMessage(), e);
	        return nglGlobalBadRequest(e.getMessage());
	    }
	}
	
	/**
     * Log exception and return a bad request response using {@link #badRequestAsJson(Object)}
     * @param e validation exception
     * @return bad request response in json format
     */
    protected Result badRequestLoggingForValidationException(APIValidationException e) {
        if (getLogger().isDebugEnabled()) getLogger().warn(e.getMessage(), e);
        if (e.getErrors() != null) {
            return badRequestAsJson(errorsAsJson(e.getErrors()));
        } else {
            return badRequestAsJson(e.getMessage());
        }
    }
}
