package ngl.data;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import static fr.cea.ig.play.test.DevAppTesting.testInServer;
import fr.cea.ig.play.test.RoutesTest;

import play.Application;
import static ngl.data.Global.devapp;

public class Heavy {
	
	// API heavy test, nothing to test automatically
	// @Test
	public void test01() throws Exception {
	    testInServer(devapp(),
	    		ws -> {	    	
	    	      // checkRoutes(ws);
	    			new RoutesTest()
	    			.autoRoutes()
	    			.ignore("/api/description/ngl-sq")
	    			.ignore("/api/description/ngl-bi")
	    			.run(ws);
	    		});
	}	
	
}


