package fr.cea.ig.play.migration;

// import java.awt.font.ImageGraphicAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.fasterxml.jackson.databind.JsonNode;

//import akka.actor.ActorRef;
//import akka.actor.ActorRefFactory;
import akka.actor.ActorSystem;
import fr.cea.ig.play.IGGlobals;
//import akka.actor.Props;
import play.i18n.Lang;
import play.i18n.MessagesApi;
import play.inject.Injector;
import play.libs.Json;
import play.libs.ws.WSClient;
import rules.services.LazyRules6Actor;
// import rules.services.RulesActor6;
import rules.services.RulesServices6;
import play.i18n.Messages;
import play.cache.SyncCacheApi;
// import play.Logger;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.data.validation.ValidationError;
import play.data.Form;

//import fr.cea.ig.play.NGLConfig;
// import io.jsonwebtoken.lang.Collections;
import jsmessages.JsMessages;
import jsmessages.JsMessagesFactory;

// TODO: clean, comment

/**
 * Class to help transition from the old style globals to new style
 * DI. An instance of this class is supposed to be injected instead of
 * using IGGlobals.
 * 
 * @author vrd
 *
 */
@Singleton
public class NGLContext {

//	/**
//	 * Logger.
//	 */
//	private static final play.Logger.ALogger logger = play.Logger.of(NGLContext.class);
	
	/**
	 * NGL play configuration wrapper.
	 */
	private final NGLConfig config;
	
	/**
	 * I18N.
	 */
	private final MessagesApi messagesApi;
	
	/**
	 * Form factory.
	 */
	private final FormFactory formFactory;
	
	
	/**
	 * Akka ActorSystem.
	 */
	private final ActorSystem actor;
	
	/**
	 * Injector.
	 */
	private final Injector injector;
	
	private final JsMessagesFactory jsMessagesFactory;
	
	private final WSClient wsclient;
	private final SyncCacheApi syncCacheApi;
	
	private final LazyRules6Actor rules6Actor;
	
	/*
	// TODO: remove static NGL context access 
	private static NGLContext nglContext;
	// Seems that it's not used anymore as it's not initialized.
	public static NGLContext getNglContext() {
		// if (nglContext == null)
		//   nglContext = IGGlobals.injector().instanceOf(NGLContext.class);
		return nglContext;
	}
    */
	
	@Inject
	public NGLContext(Injector injector,
					  NGLConfig   config,
					  MessagesApi messagesApi,
			          FormFactory formFactory,
			          ActorSystem actor,
			          JsMessagesFactory jsMessagesFactory,
			          WSClient wsclient,
			          SyncCacheApi syncCacheApi,
			          LazyRules6Actor rules6Actor) {
		this.injector     = injector;
		this.config       = config;
		this.messagesApi  = messagesApi;
		this.formFactory  = formFactory;
		this.actor        = actor;
		this.jsMessagesFactory = jsMessagesFactory;
		this.wsclient     = wsclient;
		this.syncCacheApi = syncCacheApi;
		this.rules6Actor  = rules6Actor; 
	}
		
	public NGLConfig config() { 
		return config;
	}
	
	public String message(String key) {
		return messages().at(key);
		// return play.api.i18n.Messages.get(key);
		// return "Messages(" + key + ")";
	}
	
	
	
	/**
	 * Pretty poor shortcut that is used in application name displays in the
	 * menu bar. This appends the application configuration if the applicaiton is not
	 * in production mode. 
	 * @param key configuration path
	 * @return    requested path value with the NGL environment name if not in production mode
	 */
	public String messageEnv(String key) {
		if (config.isNGLEnvProd()) 
			return message(key);
		return message(key) + "-" + config.nglEnv();
	}
	
	// TODO: define as static as this relies on statics
	public String currentUser() {
		// return fr.cea.ig.authentication.Authentication.getUser(play.mvc.Http.Context.current().session());
		return fr.cea.ig.authentication.Authentication.getUser();
	}

	public Lang currentLang() {
		if (play.mvc.Http.Context.current() != null)
			return play.mvc.Http.Context.current().lang();
		return null;
		// return play.mvc.Http.Context.current() != null ? play.mvc.Http.Context.current().lang() : null;
	}
	
	// Should use the currentLang method.
	public Messages messages() {
		// logger.debug("messages");
		List<Lang> langs = new ArrayList<>();
		Lang lang = currentLang();
		if (lang != null)
			langs.add(lang);
		return messagesApi.preferred(langs);
		// return Messages;
	}
	
	public FormFactory formFactory() {
		return formFactory;
	}
	
	public <T> Form<T> form(Class<T> clazz) {
		return formFactory().form(clazz);
	}

	public DynamicForm form() {
		return formFactory().form();
	}

	public String getInstitute() {
		return config().getInstitute();
	}
	
	public List<Object> rulesServices6(String ruleAnnotationName, List<Object> facts) { 
		return RulesServices6.getInstance().callRulesWithGettingFacts(getRulesKey(), ruleAnnotationName, facts);
	}
	
	// Reexport with name matching the original call
	public List<Object> callRulesWithGettingFacts(String ruleAnnotationName, List<Object> facts) { 
		return rulesServices6(ruleAnnotationName, facts); 
	}
	
	public LazyRules6Actor rules6Actor() {
		return rules6Actor;
	}
	
	public String getRulesKey() {
		return config.getRulesKey();
	}
	
//	public ActorRef rules6Actor() {
//		return akkaSystem().actorOf(Props.create(RulesActor6.class));
//	}
		
	public ActorSystem akkaSystem() {
		return this.actor;
	}
	
	public Injector injector() {
		return injector;
	}
	
	public WSClient ws() {
		return wsclient;
	}
	
	/**
	 * Default synchronized cache instance.
	 * @return synchronized cache instance
	 */
	public SyncCacheApi cache() {
		 return syncCacheApi;
	}
	
	public JsMessages jsMessages() {
		return jsMessagesFactory.all();
	}
	
	// ---------------------------------------------------------------------------------------
	// Comes from play.data.Form
	
	/**
	 * Returns form errors serialized as JSON.
	 * @param errors errors to format
	 * @return       JSON formatted errors
	 */
	public JsonNode errorsAsJson(Map<String, List<ValidationError>> errors) {
		// return errorsAsJson(play.mvc.Http.Context.current() != null ? play.mvc.Http.Context.current().lang() : null, errors);
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

	private Object translateMsgArg(List<Object> arguments, MessagesApi messagesApi, Lang lang) {
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
	public JsonNode errorAsJson(String message, Object... args) {
		return errorAsJson(currentLang(), "error", message, Arrays.asList(args));
	}
	
	public JsonNode errorAsJson(Lang lang, String key, String message, Object... args) {
		return errorAsJson(lang, key, message, Arrays.asList(args));
	}
	
	public JsonNode errorAsJson(Lang lang, String key, String message, List<Object> args) {
		String tMessage = messagesApi.get(lang, message, translateMsgArg(args, messagesApi, lang));
		Map<String, String> jMessage = new java.util.HashMap<>();
		jMessage.put(key,tMessage);
		return play.libs.Json.toJson(tMessage);
	}
	
	// ----------------------------------------------------------------------
	// Transitional code before fully DI compliant code
	
	// TODO: remove static NGL context access 
	
	private static NGLContext instance;
	
	// @Deprecated
	public static NGLContext instance() {
		if (instance == null)
			instance = IGGlobals.injector().instanceOf(NGLContext.class);
		return instance;
	}
	
	// @Deprecated
	public static JsonNode _errorsAsJson(Map<String, List<ValidationError>> errors) {
		return instance().errorsAsJson(errors);
	}
	
}
