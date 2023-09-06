package fr.cea.ig.lfw.controllers.scripts.chunked;

import play.mvc.Http.RequestBody;

/**
 * Script with no URL parameters and a HTTP request body.
 * 
 * @author vrd
 *
 */
public abstract class ScriptWithBody extends ChunkedOutputScriptBase<Object> {

	@Override
	public void execute(Object args, RequestBody body) throws Exception {
		execute(body);
	}
	
	public abstract void execute(RequestBody body) throws Exception;
	
}
