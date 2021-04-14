package ngl.reagents.auth;

import org.junit.Test;

import fr.cea.ig.ngl.test.authentication.Identity;
import ngl.reagents.Global;

public class TestKitCatalogsRoutes {

	@Test
	public void testHome() {
		Global.af.authURL(Identity.Read,"/kit-catalogs/spongebob/home");
	}	
	
	@Test
	public void testJsRoutes() {
		Global.af.authNobody("/tpl/reagent-catalogs/js-routes");
	}
	
	@Test
	public void testTplCreation() {
		Global.af.authNobody("/tpl/kit-catalogs/creation");
	}
	@Test
	public void testTplSearch() {
		Global.af.authNobody("/tpl/kit-catalogs/search");
	}
	
}
