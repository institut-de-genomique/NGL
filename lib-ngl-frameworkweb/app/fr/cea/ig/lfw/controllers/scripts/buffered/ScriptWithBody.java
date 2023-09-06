package fr.cea.ig.lfw.controllers.scripts.buffered;

import play.mvc.Http.RequestBody;

/**
 * Script with HTTP body access, no URL parameters.
 * 
 * @author vrd
 *
 */
public abstract class ScriptWithBody extends Script<Object> {
	
	@Override
	public void execute(Object args) throws Exception {
		execute(play.mvc.Http.Context.current().request().body());
	}
	
	public abstract void execute(RequestBody body) throws Exception;
	
}
