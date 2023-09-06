package fr.cea.ig.lfw.controllers.scripts.chunked;

import fr.cea.ig.lfw.controllers.scripts.ScriptException;
import play.mvc.Http.RequestBody;

/**
 * No parameter, no request body script.
 * 
 * @author vrd
 *
 */
public abstract class Script extends ChunkedOutputScriptBase<Object> {

	@Override
	public void execute(Object args, RequestBody body) throws Exception {
		ScriptException.assertEmpty(body);
		execute();
	}
	
	public abstract void execute() throws Exception;
	
}
