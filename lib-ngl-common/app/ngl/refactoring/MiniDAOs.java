package ngl.refactoring;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import models.laboratory.common.description.State;

/**
 * Orthogonal definition of the MiniDAO.
 * 
 * @author vrd
 *
 */
public class MiniDAOs {

	private static final Map<Class<?>, Supplier<MiniDAO<?>>> daos;
	
	static <T> void register(Class<T> c, Supplier<MiniDAO<T>> s) {
		daos.put(c, () -> s.get());
	}
	
	static {
		daos = new HashMap<>();
	    register(State.class, MiniDAO.createSupplier(State.find));

	}
	
}
