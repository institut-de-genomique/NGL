package workflows;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;
import java.util.TreeSet;

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
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.project.instance.Project;
import models.laboratory.run.instance.Lane;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.utils.InstanceConstants;
import ngl.bi.Global;
import play.Application;
import utils.RunMockHelper;

//public class RunWorkflowTest extends AbstractAbstractTests {
public class RunWorkflowTest {

	private static final play.Logger.ALogger logger = play.Logger.of(RunWorkflowTest.class);
	
//	static Run run;
//	static Run runInvalidLane;
//	static ContainerSupport cs;
//	static List<Container> containers;
//	
//	@BeforeClass
//	public static void initData()
//	{
//		//get run to test
//		List<Run> runs =  MongoDBDAO.find("ngl_bi.RunIllumina_dataWF", Run.class, DBQuery.is("state.code", "IP-RG").exists("lanes").exists("lanes.readSetCodes").exists("lanes.treatments")).toList();
//		run = runs.get(0);
//		//Remove libProcessTypeCode for rule IP-S
//		//Remove projectCodes/sampleCodes for rule IP-S
//		run.properties.remove("libProcessTypeCodes");
//		run.sampleCodes= new TreeSet<>();
//		run.projectCodes = new TreeSet<>();
//
//		//Get one readSet with treatment ngsrg
//		ReadSet readSetNgsrg = MongoDBDAO.find("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class, DBQuery.exists("treatments.ngsrg"), getReadSetKeys()).limit(1).toList().get(0);
//		readSetNgsrg = MongoDBDAO.findByCode("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class, readSetNgsrg.code);
//		for(Lane lane :  run.lanes){
//			if(lane.valuation.valid.equals(TBoolean.FALSE)){
//				lane.valuation.valid=TBoolean.TRUE;
//				lane.treatments.put("ngsrg", readSetNgsrg.treatments.get("ngsrg"));
//			}
//			//Save ReadSets
//			/*List<ReadSet> readSets = MongoDBDAO.find("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class, 
//					DBQuery.and(DBQuery.is("runCode", run.code), DBQuery.is("laneNumber", lane.number)),getReadSetKeys()).toList();*/
//			List<ReadSet> readSets = MongoDBDAO.find("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class, 
//					DBQuery.in("code", lane.readSetCodes),getReadSetKeys()).toList();
//			for(ReadSet readSet : readSets){
//				readSet.treatments.put("ngsrg", readSetNgsrg.treatments.get("ngsrg"));
//				MongoDBDAO.save(InstanceConstants.READSET_ILLUMINA_COLL_NAME,readSet);
//			}
//
//		}
//		run.state.code="N";
//		MongoDBDAO.save(InstanceConstants.RUN_ILLUMINA_COLL_NAME, run);
//
//		for(String codeProjet : run.projectCodes){
//			Project project = MongoDBDAO.findByCode("ngl_project.Project_dataWF", Project.class, codeProjet);
//			MongoDBDAO.save(InstanceConstants.PROJECT_COLL_NAME,project);
//		}
//		
//		runInvalidLane = runs.get(1);
//		for(Lane lane : runInvalidLane.lanes){
//			lane.valuation.valid=TBoolean.FALSE;
//			//Save ReadSets
//			
//			List<ReadSet> readSets = MongoDBDAO.find("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class, 
//					DBQuery.in("code", lane.readSetCodes),getReadSetKeys()).toList();
//			for(ReadSet readSet : readSets){
//				readSet.treatments.put("ngsrg", readSetNgsrg.treatments.get("ngsrg"));
//				lane.treatments.put("ngsrg", readSetNgsrg.treatments.get("ngsrg"));
//				MongoDBDAO.save(InstanceConstants.READSET_ILLUMINA_COLL_NAME,readSet);
//			}
//		}
//		MongoDBDAO.save(InstanceConstants.RUN_ILLUMINA_COLL_NAME, runInvalidLane);
//
//		for(String codeProjet : runInvalidLane.projectCodes){
//			Project project = MongoDBDAO.findByCode("ngl_project.Project_dataWF", Project.class, codeProjet);
//			MongoDBDAO.save(InstanceConstants.PROJECT_COLL_NAME,project);
//		}
//		
//		//Get containerSupport
//		cs = MongoDBDAO.findByCode("ngl_sq.ContainerSupport_dataWF", ContainerSupport.class, run.containerSupportCode);
//		MongoDBDAO.save(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, cs);
//		//Get container
//		containers = MongoDBDAO.find("ngl_sq.Container_dataWF", Container.class, DBQuery.is("support.code", run.containerSupportCode)).toList();
//		for(Container c : containers){
//			MongoDBDAO.save(InstanceConstants.CONTAINER_COLL_NAME, c);
//		}
//
//	}
//
//	@AfterClass
//	public static void deleteData()
//	{
//		//get run to test
//		run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, run.code);
//		MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, run);
//		runInvalidLane = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runInvalidLane.code);
//		MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, runInvalidLane);
//		cs = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, cs.code);
//		MongoDBDAO.delete(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, cs);
//		for(Container c : containers){
//			//List<Container> containerToDelete = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.is("code", c.code)).toList();
//			MongoDBDAO.deleteByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, c.code);
//		}
//		for(String codeProjet : runInvalidLane.projectCodes){
//			MongoDBDAO.deleteByCode(InstanceConstants.PROJECT_COLL_NAME,Project.class, codeProjet);
//		}
//		for(String codeProjet : run.projectCodes){
//			MongoDBDAO.deleteByCode(InstanceConstants.PROJECT_COLL_NAME,Project.class, codeProjet);
//		}
//		
//		for(Lane lane : runInvalidLane.lanes){
//			List<ReadSet> readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
//					DBQuery.in("code", lane.readSetCodes),getReadSetKeys()).toList();
//			for(ReadSet readSet : readSets){
//				MongoDBDAO.deleteByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class, readSet.code);
//			}
//		}
//		for(Lane lane : run.lanes){
//			List<ReadSet> readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
//					DBQuery.in("code", lane.readSetCodes),getReadSetKeys()).toList();
//			for(ReadSet readSet : readSets){
//				MongoDBDAO.deleteByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class, readSet.code);
//			}
//		}
//		
//	}

	private static class TestContext implements CC1Managed {
		Run run;
		Run runInvalidLane;
		ContainerSupport cs;
		List<Container> containers;

		@Override
		public void setUp()	{
			//get run to test
			List<Run> runs =  MongoDBDAO.find("ngl_bi.RunIllumina_dataWF", Run.class, DBQuery.is("state.code", "IP-RG").exists("lanes").exists("lanes.readSetCodes").exists("lanes.treatments")).toList();
			run = runs.get(0);
			//Remove libProcessTypeCode for rule IP-S
			//Remove projectCodes/sampleCodes for rule IP-S
			run.properties.remove("libProcessTypeCodes");
			run.sampleCodes= new TreeSet<>();
			run.projectCodes = new TreeSet<>();

			//Get one readSet with treatment ngsrg
			ReadSet readSetNgsrg = MongoDBDAO.find("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class, DBQuery.exists("treatments.ngsrg"), getReadSetKeys()).limit(1).toList().get(0);
			readSetNgsrg = MongoDBDAO.findByCode("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class, readSetNgsrg.code);
			for(Lane lane :  run.lanes){
				if(lane.valuation.valid.equals(TBoolean.FALSE)){
					lane.valuation.valid=TBoolean.TRUE;
					lane.treatments.put("ngsrg", readSetNgsrg.treatments.get("ngsrg"));
				}
				//Save ReadSets
				/*List<ReadSet> readSets = MongoDBDAO.find("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class, 
					DBQuery.and(DBQuery.is("runCode", run.code), DBQuery.is("laneNumber", lane.number)),getReadSetKeys()).toList();*/
				List<ReadSet> readSets = MongoDBDAO.find("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class, 
						DBQuery.in("code", lane.readSetCodes),getReadSetKeys()).toList();
				for(ReadSet readSet : readSets){
					readSet.treatments.put("ngsrg", readSetNgsrg.treatments.get("ngsrg"));
					MongoDBDAO.save(InstanceConstants.READSET_ILLUMINA_COLL_NAME,readSet);
				}

			}
			run.state.code="N";
			MongoDBDAO.save(InstanceConstants.RUN_ILLUMINA_COLL_NAME, run);

			for(String codeProjet : run.projectCodes){
				Project project = MongoDBDAO.findByCode("ngl_project.Project_dataWF", Project.class, codeProjet);
				MongoDBDAO.save(InstanceConstants.PROJECT_COLL_NAME,project);
			}

			runInvalidLane = runs.get(1);
			for(Lane lane : runInvalidLane.lanes){
				lane.valuation.valid=TBoolean.FALSE;
				//Save ReadSets

				List<ReadSet> readSets = MongoDBDAO.find("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class, 
						DBQuery.in("code", lane.readSetCodes),getReadSetKeys()).toList();
				for(ReadSet readSet : readSets){
					readSet.treatments.put("ngsrg", readSetNgsrg.treatments.get("ngsrg"));
					lane.treatments.put("ngsrg", readSetNgsrg.treatments.get("ngsrg"));
					MongoDBDAO.save(InstanceConstants.READSET_ILLUMINA_COLL_NAME,readSet);
				}
			}
			MongoDBDAO.save(InstanceConstants.RUN_ILLUMINA_COLL_NAME, runInvalidLane);

			for(String codeProjet : runInvalidLane.projectCodes){
				Project project = MongoDBDAO.findByCode("ngl_project.Project_dataWF", Project.class, codeProjet);
				MongoDBDAO.save(InstanceConstants.PROJECT_COLL_NAME,project);
			}

			//Get containerSupport
			cs = MongoDBDAO.findByCode("ngl_sq.ContainerSupport_dataWF", ContainerSupport.class, run.containerSupportCode);
			MongoDBDAO.save(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, cs);
			//Get container
			containers = MongoDBDAO.find("ngl_sq.Container_dataWF", Container.class, DBQuery.is("support.code", run.containerSupportCode)).toList();
			for(Container c : containers){
				MongoDBDAO.save(InstanceConstants.CONTAINER_COLL_NAME, c);
			}

		}

		@Override
		public void tearDown() {
			//get run to test
			run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, run.code);
			MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, run);
			runInvalidLane = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runInvalidLane.code);
			MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, runInvalidLane);
			cs = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, cs.code);
			MongoDBDAO.delete(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, cs);
			for(Container c : containers){
				//List<Container> containerToDelete = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.is("code", c.code)).toList();
				MongoDBDAO.deleteByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, c.code);
			}
			for(String codeProjet : runInvalidLane.projectCodes){
				MongoDBDAO.deleteByCode(InstanceConstants.PROJECT_COLL_NAME,Project.class, codeProjet);
			}
			for(String codeProjet : run.projectCodes){
				MongoDBDAO.deleteByCode(InstanceConstants.PROJECT_COLL_NAME,Project.class, codeProjet);
			}

			for(Lane lane : runInvalidLane.lanes){
				List<ReadSet> readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
						DBQuery.in("code", lane.readSetCodes),getReadSetKeys()).toList();
				for(ReadSet readSet : readSets){
					MongoDBDAO.deleteByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class, readSet.code);
				}
			}
			for(Lane lane : run.lanes){
				List<ReadSet> readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
						DBQuery.in("code", lane.readSetCodes),getReadSetKeys()).toList();
				for(ReadSet readSet : readSets){
					MongoDBDAO.deleteByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class, readSet.code);
				}
			}

		}

	}

	private static final CC3<Application,NGLWSClient,TestContext> af = 
			Global.af.cc2()
			.and(CCActions.managed(TestContext::new));
			
//	@Test
//	public void setStateIPS() throws InterruptedException
//	{
//		testInServer(devapp(),
//				ws -> {	
//					logger.debug("setStateIPS");
//					State state = new State("IP-S","bot");
//					//Result r = callAction(controllers.runs.api.routes.ref.State.update(run.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
//					//assertThat(status(r)).isEqualTo(OK);
//					WSHelper.putAsBot(ws, "/api/runs/"+run.code+"/state", RunMockHelper.getJsonState(state).toString(), 200);
//					run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, run.code);
//					logger.debug("state code"+run.state.code);
//					assertThat(run.state.code).isEqualTo("IP-S");
//				});
//	}
	
	@Test
	public void setStateIPS() throws Exception {
		af.accept((app,ws,ctx) -> {
			logger.debug("setStateIPS");
			State state = new State("IP-S","bot");
			//Result r = callAction(controllers.runs.api.routes.ref.State.update(run.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
			//assertThat(status(r)).isEqualTo(OK);
			ws.asBot().put("/api/runs/"+ctx.run.code+"/state", RunMockHelper.getJsonState(state).toString(), 200);
			ctx.run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, ctx.run.code);
			logger.debug("state code"+ctx.run.state.code);
			assertThat(ctx.run.state.code).isEqualTo("IP-S");
		});
	}

//	@Test
//	public void setStateFS()
//	{
//		testInServer(devapp(),
//				ws -> {	
//					State state = new State("F-S","bot");
//					//Result r = callAction(controllers.runs.api.routes.ref.State.update(run.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
//					//assertThat(status(r)).isEqualTo(OK);
//					WSHelper.putAsBot(ws, "/api/runs/"+run.code+"/state", RunMockHelper.getJsonState(state).toString(), 200);
//					run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, run.code);
//					logger.debug("state code"+run.state.code);
//					assertThat(run.state.code).isEqualTo("IW-RG");
//				});
//	}

	@Test
	public void setStateFS() throws Exception {
		af.accept((app,ws,ctx) -> {
			State state = new State("F-S","bot");
			//Result r = callAction(controllers.runs.api.routes.ref.State.update(run.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
			//assertThat(status(r)).isEqualTo(OK);
			ws.asBot().put("/api/runs/"+ctx.run.code+"/state", RunMockHelper.getJsonState(state).toString(), 200);
			ctx.run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, ctx.run.code);
			logger.debug("state code"+ctx.run.state.code);
			assertThat(ctx.run.state.code).isEqualTo("IW-RG");
		});
	}

//	@Test
//	public void setStateFRG()
//	{
//		testInServer(devapp(),
//				ws -> {	
//					State state = new State("F-RG","bot");
//					//Result r = callAction(controllers.runs.api.routes.ref.State.update(run.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
//					//assertThat(status(r)).isEqualTo(OK);
//					WSHelper.putAsBot(ws, "/api/runs/"+run.code+"/state", RunMockHelper.getJsonState(state).toString(), 200);
//					run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, run.code);
//					logger.debug("state code"+run.state.code);
//					assertThat(run.state.code).isEqualTo("IW-V");
//					//Check dispatch
//					assertThat(run.dispatch).isEqualTo(true);
//					for(Lane lane : run.lanes){
//						List<ReadSet> readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
//								DBQuery.and(DBQuery.is("runCode", run.code), DBQuery.is("laneNumber", lane.number)),getReadSetKeys()).toList();
//						for(ReadSet readSet : readSets){
//							assertThat(readSet.state.code).isEqualTo("IW-QC");
//						}
//					}
//				});
//
//
//	}

	@Test
	public void setStateFRG() throws Exception {
		af.accept((app,ws,ctx) -> {
			State state = new State("F-RG","bot");
			//Result r = callAction(controllers.runs.api.routes.ref.State.update(run.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
			//assertThat(status(r)).isEqualTo(OK);
			ws.asBot().put("/api/runs/"+ctx.run.code+"/state", RunMockHelper.getJsonState(state).toString(), 200);
			ctx.run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, ctx.run.code);
			logger.debug("state code"+ctx.run.state.code);
			assertThat(ctx.run.state.code).isEqualTo("IW-V");
			//Check dispatch
			assertThat(ctx.run.dispatch).isEqualTo(true);
			for(Lane lane : ctx.run.lanes){
				List<ReadSet> readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
						DBQuery.and(DBQuery.is("runCode", ctx.run.code), DBQuery.is("laneNumber", lane.number)),getReadSetKeys()).toList();
				for(ReadSet readSet : readSets){
					assertThat(readSet.state.code).isEqualTo("IW-QC");
				}
			}
		});
	}

//	@Test
//	public void setStateFRGLaneValid()
//	{
//		testInServer(devapp(),
//				ws -> {	
//					State state = new State("F-RG","bot");
//					//Result r = callAction(controllers.runs.api.routes.ref.State.update(runInvalidLane.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
//					//assertThat(status(r)).isEqualTo(OK);
//					WSHelper.putAsBot(ws, "/api/runs/"+runInvalidLane.code+"/state", RunMockHelper.getJsonState(state).toString(), 200);
//					runInvalidLane = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runInvalidLane.code);
//					logger.debug("state code"+runInvalidLane.state.code);
//					for(Lane lane : runInvalidLane.lanes){
//					/*	List<ReadSet> readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
//								DBQuery.and(DBQuery.is("runCode", runInvalidLane.code), DBQuery.is("laneNumber", lane.number)),getReadSetKeys()).toList();*/
//						List<ReadSet> readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
//								DBQuery.in("code", lane.readSetCodes),getReadSetKeys()).toList();
//						for(ReadSet readSet : readSets){
//							logger.debug("ReadSet "+readSet.code);
//							logger.debug(readSet.bioinformaticValuation.valid.toString());
//							assertThat(readSet.bioinformaticValuation.valid.toString()).isEqualTo(TBoolean.FALSE.toString());
//							assertThat(readSet.productionValuation.resolutionCodes).contains("Run-abandonLane");
//							assertThat(readSet.bioinformaticValuation.valid.toString()).isEqualTo(TBoolean.FALSE.toString());
//							assertThat(readSet.state.code).isEqualTo("UA");
//						}
//					}
//				});
//	}

	@Test
	public void setStateFRGLaneValid() throws Exception {
		af.accept((app,ws,ctx) -> {
			State state = new State("F-RG","bot");
			//Result r = callAction(controllers.runs.api.routes.ref.State.update(runInvalidLane.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
			//assertThat(status(r)).isEqualTo(OK);
			ws.asBot().put("/api/runs/"+ctx.runInvalidLane.code+"/state", RunMockHelper.getJsonState(state).toString(), 200);
			ctx.runInvalidLane = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, ctx.runInvalidLane.code);
			logger.debug("state code"+ctx.runInvalidLane.state.code);
			for(Lane lane : ctx.runInvalidLane.lanes){
				/*	List<ReadSet> readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
								DBQuery.and(DBQuery.is("runCode", runInvalidLane.code), DBQuery.is("laneNumber", lane.number)),getReadSetKeys()).toList();*/
				List<ReadSet> readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
						DBQuery.in("code", lane.readSetCodes),getReadSetKeys()).toList();
				for(ReadSet readSet : readSets){
					logger.debug("ReadSet "+readSet.code);
					logger.debug(readSet.bioinformaticValuation.valid.toString());
					assertThat(readSet.bioinformaticValuation.valid.toString()).isEqualTo(TBoolean.FALSE.toString());
					assertThat(readSet.productionValuation.resolutionCodes).contains("Run-abandonLane");
					assertThat(readSet.bioinformaticValuation.valid.toString()).isEqualTo(TBoolean.FALSE.toString());
					assertThat(readSet.state.code).isEqualTo("UA");
				}
			}
		});
	}

//	@Test
//	public void setStateFVLaneInvalid()
//	{
//		testInServer(devapp(),
//				ws -> {	
//					State state = new State("F-V","bot");
//					//Result r = callAction(controllers.runs.api.routes.ref.State.update(runInvalidLane.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
//					//assertThat(status(r)).isEqualTo(OK);
//					WSHelper.putAsBot(ws, "/api/runs/"+runInvalidLane.code+"/state", RunMockHelper.getJsonState(state).toString(), 200);
//					runInvalidLane = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runInvalidLane.code);
//					logger.debug("state code"+runInvalidLane.state.code);
//					for(Lane lane : runInvalidLane.lanes){
//						List<ReadSet> readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
//								DBQuery.and(DBQuery.is("runCode", runInvalidLane.code), DBQuery.is("laneNumber", lane.number)),getReadSetKeys()).toList();
//						for(ReadSet readSet : readSets){
//							assertThat(readSet.productionValuation.valid.toString()).isEqualTo(TBoolean.FALSE.toString());
//							assertThat(readSet.productionValuation.resolutionCodes).contains("Run-abandonLane");
//							assertThat(readSet.state.code).isEqualTo("UA");
//						}
//					}
//				});
//	}

	@Test
	public void setStateFVLaneInvalid() throws Exception {
		af.accept((app,ws,ctx) -> {
			State state = new State("F-V","bot");
			//Result r = callAction(controllers.runs.api.routes.ref.State.update(runInvalidLane.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
			//assertThat(status(r)).isEqualTo(OK);
			ws.asBot().put("/api/runs/"+ctx.runInvalidLane.code+"/state", RunMockHelper.getJsonState(state).toString(), 200);
			ctx.runInvalidLane = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, ctx.runInvalidLane.code);
			logger.debug("state code"+ctx.runInvalidLane.state.code);
			for(Lane lane : ctx.runInvalidLane.lanes){
				List<ReadSet> readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
						DBQuery.and(DBQuery.is("runCode", ctx.runInvalidLane.code), DBQuery.is("laneNumber", lane.number)),getReadSetKeys()).toList();
				for(ReadSet readSet : readSets){
					assertThat(readSet.productionValuation.valid.toString()).isEqualTo(TBoolean.FALSE.toString());
					assertThat(readSet.productionValuation.resolutionCodes).contains("Run-abandonLane");
					assertThat(readSet.state.code).isEqualTo("UA");
				}
			}
		});
	}

	private static BasicDBObject getReadSetKeys() {
		BasicDBObject keys = new BasicDBObject();
		keys.put("treatments", 0);
		return keys;
	}
	
}


