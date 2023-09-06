package fr.cea.ig.lfw;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorSystem;
import fr.cea.ig.authentication.Authentication;
import jsmessages.JsMessages;
import jsmessages.JsMessagesFactory;
import play.cache.SyncCacheApi;
import play.data.DynamicForm;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.ValidationError;
import play.i18n.Lang;
import play.i18n.MessagesApi;
import play.inject.Injector;
import play.libs.Json;
import rules.services.IDrools6Actor;
import rules.services.RulesServices6;

@Singleton
public class LFWApplication {

	/**
	 * Injector.
	 */
	private final Injector injector;
	
	/**
	 * I18N.
	 */
	private final MessagesApi messagesApi;

	/**
	 * Javascript side messages.
	 */
	private final JsMessagesFactory jsMessagesFactory;

	/**
	 * Form factory.
	 */
	private final FormFactory formFactory;
		
	/**
	 * Akka ActorSystem.
	 */
	private final ActorSystem actorSystem;
	
	/**
	 * Configuration wrapper.
	 */
	private final LFWConfig config;
	
	private final SyncCacheApi syncCacheApi;
	
	/**
	 * Drools asychronous execution support.
	 */
	private final IDrools6Actor rules6Actor;

	/**
	 * DI Constructor.
	 * @param injector          injector
	 * @param messagesApi       internationalization messages
	 * @param jsMessagesFactory javascript messages
	 * @param formFactory       form factory
	 * @param actorSystem       actor system
	 * @param config            configuration
	 * @param syncCacheApi      cache
	 * @param rules6Actor       actor
	 */
	@Inject
	public LFWApplication(Injector injector, 
			              MessagesApi messagesApi, 
			              JsMessagesFactory jsMessagesFactory, 
			              FormFactory formFactory,
			              ActorSystem actorSystem,
			              LFWConfig config,
			              SyncCacheApi syncCacheApi,
			              IDrools6Actor rules6Actor) {
		this.injector          = injector;
		this.messagesApi       = messagesApi;
		this.jsMessagesFactory = jsMessagesFactory;
		this.formFactory       = formFactory;
		this.actorSystem       = actorSystem;
		this.config            = config;
		this.syncCacheApi      = syncCacheApi;
		this.rules6Actor       = rules6Actor;
	}
	
	/**
	 * Subclass constructor.
	 * @param app super class instance
	 */
	protected LFWApplication(LFWApplication app) {
		this(app.injector(),
			 app.messagesApi(),
			 app.jsMessagesFactory(),
			 app.formFactory(),
			 app.actorSystem(),
			 app.lfwConfig(),
			 app.cache(),
			 app.rules6Actor());
	}

	public Injector          injector()          { return injector;          }
	public MessagesApi       messagesApi()       { return messagesApi;       }
	public JsMessagesFactory jsMessagesFactory() { return jsMessagesFactory; }
	public FormFactory       formFactory()       { return formFactory;       }
	
	/**
	 * Akka actor system. 
	 * @return Akka actor system
	 */
	// Only referenced from ngl-data-api
	public ActorSystem actorSystem() { 
		return actorSystem;       
	}
	
	public LFWConfig         lfwConfig()         { return config;            }
	
	// --------------------------------------------------------------------------------------------------------------
	// -- Raw migration code
	
	public <T> Form<T> form(Class<T> clazz) {
		return formFactory().form(clazz);
	}

	public DynamicForm form() {
		return formFactory().form();
	}
	
	public SyncCacheApi cache() { 
		return syncCacheApi; 
	}
	
//	@Deprecated
//	public LazyRules6Actor rules6Actor() {
//		return rules6Actor;
//	}
	public IDrools6Actor rules6Actor() {
		return rules6Actor;
	}
	
	public JsMessages jsMessages() {
		return jsMessagesFactory.all();
	}

	public String currentUser() {
		return Authentication.isApiKeyUser() ? Authentication.getApiKeyUser() : Authentication.getUser();
	}

	public Lang currentLang() {
		if (play.mvc.Http.Context.current() != null)
			return play.mvc.Http.Context.current().lang();
		return null;
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

	/**
	 * Synchronous firing of the rules matching the application rules key
	 * and the annotation name and return the drools facts. 
	 * @param ruleAnnotationName rule name
	 * @param facts              facts
	 * @return                   drools returned facts
	 */	
	public List<Object> rulesServices6(String ruleAnnotationName, List<Object> facts) { 
//		return RulesServices6.getInstance().callRulesWithGettingFacts(nglConfig().getRulesKey(), ruleAnnotationName, facts);
		return RulesServices6.getInstance().callRulesWithGettingFacts(config.getRulesKey(), ruleAnnotationName, facts);
	}
	
	// Reexport with name matching the original call
	/**
	 * Synchronous firing of the rules matching the application rules key
	 * and the annotation name and return the drools facts. 
	 * @param ruleAnnotationName rule name
	 * @param facts              facts
	 * @return                   drools returned facts
	 */	
	public List<Object> callRulesWithGettingFacts(String ruleAnnotationName, List<Object> facts) { 
		return rulesServices6(ruleAnnotationName, facts); 
	}
	
	public void callRules(String ruleAnnotationName, List<Object> facts) { 
		RulesServices6.getInstance().callRules(config.getRulesKey(), ruleAnnotationName, facts);
	}


	// ------------------------------------------------------------
	// Parallel stream support. We need threads that have the main thread permissions
	// that allow injector class loader manipulation (NGL-2277).
	
	/**
	 * Framework fork join pool, used to provide threads that have the same
	 * permission as the main thread (NGL-2277).
	 */
	// Depending on the nature of tasks (e.g. I/O bound) we may want to increase parallelism.
	private final ForkJoinPool forkJoinPool = new ForkJoinPool();
	
	/**
	 * Executes the given task ({@link Callable}) using the framework thread pool,
	 * returns when the task has been completed.
	 * @param <T>  task return type
	 * @param task task to execute
	 * @return     task return value
	 */
	public <T> T parallelCall(Callable<T> task) {
		try {
			return forkJoinPool.submit(task).get();
		} catch (ExecutionException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Executes the given task ({@link Runnable}) using the framework thread pool,
	 * returns when the task has been completed.
	 * @param task task to execute
	 */
	public void parallelRun(Runnable task) {
		try {
			forkJoinPool.submit(task).get();
		} catch (ExecutionException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

}
