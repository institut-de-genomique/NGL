package fr.cea.ig.lfw.controllers.scripts.chunked;

import fr.cea.ig.lfw.controllers.scripts.ScriptException;
import play.mvc.Http.RequestBody;

/**
 * Script with HTTP URL parameters defined through a parameter class T.
 *  
 * @author vrd
 *
 * @param <T> URL parameter definition
 */
public abstract class ScriptWithArgs<T> extends ChunkedOutputScriptBase<T> {

	@Override
	public void execute(T args, RequestBody body) throws Exception {
		ScriptException.assertEmpty(body);
		execute(args);
	}

	public abstract void execute(T args) throws Exception;
	
}
