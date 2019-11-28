package data.resources;

import static org.fest.assertions.Assertions.assertThat;

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
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.project.instance.Project;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import ngl.bi.Global;
import play.Application;
import play.libs.Json;
import play.libs.ws.WSResponse;

//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
//public class RunTest extends AbstractBIServerTest {
public class RunTest {
	
	private static final play.Logger.ALogger logger = play.Logger.of(RunTest.class);
	
//	static Run run;
//	static ContainerSupport containerSupport;
//	static List<ReadSet> readSets;
//	static List<Container> containers;
//	static String jsonRun;
//
//	@BeforeClass
//	public static void initData()
//	{
//		//get JSON Run to insert
//		List<Run> runs  = MongoDBDAO.find("ngl_bi.RunIllumina_dataWF", Run.class, DBQuery.exists("properties.libProcessTypeCodes")).toList();
//		for(Run runDB : runs){
//			readSets = MongoDBDAO.find("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class, DBQuery.is("runCode", runDB.code)).toList();
//			if(readSets.size()>0){
//				run=runDB;
//				break;
//			}
//		}
//		run._id=null;
//		run.state.code="IP-S";
//		jsonRun = Json.toJson(run).toString();
//		logger.debug("Run code "+run.code);
//		//insert project in collection
//		for(String codeProjet : run.projectCodes){
//			Project project = MongoDBDAO.findByCode("ngl_project.Project_dataWF", Project.class, codeProjet);
//			MongoDBDAO.save(InstanceConstants.PROJECT_COLL_NAME, project);
//		}
//		//insert containerSupportCode
//		containerSupport = MongoDBDAO.findByCode("ngl_sq.ContainerSupport_dataWF", ContainerSupport.class, run.containerSupportCode);
//		MongoDBDAO.save(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME,containerSupport);
//		//insert sampleCode
//		for(String codeSample : run.sampleCodes){
//			Sample sample = MongoDBDAO.findByCode("ngl_bq.Sample_dataWF", Sample.class, codeSample);
//			MongoDBDAO.save(InstanceConstants.SAMPLE_COLL_NAME,sample);
//		}
//
//		//insert containers
//		containers = MongoDBDAO.find("ngl_sq.Container_dataWF", Container.class, DBQuery.is("support.code", containerSupport.code)).toList();
//		for(Container container: containers){
//			MongoDBDAO.save(InstanceConstants.CONTAINER_COLL_NAME,container);
//		}
//		//insert readSets
//		//readSets = MongoDBDAO.find("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class, DBQuery.is("runCode", run.code)).toList();
//		for(ReadSet readSet : readSets){
//			MongoDBDAO.save(InstanceConstants.READSET_ILLUMINA_COLL_NAME,readSet);
//		}
//
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
//		for(String codeProjet : run.projectCodes){
//			MongoDBDAO.deleteByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, codeProjet);
//		}
//		MongoDBDAO.deleteByCode(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, containerSupport.code);
//		for(Container container : containers){
//			MongoDBDAO.deleteByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, container.code);
//		}
//		for(String sampleCode:run.sampleCodes){
//			MongoDBDAO.deleteByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, sampleCode);
//		}
//
//	}

	private static class TestContext implements CC1Managed {
		
		Run run;
		ContainerSupport containerSupport;
		List<ReadSet> readSets;
		List<Container> containers;
		String jsonRun;

		@Override
		public void setUp() {
			//get JSON Run to insert
			List<Run> runs  = MongoDBDAO.find("ngl_bi.RunIllumina_dataWF", Run.class, DBQuery.exists("properties.libProcessTypeCodes")).toList();
			for(Run runDB : runs){
				readSets = MongoDBDAO.find("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class, DBQuery.is("runCode", runDB.code)).toList();
				if(readSets.size()>0){
					run=runDB;
					break;
				}
			}
			run._id=null;
			run.state.code="IP-S";
			jsonRun = Json.toJson(run).toString();
			logger.debug("Run code "+run.code);
			//insert project in collection
			for(String codeProjet : run.projectCodes){
				Project project = MongoDBDAO.findByCode("ngl_project.Project_dataWF", Project.class, codeProjet);
				MongoDBDAO.save(InstanceConstants.PROJECT_COLL_NAME, project);
			}
			//insert containerSupportCode
			containerSupport = MongoDBDAO.findByCode("ngl_sq.ContainerSupport_dataWF", ContainerSupport.class, run.containerSupportCode);
			MongoDBDAO.save(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME,containerSupport);
			//insert sampleCode
			for (String codeSample : run.sampleCodes) {
				Sample sample = MongoDBDAO.findByCode("ngl_bq.Sample_dataWF", Sample.class, codeSample);
				MongoDBDAO.save(InstanceConstants.SAMPLE_COLL_NAME,sample);
			}

			//insert containers
			containers = MongoDBDAO.find("ngl_sq.Container_dataWF", Container.class, DBQuery.is("support.code", containerSupport.code)).toList();
			for(Container container: containers){
				MongoDBDAO.save(InstanceConstants.CONTAINER_COLL_NAME,container);
			}
			//insert readSets
			//readSets = MongoDBDAO.find("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class, DBQuery.is("runCode", run.code)).toList();
			for(ReadSet readSet : readSets){
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
			for(String codeProjet : run.projectCodes){
				MongoDBDAO.deleteByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, codeProjet);
			}
			MongoDBDAO.deleteByCode(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, containerSupport.code);
			for(Container container : containers){
				MongoDBDAO.deleteByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, container.code);
			}
			for(String sampleCode:run.sampleCodes){
				MongoDBDAO.deleteByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, sampleCode);
			}
		}
		
	}
	
	private static final CC3<Application,NGLWSClient,TestContext> af = 
			Global.af.cc2()
			.and(CCActions.managed(TestContext::new));

	private static final C3<Application,NGLWSClient,TestContext>
		test1Impl = (app,ws,ctx) -> {
			logger.debug("save Run");
			ws.asBot().post("/api/runs", ctx.jsonRun, 200);
			ctx.run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, ctx.run.code);
			logger.debug("Run "+ctx.run.code);
			assertThat(ctx.run).isNotNull();	
		},
		test2Impl = test1Impl.andThen((app,ws,ctx) -> {
			logger.debug("save Run");
			Date date = new Date();
			ctx.run.sequencingStartDate = date;
			//run.properties.remove("libProcessTypeCodes");
			ws.asBot().putObject("/api/runs/"+ctx.run.code,ctx.run, 200);
			ctx.run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, ctx.run.code);
			assertThat(ctx.run.sequencingStartDate).isEqualTo(date);
			//assertThat(run.properties.get("libProcessTypeCodes")).isNull();
		}),
		test3Impl = test2Impl.andThen((app,ws,ctx) -> {
			logger.debug("save Run");
			WSResponse response = ws.asBot().get("/api/runs", 200);
			assertThat(response.asJson()).isNotNull();			
		}),
		test4Impl = test3Impl.andThen((app,ws,ctx) -> {
			logger.debug("get Run");
			WSResponse response = ws.asBot().get("/api/runs/"+ctx.run.code, 200);
			assertThat(response.asJson()).isNotNull();			
		}),
		test5Impl = test4Impl.andThen((app,ws,ctx) -> {
			logger.debug("head Run");
			WSResponse response = ws.asBot().head("/api/runs/"+ctx.run.code, 200);
			assertThat(response).isNotNull();			
		}),
		test6Impl = test5Impl.andThen((app,ws,ctx) -> {
			logger.debug("get State");
			WSResponse response = ws.asBot().get("/api/runs/"+ctx.run.code+"/state", 200);
			assertThat(response.asJson()).isNotNull();			
		}),
		test7Impl = test6Impl.andThen((app,ws,ctx) -> {
			logger.debug("get State historical");
			WSResponse response = ws.asBot().get("/api/runs/"+ctx.run.code+"/state/historical", 200);
			assertThat(response.asJson()).isNotNull();			
		}),
		test8Impl = test7Impl.andThen((app,ws,ctx) -> {
			logger.debug("valuation");
			//Create valuation
			Valuation valuation = new Valuation();
			valuation.comment = "test valuation";
			valuation.date = new Date();
			valuation.valid = TBoolean.FALSE;

			ws.asBot().putObject("/api/runs/"+ctx.run.code+"/valuation",valuation, 200);
			ctx.run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, ctx.run.code);
			assertThat(ctx.run.valuation.valid).isEqualTo(TBoolean.FALSE);
			assertThat(ctx.run.valuation.comment).isEqualTo("test valuation");			
		}),
		test9Impl = test8Impl.andThen((app,ws,ctx) -> {
			logger.debug("delete Run");
			ws.asBot().delete("/api/runs/"+ctx.run.code,200);
			Run runDB = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, ctx.run.code);
			assertThat(runDB).isNull();			
		});

	@Test public void test1saveRun()            throws Exception { af.accept(test1Impl); }
	@Test public void test2updateRun()          throws Exception { af.accept(test2Impl); }
	@Test public void test3listRun()            throws Exception { af.accept(test3Impl); }
	@Test public void test4getRun()             throws Exception { af.accept(test4Impl); }
	@Test public void test5headRun()            throws Exception { af.accept(test5Impl); }
	@Test public void test6GetState()           throws Exception { af.accept(test6Impl); }
	@Test public void test7GetStateHistorical() throws Exception { af.accept(test7Impl); }
	@Test public void test8Valuation()          throws Exception { af.accept(test8Impl); }
	@Test public void test9DeleteRun()          throws Exception { af.accept(test9Impl); }

//	@Test
//	public void test1saveRun() throws InterruptedException
//	{
//		logger.debug("save Run");
////		WSHelper.postAsBot(ws, "/api/runs", jsonRun, 200);
//		wsBot.post("/api/runs", jsonRun, 200);
//		run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, run.code);
//		logger.debug("Run "+run.code);
//		assertThat(run).isNotNull();	
//	}

//	@Test
//	public void test2updateRun()
//	{
//		logger.debug("save Run");
//		Date date = new Date();
//		run.sequencingStartDate=date;
//		//run.properties.remove("libProcessTypeCodes");
////		WSHelper.putObjectAsBot(ws, "/api/runs/"+run.code,run, 200);
//		wsBot.putObject("/api/runs/"+run.code,run, 200);
//		run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, run.code);
//		assertThat(run.sequencingStartDate).isEqualTo(date);
//		//assertThat(run.properties.get("libProcessTypeCodes")).isNull();
//	}

//	@Test
//	public void test3listRun()
//	{
//		logger.debug("save Run");
////		WSResponse response = WSHelper.getAsBot(ws, "/api/runs", 200);
//		WSResponse response = wsBot.get("/api/runs", 200);
//		assertThat(response.asJson()).isNotNull();
//	}

//	@Test
//	public void test4getRun()
//	{
//		logger.debug("get Run");
////		WSResponse response = WSHelper.getAsBot(ws, "/api/runs/"+run.code, 200);
//		WSResponse response = wsBot.get("/api/runs/"+run.code, 200);
//		assertThat(response.asJson()).isNotNull();
//	}

//	@Test
//	public void test5headRun()
//	{
//		logger.debug("head Run");
////		WSResponse response = WSHelper.headAsBot(ws, "/api/runs/"+run.code, 200);
//		WSResponse response = wsBot.head("/api/runs/"+run.code, 200);
//		assertThat(response).isNotNull();
//	}

//	@Test
//	public void test6GetState()
//	{
//		logger.debug("get State");
////		WSResponse response = WSHelper.getAsBot(ws, "/api/runs/"+run.code+"/state", 200);
//		WSResponse response = wsBot.get("/api/runs/"+run.code+"/state", 200);
//		assertThat(response.asJson()).isNotNull();
//	}

//	@Test
//	public void test7GetStateHistorical()
//	{
//		logger.debug("get State historical");
////		WSResponse response = WSHelper.getAsBot(ws, "/api/runs/"+run.code+"/state/historical", 200);
//		WSResponse response = wsBot.get("/api/runs/"+run.code+"/state/historical", 200);
//		assertThat(response.asJson()).isNotNull();
//	}

//	@Test
//	public void test8Valuation()
//	{
//		logger.debug("valuation");
//		//Create valuation
//		Valuation valuation = new Valuation();
//		valuation.comment="test valuation";
//		valuation.date=new Date();
//		valuation.valid=TBoolean.FALSE;
//
////		WSHelper.putObjectAsBot(ws, "/api/runs/"+run.code+"/valuation",valuation, 200);
//		wsBot.putObject("/api/runs/"+run.code+"/valuation",valuation, 200);
//		run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, run.code);
//		assertThat(run.valuation.valid).isEqualTo(TBoolean.FALSE);
//		assertThat(run.valuation.comment).isEqualTo("test valuation");
//	}

	/*
	 * EJACOBY: Ne peut etre testé
	 */
	//@Test
	/*public void test9ApplyRules()
	{
		testInServer(devapp(),
				ws -> {	
					Logger.debug("apply rules");
					//Set state to IPS
					State state = new State("IP-S","bot");
					WSHelper.put(ws, "/api/runs/"+run.code+"/state", RunMockHelper.getJsonState(state).toString(), 200);
					WSResponse response = WSHelper.put(ws, "/api/runs/"+run.code+"/apply-rules/IP_S_1","{}",200);
					run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, run.code);
					Logger.debug("Run "+Json.toJson(run.properties));
					//assertThat(run.properties.get("libProcessTypeCodes")).isNotNull();
				});
	}*/

//	@Test
//	public void test9DeleteRun()
//	{
//		logger.debug("delete Run");
////		WSHelper.deleteAsBot(ws,"/api/runs/"+run.code,200);
//		wsBot.delete("/api/runs/"+run.code,200);
//		Run runDB = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, run.code);
//		assertThat(runDB).isNull();
//	}

}
