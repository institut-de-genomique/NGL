package ngl.sq.auth;

import org.junit.Test;

import fr.cea.ig.ngl.test.authentication.Identity;
import ngl.sq.Global;

public class TestExperimentsRoutes {

	@Test
	public void testHome() {
		Global.af.authURL(Identity.Read,"/experiments/spongebob/home");
	}
	
	@Test
	public void testTplDetails() {
		Global.af.authNobody("/tpl/experiments/details");
	}
	
	@Test
	public void testTplGraph() {
		Global.af.authNobody("/tpl/experiments/graph");
	}
	
	@Test
	public void testTplSearchContainers() {
		Global.af.authNobody("/tpl/experiments/search-containers");
	}
	
	@Test
	public void testTplListContainers() {
		Global.af.authNobody("/tpl/experiments/list-containers");
	}
	
	@Test
	public void testTplTemplate() {
		Global.af.authNobody("/tpl/experiments/get-template/onetoone/fragmentation/transformation/null");
	}
	
	@Test
	public void testTplSearch() {
		Global.af.authNobody("/tpl/experiments/search/spongebob");
	}

	@Test
	public void testJsRoutes() {
		Global.af.authNobody("/tpl/experiments/js-routes");
	}
	
	// GET		/tpl/experiments/get-template/:atomicType/:outputCategoryCode/:experimentCategoryCode/:experimentType			controllers.experiments.tpl.Experiments.getTemplate(atomicType:java.lang.String,outputCategoryCode:java.lang.String, experimentCategoryCode:java.lang.String, experimentType:java.lang.String)

}
