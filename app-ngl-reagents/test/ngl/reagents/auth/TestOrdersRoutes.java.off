package ngl.reagents.auth;

import org.junit.Test;

import fr.cea.ig.ngl.test.authentication.Identity;
import ngl.reagents.Global;

public class TestOrdersRoutes {
	
	@Test
	public void testHome() {
		Global.af.authURL(Identity.Read,"/orders/spongebob/home");
	}
	
	@Test
	public void testTplCreation() {
		Global.af.authNobody("/tpl/orders/creation");
	}

}
