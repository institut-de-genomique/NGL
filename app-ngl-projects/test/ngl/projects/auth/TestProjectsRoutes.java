package ngl.projects.auth;

import static ngl.projects.Global.af;

import org.junit.Test;

import fr.cea.ig.ngl.test.authentication.Identity;

public class TestProjectsRoutes {
	
	@Test
	public void testHome() {
		af.authURL(Identity.Read,"/projects/spongebob/home");
	}
	
	// test disabled because it requires AD configuration which is not available in tests
	//@Test
	public void testTplDetails() {
		af.authNobody("/tpl/projects/details");
	}

	@Test
	public void testTplSearch() {
		af.authNobody("/tpl/projects/search/spongebob");
	}
	
	@Test
	public void testJsRoute() {
		af.authNobody("/tpl/projects/js-routes");
	}

}
