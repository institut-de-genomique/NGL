package rules.services;

import java.util.List;

import javax.inject.Inject;

import com.google.inject.Provider;

import fr.cea.ig.lfw.LFWApplication;

/**
 * Executes the rules synchronously using a lazy application reference.
 * <p>
 * This is the Drools execution implementation for the tests that requires 
 * synchronous rules execution.
 *  
 * @author vrd
 *
 */
public class LazyRules6Executor implements IDrools6Actor {
	
	private Provider<LFWApplication> app;
	
	@Inject
	public LazyRules6Executor(Provider<LFWApplication> app) {
		this.app      = app;
	}
	
	@Override
	public void tellMessage(String rulesCode, List<Object> objects) {
		app.get().callRules(rulesCode, objects);
	}
	
//	@Override
//	public void tellMessage(String rulesCode, Object... objects) {
//		tellMessage(rulesCode, Arrays.asList(objects));
//	}
	
}
