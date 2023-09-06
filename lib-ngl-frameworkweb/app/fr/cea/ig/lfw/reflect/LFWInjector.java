package fr.cea.ig.lfw.reflect;

import javax.inject.Inject;

import play.Application;
import play.inject.Injector;

/**
 * Injection related methods, to be used when class resolution by
 * name is needed, otherwise it provides the {@link #instanceOf(Class)} method
 * from {@link Injector#instanceOf(Class)} and can be seen as some extended
 * injector.
 * 
 * @author vrd
 *
 */
public class LFWInjector {
	
	/**
	 * Application for class resolution.
	 */
	private final Application app;
	
	/**
	 * Injector for instance creation.
	 */
	private final Injector injector;
	
	@Inject
	public LFWInjector(Application app, Injector injector) {
		this.app      = app;
		this.injector = injector;
	}
	
	/**
	 * Resolve a class by name using the application class loader.
	 * @param className               class name
	 * @return                        resolved class
	 * @throws ClassNotFoundException class not found
	 */
	public Class<?> forName(String className) throws ClassNotFoundException {
		return app.classloader().loadClass(className);
	}
	
	/**
	 * Create an injected instance of a class name.
	 * @param className  class name
	 * @return           created instance
	 * @throws Exception error
	 */
	public Object newInstance(String className) throws Exception {
		return injector.instanceOf(forName(className));
	}
	
	/**
	 * Create an injected instance of a class (see {@link Injector#instanceOf(Class)}).
	 * @param <T> type of created instance
	 * @param c   class to create instance of
	 * @return    created instance
	 */
	public <T> T instanceOf(Class<T> c) {
		return injector.instanceOf(c);
	}
	
}
