package data.resources;

import static org.fest.assertions.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.play.test.NGLWSClient;
import fr.cea.ig.util.function.C3;
import fr.cea.ig.util.function.CC1Managed;
import fr.cea.ig.util.function.CC3;
import fr.cea.ig.util.function.CCActions;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.common.instance.Valuation;
import models.laboratory.run.instance.Lane;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.utils.InstanceConstants;
import ngl.bi.Global;
import play.Application;
import play.libs.Json;
import play.libs.ws.WSResponse;

//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
//public class LaneTest extends AbstractBIServerTest {
public class LaneTest {

	private static final play.Logger.ALogger logger = play.Logger.of(LaneTest.class);
	
//	static Run run;
//	static List<ReadSet> readSets;
//	static String laneJson;
//
//	@BeforeClass
//	public static void initData()
//	{
//		//get JSON Run to insert
//		List<Run> runs  = MongoDBDAO.find("ngl_bi.RunIllumina_dataWF", Run.class, DBQuery.size("lanes", 8)).toList();
//		for (Run runDB : runs) {
//			readSets = MongoDBDAO.find("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class, DBQuery.is("runCode", runDB.code)).toList();
//			if (readSets.size() > 0) {
//				run = runDB;
//				break;
//			}
//		}
//		//Get lane 8
//		List<Lane> newLanes = new ArrayList<>();
//		for (Lane lane : run.lanes) {
//			if (lane.number == 8)
//				laneJson = Json.toJson(lane).toString();
//			else
//				newLanes.add(lane);
//		}
//		run.lanes = newLanes;
//
//		//Insert run
//		MongoDBDAO.save(InstanceConstants.RUN_ILLUMINA_COLL_NAME, run);
//
//		//insert readSets
//		//readSets = MongoDBDAO.find("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class, DBQuery.is("runCode", run.code)).toList();
//		for(ReadSet readSet : readSets){
//			MongoDBDAO.save(InstanceConstants.READSET_ILLUMINA_COLL_NAME,readSet);
//		}
//	}
//
//	@AfterClass
//	public static void deleteData()
//	{
//		if (MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, run.code) != null) {
//			MongoDBDAO.deleteByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, run.code);
//			for(ReadSet readSet: readSets){
//				MongoDBDAO.deleteByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
//			}
//		}
//	}

	private static class TestContext implements CC1Managed {
		Run run;
		List<ReadSet> readSets;
		String laneJson;

		@Override
		public void setUp() {
			//get JSON Run to insert
			List<Run> runs  = MongoDBDAO.find("ngl_bi.RunIllumina_dataWF", Run.class, DBQuery.size("lanes", 8)).toList();
			for (Run runDB : runs) {
				readSets = MongoDBDAO.find("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class, DBQuery.is("runCode", runDB.code)).toList();
				if (readSets.size() > 0) {
					run = runDB;
					break;
				}
			}
			//Get lane 8
			List<Lane> newLanes = new ArrayList<>();
			for (Lane lane : run.lanes) {
				if (lane.number == 8)
					laneJson = Json.toJson(lane).toString();
				else
					newLanes.add(lane);
			}
			run.lanes = newLanes;

			//Insert run
			MongoDBDAO.save(InstanceConstants.RUN_ILLUMINA_COLL_NAME, run);

			//insert readSets
			//readSets = MongoDBDAO.find("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class, DBQuery.is("runCode", run.code)).toList();
			for(ReadSet readSet : readSets){
				MongoDBDAO.save(InstanceConstants.READSET_ILLUMINA_COLL_NAME,readSet);
			}
		}

		@Override
		public void tearDown() {
			if (MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, run.code) != null) {
				MongoDBDAO.deleteByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, run.code);
				for(ReadSet readSet: readSets){
					MongoDBDAO.deleteByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
				}
			}
		}
	}
	
	private static final CC3<Application,NGLWSClient,TestContext> af = 
			Global.af.cc2()
			.and(CCActions.managed(TestContext::new));

	private static final C3<Application,NGLWSClient,TestContext>
		test1Impl = (app,ws,ctx) -> {
			logger.debug("list Lane");
			WSResponse response = ws.asBot().get("/api/runs/"+ctx.run.code+"/lanes", 200);
			assertThat(response.asJson()).isNotNull();
		},
		test2Impl = test1Impl.andThen((app,ws,ctx) -> {
			logger.debug("get Lane");
			WSResponse response = ws.asBot().get("/api/runs/"+ctx.run.code+"/lanes/1", 200);
			assertThat(response.asJson()).isNotNull();
		}),
		test3Impl = test2Impl.andThen((app,ws,ctx) -> {
			logger.debug("head Lane");
			WSResponse response = ws.asBot().head("/api/runs/"+ctx.run.code+"/lanes/1", 200);
			assertThat(response).isNotNull();			
		}),
		test4Impl = test3Impl.andThen((app,ws,ctx) -> {
			logger.debug("delete Lane number");
			ws.asBot().delete("/api/runs/"+ctx.run.code+"/lanes/7",200);
			ctx.run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, ctx.run.code);
			assertThat(ctx.run.lanes.size()).isEqualTo(6);
		}),
		test5Impl = test4Impl.andThen((app,ws,ctx) -> {
			logger.debug("save Lane with json "+ctx.laneJson);
			ws.asBot().post("/api/runs/"+ctx.run.code+"/lanes", ctx.laneJson, 200);
			ctx.run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, ctx.run.code);
			assertThat(ctx.run.lanes.size()).isEqualTo(7);			
		}),
		test6Impl = test5Impl.andThen((app,ws,ctx) -> {
			logger.debug("update Lane");
			//Get Run
			ctx.run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, ctx.run.code);
			Date date = new Date();
			Lane lane = ctx.run.lanes.get(0);
			lane.valuation.date = date;
			ws.asBot().putObject("/api/runs/"+ctx.run.code+"/lanes/"+ctx.run.lanes.get(0).number, lane, 200);
			ctx.run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, ctx.run.code);
			assertThat(ctx.run.lanes.get(0).valuation.date).isEqualTo(date);
		}),
		test7Impl = test6Impl.andThen((app,ws,ctx) -> {
			logger.debug("valuation Lane");
			Valuation valuation = new Valuation();
			valuation.comment="test valuation";
			valuation.date=new Date();
			valuation.valid=TBoolean.FALSE;
			ws.asBot().putObject("/api/runs/"+ctx.run.code+"/lanes/"+ctx.run.lanes.get(0).number+"/valuation", valuation, 200);
			ctx.run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, ctx.run.code);
			assertThat(ctx.run.lanes.get(0).valuation.comment).isEqualTo("test valuation");
			assertThat(ctx.run.lanes.get(0).valuation.valid).isEqualTo(TBoolean.FALSE);
		}),		
		test8Impl = test7Impl.andThen((app,ws,ctx) -> {
			logger.debug("delete Lane number");
			ws.asBot().delete("/api/runs/"+ctx.run.code+"/lanes",200);
			ctx.run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, ctx.run.code);
			assertThat(ctx.run.lanes).isNull();
		});

	@Test public void test1list()         throws Exception { af.accept(test1Impl); }
	@Test public void test2get()          throws Exception { af.accept(test2Impl); }
	@Test public void test3head()         throws Exception { af.accept(test3Impl); }
	@Test public void test4deleteNumber() throws Exception { af.accept(test4Impl); }
	@Test public void test5save()         throws Exception { af.accept(test5Impl); }
	@Test public void test6update()       throws Exception { af.accept(test6Impl); }
	@Test public void test7valuation()    throws Exception { af.accept(test7Impl); }
	@Test public void test8delete()       throws Exception { af.accept(test8Impl); }

//	@Test
//	public void test1list()
//	{
//		logger.debug("list Lane");
////		WSResponse response = WSHelper.getAsBot(ws, "/api/runs/"+run.code+"/lanes", 200);
//		WSResponse response = wsBot.get("/api/runs/"+run.code+"/lanes", 200);
//		assertThat(response.asJson()).isNotNull();
//	}

//	@Test
//	public void test2get()
//	{
//		logger.debug("get Lane");
////		WSResponse response = WSHelper.getAsBot(ws, "/api/runs/"+run.code+"/lanes/1", 200);
//		WSResponse response = wsBot.get("/api/runs/"+run.code+"/lanes/1", 200);
//		assertThat(response.asJson()).isNotNull();
//	}

//	@Test
//	public void test3head()
//	{
//		logger.debug("head Lane");
////		WSResponse response = WSHelper.headAsBot(ws, "/api/runs/"+run.code+"/lanes/1", 200);
//		WSResponse response = wsBot.head("/api/runs/"+run.code+"/lanes/1", 200);
//		assertThat(response).isNotNull();
//	}

//	@Test
//	public void test4deleteNumber()
//	{
//		logger.debug("delete Lane number");
////		WSHelper.deleteAsBot(ws,"/api/runs/"+run.code+"/lanes/7",200);
//		wsBot.delete("/api/runs/"+run.code+"/lanes/7",200);
//		run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, run.code);
//		assertThat(run.lanes.size()).isEqualTo(6);
//	}

//	@Test
//	public void test5save()
//	{
//		logger.debug("save Lane with json "+laneJson);
////		WSHelper.postAsBot(ws, "/api/runs/"+run.code+"/lanes", laneJson, 200);
//		wsBot.post("/api/runs/"+run.code+"/lanes", laneJson, 200);
//		run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, run.code);
//		assertThat(run.lanes.size()).isEqualTo(7);
//	}

//	@Test
//	public void test6update()
//	{
//		logger.debug("update Lane");
//		//Get Run
//		run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, run.code);
//		Date date = new Date();
//		Lane lane = run.lanes.get(0);
//		lane.valuation.date=date;
////		WSHelper.putObjectAsBot(ws, "/api/runs/"+run.code+"/lanes/"+run.lanes.get(0).number, lane, 200);
//		wsBot.putObject("/api/runs/"+run.code+"/lanes/"+run.lanes.get(0).number, lane, 200);
//		run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, run.code);
//		assertThat(run.lanes.get(0).valuation.date).isEqualTo(date);
//	}

//	@Test
//	public void test7valuation()
//	{
//		logger.debug("valuation Lane");
//		Valuation valuation = new Valuation();
//		valuation.comment="test valuation";
//		valuation.date=new Date();
//		valuation.valid=TBoolean.FALSE;
////		WSHelper.putObjectAsBot(ws, "/api/runs/"+run.code+"/lanes/"+run.lanes.get(0).number+"/valuation", valuation, 200);
//		wsBot.putObject("/api/runs/"+run.code+"/lanes/"+run.lanes.get(0).number+"/valuation", valuation, 200);
//		run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, run.code);
//		assertThat(run.lanes.get(0).valuation.comment).isEqualTo("test valuation");
//		assertThat(run.lanes.get(0).valuation.valid).isEqualTo(TBoolean.FALSE);
//	}

//	@Test
//	public void test8delete()
//	{
//		logger.debug("delete Lane number");
////		WSHelper.deleteAsBot(ws,"/api/runs/"+run.code+"/lanes",200);
//		wsBot.delete("/api/runs/"+run.code+"/lanes",200);
//		run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, run.code);
//		assertThat(run.lanes).isNull();
//	}
	
}
