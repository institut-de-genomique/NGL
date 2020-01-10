package ngl.bi.auth;

import org.junit.Test;

import fr.cea.ig.ngl.test.authentication.Identity;
import ngl.bi.Global;

public class TestBalanceSheetsRoutes {
	
	@Test
	public void testHome() {
		Global.af.authURL(Identity.Read,"/balance-sheets/spongebob/0/home");
	}	
	
	@Test
	public void testTplYear() {
		Global.af.authNobody("/tpl/balance-sheets/year");
	}

	@Test
	public void testTplGeneral() {
		Global.af.authNobody("/tpl/balance-sheets/general");
	}
	
	@Test
	public void testJsRoutes() {
		Global.af.authNobody("/tpl/stats/js-routes");
	}

}
