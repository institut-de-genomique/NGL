package fr.cea.ig.play.test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import fr.cea.ig.util.function.C1;
import fr.cea.ig.util.function.C2;
import fr.cea.ig.util.function.CC1;
import fr.cea.ig.util.function.CC2;
import play.Application;
import play.inject.Bindings;
import play.inject.guice.GuiceApplicationBuilder;

// This is work in progress.

/**
 * Wrapper around the guice application builder. This allows a base configuration
 * to be stored as an application factory. Methods that configure the factory return
 * a new cloned factory to allow definitions to be built from some base definition
 * without affecting the base definition. 
 * <p>
 * Subclasses must override the {@link #constructorClone()} method that is 
 * a clone method.
 *  
 * @author vrd
 *
 */
public class ApplicationFactory {

	private static final play.Logger.ALogger logger = play.Logger.of(ApplicationFactory.class);
	
	/**
	 * Configuration file name (see @link DevAppTesting}.
	 */
	private final String configFileName;

	/**
	 * Guice application builder modification to apply. 
	 */
	private final List<Function<GuiceApplicationBuilder,GuiceApplicationBuilder>> mods;
	
	/**
	 * Create a factory using a configuration file
	 * @param configFileName configuration file to use
	 */
	public ApplicationFactory(String configFileName) {
		this.configFileName = configFileName;
		mods                = new ArrayList<>();
	}
	
	// Cloning by constructor
	protected ApplicationFactory(ApplicationFactory f) {
		configFileName = f.configFileName;
		mods           = new ArrayList<>(f.mods);		
	}

	/**
	 * Clone this factory.
	 * @return cloned factory
	 */
	protected ApplicationFactory constructorClone() {
		return new ApplicationFactory(this);
	}
	
	/**
	 * Add a raw guice application builder modification.
	 * @param mod guice builder modification
	 * @return cloned factory with the added modification
	 */
	public ApplicationFactory mod(Function<GuiceApplicationBuilder,GuiceApplicationBuilder> mod) {
		ApplicationFactory f = constructorClone();
		f.mods.add(mod);
		return f;
	}
	
	/**
	 * Build a cloned application factory that has the given binding added. 
	 * @param <T> binding point type
	 * @param <U> binding value type
	 * @param t binding point
	 * @param u binding value
	 * @return cloned factory with the binding added
	 */
	public <T,U extends T> ApplicationFactory overrideEagerly(Class<T> t, Class<U> u) {
		// This is a clone operation, should be declared as such.
		// ApplicationFactory f = new ApplicationFactory(this);
		// f.mods.add(b -> b.overrides(Bindings.bind(t).to(u)));
		// return f;
		return mod(b -> b.overrides(Bindings.bind(t).to(u).eagerly()));
	}

	/**
	 * Build a cloned factory with the given to self eager binding added.
	 * @param <T> binding point and value type
	 * @param t self binding to add
	 * @return  cloned factory with the added binding
	 */
	public <T> ApplicationFactory overrideEagerly(Class<T> t) {
		return mod(b -> b.overrides(Bindings.bind(t).toSelf().eagerly()));
	}

	/**
	 * Build a cloned factory with the lazy override added.
	 * @param <T> binding point type
	 * @param <U> binding value type 
	 * @param t binding point
	 * @param u binding value
	 * @return  cloned factory with added override
	 */
	public <T,U extends T> ApplicationFactory override(Class<T> t, Class<U> u) {
		return mod(b -> b.overrides(Bindings.bind(t).to(u)));
	}

	/**
	 * Set a configuration key to a given value.
	 * @param key   key to set value of
	 * @param value value
	 * @return      new configured application factory
	 */
	public ApplicationFactory configure(String key, String value) {
		return mod(b -> b.configure(key,value));
	}
	
	/**
	 * Create an application.
	 * @return created application
	 */
	public Application createApplication() {
		return DevAppTesting.devapp(configFileName, mods);
	}
	
	/**
	 * Test the application through a WS client.
	 * @param c code to execute with the created WS client 
	 */
//	public void ws(Consumer<WSClient> c) {
//		DevAppTesting.testInServer(this.createApplication(), c);
//	}
	
	public void ws(Consumer<NGLWSClient> c) {
		DevAppTesting.testInServer(this.createApplication(), ws -> c.accept(new NGLWSClient(ws)));
	}
	
	/**
	 * Test the application directly.
	 * @param  c         code to execute using the created application
	 * @throws Exception exception
	 */
//	public void run(Consumer<Application> c) {
	public void run(C1<Application> c) throws Exception {
		logger.debug("run - creating application");
		Application a = createApplication();
		logger.debug("run - created {}, will run {}", a, c);
		try {
			c.accept(a);
			logger.debug("run - done {}", c);
		} finally {
			a.asScala().stop();
			logger.debug("run -application shut down");
		}
	}
	
	/**
	 * Test the application using a WS client and the application.
	 * @param  c         code to execute using the created application and WS client.
	 * @throws Exception exception
	 */
//	public void runWs(BiConsumer<Application,WSClient> c)  {
//		final Application a = createApplication();
//		DevAppTesting.testInServer(a,ws -> c.accept(a,ws));
//	}

	public void runWs(C2<Application,NGLWSClient> c) throws Exception {
		final Application a = createApplication();
		DevAppTesting.testInServer(a,ws -> c.accept(a, new NGLWSClient(ws)));
	}
	
	/**
	 * Turns this application factory into a CC1 of application.
	 * @return CC1 of application
	 */
	public CC1<Application> cc1() {
		return Actions.withApp(this);
	}
	
	/**
	 * Turns this application factory into a CC2 of application and WS client.
	 * @return CC2 of application and WS client
	 */
	public CC2<Application,NGLWSClient> cc2() {
		return Actions.withAppWS(this);
	}
	
	/**
	 * Transformers of application factories into CCs.
	 * 
	 * @author vrd
	 *
	 */
	public static class Actions {
		
		/**
		 * No need to instanciate a utility methods class.
		 */
		private Actions() {}
		
		public static final CC1<Application> withApp(ApplicationFactory f) {
			return nc -> f.run(nc::accept);
		}
		
		public static final CC2<Application,NGLWSClient> withAppWS(ApplicationFactory f) {
			return nc -> f.runWs(nc::accept);
		}

	}
	
}
