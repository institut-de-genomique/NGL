package controllers.main.tpl;

import javax.inject.Inject;

import jsmessages.JsMessages;
import play.libs.Scala;
import play.mvc.Controller;
import play.mvc.Result;

public class Main extends Controller{

	private final JsMessages messages;

	@Inject
	public Main(jsmessages.JsMessagesFactory jsMessagesFactory) {
		messages  = jsMessagesFactory.all();
	}


//	final static JsMessages messages = JsMessages.create(play.Play.application());	

	public Result jsMessages() {
//		return ok(messages.generate("Messages")).as("application/javascript");
		return ok(messages.apply(Scala.Option("Messages"), jsmessages.japi.Helper.messagesFromCurrentHttpContext())).as("application/javascript");
	}
}

