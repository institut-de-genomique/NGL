package ngl.sq.auth;

import org.junit.Test;

import fr.cea.ig.ngl.test.authentication.Identity;
import ngl.sq.Global;

public class TestProcessesRoutes {

//	GET 	/processes/:homecode/home										controllers.processes.tpl.Processes.home(homecode:java.lang.String)
//	GET		/tpl/processes/search/:processTypeCode							controllers.processes.tpl.Processes.search(processTypeCode:java.lang.String)
//	GET		/tpl/processes/search-containers								controllers.processes.tpl.Processes.searchContainers()
//	GET		/tpl/processes/search-samples									controllers.processes.tpl.Processes.searchSamples()
//	GET		/tpl/processes/new/:processTypeCode								controllers.processes.tpl.Processes.newProcesses(processTypeCode:java.lang.String)
//	GET		/tpl/processes/assign/:processTypeCode							controllers.processes.tpl.Processes.assignProcesses(processTypeCode:java.lang.String)
//	GET		/tpl/processes/js-routes										controllers.processes.tpl.Processes.javascriptRoutes()

	@Test
	public void testHome() {
		Global.af.authURL(Identity.Read,"/processes/searchContainers/home");
	}
	
	@Test
	public void testHomeSponge() {
		Global.af.authURL(Identity.Read,"/processes/spongebob/home");
	}

	@Test
	public void testTplSearchContainers() {
		Global.af.authNobody("/tpl/processes/search-containers");
	}
	
	@Test
	public void testTplSearchSamples() {
		Global.af.authNobody("/tpl/processes/search-samples");
	}

	@Test
	public void testJsRoutes() {
		Global.af.authNobody("/tpl/processes/js-routes");
	}
	
}
