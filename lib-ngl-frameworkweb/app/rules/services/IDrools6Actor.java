package rules.services;

import java.util.Arrays;
import java.util.List;

/**
 * Drools execution implementation to be bound at application creation time. This
 * provides a way to use asynchronous (production) or synchronous
 * (test) rules execution.
 * 
 * @author vrd
 *
 */
public interface IDrools6Actor {
	
	/**
	 * Drools execution.
	 * @param rulesCode rules code
	 * @param objects   parameters
	 */
	void tellMessage(String rulesCode, List<Object> objects);
	
	/**
	 * Drools execution.
	 * @param rulesCode rules code
	 * @param objects   parameters
	 */
	default void tellMessage(String rulesCode, Object... objects) {
		tellMessage(rulesCode, Arrays.asList(objects));
	}
	
}
