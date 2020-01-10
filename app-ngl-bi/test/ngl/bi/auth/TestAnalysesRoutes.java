package ngl.bi.auth;

import org.junit.Test;

import fr.cea.ig.ngl.test.authentication.Identity;
import ngl.bi.Global;

public class TestAnalysesRoutes {

	@Test
	public void testHome() {
		Global.af.authURL(Identity.Read,"/analyses/spongebob/home");
	}	
	
	@Test
	public void testTplDetails() {
		Global.af.authNobody("/tpl/analyses/details");
	}
	
	@Test
	public void testTplJsRoutes() {
		Global.af.authNobody("/tpl/analyses/js-routes");
	}
	
}
