package ngl.bi;

//import org.junit.Test;

import fr.cea.ig.play.test.RoutesTest;

public class Heavy {
	
//	@Test
//	public void test01() throws Exception {
//	    testInServer(devapp(),
//	    		ws -> {	    	
//	    			new RoutesTest()
//	    			.autoRoutes()
//	    			//.ignore("/permissions.js")
//	    			.run(ws);
//	    		});
//	}	
	
	// @Test
	// Ignored as the user database part is missing
	public void test01() throws Exception {
	    Global.af.runWs((app,ws) -> {
	    			new RoutesTest()
	    			.autoRoutes()
	    			//.ignore("/permissions.js")
	    			.run(ws);
	    		});
	}	

}


