package controllers;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;

import fr.cea.ig.lfw.LFWApplication;
import play.data.validation.ValidationError;
import play.i18n.Lang;
import play.libs.Scala;
import play.mvc.Controller;
import play.mvc.Result;
import play.routing.JavaScriptReverseRouter;

/**
 * Controller providing methods that are used across NGL controllers
 * but not tied to application domain objects.
 *   
 * @author vrd
 *
 */
class NGLBaseController extends Controller {
	
//	/**
//	 * NGL context.
//	 */
//	protected NGLContext ctx;
//	
//	@Inject
//	/**
//	 * DI constructor.
//	 * @param ctx NGL context
//	 */
//	public NGLBaseController(NGLContext ctx) {
//		this.ctx = ctx;
//	}
	
	/**
	 * NGL context.
	 */
	protected LFWApplication app;
	
	/**
	 * DI constructor.
	 * @param app NGL context
	 */
	@Inject
	public NGLBaseController(LFWApplication app) {
		this.app = app;
	}
	
	/**
	 * Get current user from HTTP context.
	 * @return current user
	 */
	protected String getCurrentUser(){
		return app.currentUser();
	}
	
	/**
	 * Formats errors as a proper JSON result.
	 * @param errors errors to format
	 * @return       JSON formatted errors
	 */
	public JsonNode errorsAsJson(Map<String, List<ValidationError>> errors) {
		return app.errorsAsJson(errors);
	}
	
	/**
	 * Formats errors as a proper JSON result.
	 * @param errors errors to format
	 * @param lang	 lang to format messages
	 * @return       JSON formatted errors
	 */
	public JsonNode errorsAsJson(Map<String, List<ValidationError>> errors, Lang lang) {
		return app.errorsAsJson(lang, errors);
	}
	
	/**
	 * Javascript routes.
	 * @param routes routes to provide as javascript
	 * @return       routes javascript
	 */
	public Result jsRoutes(play.api.routing.JavaScriptReverseRoute... routes) {
		return ok(JavaScriptReverseRouter.create("jsRoutes",routes)).as("text/javascript");
	}

	public Result jsMessages() {
//		return ok(app.jsMessages().apply(Scala.Option("Messages"), 
		return ok(app.jsMessages().apply(Scala.Option("Messages"), 
				                         jsmessages.japi.Helper.messagesFromCurrentHttpContext())).as("application/javascript");
	}

}
