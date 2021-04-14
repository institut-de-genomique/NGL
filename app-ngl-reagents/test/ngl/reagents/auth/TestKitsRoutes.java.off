package ngl.reagents.auth;

import org.junit.Test;

import fr.cea.ig.ngl.test.authentication.Identity;
import ngl.reagents.Global;

public class TestKitsRoutes {
	
	@Test
	public void testHome() {
		Global.af.authURL(Identity.Read,"/kits/spongebob/home");
	}	
	
	@Test
	public void testJsRoutes() {
		Global.af.authNobody("/tpl/reagent-declarations/js-routes");
	}
	
	@Test
	public void testTplCreation() {
		Global.af.authNobody("/tpl/kits/creation");
	}
	
	@Test
	public void testTplSearch() {
		Global.af.authNobody("/tpl/kits/search");
	}
	
}
