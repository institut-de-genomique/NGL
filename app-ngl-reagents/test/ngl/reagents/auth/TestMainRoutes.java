package ngl.reagents.auth;

import org.junit.Test;

import fr.cea.ig.ngl.test.authentication.Identity;
import ngl.reagents.Global;

public class TestMainRoutes {
	
	@Test
	public void testHome() {
		Global.af.authURL(Identity.Read,"/");
	}	
	
	@Test
	public void testMessages() {
		Global.af.authNobody("/messages.js");
	}
	
	@Test
	public void testCodes() {
		Global.af.authNobody("/codes.js");
	}
	
}
