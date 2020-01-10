package ngl.sq.instruments.io;

import static org.junit.Assert.assertEquals;

import fr.cea.ig.play.test.NGLWSClient;
import ngl.sq.Global;
import play.libs.ws.WSResponse;

// We miss data.
public class NovaSeqOutputTest {

	private static final play.Logger.ALogger logger = play.Logger.of(NovaSeqOutputTest.class);

	public static class OutputTest {
		
		// Given an url, we compare the body of the result.
		// Do not need to be static stuff.
		private String s0,s1;

		public void test(String url) {
			Global.afOld.ws(ws -> {
				NGLWSClient c = ws;
				WSResponse r = c.get(url,200);
				s0 = r.getBody();
				logger.debug("response 0 : {}", s0);
			});
			Global.afNew.ws(ws -> {
				NGLWSClient c = ws; // new NGLWSClient(ws);
				WSResponse r = c.get(url,200);
				s1 = r.getBody();			
				logger.debug("response 1 : {}", s1);
			});		
			assertEquals("responses",s0,s1);
		}

	}
	
	// @Test
	public void testSampleSheet1() {
		logger.debug("** testSampleSheet1");
		// Should upload some data to run the tests.
		
		// http://appuat.genoscope.cns.fr:9153/experiments/ILLUMINA-DEPOT-20171005_151349HFF
//		String url = "/api/experiments/ILLUMINA-DEPOT-20171005_151349HFF/file/generation";		
//		Global.afOld.ws(ws -> {
//			NGLWSClient c = new NGLWSClient(ws);
//			WSResponse r = c.get(url,200);
//			s0 = r.getBody();
//		});
//		Global.afNew.ws(ws -> {
//			NGLWSClient c = new NGLWSClient(ws);
//			WSResponse r = c.get(url,200);
//			s1 = r.getBody();			
//		});
		new OutputTest().test("/api/experiments/ILLUMINA-DEPOT-20171005_151349HFF/file/generation");
	}
	
//	@Test
//	public void testSampleSheet2() {
//	}

}
