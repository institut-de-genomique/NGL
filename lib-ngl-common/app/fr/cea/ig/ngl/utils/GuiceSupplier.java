package fr.cea.ig.ngl.utils;

import java.util.function.Supplier;

import fr.cea.ig.play.IGGlobals;

public class GuiceSupplier<T> implements Supplier<T> {
	
	/**
	 * Class of instances to create.
	 */
	private final Class<T> instanceClass;
	
	/**
	 * Construct a supplier of instances of the given class.
	 * @param clazz class of instances to create
	 */
	public GuiceSupplier(Class<T> clazz) {
		instanceClass = clazz;
	}
	
	@Override
	public T get() {
		return IGGlobals.injector().instanceOf(instanceClass);
	}

}
