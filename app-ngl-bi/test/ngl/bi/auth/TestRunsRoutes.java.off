package ngl.bi.auth;

import org.junit.Test;

import fr.cea.ig.ngl.test.authentication.Identity;
import ngl.bi.Global;

public class TestRunsRoutes {
	
	@Test
	public void testHome() {
		Global.af.authURL(Identity.Read,"/runs/spongebob/home");
	}	
	
	@Test
	public void testTplDetails() {
		Global.af.authNobody("/tpl/runs/details");
	}
	
	@Test
	public void testJsRoutes() {
		Global.af.authNobody("/tpl/runs/js-routes");
	}
	
}
