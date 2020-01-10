package ngl.sq.auth;

import org.junit.Test;

import fr.cea.ig.ngl.test.authentication.Identity;
import ngl.sq.Global;

public class TestContainerSupportsRoutes {

	@Test
	public void testScannerHome() {
		Global.af.authURL(Identity.Read,"/supports/scanner/home");
	}

	@Test
	public void testSearchHome() {
		Global.af.authURL(Identity.Read,"/supports/spongebob/home");
	}
	
	@Test
	public void testTplSearch() {
		Global.af.authNobody("/tpl/supports/search");
	}
	
	@Test
	public void testTplDetails() {
		Global.af.authNobody("/tpl/supports/details");
	}
	
	@Test
	public void testJsRoutes() {
		Global.af.authNobody("/tpl/supports/js-routes");
	}

}
