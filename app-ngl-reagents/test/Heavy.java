
//import static org.junit.Assert.assertEquals;
import org.junit.Test;

import fr.cea.ig.play.test.ApplicationFactory;

import static fr.cea.ig.play.test.DevAppTesting.*;

//import play.Application;

public class Heavy {
	
//	// Should be in some global. 
//	public static Application devapp() { 
//		return fr.cea.ig.play.test.DevAppTesting.devapp("ngl-reagents.test.conf");
//	}

	public static final ApplicationFactory af = new ApplicationFactory("ngl-reagents.test.conf");
			
//	@Test
//	public void test01() throws Exception {
//	    testInServer(devapp(),
//	    		ws -> {	    	
//	    	      checkRoutes(ws);
//	    		});
//	}	
	
	@Test
	public void test01() throws Exception {
		af.runWs((app,ws) -> {
			checkRoutes(ws);
		});
	}	
	
}


