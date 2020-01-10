package controllers.main.tpl;

import javax.inject.Inject;

import controllers.NGLController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.lfw.Historized;
import fr.cea.ig.lfw.utils.JavascriptGeneration.Codes;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.support.NGLJavascript;
import play.mvc.Result;
import views.html.home;

//import controllers.CommonController;
//public class Main extends CommonController {
public class Main extends NGLController
                 implements NGLJavascript {
	
	//final static JsMessages messages = JsMessages.create(play.Play.application());

	// private JsMessages messages;

	private home home;

//	@Inject
//	public Main(jsmessages.JsMessagesFactory jsMessagesFactory, home home) {
//		messages = jsMessagesFactory.all();
//		this.home = home;
//	}

	@Inject
	public Main(NGLApplication app, home home) {
		super(app);
		this.home = home;
	}
	
	@Authenticated
	@Historized
	@Authorized.Read
	public Result home() {
		return ok(home.render());
	}

	public Result jsCodes() {
		return new Codes()
				.addValuationCodes()
				.asCodeFunction();
	}
	
//	public Result jsMessages() {
//		// return ok(messages.generate("Messages")).as("application/javascript");
//		//return ok(messages.all(Scala.Option("Messages"))).as("application/javascript");
//		return ok(messages.apply(Scala.Option("Messages"), jsmessages.japi.Helper.messagesFromCurrentHttpContext()));
//	}

//	public Result jsCodes() {
//		return ok(generateCodeLabel()).as("application/javascript");
//	}
//
//	private static String generateCodeLabel() {
//
//		StringBuilder sb = new StringBuilder();
//		sb.append("Codes=(function(){var ms={");
//
//		sb.append("\"valuation.TRUE\":\"Oui\",");
//		sb.append("\"valuation.FALSE\":\"Non\",");
//		sb.append("\"valuation.UNSET\":\"---\"");
//		sb.append("};return function(k){if(typeof k == 'object'){for(var i=0;i<k.length&&!ms[k[i]];i++);var m=ms[k[i]]||k[0]}else{m=ms[k]||k}for(i=1;i<arguments.length;i++){m=m.replace('{'+(i-1)+'}',arguments[i])}return m}})();");
//		return sb.toString();
//	}
	
}
