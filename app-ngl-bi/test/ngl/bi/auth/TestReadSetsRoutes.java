package ngl.bi.auth;

import org.junit.Test;

import fr.cea.ig.ngl.test.authentication.Identity;
import ngl.bi.Global;

public class TestReadSetsRoutes {

	@Test
	public void testHome() {
		Global.af.authURL(Identity.Read,"/readsets/spongebob/home");
	}	
	
	@Test
	public void testTplSearch() {
		Global.af.authNobody("/tpl/readsets/search");
	}

	@Test
	public void testTplDetails() {
		Global.af.authNobody("/tpl/readsets/details");
	}
	
	@Test
	public void testTplDetailsPrintView() {
		Global.af.authNobody("/tpl/readsets/details-print-view");
	}
	
	@Test
	public void testJsRoutes() {
		Global.af.authNobody("/tpl/readsets/js-routes");
	}

}
