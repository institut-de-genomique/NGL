package ngl.projects.auth;

import static ngl.projects.Global.af;

import org.junit.Test;

import fr.cea.ig.ngl.test.authentication.Identity;

public class TestUmbrellaProjects {
	
	@Test
	public void testHome() {
		af.authURL(Identity.Read,"/umbrellaprojects/spongebob/home");
	}
	
	@Test
	public void testTplDetails() {
		af.authNobody("/tpl/umbrellaprojects/details/spongebob");
	}
	
	@Test
	public void testTplSearch() {
		af.authNobody("/tpl/umbrellaprojects/search/spongebob");
	}
	
	@Test
	public void testJsRoutes() {
		af.authNobody("/tpl/umbrellaprojects/js-routes");
	}
	
}
