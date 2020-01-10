package data.resources;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.play.test.NGLWSClient;
import fr.cea.ig.util.function.C3;
import fr.cea.ig.util.function.CC1Managed;
import fr.cea.ig.util.function.CC3;
import fr.cea.ig.util.function.CCActions;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.laboratory.run.instance.Treatment;
import models.utils.InstanceConstants;
import ngl.bi.Global;
import play.Application;
import play.libs.Json;
import play.libs.ws.WSResponse;

//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
//public class LaneTreatmentTest extends AbstractBIServerTest{
public class LaneTreatmentTest {

	private static final play.Logger.ALogger logger = play.Logger.of(LaneTreatmentTest.class); 
	
//	static Run run;
//	static List<ReadSet> readSets;
//	static String jsonTopIndex;
//	static int nbLane;
//
//	@BeforeClass
//	public static void initData()
//	{
//		//get JSON Run to insert
//		List<Run> runs  = MongoDBDAO.find("ngl_bi.RunIllumina_dataWF", Run.class, DBQuery.exists("lanes.treatments.ngsrg").exists("lanes.treatments.topIndex")).toList();
//		for(Run runDB : runs){
//			readSets = MongoDBDAO.find("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class, DBQuery.is("runCode", runDB.code)).toList();
//			if(readSets.size()>0){
//				run=runDB;
//				break;
//			}
//		}
//		//Insert run
//		jsonTopIndex=Json.toJson(run.lanes.get(0).treatments.get("topIndex")).toString();
//		nbLane=run.lanes.get(0).number;
//		run.lanes.get(0).treatments.remove("topIndex");
//		MongoDBDAO.save(InstanceConstants.RUN_ILLUMINA_COLL_NAME, run);
//
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
//		if(MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, run.code)!=null){
//			MongoDBDAO.deleteByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, run.code);
//			for(ReadSet readSet: readSets){
//				MongoDBDAO.deleteByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
//			}
//		}
//	}
	
	private static class TestContext implements CC1Managed {
		
		Run run;
		List<ReadSet> readSets;
		String jsonTopIndex;
		int nbLane;

		@Override
		public void setUp() {
			// get JSON Run to insert
			List<Run> runs  = MongoDBDAO.find("ngl_bi.RunIllumina_dataWF", Run.class, DBQuery.exists("lanes.treatments.ngsrg").exists("lanes.treatments.topIndex")).toList();
			for (Run runDB : runs) {
				readSets = MongoDBDAO.find("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class, DBQuery.is("runCode", runDB.code)).toList();
				if (readSets.size() > 0) {
					run = runDB;
					break;
				}
			}
			// Insert run
			logger.debug("using run {}", run.code);
			jsonTopIndex = Json.toJson(run.lanes.get(0).treatments.get("topIndex")).toString();
			nbLane       = run.lanes.get(0).number;
			run.lanes.get(0).treatments.remove("topIndex");
			MongoDBDAO.save(InstanceConstants.RUN_ILLUMINA_COLL_NAME, run);

			// insert readSets
			// readSets = MongoDBDAO.find("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class, DBQuery.is("runCode", run.code)).toList();
			for (ReadSet readSet : readSets) {
				MongoDBDAO.save(InstanceConstants.READSET_ILLUMINA_COLL_NAME,readSet);
			}
		}

		@Override
		public void tearDown() {
			if(MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, run.code)!=null){
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
			logger.debug("list LaneTreatment");
			WSResponse response = ws.asBot().get("/api/runs/"+ctx.run.code+"/lanes/1/treatments", 200);
			assertThat(response.asJson()).isNotNull();
		},
		test2Impl = test1Impl.andThen((app,ws,ctx) -> {
			logger.debug("get LaneTreatment");
			WSResponse response = ws.asBot().get("/api/runs/"+ctx.run.code+"/lanes/1/treatments/ngsrg", 200);
			assertThat(response.asJson()).isNotNull();			
		}),
		test3Impl = test2Impl.andThen((app,ws,ctx) -> {
			logger.debug("head LaneTreatment");
			WSResponse response = ws.asBot().head("/api/runs/"+ctx.run.code+"/lanes/1/treatments/ngsrg", 200);
			assertThat(response).isNotNull();
		}),
		test4Impl = test3Impl.andThen((app,ws,ctx) -> {
			logger.debug("save LaneTreatment");
			ws.asBot().post("/api/runs/"+ctx.run.code+"/lanes/"+ctx.nbLane+"/treatments", ctx.jsonTopIndex, 200);
			ctx.run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, ctx.run.code);
			logger.debug("Run "+ctx.run.code);
			assertThat(ctx.run.lanes.get(0).treatments.get("topIndex")).isNotNull();
		}),
		test5Impl = test4Impl.andThen((app,ws,ctx) -> {
			logger.debug("update LaneTreatment");
			//Get Treatment ngsrg
			Treatment ngsrg = ctx.run.lanes.get(0).treatments.get("ngsrg");
			ngsrg.results.get("default").put("nbClusterIlluminaFilter", new PropertySingleValue(new Long(0)));
			ws.asBot().putObject("/api/runs/"+ctx.run.code+"/lanes/"+ctx.nbLane+"/treatments/ngsrg", ngsrg, 200);
			ctx.run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, ctx.run.code);
			assertThat(ctx.run.lanes.get(0).treatments.get("ngsrg").results.get("default").get("nbClusterIlluminaFilter").getValue()).isEqualTo(new Long(0));
		}),
		test6Impl = test5Impl.andThen((app,ws,ctx) -> {
			logger.debug("delete LaneTreatment");
			ws.asBot().delete("/api/runs/"+ctx.run.code+"/lanes/"+ctx.nbLane+"/treatments/topIndex",200);
			ctx.run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, ctx.run.code);
			assertThat(ctx.run.lanes.get(0).treatments.get("topIndex")).isNull();
		});
		
	@Test public void test1list()   throws Exception { af.accept(test1Impl); }
	@Test public void test2get()    throws Exception { af.accept(test2Impl); }
	@Test public void test3head()   throws Exception { af.accept(test3Impl); }
	@Test public void test4save()   throws Exception { af.accept(test4Impl); }
	@Test public void test5update() throws Exception { af.accept(test5Impl); }
	@Test public void test6delete() throws Exception { af.accept(test6Impl); }

//	@Test
//	public void test1list()
//	{
//		logger.debug("list LaneTreatment");
////		WSResponse response = WSHelper.getAsBot(ws, "/api/runs/"+run.code+"/lanes/1/treatments", 200);
//		WSResponse response = wsBot.get("/api/runs/"+run.code+"/lanes/1/treatments", 200);
//		assertThat(response.asJson()).isNotNull();
//	}

//		@Test
//	public void test2get()
//	{
//		logger.debug("get LaneTreatment");
////		WSResponse response = WSHelper.getAsBot(ws, "/api/runs/"+run.code+"/lanes/1/treatments/ngsrg", 200);
//		WSResponse response = wsBot.get("/api/runs/"+run.code+"/lanes/1/treatments/ngsrg", 200);
//		assertThat(response.asJson()).isNotNull();
//	}

//	@Test
//	public void test3head()
//	{
//		logger.debug("head LaneTreatment");
////		WSResponse response = WSHelper.headAsBot(ws, "/api/runs/"+run.code+"/lanes/1/treatments/ngsrg", 200);
//		WSResponse response = wsBot.head("/api/runs/"+run.code+"/lanes/1/treatments/ngsrg", 200);
//		assertThat(response).isNotNull();
//	}
	
//	@Test
//	public void test4save()
//	{
//		logger.debug("save LaneTreatment");
////		WSHelper.postAsBot(ws, "/api/runs/"+run.code+"/lanes/"+nbLane+"/treatments", jsonTopIndex, 200);
//		wsBot.post("/api/runs/"+run.code+"/lanes/"+nbLane+"/treatments", jsonTopIndex, 200);
//		run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, run.code);
//		logger.debug("Run "+run.code);
//		assertThat(run.lanes.get(0).treatments.get("topIndex")).isNotNull();
//	}

//	@Test
//	public void test5update()
//	{
//		logger.debug("update LaneTreatment");
//		//Get Treatment ngsrg
//		Treatment ngsrg = run.lanes.get(0).treatments.get("ngsrg");
//		ngsrg.results.get("default").put("nbClusterIlluminaFilter", new PropertySingleValue(new Long(0)));
////		WSHelper.putObjectAsBot(ws, "/api/runs/"+run.code+"/lanes/"+nbLane+"/treatments/ngsrg", ngsrg, 200);
//		wsBot.putObject("/api/runs/"+run.code+"/lanes/"+nbLane+"/treatments/ngsrg", ngsrg, 200);
//		run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, run.code);
//		assertThat(run.lanes.get(0).treatments.get("ngsrg").results.get("default").get("nbClusterIlluminaFilter").getValue()).isEqualTo(new Long(0));
//	}
	
//	@Test
//	public void test6delete()
//	{
//		logger.debug("delete LaneTreatment");
////		WSHelper.deleteAsBot(ws,"/api/runs/"+run.code+"/lanes/"+nbLane+"/treatments/topIndex",200);
//		wsBot.delete("/api/runs/"+run.code+"/lanes/"+nbLane+"/treatments/topIndex",200);
//		run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, run.code);
//		assertThat(run.lanes.get(0).treatments.get("topIndex")).isNull();
//	}
	
}
