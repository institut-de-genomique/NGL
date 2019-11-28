package workflows;

import static org.fest.assertions.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mongojack.DBQuery;

import com.mongodb.BasicDBObject;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.play.test.NGLWSClient;
import fr.cea.ig.util.function.CC1Managed;
import fr.cea.ig.util.function.CC3;
import fr.cea.ig.util.function.CCActions;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.Content;
import models.laboratory.project.instance.Project;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import ngl.bi.Global;
import play.Application;
import utils.RunMockHelper;

public class ReadSetWorkflowTest {

	private static final play.Logger.ALogger logger = play.Logger.of(ReadSetWorkflowTest.class);
	
//	static ReadSet readSet;
//	static Run run;
//	static Container container;
//	static Project project;
//
//	@BeforeClass
//	public static void initData() 
//	{
//		readSet = MongoDBDAO.find("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class, DBQuery.is("state.code","N"), getReadSetKeys()).limit(1).toList().get(0);
//		readSet.sampleOnContainer=null;
//		readSet.dispatch=false;
//		readSet.productionValuation.valid=TBoolean.UNSET;
//
//		MongoDBDAO.save(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet);
//
//		//get container 
//		run = MongoDBDAO
//				.findOne("ngl_bi.RunIllumina_dataWF", Run.class, DBQuery.is("code", readSet.runCode));
//		MongoDBDAO.save(InstanceConstants.RUN_ILLUMINA_COLL_NAME, run);
//
//		String containerSupportCode = run.containerSupportCode;
//		List<String> qCodes = new ArrayList<>();
//		qCodes.add(readSet.sampleCode);
//		container = MongoDBDAO.findOne("ngl_sq.Container_dataWF",Container.class,
//				DBQuery.and(DBQuery.is("support.code", containerSupportCode),
//						    DBQuery.is("support.line", readSet.laneNumber.toString()),
//						    DBQuery.in("sampleCodes", readSet.sampleCode)));
//		MongoDBDAO.save(InstanceConstants.CONTAINER_COLL_NAME, container);
//
//		for(Content content : container.contents){
//			Sample sample = MongoDBDAO.findByCode("ngl_bq.Sample_dataWF", Sample.class, content.sampleCode);
//			MongoDBDAO.save(InstanceConstants.SAMPLE_COLL_NAME, sample);
//		}
//
//		project = MongoDBDAO.findByCode("ngl_project.Project_dataWF", Project.class, readSet.projectCode);
//		project.bioinformaticParameters.biologicalAnalysis=false;
//		project.bioinformaticParameters.fgGroup=null;
//		MongoDBDAO.save(InstanceConstants.PROJECT_COLL_NAME, project);
//	}
//
//	@AfterClass
//	public static void deleteData()
//	{
//		//get readSet to test
//		readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
//		MongoDBDAO.delete(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet);
//		run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, run.code);
//		MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, run);
//		container = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, container.code);
//		MongoDBDAO.delete(InstanceConstants.CONTAINER_COLL_NAME, container);
//		for(Content content : container.contents){
//			Sample sample = MongoDBDAO.findByCode("ngl_bq.Sample_dataWF", Sample.class, content.sampleCode);
//			MongoDBDAO.delete(InstanceConstants.SAMPLE_COLL_NAME, sample);
//
//		}
//		project = MongoDBDAO.findByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, readSet.projectCode);
//		MongoDBDAO.delete(InstanceConstants.PROJECT_COLL_NAME, project);
//	}

	private static class TestContext implements CC1Managed {
		ReadSet readSet;
		Run run;
		Container container;
		Project project;

		@Override
		public void setUp() {
			readSet = MongoDBDAO.find("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class, DBQuery.is("state.code","N"), getReadSetKeys()).limit(1).toList().get(0);
			readSet.sampleOnContainer         = null;
			readSet.dispatch                  = false;
			readSet.productionValuation.valid = TBoolean.UNSET;

			MongoDBDAO.save(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet);

			//get container 
			run = MongoDBDAO.findOne("ngl_bi.RunIllumina_dataWF", Run.class, DBQuery.is("code", readSet.runCode));
			MongoDBDAO.save(InstanceConstants.RUN_ILLUMINA_COLL_NAME, run);

			String containerSupportCode = run.containerSupportCode;
			List<String> qCodes = new ArrayList<>();
			qCodes.add(readSet.sampleCode);
			container = MongoDBDAO.findOne("ngl_sq.Container_dataWF", Container.class,
					                       DBQuery.and(DBQuery.is("support.code", containerSupportCode),
							                           DBQuery.is("support.line", readSet.laneNumber.toString()),
							                           DBQuery.in("sampleCodes", readSet.sampleCode)));
			MongoDBDAO.save(InstanceConstants.CONTAINER_COLL_NAME, container);

			for (Content content : container.contents) {
				Sample sample = MongoDBDAO.findByCode("ngl_bq.Sample_dataWF", Sample.class, content.sampleCode);
				MongoDBDAO.save(InstanceConstants.SAMPLE_COLL_NAME, sample);
			}

			project = MongoDBDAO.findByCode("ngl_project.Project_dataWF", Project.class, readSet.projectCode);
			project.bioinformaticParameters.biologicalAnalysis=false;
			project.bioinformaticParameters.fgGroup=null;
			MongoDBDAO.save(InstanceConstants.PROJECT_COLL_NAME, project);
		}

		@Override
		public void tearDown() {
			//get readSet to test
			readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
			MongoDBDAO.delete(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet);
			run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, run.code);
			MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, run);
			container = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, container.code);
			MongoDBDAO.delete(InstanceConstants.CONTAINER_COLL_NAME, container);
			for (Content content : container.contents) {
				Sample sample = MongoDBDAO.findByCode("ngl_bq.Sample_dataWF", Sample.class, content.sampleCode);
				MongoDBDAO.delete(InstanceConstants.SAMPLE_COLL_NAME, sample);
			}
			project = MongoDBDAO.findByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, readSet.projectCode);
			MongoDBDAO.delete(InstanceConstants.PROJECT_COLL_NAME, project);
		}

	}
	
	private static final CC3<Application,NGLWSClient,TestContext> af = 
			Global.af.cc2()
			.and(CCActions.managed(TestContext::new));

//	@Test
//	public void setStateIPRG()
//	{
//		testInServer(devapp(),
//				ws -> {	
//					Logger.debug("setStateIPRG");
//					State state = new State("IP-RG","bot");
//					//Result r = callAction(controllers.readsets.api.routes.ref.ReadSets.state(readSet.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
//					//assertThat(status(r)).isEqualTo(OK);
//					WSHelper.putAsBot(ws, "/api/readsets/"+readSet.code+"/state", RunMockHelper.getJsonState(state).toString(), 200);
//
//					readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
//					Logger.debug("state code"+readSet.state.code);
//					assertThat(readSet.state.code).isEqualTo("IP-RG");
//
//					//Check sampleOnContainer
//					assertThat(readSet.sampleOnContainer).isNotNull();
//				});
//	}

	@Test
	public void setStateIPRG() throws Exception {
		af.accept((app,ws,ctx) -> {
			logger.debug("setStateIPRG");
			State state = new State("IP-RG","bot");
			//Result r = callAction(controllers.readsets.api.routes.ref.ReadSets.state(readSet.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
			//assertThat(status(r)).isEqualTo(OK);
			ws.asBot().put("/api/readsets/"+ctx.readSet.code+"/state", RunMockHelper.getJsonState(state).toString(), 200);
			ctx.readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, ctx.readSet.code);
			logger.debug("state code"+ctx.readSet.state.code);
			assertThat(ctx.readSet.state.code).isEqualTo("IP-RG");
			// Check sampleOnContainer
			assertThat(ctx.readSet.sampleOnContainer).isNotNull();
		});
	}

//	@Test
//	public void setStateFRG()
//	{
//		testInServer(devapp(),
//				ws -> {	
//					Logger.debug("setStateFRG");
//					State state = new State("F-RG","bot");
//					//Result r = callAction(controllers.readsets.api.routes.ref.ReadSets.state(readSet.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
//					//assertThat(status(r)).isEqualTo(OK);
//					WSHelper.putAsBot(ws, "/api/readsets/"+readSet.code+"/state", RunMockHelper.getJsonState(state).toString(), 200);
//
//					readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
//					Logger.debug("state code"+readSet.state.code);
//					assertThat(readSet.state.code).isEqualTo("IW-QC");
//					//check dispatch
//					assertThat(readSet.dispatch).isEqualTo(true);
//				});
//
//	}

	@Test
	public void setStateFRG() throws Exception {
		af.accept((app,ws,ctx) -> {
			logger.debug("setStateFRG");
			State state = new State("F-RG","bot");
			//Result r = callAction(controllers.readsets.api.routes.ref.ReadSets.state(readSet.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
			//assertThat(status(r)).isEqualTo(OK);
			ws.asBot().put("/api/readsets/"+ctx.readSet.code+"/state", RunMockHelper.getJsonState(state).toString(), 200);

			ctx.readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, ctx.readSet.code);
			logger.debug("state code"+ctx.readSet.state.code);
			assertThat(ctx.readSet.state.code).isEqualTo("IW-QC");
			//check dispatch
			assertThat(ctx.readSet.dispatch).isEqualTo(true);
		});
	}

//	@Test
//	public void setStateFQC()
//	{
//		testInServer(devapp(),
//				ws -> {	
//					readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
//					readSet.productionValuation.valid=TBoolean.UNSET;
//					MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet);
//					Logger.debug("setStateFQC");
//					State state = new State("F-QC","bot");
//					//Result r = callAction(controllers.readsets.api.routes.ref.ReadSets.state(readSet.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
//					//assertThat(status(r)).isEqualTo(OK);
//					WSHelper.putAsBot(ws, "/api/readsets/"+readSet.code+"/state", RunMockHelper.getJsonState(state).toString(), 200);
//
//					readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
//					Logger.debug("state code"+readSet.state.code);
//					assertThat(readSet.state.code).isEqualTo("IW-VQC");
//				});
//	}

	@Test
	public void setStateFQC() throws Exception {
		af.accept((app,ws,ctx) -> {
			ctx.readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, ctx.readSet.code);
			ctx.readSet.productionValuation.valid=TBoolean.UNSET;
			MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ctx.readSet);
			logger.debug("setStateFQC");
			State state = new State("F-QC","bot");
			//Result r = callAction(controllers.readsets.api.routes.ref.ReadSets.state(readSet.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
			//assertThat(status(r)).isEqualTo(OK);
			ws.asBot().put("/api/readsets/"+ctx.readSet.code+"/state", RunMockHelper.getJsonState(state).toString(), 200);

			ctx.readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, ctx.readSet.code);
			logger.debug("state code"+ctx.readSet.state.code);
			assertThat(ctx.readSet.state.code).isEqualTo("IW-VQC");
		});
	}

//	@Test
//	public void setStateFQCA()
//	{
//		testInServer(devapp(),
//				ws -> {	
//					readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
//					readSet.productionValuation.valid=TBoolean.TRUE;
//					MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet);
//
//					Logger.debug("setStateFQC to A");
//					State state = new State("F-QC","bot");
//					//Result r = callAction(controllers.readsets.api.routes.ref.ReadSets.state(readSet.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
//					//assertThat(status(r)).isEqualTo(OK);
//					WSHelper.putAsBot(ws, "/api/readsets/"+readSet.code+"/state", RunMockHelper.getJsonState(state).toString(), 200);
//
//					readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
//					Logger.debug("state code"+readSet.state.code);
//					assertThat(readSet.state.code).isEqualTo("A");
//				});
//	}

	@Test
	public void setStateFQCA() throws Exception {
		af.accept((app,ws,ctx) -> {
			ctx.readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, ctx.readSet.code);
			ctx.readSet.productionValuation.valid = TBoolean.TRUE;
			MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ctx.readSet);

			logger.debug("setStateFQC to A");
			State state = new State("F-QC","bot");
			//Result r = callAction(controllers.readsets.api.routes.ref.ReadSets.state(readSet.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
			//assertThat(status(r)).isEqualTo(OK);
			ws.asBot().put("/api/readsets/"+ctx.readSet.code+"/state", RunMockHelper.getJsonState(state).toString(), 200);

			ctx.readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, ctx.readSet.code);
			logger.debug("state code"+ctx.readSet.state.code);
			assertThat(ctx.readSet.state.code).isEqualTo("A");
		});
	}

//	@Test
//	public void setStateIWBA()
//	{
//		testInServer(devapp(),
//				ws -> {	
//					Logger.debug("setState IWBA");
//					State state = new State("IW-BA","bot");
//					//Result r = callAction(controllers.readsets.api.routes.ref.ReadSets.state(readSet.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
//					//assertThat(status(r)).isEqualTo(OK);
//					WSHelper.putAsBot(ws, "/api/readsets/"+readSet.code+"/state", RunMockHelper.getJsonState(state).toString(), 200);
//
//					readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
//					Logger.debug("state code"+readSet.state.code);
//					assertThat(readSet.state.code).isEqualTo("IW-BA");
//				});
//	}

	@Test
	public void setStateIWBA() throws Exception {
		af.accept((app,ws,ctx) -> {
			logger.debug("setState IWBA");
			State state = new State("IW-BA","bot");
			//Result r = callAction(controllers.readsets.api.routes.ref.ReadSets.state(readSet.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
			//assertThat(status(r)).isEqualTo(OK);
			ws.asBot().put("/api/readsets/"+ctx.readSet.code+"/state", RunMockHelper.getJsonState(state).toString(), 200);

			ctx.readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, ctx.readSet.code);
			logger.debug("state code"+ctx.readSet.state.code);
			assertThat(ctx.readSet.state.code).isEqualTo("IW-BA");
		});
	}

//	@Test
//	public void setStateIPVQC()
//	{
//		testInServer(devapp(),
//				ws -> {	
//					readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
//					readSet.productionValuation.valid=TBoolean.UNSET;
//					MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet);
//					Logger.debug("setState IPVQC");
//					State state = new State("IP-VQC","bot");
//					//Result r = callAction(controllers.readsets.api.routes.ref.ReadSets.state(readSet.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
//					//assertThat(status(r)).isEqualTo(OK);
//					WSHelper.putAsBot(ws, "/api/readsets/"+readSet.code+"/state", RunMockHelper.getJsonState(state).toString(), 200);
//
//					readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
//					Logger.debug("state code"+readSet.state.code);
//					assertThat(readSet.state.code).isEqualTo("IP-VQC");
//				});
//	}

	@Test
	public void setStateIPVQC()	throws Exception {
		af.accept((app,ws,ctx) -> {
			ctx.readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, ctx.readSet.code);
			ctx.readSet.productionValuation.valid=TBoolean.UNSET;
			MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ctx.readSet);
			logger.debug("setState IPVQC");
			State state = new State("IP-VQC","bot");
			//Result r = callAction(controllers.readsets.api.routes.ref.ReadSets.state(readSet.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
			//assertThat(status(r)).isEqualTo(OK);
			ws.asBot().put("/api/readsets/"+ctx.readSet.code+"/state", RunMockHelper.getJsonState(state).toString(), 200);

			ctx.readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, ctx.readSet.code);
			logger.debug("state code"+ctx.readSet.state.code);
			assertThat(ctx.readSet.state.code).isEqualTo("IP-VQC");
		});
	}

//	@Test
//	public void setStateIPVQCA()
//	{
//		testInServer(devapp(),
//				ws -> {	
//					readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
//					readSet.productionValuation.valid=TBoolean.TRUE;
//					readSet.bioinformaticValuation.valid=TBoolean.TRUE;
//
//					MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet);
//					Logger.debug("setState IPVQC");
//					State state = new State("IP-VQC","bot");
//					//Result r = callAction(controllers.readsets.api.routes.ref.ReadSets.state(readSet.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
//					//assertThat(status(r)).isEqualTo(OK);
//					WSHelper.putAsBot(ws, "/api/readsets/"+readSet.code+"/state", RunMockHelper.getJsonState(state).toString(), 200);
//					readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
//					Logger.debug("state code"+readSet.state.code);
//					assertThat(readSet.state.code).isEqualTo("A");
//				});
//	}

	@Test
	public void setStateIPVQCA() throws Exception {
		af.accept((app,ws,ctx) -> {
			ctx.readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, ctx.readSet.code);
			ctx.readSet.productionValuation.valid=TBoolean.TRUE;
			ctx.readSet.bioinformaticValuation.valid=TBoolean.TRUE;

			MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ctx.readSet);
			logger.debug("setState IPVQC");
			State state = new State("IP-VQC","bot");
			//Result r = callAction(controllers.readsets.api.routes.ref.ReadSets.state(readSet.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
			//assertThat(status(r)).isEqualTo(OK);
			ws.asBot().put("/api/readsets/"+ctx.readSet.code+"/state", RunMockHelper.getJsonState(state).toString(), 200);
			ctx.readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, ctx.readSet.code);
			logger.debug("state code"+ctx.readSet.state.code);
			assertThat(ctx.readSet.state.code).isEqualTo("A");
		});
	}

//	@Test
//	public void setStateIPVQCUA() 
//	{
//		testInServer(devapp(),
//				ws -> {	
//					readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
//					readSet.productionValuation.valid=TBoolean.TRUE;
//					readSet.bioinformaticValuation.valid=TBoolean.FALSE;
//
//					MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet);
//					Logger.debug("setState IPVQC");
//					State state = new State("IP-VQC","bot");
//					//Result r = callAction(controllers.readsets.api.routes.ref.ReadSets.state(readSet.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
//					//assertThat(status(r)).isEqualTo(OK);
//					WSHelper.putAsBot(ws, "/api/readsets/"+readSet.code+"/state", RunMockHelper.getJsonState(state).toString(), 200);
//					readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
//					Logger.debug("state code"+readSet.state.code);
//					assertThat(readSet.state.code).isEqualTo("UA");
//				});
//	}

	@Test
	public void setStateIPVQCUA() throws Exception {
		af.accept((app,ws,ctx) -> {
			ctx.readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, ctx.readSet.code);
			ctx.readSet.productionValuation.valid    = TBoolean.TRUE;
			ctx.readSet.bioinformaticValuation.valid = TBoolean.FALSE;

			MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ctx.readSet);
			logger.debug("setState IPVQC");
			State state = new State("IP-VQC","bot");
			//Result r = callAction(controllers.readsets.api.routes.ref.ReadSets.state(readSet.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
			//assertThat(status(r)).isEqualTo(OK);
			ws.asBot().put("/api/readsets/"+ctx.readSet.code+"/state", RunMockHelper.getJsonState(state).toString(), 200);
			ctx.readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, ctx.readSet.code);
			logger.debug("state code"+ctx.readSet.state.code);
			assertThat(ctx.readSet.state.code).isEqualTo("UA");
		});
	}

//	@Test
//	public void setStateFVQCIWBA()
//	{
//		testInServer(devapp(),
//				ws -> {	
//					project = MongoDBDAO.findByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, readSet.projectCode);
//					project.bioinformaticParameters.biologicalAnalysis=true;
//					//project.bioinformaticParameters.regexBiologicalAnalysis="test";
//					MongoDBDAO.update(InstanceConstants.PROJECT_COLL_NAME, project);
//
//					readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
//					readSet.productionValuation.valid=TBoolean.TRUE;
//					readSet.bioinformaticValuation.valid=TBoolean.TRUE;
//
//					MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet);
//					Logger.debug("setState FVQC");
//					State state = new State("F-VQC","bot");
//					//Result r = callAction(controllers.readsets.api.routes.ref.ReadSets.state(readSet.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
//					//assertThat(status(r)).isEqualTo(OK);
//					WSHelper.putAsBot(ws, "/api/readsets/"+readSet.code+"/state", RunMockHelper.getJsonState(state).toString(), 200);
//					readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
//					Logger.debug("state code"+readSet.state.code);
//					assertThat(readSet.state.code).isEqualTo("IW-BA");
//				});
//	}

	@Test
	public void setStateFVQCIWBA() throws Exception {
		af.accept((app,ws,ctx) -> {
			ctx.project = MongoDBDAO.findByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, ctx.readSet.projectCode);
			ctx.project.bioinformaticParameters.biologicalAnalysis = true;
			//project.bioinformaticParameters.regexBiologicalAnalysis="test";
			MongoDBDAO.update(InstanceConstants.PROJECT_COLL_NAME, ctx.project);

			ctx.readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, ctx.readSet.code);
			ctx.readSet.productionValuation.valid    = TBoolean.TRUE;
			ctx.readSet.bioinformaticValuation.valid = TBoolean.TRUE;

			MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ctx.readSet);
			logger.debug("setState FVQC");
			State state = new State("F-VQC","bot");
			//Result r = callAction(controllers.readsets.api.routes.ref.ReadSets.state(readSet.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
			//assertThat(status(r)).isEqualTo(OK);
			ws.asBot().put("/api/readsets/"+ctx.readSet.code+"/state", RunMockHelper.getJsonState(state).toString(), 200);
			ctx.readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, ctx.readSet.code);
			logger.debug("state code"+ctx.readSet.state.code);
			assertThat(ctx.readSet.state.code).isEqualTo("IW-BA");
		});
	}

//	@Test
//	public void setStateFBAIWVBA()
//	{
//		testInServer(devapp(),
//				ws -> {	
//					readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
//					readSet.productionValuation.valid=TBoolean.TRUE;
//					readSet.bioinformaticValuation.valid=TBoolean.UNSET;
//
//					MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet);
//					Logger.debug("setState FBA");
//					State state = new State("F-BA","bot");
//					//Result r = callAction(controllers.readsets.api.routes.ref.ReadSets.state(readSet.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
//					//assertThat(status(r)).isEqualTo(OK);
//					WSHelper.putAsBot(ws, "/api/readsets/"+readSet.code+"/state", RunMockHelper.getJsonState(state).toString(), 200);
//
//					readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
//					Logger.debug("state code"+readSet.state.code);
//					assertThat(readSet.state.code).isEqualTo("IW-VBA");
//				});
//	}

	@Test
	public void setStateFBAIWVBA() throws Exception {
		af.accept((app,ws,ctx) -> {
			ctx.readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, ctx.readSet.code);
			ctx.readSet.productionValuation.valid=TBoolean.TRUE;
			ctx.readSet.bioinformaticValuation.valid=TBoolean.UNSET;

			MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ctx.readSet);
			logger.debug("setState FBA");
			State state = new State("F-BA","bot");
			//Result r = callAction(controllers.readsets.api.routes.ref.ReadSets.state(readSet.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
			//assertThat(status(r)).isEqualTo(OK);
			ws.asBot().put("/api/readsets/"+ctx.readSet.code+"/state", RunMockHelper.getJsonState(state).toString(), 200);

			ctx.readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, ctx.readSet.code);
			logger.debug("state code"+ctx.readSet.state.code);
			assertThat(ctx.readSet.state.code).isEqualTo("IW-VBA");
		});
	}

//	@Test
//	public void setStateFBAA()
//	{
//		testInServer(devapp(),
//				ws -> {	
//					readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
//					readSet.productionValuation.valid=TBoolean.TRUE;
//					readSet.bioinformaticValuation.valid=TBoolean.TRUE;
//
//					MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet);
//					Logger.debug("setState FBA");
//					State state = new State("F-BA","bot");
//					//Result r = callAction(controllers.readsets.api.routes.ref.ReadSets.state(readSet.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
//					//assertThat(status(r)).isEqualTo(OK);
//					WSHelper.putAsBot(ws, "/api/readsets/"+readSet.code+"/state", RunMockHelper.getJsonState(state).toString(), 200);
//
//					readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
//					Logger.debug("state code"+readSet.state.code);
//					assertThat(readSet.state.code).isEqualTo("A");
//				});
//	}

	@Test
	public void setStateFBAA() throws Exception {
		af.accept((app,ws,ctx) -> {
			ctx.readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, ctx.readSet.code);
			ctx.readSet.productionValuation.valid=TBoolean.TRUE;
			ctx.readSet.bioinformaticValuation.valid=TBoolean.TRUE;

			MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ctx.readSet);
			logger.debug("setState FBA");
			State state = new State("F-BA","bot");
			//Result r = callAction(controllers.readsets.api.routes.ref.ReadSets.state(readSet.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
			//assertThat(status(r)).isEqualTo(OK);
			ws.asBot().put("/api/readsets/"+ctx.readSet.code+"/state", RunMockHelper.getJsonState(state).toString(), 200);

			ctx.readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, ctx.readSet.code);
			logger.debug("state code"+ctx.readSet.state.code);
			assertThat(ctx.readSet.state.code).isEqualTo("A");
		});
	}

//	@Test
//	public void setStateFBAUA()
//	{
//		testInServer(devapp(),
//				ws -> {	
//					readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
//					readSet.productionValuation.valid=TBoolean.TRUE;
//					readSet.bioinformaticValuation.valid=TBoolean.FALSE;
//
//					MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet);
//					Logger.debug("setState FBA");
//					State state = new State("F-BA","bot");
//					//Result r = callAction(controllers.readsets.api.routes.ref.ReadSets.state(readSet.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
//					//assertThat(status(r)).isEqualTo(OK);
//					WSHelper.putAsBot(ws, "/api/readsets/"+readSet.code+"/state", RunMockHelper.getJsonState(state).toString(), 200);
//
//					readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
//					Logger.debug("state code"+readSet.state.code);
//					assertThat(readSet.state.code).isEqualTo("UA");
//				});
//	}

	@Test
	public void setStateFBAUA()	throws Exception {
		af.accept((app,ws,ctx) -> {
			ctx.readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, ctx.readSet.code);
			ctx.readSet.productionValuation.valid=TBoolean.TRUE;
			ctx.readSet.bioinformaticValuation.valid=TBoolean.FALSE;

			MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ctx.readSet);
			logger.debug("setState FBA");
			State state = new State("F-BA","bot");
			//Result r = callAction(controllers.readsets.api.routes.ref.ReadSets.state(readSet.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
			//assertThat(status(r)).isEqualTo(OK);
			ws.asBot().put("/api/readsets/"+ctx.readSet.code+"/state", RunMockHelper.getJsonState(state).toString(), 200);

			ctx.readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, ctx.readSet.code);
			logger.debug("state code"+ctx.readSet.state.code);
			assertThat(ctx.readSet.state.code).isEqualTo("UA");
		});
	}

	private static BasicDBObject getReadSetKeys() {
		BasicDBObject keys = new BasicDBObject();
		keys.put("treatments", 0);
		return keys;
	}
	
}


