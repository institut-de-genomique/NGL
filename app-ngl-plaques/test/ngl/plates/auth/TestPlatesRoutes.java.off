package ngl.plates.auth;

import org.junit.Test;

import fr.cea.ig.ngl.test.authentication.Identity;
import ngl.plates.Global;

public class TestPlatesRoutes {

	@Test
	public void testHome() {
		Global.af.authURL(Identity.Read,"/plates/spongebob/home");
	}	
	
	@Test
	public void testTplSearchManips() {
		Global.af.authNobody("/tpl/plates/search-manips");
	}
	
	@Test
	public void testTplFromFile() {
		Global.af.authNobody("/tpl/plates/from-file");
	}
	
	@Test
	public void testTplSearch() {
		Global.af.authNobody("/tpl/plates/search");
	}
		
	@Test
	public void testTplDetails() {
		Global.af.authNobody("/tpl/plates/details");
	}

	@Test
	public void testJsRoutes() {
		Global.af.authNobody("/tpl/plates/js-routes");
	}

}
