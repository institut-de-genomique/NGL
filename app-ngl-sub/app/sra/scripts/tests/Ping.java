package sra.scripts.tests;


import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;

/*
 * Script TEST
 * {@code http://localhost:9000/sra/scripts/run/sra.scripts.tests.Ping}
 * <br> *  
 * @author sgas
 *
 */
public class Ping extends  ScriptNoArgs { 

	@Override
	public void execute() throws Exception {
		println("{\"pong\": true}");
	}
	
}
