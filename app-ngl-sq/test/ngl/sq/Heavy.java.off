//
//package ngl.sq;
//
//import org.junit.Test;
//
//import com.fasterxml.jackson.databind.JsonNode;
//
//import fr.cea.ig.authentication.IAuthenticator;
//import fr.cea.ig.authorization.IAuthorizator;
//import fr.cea.ig.ngl.test.TestAuthConfig;
//
//import static fr.cea.ig.play.test.DevAppTesting.newCode;
//import static fr.cea.ig.play.test.DevAppTesting.cr;
//import static fr.cea.ig.play.test.DevAppTesting.rurNeqTraceInfo;
//import static fr.cea.ig.play.test.DevAppTesting.testInServer;
//import static fr.cea.ig.play.test.DevAppTesting.savage;
//import static fr.cea.ig.play.test.RoutesTest.checkRoutes;
//import static fr.cea.ig.play.test.WSHelper.get;
//
//import static ngl.sq.Global.devapp;
//import static org.junit.Assert.assertEquals;
//import static play.test.Helpers.OK;
//
//import java.io.IOException;
//
//import static play.test.Helpers.NOT_FOUND;
//
//import models.laboratory.container.instance.Container;
//import models.laboratory.sample.instance.Sample;
//import play.Application;
//import play.test.TestServer;
//
//public class Heavy {
//	
//	private static final play.Logger.ALogger logger = play.Logger.of(Heavy.class);
//	
//	// @Test
//	public void test01() throws Exception {
//		Application app = devapp(TestAuthConfig.asNobody);
//	    testInServer(app,
//	    		ws -> {	    	
//
//	    			logger.info("auth {} {}",app.injector().instanceOf(IAuthenticator.class),app.injector().instanceOf(IAuthorizator.class));
//	    			// Assuming that we have a json response from server that that the get/put
//	    			// urls are properly defined, we provide a json alteration function that is
//	    			// compared to the get after the put. The other way around is to assert that modified values
//	    			// in the input are stored and thus access the values by path and not do a full comparison.
//	    			checkRoutes(ws);
//	    			
//	    			// Check UI for Sample
//	    			Sample sample = SampleFactory.createSample(ws);
//	    			rurNeqTraceInfo(ws,"/api/samples/" + sample.getCode());
//	    			get(ws,"/samples/AAAA-A120_ST147_T0_A",OK);
//	    			// Check UI for ContainerSupport
//	    			
//	    			// Check UI for Container 
//	    			
//	    			// RUR tests are currently not modifying anything as we use the
//	    			// RUR method with the default values so those are barely tests. 
//	    			// The confirmation of success could come from the update date that would be different.
//	    			// We use the object names to trigger the view template and the rur.
//	    			/*
//	    			// Echantillons - samples
//	    			rurNeqTraceInfo(ws,"/api/samples/AAAA-A120_ST147_T0_A");
//	    			get(ws,"/samples/AAAA-A120_ST147_T0_A",OK);
//
//	    			// Supports - supports
//	    			rurNeqTraceInfo(ws,"/api/supports/2A4F4FL2H");
//	    			get(ws,"/supports/2A4F4FL2H",OK);
//
//	    			// Containers - containers
//	    			//rurNeqTraceInfo("/api/containers/HLMF5BBXX_8",ws);
//	    			rurNeqTraceInfo(ws,"/api/containers/29J81XXL4");
//	    			get(ws,"/containers/29J81XXL4",OK);
//
//	    			// Processus - processes 
//	    			rurNeqTraceInfo(ws,"/api/processes/BUK_AAAA_METAGENOMIC-PROCESS-WITH-SPRI-SELECT_2A4E2L2AK");
//	    			// This is a 404
//	    			// get(ws,"/processes/BUK_AAAA_METAGENOMIC-PROCESS-WITH-SPRI-SELECT_2A4E2L2AK",OK);
//
//	    			// Experiences - experiments
//	    			rurNeqTraceInfo(ws,"/api/experiments/CHIP-MIGRATION-20170915_144939CDA");
//	    			get(ws,"/experiments/CHIP-MIGRATION-20170915_144939CDA",OK);
//
//	    			// Create container  - create and get code/id
//	    			// probably /api/containers
//
//	    			// Create experiment - create and get code/id
//	    			*/
//
//	    		});
//	}
//	
//	//
//	
//}
