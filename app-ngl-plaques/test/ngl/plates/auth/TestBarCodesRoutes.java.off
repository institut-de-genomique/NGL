package ngl.plates.auth;

import org.junit.Test;

import fr.cea.ig.ngl.test.authentication.Identity;
import ngl.plates.Global;

public class TestBarCodesRoutes {

	@Test
	public void testHome() {
		Global.af.authURL(Identity.Read,"/barcodes/spongebob/home");
	}	
	
	@Test
	public void testTplCreate() {
		Global.af.authNobody("/tpl/barcodes/create");
	}

	@Test
	public void testTplSearch() {
		Global.af.authNobody("/tpl/barcodes/search");
	}

	@Test
	public void testJsRoutes() {
		Global.af.authNobody("/tpl/barcodes/js-routes");
	}

}
