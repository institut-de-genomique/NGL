package ngl.sq.auth;

import org.junit.Test;

import fr.cea.ig.ngl.test.authentication.Identity;
import ngl.sq.Global;


public class TestContainersRoutes {

	@Test
	public void testHome() {
		Global.af.authURL(Identity.Read,"/containers/spongebob/home");
	}

	@Test
	public void testTplSearch() {
		Global.af.authNobody("/tpl/containers/search");
	}	

	@Test
	public void testTplDetails() {
		Global.af.authNobody("/tpl/containers/details");
	}	
	
	@Test
	public void testTplNewFromFile() {
		Global.af.authNobody("/tpl/containers/new-from-file");
	}	
	
	@Test
	public void testTplJsRoutes() {
		Global.af.authNobody("/tpl/containers/js-routes");
	}	

}
