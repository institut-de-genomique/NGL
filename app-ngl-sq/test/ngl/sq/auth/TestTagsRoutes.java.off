package ngl.sq.auth;

import org.junit.Test;

import fr.cea.ig.ngl.test.authentication.Identity;
import ngl.sq.Global;

public class TestTagsRoutes {
	
	@Test
	public void testHome() {
		Global.af.authURL(Identity.Read,"/printing/search/home");
	}

	@Test
	public void testJsRoutes() {
		Global.af.authNobody("/tpl/printing/js-routes");
	}
	
	@Test
	public void testTplDisplay() {
		Global.af.authNobody("/tpl/printing/tags/display");
	}
	
}
