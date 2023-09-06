package fr.cea.ig.ngl.dao.api;

import fr.cea.ig.DBObject;
import models.laboratory.common.instance.State;
import workflows.Workflows;

/**
 * API that support state transition ({@link Workflows}).
 * <p>
 * This is to be used as an extension of the {@link DBObjectAPI} interface.
 * 
 * @author vrd
 * 
 * @param <T> type of handled objects  
 *
 */
public interface StatefulAPI<T extends DBObject> {

	/**
	 * Update the state of the object referenced by the given code.
	 * @param code        object code
	 * @param state       new state
	 * @param currentUser user doing the update
	 * @return            updated object
	 */
	T updateState(String code, State state, String currentUser);
	
}
