package ngl.projects.auth;

import org.junit.Test;

import fr.cea.ig.ngl.test.authentication.Identity;
import static ngl.projects.Global.af;

public class TestMainRoutes {
	
	@Test
	public void testHome() {
		af.authURL(Identity.Read,"/");
	}
	
	@Test
	public void testMessages() {
		af.authNobody("/messages.js");
	}

	@Test
	public void testCodes() {
		af.authNobody("/codes.js");
	}
	
}
