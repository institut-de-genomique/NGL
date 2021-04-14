package ngl.sq.auth;

import org.junit.Test;

import fr.cea.ig.ngl.test.authentication.Identity;
import ngl.sq.Global;

public class TestSamplesRoutes {

	@Test
	public void testHome() {
		Global.af.authURL(Identity.Read,"/samples/home");
	}
	
	@Test
	public void testGet() {
		Global.af.authURL(Identity.Read,"/samples/AAAA-A120_ST147_T0_A");
	}
	
	@Test
	public void testTplSearch() {
		Global.af.authNobody("/tpl/samples/search");
	}

	@Test
	public void testTplDetails() {
		Global.af.authNobody("/tpl/samples/details");
	}

	@Test
	public void testJsRoutes() {
		Global.af.authNobody("/tpl/samples/js-routes");
	}
	
}
