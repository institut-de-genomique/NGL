package ngl.sq.auth;

import org.junit.Test;

import fr.cea.ig.ngl.test.authentication.Identity;
import ngl.sq.Global;

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
	
	@Test
	public void testPermissions() {
		Global.af.authNobody("/permissions.js");
	}
	
	@Test
	public void testPrintTags() {
		Global.af.authNobody("/printTag.js");
	}
	
	@Test
	public void testAppUrls() {
		Global.af.authNobody("/app-url.js");
	}
	
}
