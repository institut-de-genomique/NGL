package data.resources;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

import ngl.bi.Global;
import play.libs.ws.WSResponse;

//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
//public class RunCategoryTest extends AbstractBIServerTest {
public class RunCategoryTest {

	private static final play.Logger.ALogger logger = play.Logger.of(RunCategoryTest.class);
	
	@Test
	public void test1list()	throws Exception {
		Global.af.runWs((app,ws) -> {
			logger.debug("list RunCategory");
			WSResponse response = ws.asBot().get("/api/run-categories", 200);
			assertThat(response.asJson()).isNotNull();
		});
	}

}
