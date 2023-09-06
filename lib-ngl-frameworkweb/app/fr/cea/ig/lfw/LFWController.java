package fr.cea.ig.lfw;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;

import fr.cea.ig.authentication.Authentication;
import fr.cea.ig.lfw.support.LoggerHolder;
import play.Logger.ALogger;
import play.data.validation.ValidationError;
import play.i18n.Lang;
import play.i18n.Messages;
import play.inject.Injector;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

public class LFWController extends Controller implements LFWApplicationHolder, LoggerHolder {
	
	private final LFWApplication app;
	
	protected final ALogger logger;

	@Inject
	public LFWController(LFWApplication app) {
		this.app = app;
		logger = play.Logger.of(getClass());
	}
	
	@Override
	public LFWApplication getLFWApplication() { 
		return app; 
	}
	
	@Override
	public ALogger getLogger() {
		return logger;
	}
	
	public Injector getInjector() {
		return app.injector();
	}
	
	public String getCurrentUser() {
		return Authentication.isApiKeyUser() ? Authentication.getApiKeyUser() : Authentication.getUser();
	}
	
	public Result okAsJson(Object o) {
		return ok(Json.toJson(o)).as("application/json");
	}
	
	public Result notFoundAsJson(Object o) {
        return notFound(Json.toJson(o)).as("application/json");
    }
	
	public Result badRequestAsJson(Object o) {
		return badRequest(Json.toJson(o)).as("application/json");
	}
	public Result badRequestAsJson(String s) {
		return badRequest(Json.newObject().put("message", s)).as("application/json");
	}

	public Lang currentLang() {
		if (play.mvc.Http.Context.current() != null) {
			return play.mvc.Http.Context.current().lang();
		} else {
			return null;
		}
	}
	
	public Messages messages() {
		List<Lang> langs = new ArrayList<>();
		Lang lang = currentLang();
		if (lang != null) {
			langs.add(lang);
		}
		return getLFWApplication().messagesApi().preferred(langs);
	}
	/**
	 * Returns form errors serialized as JSON.
	 * @param errors errors to format
	 * @return       JSON formatted errors
	 */
	public JsonNode errorsAsJson(Map<String, List<ValidationError>> errors) {
		return errorsAsJson(currentLang(), errors);
	}

	/**
	 * Returns form errors serialized as JSON using the given Lang.
	 * @param  lang   language to use for the errors 
	 * @param  errors errors
	 * @return JSON node built from the given errors
	 */
	public JsonNode errorsAsJson(Lang lang, Map<String, List<ValidationError>> errors) {
		Map<String, List<String>> allMessages = new java.util.HashMap<>();
		errors.forEach((key, errs) -> {
			if (errs != null && !errs.isEmpty()) {
				List<String> messages = new ArrayList<>();
				for (ValidationError error : errs) {
					if (getLFWApplication().messagesApi() != null && lang != null) {
						messages.add(getLFWApplication().messagesApi().get(lang, error.messages(), translateMsgArg(error.arguments(), lang)));
					} else {
						messages.add(error.message());
					}
				}
				allMessages.put(key, messages);
			}
		});
		return Json.toJson(allMessages);
	}

	private Object translateMsgArg(List<Object> arguments, Lang lang) {
		if (arguments != null) {
			return arguments.stream().map(arg -> {
				if (arg instanceof String) {
					return getLFWApplication().messagesApi() != null ? getLFWApplication().messagesApi().get(lang, (String)arg) : (String)arg;
				}
				if (arg instanceof List) {
					return ((List<?>) arg).stream().map(key -> getLFWApplication().messagesApi() != null ? getLFWApplication().messagesApi().get(lang, (String)key) : (String)key).collect(Collectors.toList());
				}
				return arg;
			}).collect(Collectors.toList());
		} else {
			return null;
		}
	}

	// -- single error translation
	public JsonNode errorAsJson(String message, Object... args) {
		return errorAsJson(currentLang(), "error", message, Arrays.asList(args));
	}
	
	public JsonNode errorAsJson(Lang lang, String key, String message, Object... args) {
		return errorAsJson(lang, key, message, Arrays.asList(args));
	}
	
	public JsonNode errorAsJson(Lang lang, String key, String message, List<Object> args) {
		String tMessage = getLFWApplication().messagesApi().get(lang, message, translateMsgArg(args, lang));
		Map<String, String> jMessage = new java.util.HashMap<>();
		jMessage.put(key,tMessage);
		return play.libs.Json.toJson(tMessage);
	}

	// Should implement proper error reporting, does shit atm.
	/*public static Result failure(play.Logger.ALogger logger, String message, Throwable t) {
		logger.error(message,t);
		throw new RuntimeException(t);
	}*/
	
}
