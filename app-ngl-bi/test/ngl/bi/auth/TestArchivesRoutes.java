package ngl.bi.auth;

import org.junit.Test;

import fr.cea.ig.ngl.test.authentication.Identity;
import ngl.bi.Global;

public class TestArchivesRoutes {
	
	@Test
	public void testHome() {
		Global.af.authURL(Identity.Read,"/archives/spongebob/home");
	}	
	
	@Test
	public void testTplSearch() {
		Global.af.authNobody("/tpl/archives/search");
	}
	
	@Test
	public void testJsRoutes() {
		Global.af.authNobody("/tpl/archives/js-routes");
	}

}
