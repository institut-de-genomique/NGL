package fr.cea.ig.ngl.utils;

import java.util.function.Supplier;

import play.api.modules.spring.Spring;

/**
 * Supplier of Spring generated instances of a given class.
 * This is not a lazy supplier as the instances are context 
 * specific and static lazy suppliers act as globals.  
 * 
 * @param <T> type of supplied objects
 * 
 * @author vrd
 *
 */
public class SpringSupplier<T> implements Supplier<T> {

	/**
	 * Class of instances to create.
	 */
	private final Class<T> instanceClass;
	
	/**
	 * Construct a supplier of instances of the given class.
	 * @param clazz class of instances to create
	 */
	public SpringSupplier(Class<T> clazz) {
//		if (!AbstractDAO.class.isAssignableFrom(clazz))
//			throw new IllegalArgumentException("not a subclass of AbstractDAO : " + clazz);
		instanceClass = clazz;
	}
	
	@Override
	public T get() {
		return Spring.getBeanOfType(instanceClass);
	}
	
}
