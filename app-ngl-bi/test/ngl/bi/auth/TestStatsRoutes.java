package ngl.bi.auth;

import org.junit.Test;

import fr.cea.ig.ngl.test.authentication.Identity;
import ngl.bi.Global;

public class TestStatsRoutes {
	
	@Test
	public void testHome() {
		Global.af.authURL(Identity.Read,"/stats/spongebob/home");
	}	
	
	@Test
	public void testJsRoutes() {
		Global.af.authNobody("/tpl/stats/js-routes");
	}
	
	@Test
	public void testTplChoice() {
		Global.af.authNobody("/tpl/stats/spongebob/choice");
	}

	@Test
	public void testTplConfig() {
		Global.af.authNobody("/tpl/stats/spongebob/config");
	}
	
	@Test
	public void testTplShow() {
		Global.af.authNobody("/tpl/stats/spongebob/show");
	}

}
