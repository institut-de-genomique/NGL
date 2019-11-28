package controllers.main.tpl;

import javax.inject.Inject;

import controllers.NGLController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.lfw.Historized;
import fr.cea.ig.lfw.utils.JavascriptGeneration.Codes;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.support.Executor;
import fr.cea.ig.ngl.support.NGLJavascript;
import fr.cea.ig.ngl.support.api.CodeLabelAPIHolder;
import fr.cea.ig.ngl.support.api.ProtocolAPIHolder;
import play.mvc.Result;
import views.html.home ;

// public class Main extends -CommonController {
public class Main extends NGLController
               implements CodeLabelAPIHolder,
                          ProtocolAPIHolder,
                          NGLJavascript,
                          Executor { // Controller {

	//final static JsMessages messages = JsMessages.create(play.Play.application());	
	// private final JsMessages messages;
	private final home home;
	
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
	public Result home() {
		return ok(home.render());
	}
	
	/*public Result jsMessages() {
		// return ok(messages.generate("Messages")).as("application/javascript");
		// return ok(messages.all(Scala.Option("Messages"))).as("application/javascript");
		// return ok(messages.apply(Scala.Option("Messages"), jsmessages.japi.Helper.messagesFromCurrentHttpContext()));
	}*/
	
	public Result jsCodes() {
		return result(
				() -> new Codes()
					// .add(Spring.getBeanOfType(CodeLabelDAO.class).findAll(), x -> x.tableName, x -> x.code, x -> x.label)
					.add(getCodeLabelAPI().all(), x -> x.tableName, x -> x.code, x -> x.label)
					.addValuationCodes()
					.addStatusCodes()
					// .add(MongoDBDAO.find(InstanceConstants.PROTOCOL_COLL_NAME,Protocol.class).toList(), x -> "protocol", x -> x.code, x -> x.name)
					.add(getProtocolAPI().all(), x -> "protocol", x -> x.code, x -> x.name)
					.asCodeFunction(),
				"error while building codes");
	}
	
	/*
	public Result jsCodes() {
		Codes codes = new Codes();
		codes.mapDotColon(Spring.getBeanOfType(CodeLabelDAO.class).findAll(),
				x -> x.tableName, x -> x.code, x -> x.label);
		codes.dotColon("valuation", "TRUE",  "Oui")
			 .dotColon("valuation", "FALSE", "Non")
			 .dotColon("valuation", "UNSET", "---")
			 .dotColon("status",    "TRUE",  "OK" )
			 .dotColon("status",    "FALSE", "KO" )
			 .dotColon("status",    "UNSET", "---");
		codes.mapDotColon(MongoDBDAO.find(InstanceConstants.PROTOCOL_COLL_NAME,Protocol.class).toList(),
				x -> "protocol", x -> x.code, x -> x.name);
		return codes.asCodeFunction();
	}
	*/
	
	/*
	public Result jsCodes() {
		return ok(generateCodeLabel()).as("application/javascript");
	}

	private static String generateCodeLabel() {
		CodeLabelDAO dao = Spring.getBeanOfType(CodeLabelDAO.class);
		List<CodeLabel> list = dao.findAll();

		StringBuilder sb = new StringBuilder();
		sb.append("Codes=(function(){var ms={");
		for(CodeLabel cl : list){
			sb.append("\"").append(cl.tableName).append(".").append(cl.code)
			.append("\":\"").append(cl.label).append("\",");
		}
		sb.append("\"valuation.TRUE\":\"Oui\",");
		sb.append("\"valuation.FALSE\":\"Non\",");
		sb.append("\"valuation.UNSET\":\"---\",");

		sb.append("\"status.TRUE\":\"OK\",");
		sb.append("\"status.FALSE\":\"KO\",");
		sb.append("\"status.UNSET\":\"---\",");

		List<Protocol> protocols = MongoDBDAO.find(InstanceConstants.PROTOCOL_COLL_NAME,Protocol.class).toList();
		for(Protocol protocol:protocols){
			sb.append("\"").append("protocol").append(".").append(protocol.code).append("\":\"").append(protocol.name).append("\",");
		}

		sb.append("};return function(k){if(typeof k == 'object'){for(var i=0;i<k.length&&!ms[k[i]];i++);var m=ms[k[i]]||k[0]}else{m=ms[k]||k}for(i=1;i<arguments.length;i++){m=m.replace('{'+(i-1)+'}',arguments[i])}return m}})();");
		return sb.toString();
	}
*/
	
}
