package fr.cea.ig.play;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.typesafe.config.Config;

import akka.actor.ActorSystem;
import play.Application;
import play.Environment;
import play.cache.SyncCacheApi;
import play.data.DynamicForm;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.Lang;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.inject.Injector;
import play.libs.ws.WSClient;

/**
 * Allows access to some globals that are hard to removed due to the
 * play application life cycle and mostly static initializers. 
 * 
 * This should allow smooth migration as removing access to this
 * will trigger compilation errors of access to globals to remove. 
 * 
 * This "works" as long any we do not trigger application() calls
 * through indirect calls.
 * 
 * We have access to the injector as it is low level enough to be accessed
 * from here. 
 * 
 * @author vrd
 * 
 */
@Singleton
public class IGGlobals {
// class UNUSED_IGGlobals {
	
	private static final play.Logger.ALogger logger = play.Logger.of(IGGlobals.class);
	
//	// This is started as a component and before any other that requires 
//	// access to globals. This component or part of this component should
//	// be injected when needed but this sets up globals as static component
//	// require that.
//	@Inject
//	public IGGlobals(Configuration conf, Environment env, Injector inj, SyncCacheApi cac) {
//		configuration = conf; // app.configuration();
//		environment   = env;  // app.environment();
//		injector      = inj;  // app.injector();
//		cache         = cac;
//		logger.debug("setup globals");
//	}
//	
//	/**
//	 * Play configuration.
//	 */
//	private static Configuration configuration;

	// This is started as a component and before any other that requires 
	// access to globals. This component or part of this component should
	// be injected when needed but this sets up globals as static component
	// require that.
	@Inject
	public IGGlobals(Application app, Config conf, Environment env, Injector inj, SyncCacheApi cac) {
		application   = app;
		config        = conf; // app.configuration();
		environment   = env;  // app.environment();
		injector      = inj;  // app.injector();
		cache         = cac;
		logger.debug("setup globals");
	}
	
	private static Application application;
	/**
	 * Play configuration.
	 */
	private static Config config;
	
	/**
	 * Play environment.
	 */
	private static Environment environment;
	
	/**
	 * Play injector.
	 */
	private static Injector injector;
	
	/**
	 * Default cache.
	 */
	private static SyncCacheApi cache;
	
	public static Application application() {
		return assertInitialized("application",application);
	}
	
	/**
	 * Play configuration. 
	 * @return Play configuration
	 */
	public static Config configuration() {
		return assertInitialized("configuration",config);
	}
	
	/**
	 * Play environment.
	 * @return Play environment
	 */
	public static Environment environment() {
		return assertInitialized("environment",environment);
	}
	
	/**
	 * Play injector.
	 * @return Play injector
	 */
	public static Injector injector() {
		return assertInitialized("injector",injector);
	}
	
	public static <T> T instanceOf(Class<T> c) {
		return injector().instanceOf(c);
	}
	
	/**
	 * Default synchronized cache instance.
	 * @return synchronized cache instance
	 */
	public static SyncCacheApi cache() {
		// return injector().instanceOf(SyncCacheApi.class);
		// NoCache or HashCache resolve the security exception problem.
		// return NoCache.instance();
		// return HashCache.instance();
		return assertInitialized("cache",cache);
	}

	/**
	 * Throw a runtime exception if a null value is passed as t.
	 * @param name name of the static field to check
	 * @param t value of the static field to check
	 * @return t if it's not null
	 */
	private static <T> T assertInitialized(String name, T t) {
		if (t == null)
			throw new RuntimeException("IGGlobals '" + name + "()' is not intiailzed");
		return t;
	}

	// -------------------------------------
	// Implementation of methods that are removed from play but still needed by 
	// NGL static methods.
	
	// TODO: inject
	public static FormFactory formFactory() {
		return injector().instanceOf(FormFactory.class);
	}
	
	public static <T> Form<T> form(Class<T> clazz) {
		return formFactory().form(clazz);
	}

	// TODO: inject
	public static DynamicForm form() {
		return formFactory().form();
	}
	
	public static MessagesApi messagesApi() {
		return injector().instanceOf(MessagesApi.class);
	}
	
	// TODO: possibly use httpcontext, maybe some lang at least
	// TODO: inject
	public static Messages messages() {
		return messagesApi().preferred(new ArrayList<Lang>());
	}
	
	// TODO:inject
	public static ActorSystem akkaSystem() {
		return injector().instanceOf(ActorSystem.class);
	}
	
	// TODO: inject
	public static WSClient ws() {
		return injector().instanceOf(WSClient.class);
	}
	
	/*
	static class NoCache implements SyncCacheApi {
		private  static NoCache instance;
		public static NoCache instance() {
			if (instance == null)
				instance = new NoCache();
			return instance;
		}
		public <T> T get(String key) { 
			return null; 
		}
		public <T> T getOrElseUpdate(String key, Callable<T> block) { 
			try {
				return block.call();
			} catch (Exception e) {
				throw new RuntimeException("block call failed",e);
			}
		}
		public <T> T getOrElseUpdate(String key, Callable<T> block, int expiration) { 
			return getOrElseUpdate(key,block);
		}
		public void remove(String key) {
		}
		public void set(String key, Object value) {
		}
		public void set(String key, Object value, int expiration) {
		}
	}
	
	static class HashCache implements SyncCacheApi {
		private  static HashCache instance;
		public static HashCache instance() {
			if (instance == null)
				instance = new HashCache();
			return instance;
		}
		private Map<String,Object> cache = new HashMap<String,Object>();
		public <T> T get(String key) { 
			return (T)cache.get(key); 
		}
		public <T> T getOrElseUpdate(String key, Callable<T> block) {
			T t = get(key);
			if (t != null)
				return t;
			try {
				t = block.call();
				cache.put(key,t);
				return t;
			} catch (Exception e) {
				throw new RuntimeException("block call failed",e);
			}
		}
		public <T> T getOrElseUpdate(String key, Callable<T> block, int expiration) { 
			return getOrElseUpdate(key,block);
		}
		public void remove(String key) {
			cache.remove(key);
		}
		public void set(String key, Object value) {
			cache.put(key,value);
		}
		public void set(String key, Object value, int expiration) {
			set(key,value);
		}
	}*/
	
}
