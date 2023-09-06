package fr.cea.ig.lfw.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;

import fr.cea.ig.lfw.LFWApplicationHolder;
import fr.cea.ig.lfw.utils.JavascriptGeneration;
import play.data.validation.ValidationError;
import play.i18n.Lang;
import play.i18n.MessagesApi;
import play.libs.Json;
import play.libs.Scala;
import play.mvc.Result;
import play.mvc.Results;
import play.routing.JavaScriptReverseRouter;

public interface LFWJavascript extends LFWApplicationHolder {

	default Result jsPermissions(Iterable<String> i) {
		return jsPermissions(i, x -> x);
	}
	
	default <T> Result jsPermissions(Iterable<T> i, Function<T,String> f) {
		return JavascriptGeneration.Permissions.jsPermissions(i,f);
	}

	/**
	 * Javascript routes.
	 * @param routes routes to provide as javascript
	 * @return       routes javascript
	 */
	default Result jsRoutes(play.api.routing.JavaScriptReverseRoute... routes) {
		return Results.ok(JavaScriptReverseRouter.create("jsRoutes",routes)).as("text/javascript");
	}

	default Result jsMessages() {
		return Results.ok(getLFWApplication().jsMessagesFactory().all().apply(Scala.Option("Messages"), 
				                         jsmessages.japi.Helper.messagesFromCurrentHttpContext())).as("application/javascript");
	}

	default Lang currentLang() {
		if (play.mvc.Http.Context.current() != null)
			return play.mvc.Http.Context.current().lang();
		return null;
	}

	/**
	 * Returns form errors serialized as JSON.
	 * @param errors errors to format
	 * @return       JSON formatted errors
	 */
	default JsonNode errorsAsJson(Map<String, List<ValidationError>> errors) {
		// return errorsAsJson(play.mvc.Http.Context.current() != null ? play.mvc.Http.Context.current().lang() : null, errors);
		return errorsAsJson(currentLang(), errors);
	}

	/**
	 * Returns form errors serialized as JSON using the given Lang.
	 * @param  lang   language to use for the errors 
	 * @param  errors errors
	 * @return JSON node built from the given errors
	 */
	default JsonNode errorsAsJson(Lang lang, Map<String, List<ValidationError>> errors) {
		MessagesApi messagesApi = getLFWApplication().messagesApi();
		Map<String, List<String>> allMessages = new java.util.HashMap<>();
		errors.forEach((key, errs) -> {
			if (errs != null && !errs.isEmpty()) {
				List<String> messages = new ArrayList<>();
				for (ValidationError error : errs) {
					if (messagesApi != null && lang != null) {
						messages.add(messagesApi.get(lang, error.messages(), translateMsgArg(error.arguments(), messagesApi, lang)));
					} else {
						messages.add(error.message());
					}
				}
				allMessages.put(key, messages);
			}
		});
		return Json.toJson(allMessages);
	}

	default Object translateMsgArg(List<Object> arguments, MessagesApi messagesApi, Lang lang) {
		if (arguments != null) {
			return arguments.stream().map(arg -> {
				if (arg instanceof String) {
					return messagesApi != null ? messagesApi.get(lang, (String)arg) : (String)arg;
				}
				if (arg instanceof List) {
					return ((List<?>) arg).stream().map(key -> messagesApi != null ? messagesApi.get(lang, (String)key) : (String)key).collect(Collectors.toList());
				}
				return arg;
			}).collect(Collectors.toList());
		} else {
			return null;
		}
	}

	// -- single error translation
	default JsonNode errorAsJson(String message, Object... args) {
		return errorAsJson(currentLang(), "error", message, Arrays.asList(args));
	}
	
	default JsonNode errorAsJson(Lang lang, String key, String message, Object... args) {
		return errorAsJson(lang, key, message, Arrays.asList(args));
	}
	
	default JsonNode errorAsJson(Lang lang, String key, String message, List<Object> args) {
		MessagesApi messagesApi = getLFWApplication().messagesApi();
		String tMessage = messagesApi.get(lang, message, translateMsgArg(args, messagesApi, lang));
		Map<String, String> jMessage = new java.util.HashMap<>();
		jMessage.put(key,tMessage);
		return play.libs.Json.toJson(tMessage);
	}

}
