package workflows;

//import static fr.cea.ig.play.test.DevAppTesting.testInServer;
//import static ngl.bi.Global.devapp;
import static org.fest.assertions.Assertions.assertThat;

//import java.util.function.Supplier;

//import org.junit.AfterClass;
//import org.junit.BeforeClass;
import org.junit.Test;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.play.test.NGLWSClient;
import fr.cea.ig.util.function.CC1Managed;
//import fr.cea.ig.util.function.C1;
//import fr.cea.ig.util.function.CC1;
import fr.cea.ig.util.function.CC3;
import fr.cea.ig.util.function.CCActions;
//import fr.cea.ig.util.function.F0;
//import fr.cea.ig.play.test.WSHelper;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.run.instance.Analysis;
import models.laboratory.run.instance.ReadSet;
import models.utils.InstanceConstants;
import ngl.bi.Global;
import play.Application;
//import utils.AbstractAbstractTests;
import utils.RunMockHelper;




//public class AnalysesWorkflowTest extends AbstractAbstractTests {
public class AnalysesWorkflowTest {

	private static final play.Logger.ALogger logger = play.Logger.of(AnalysesWorkflowTest.class);
	
//	static Analysis analysis;
//
//	@BeforeClass
//	public static void initData() {
//		analysis = MongoDBDAO.find("ngl_bi.Analysis_dataWF", Analysis.class).toList().get(0);
//		MongoDBDAO.save(InstanceConstants.ANALYSIS_COLL_NAME, analysis);
//
//		for (String codeReadSet : analysis.masterReadSetCodes) {
//			ReadSet readSet = MongoDBDAO.findByCode("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class, codeReadSet);
//			MongoDBDAO.save(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet);
//		}
//	}
//
//	@AfterClass
//	public static void deleteData() {
//		analysis = MongoDBDAO.findByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, analysis.code);
//		MongoDBDAO.delete(InstanceConstants.ANALYSIS_COLL_NAME, analysis);
//		for (String codeReadSet : analysis.masterReadSetCodes) {
//			ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, codeReadSet);
//			MongoDBDAO.delete(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet);
//		}
//	}

	private static class TestContext implements CC1Managed {
		
		Analysis analysis;

		@Override
		public void setUp() {
			analysis = MongoDBDAO.find("ngl_bi.Analysis_dataWF", Analysis.class).toList().get(0);
			MongoDBDAO.save(InstanceConstants.ANALYSIS_COLL_NAME, analysis);

			for (String codeReadSet : analysis.masterReadSetCodes) {
				ReadSet readSet = MongoDBDAO.findByCode("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class, codeReadSet);
				MongoDBDAO.save(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet);
			}
		}

		@Override
		public void tearDown() {
			analysis = MongoDBDAO.findByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, analysis.code);
			MongoDBDAO.delete(InstanceConstants.ANALYSIS_COLL_NAME, analysis);
			for (String codeReadSet : analysis.masterReadSetCodes) {
				ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, codeReadSet);
				MongoDBDAO.delete(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet);
			}
		}
		
	}
	
	private static final CC3<Application,NGLWSClient,TestContext> af = 
			Global.af.cc2()
			.and(CCActions.managed(TestContext::new));
	
//	@Test
//	public void setStateIPBA() {
//		testInServer(devapp(),
//				ws -> {	
//					logger.debug("setStateIPS");
//					State state = new State("IP-BA","bot");
//					//Result r = callAction(controllers.analyses.api.routes.ref.Analyses.state(analysis.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
//					//assertThat(status(r)).isEqualTo(OK);
//					WSHelper.putAsBot(ws, "/api/analyses/"+analysis.code+"/state", RunMockHelper.getJsonState(state).toString(), 200);
//					analysis = MongoDBDAO.findByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, analysis.code);
//					logger.debug("state code"+analysis.state.code);
//					assertThat(analysis.state.code).isEqualTo("IP-BA");
//					for(String codeReadSet : analysis.masterReadSetCodes){
//						ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, codeReadSet);
//						assertThat(readSet.state.code).isEqualTo("IP-BA");
//					}
//				});
//	}

	@Test
	public void setStateIPBA() throws Exception {
		af.accept((app,ws,ctx) -> {
			logger.debug("setStateIPS");
			State state = new State("IP-BA","bot");
			//Result r = callAction(controllers.analyses.api.routes.ref.Analyses.state(analysis.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
			//assertThat(status(r)).isEqualTo(OK);
			ws.asBot().put("/api/analyses/"+ctx.analysis.code+"/state", RunMockHelper.getJsonState(state).toString(), 200);
			ctx.analysis = MongoDBDAO.findByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, ctx.analysis.code);
			logger.debug("state code"+ctx.analysis.state.code);
			assertThat(ctx.analysis.state.code).isEqualTo("IP-BA");
			for(String codeReadSet : ctx.analysis.masterReadSetCodes){
				ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, codeReadSet);
				assertThat(readSet.state.code).isEqualTo("IP-BA");
			}
		});
	}

//	@Test
//	public void setStateFBA()
//	{
//		testInServer(devapp(),
//				ws -> {	
//					analysis = MongoDBDAO.findByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, analysis.code);
//					analysis.valuation.valid=TBoolean.TRUE;
//					MongoDBDAO.save(InstanceConstants.ANALYSIS_COLL_NAME, analysis);
//
//					logger.debug("setStateFBA");
//					State state = new State("F-BA","bot");
//					//Result r = callAction(controllers.analyses.api.routes.ref.Analyses.state(analysis.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
//					//assertThat(status(r)).isEqualTo(OK);
//					WSHelper.putAsBot(ws, "/api/analyses/"+analysis.code+"/state", RunMockHelper.getJsonState(state).toString(), 200);
//					analysis = MongoDBDAO.findByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, analysis.code);
//					logger.debug("state code"+analysis.state.code);
//					assertThat(analysis.state.code).isEqualTo("F-V");
//					for(String codeReadSet : analysis.masterReadSetCodes){
//						ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, codeReadSet);
//						assertThat(readSet.state.code).isEqualTo("A");
//					}
//				});
//	}

	@Test
	public void setStateFBA() throws Exception {
		af.accept((app,ws,ctx) -> {
			ctx.analysis = MongoDBDAO.findByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, ctx.analysis.code);
			ctx.analysis.valuation.valid=TBoolean.TRUE;
			MongoDBDAO.save(InstanceConstants.ANALYSIS_COLL_NAME, ctx.analysis);

			logger.debug("setStateFBA");
			State state = new State("F-BA","bot");
			//Result r = callAction(controllers.analyses.api.routes.ref.Analyses.state(analysis.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
			//assertThat(status(r)).isEqualTo(OK);
			ws.asBot().put("/api/analyses/"+ctx.analysis.code+"/state", RunMockHelper.getJsonState(state).toString(), 200);
			ctx.analysis = MongoDBDAO.findByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, ctx.analysis.code);
			logger.debug("state code"+ctx.analysis.state.code);
			assertThat(ctx.analysis.state.code).isEqualTo("F-V");
			for(String codeReadSet : ctx.analysis.masterReadSetCodes){
				ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, codeReadSet);
				assertThat(readSet.state.code).isEqualTo("A");
			}
		});
	}

//	@Test
//	public void setStateIWV()
//	{
//		testInServer(devapp(),
//				ws -> {	
//					analysis = MongoDBDAO.findByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, analysis.code);
//					analysis.valuation.valid=TBoolean.UNSET;
//					MongoDBDAO.save(InstanceConstants.ANALYSIS_COLL_NAME, analysis);
//
//					logger.debug("setStateFBA");
//					State state = new State("IW-V","bot");
//					//Result r = callAction(controllers.analyses.api.routes.ref.Analyses.state(analysis.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
//					//assertThat(status(r)).isEqualTo(OK);
//					WSHelper.putAsBot(ws, "/api/analyses/"+analysis.code+"/state", RunMockHelper.getJsonState(state).toString(), 200);
//
//					analysis = MongoDBDAO.findByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, analysis.code);
//					logger.debug("state code"+analysis.state.code);
//					assertThat(analysis.state.code).isEqualTo("IW-V");
//					for(String codeReadSet : analysis.masterReadSetCodes){
//						ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, codeReadSet);
//						assertThat(readSet.state.code).isEqualTo("IW-VBA");
//					}
//				});
//	}

	@Test
	public void setStateIWV() throws Exception {
		af.accept((app,ws,ctx) -> {
			ctx.analysis = MongoDBDAO.findByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, ctx.analysis.code);
			ctx.analysis.valuation.valid = TBoolean.UNSET;
			MongoDBDAO.save(InstanceConstants.ANALYSIS_COLL_NAME, ctx.analysis);

			logger.debug("setStateFBA");
			State state = new State("IW-V","bot");
			//Result r = callAction(controllers.analyses.api.routes.ref.Analyses.state(analysis.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
			//assertThat(status(r)).isEqualTo(OK);
			ws.asBot().put("/api/analyses/"+ctx.analysis.code+"/state", RunMockHelper.getJsonState(state).toString(), 200);

			ctx.analysis = MongoDBDAO.findByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, ctx.analysis.code);
			logger.debug("state code"+ctx.analysis.state.code);
			assertThat(ctx.analysis.state.code).isEqualTo("IW-V");
			for(String codeReadSet : ctx.analysis.masterReadSetCodes){
				ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, codeReadSet);
				assertThat(readSet.state.code).isEqualTo("IW-VBA");
			}
		});
	}

//	@Test
//	public void setStateFV()
//	{
//		testInServer(devapp(),
//				ws -> {	
//					analysis = MongoDBDAO.findByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, analysis.code);
//					analysis.valuation.valid=TBoolean.UNSET;
//					MongoDBDAO.save(InstanceConstants.ANALYSIS_COLL_NAME, analysis);
//
//					logger.debug("setStateFBA");
//					State state = new State("IW-V","bot");
//					//Result r = callAction(controllers.analyses.api.routes.ref.Analyses.state(analysis.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
//					//assertThat(status(r)).isEqualTo(OK);
//					WSHelper.putAsBot(ws, "/api/analyses/"+analysis.code+"/state", RunMockHelper.getJsonState(state).toString(), 200);
//					
//					analysis = MongoDBDAO.findByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, analysis.code);
//					logger.debug("state code"+analysis.state.code);
//					assertThat(analysis.state.code).isEqualTo("IW-V");
//					for(String codeReadSet : analysis.masterReadSetCodes){
//						ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, codeReadSet);
//						assertThat(readSet.state.code).isEqualTo("IW-VBA");
//					}
//				});
//	}
	
	@Test
	public void setStateFV() throws Exception {
		af.accept((app,ws,ctx) -> {
			ctx.analysis = MongoDBDAO.findByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, ctx.analysis.code);
			ctx.analysis.valuation.valid=TBoolean.UNSET;
			MongoDBDAO.save(InstanceConstants.ANALYSIS_COLL_NAME, ctx.analysis);

			logger.debug("setStateFBA");
			State state = new State("IW-V","bot");
			//Result r = callAction(controllers.analyses.api.routes.ref.Analyses.state(analysis.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
			//assertThat(status(r)).isEqualTo(OK);
			ws.asBot().put("/api/analyses/"+ctx.analysis.code+"/state", RunMockHelper.getJsonState(state).toString(), 200);

			ctx.analysis = MongoDBDAO.findByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, ctx.analysis.code);
			logger.debug("state code"+ctx.analysis.state.code);
			assertThat(ctx.analysis.state.code).isEqualTo("IW-V");
			for(String codeReadSet : ctx.analysis.masterReadSetCodes){
				ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, codeReadSet);
				assertThat(readSet.state.code).isEqualTo("IW-VBA");
			}
		});
	}
	
}


