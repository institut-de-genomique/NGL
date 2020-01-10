package ngl.reagents.auth;

import org.junit.Test;

import fr.cea.ig.ngl.test.authentication.Identity;
import ngl.reagents.Global;

public class TestBoxesRoutes {
	
	@Test
	public void testHome() {
		Global.af.authURL(Identity.Read,"/boxes/spongebob/home");
	}	
	
	@Test
	public void testMessages() {
		Global.af.authNobody("/tpl/boxes/search");
	}
	
}
