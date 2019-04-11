package ngl.plates;

import org.junit.Test;

import fr.cea.ig.play.test.RoutesTest;

public class Heavy {
	
//	@Test
//	public void test01() throws Exception {
//	    testInServer(devapp(),
//	    		ws -> {	    	
//	    	      // checkRoutes(ws);
//	    			new RoutesTest()
//	    			.autoRoutes()
//	    			.ignore("/api/manip")
//	    			.run(ws);
//	    		});
//	}	
	
	@Test
	public void test01() throws Exception {
		Global.af.runWs((app,ws) -> { 
			// checkRoutes(ws);
			new RoutesTest()
			.autoRoutes()
			.ignore("/api/manip")
			.run(ws);
		});
	}	

}


