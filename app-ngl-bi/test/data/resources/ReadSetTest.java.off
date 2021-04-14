package data.resources;

import static org.fest.assertions.Assertions.assertThat;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mongojack.DBQuery;

import controllers.readsets.api.ReadSetValuation;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.play.test.NGLWSClient;
import fr.cea.ig.util.function.C3;
import fr.cea.ig.util.function.CC1Managed;
import fr.cea.ig.util.function.CC3;
import fr.cea.ig.util.function.CCActions;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.common.instance.Valuation;
import models.laboratory.project.instance.Project;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import ngl.bi.Global;
import play.Application;
import play.libs.Json;
import play.libs.ws.WSResponse;
import utils.RunMockHelper;

//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
//public class ReadSetTest extends AbstractBIServerTest{

public class ReadSetTest {
	
	private static final play.Logger.ALogger logger = play.Logger.of(ReadSetTest.class); 
	
//	static ReadSet readSet;
//	static ReadSet readSetExt;
//	static List<ReadSet> readSets;
//	static String jsonReadSet;
//	static String jsonReadSetExt;
//	static Run run;
//
//	@BeforeClass
//	public static void initData()
//	{	
//		List<Run> runs  = MongoDBDAO.find("ngl_bi.RunIllumina_dataWF", Run.class, DBQuery.exists("properties.libProcessTypeCodes")).toList();
//		for(Run runDB : runs){
//			readSets = MongoDBDAO.find("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class, DBQuery.is("runCode", runDB.code)).toList();
//			if (readSets.size() > 0) {
//				run=runDB;
//				break;
//			}
//		}
//		//get JSON Run to insert
//		//List<ReadSet> readSets  = MongoDBDAO.find("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class).toList();
//		readSet = readSets.remove(0);
//		
//		readSet._id=null;
//		jsonReadSet = Json.toJson(readSet).toString();
//		
//		readSetExt = RunMockHelper.newReadSet("rdCode");
//		readSetExt.location="CNS";
//		readSetExt.runCode = run.code;
//		jsonReadSetExt = Json.toJson(readSetExt).toString();
//		
//		//get run
//		run = MongoDBDAO.findByCode("ngl_bi.RunIllumina_dataWF", Run.class, run.code);
//		logger.debug("RUN CODE "+run.code);
//		MongoDBDAO.save(InstanceConstants.RUN_ILLUMINA_COLL_NAME, run);
//		//insert project in collection
//		for(String codeProjet : run.projectCodes){
//			Project project = MongoDBDAO.findByCode("ngl_project.Project_dataWF", Project.class, codeProjet);
//			MongoDBDAO.save(InstanceConstants.PROJECT_COLL_NAME, project);
//		}
//		Project projectExt = MongoDBDAO.findByCode("ngl_project.Project_dataWF", Project.class, "BFB");
//		MongoDBDAO.save(InstanceConstants.PROJECT_COLL_NAME, projectExt);
//
//		//insert sampleCode
//		for(String codeSample : run.sampleCodes){
//			Sample sample = MongoDBDAO.findByCode("ngl_bq.Sample_dataWF", Sample.class, codeSample);
//			MongoDBDAO.save(InstanceConstants.SAMPLE_COLL_NAME,sample);
//		}
//		for(ReadSet readSet : readSets){
//			MongoDBDAO.save(InstanceConstants.READSET_ILLUMINA_COLL_NAME,readSet);
//		}
//
//	}
//
//	@AfterClass
//	public static void deleteData()
//	{
//		if (MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code) != null) {
//			MongoDBDAO.deleteByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
//		}
//		MongoDBDAO.deleteByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSetExt.code);
//		for(ReadSet readSet: readSets){
//			if(MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code)!=null){
//				MongoDBDAO.deleteByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
//			}
//		}
//		MongoDBDAO.deleteByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, run.code);
//
//		for(String codeProjet : run.projectCodes){
//			MongoDBDAO.deleteByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, codeProjet);
//		}
//		for(String sampleCode:run.sampleCodes){
//			MongoDBDAO.deleteByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, sampleCode);
//		}
//		MongoDBDAO.deleteByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, "BFB_AABA");
//	}

	private static class TestContext implements CC1Managed {
		ReadSet readSet;
		ReadSet readSetExt;
		List<ReadSet> readSets;
		String jsonReadSet;
		String jsonReadSetExt;
		Run run;

		@Override
		public void setUp() {	
			List<Run> runs  = MongoDBDAO.find("ngl_bi.RunIllumina_dataWF", Run.class, DBQuery.exists("properties.libProcessTypeCodes")).toList();
			for(Run runDB : runs){
				readSets = MongoDBDAO.find("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class, DBQuery.is("runCode", runDB.code)).toList();
				if (readSets.size() > 0) {
					run=runDB;
					break;
				}
			}
			//get JSON Run to insert
			//List<ReadSet> readSets  = MongoDBDAO.find("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class).toList();
			readSet = readSets.remove(0);

			readSet._id=null;
			jsonReadSet = Json.toJson(readSet).toString();

			readSetExt = RunMockHelper.newReadSet("rdCode");
			readSetExt.location="CNS";
			readSetExt.runCode = run.code;
			jsonReadSetExt = Json.toJson(readSetExt).toString();

			//get run
			run = MongoDBDAO.findByCode("ngl_bi.RunIllumina_dataWF", Run.class, run.code);
			logger.debug("RUN CODE "+run.code);
			MongoDBDAO.save(InstanceConstants.RUN_ILLUMINA_COLL_NAME, run);
			//insert project in collection
			for(String codeProjet : run.projectCodes){
				Project project = MongoDBDAO.findByCode("ngl_project.Project_dataWF", Project.class, codeProjet);
				MongoDBDAO.save(InstanceConstants.PROJECT_COLL_NAME, project);
			}
			Project projectExt = MongoDBDAO.findByCode("ngl_project.Project_dataWF", Project.class, "BFB");
			MongoDBDAO.save(InstanceConstants.PROJECT_COLL_NAME, projectExt);

			//insert sampleCode
			for(String codeSample : run.sampleCodes){
				Sample sample = MongoDBDAO.findByCode("ngl_bq.Sample_dataWF", Sample.class, codeSample);
				MongoDBDAO.save(InstanceConstants.SAMPLE_COLL_NAME,sample);
			}
			for(ReadSet readSet : readSets){
				MongoDBDAO.save(InstanceConstants.READSET_ILLUMINA_COLL_NAME,readSet);
			}

		}

		@Override
		public void tearDown() {
			if (MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code) != null) {
				MongoDBDAO.deleteByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
			}
			MongoDBDAO.deleteByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSetExt.code);
			for(ReadSet readSet: readSets){
				if(MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code)!=null){
					MongoDBDAO.deleteByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
				}
			}
			MongoDBDAO.deleteByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, run.code);

			for(String codeProjet : run.projectCodes){
				MongoDBDAO.deleteByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, codeProjet);
			}
			for(String sampleCode:run.sampleCodes){
				MongoDBDAO.deleteByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, sampleCode);
			}
			MongoDBDAO.deleteByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, "BFB_AABA");
		}
		
	}
	
	private static final CC3<Application,NGLWSClient,TestContext> af = 
			Global.af.cc2()
			.and(CCActions.managed(TestContext::new));

	private static final C3<Application,NGLWSClient,TestContext>
		test1Impl = (app,ws,ctx) -> {
			logger.debug("save ReadSet");
			ws.asBot().post("/api/readsets", ctx.jsonReadSet, 200);
			ctx.readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, ctx.readSet.code);
			logger.debug("ReadSet "+ctx.readSet.code);
			assertThat(ctx.readSet).isNotNull();
		},
		test1ExtImpl = test1Impl.andThen((app,ws,ctx) -> {
			final play.Logger.ALogger logger = play.Logger.of(ReadSetTest.class.getName() + ".test1saveExt");
			final String SAMPLE_CODE = "BFB_AABA";
			
			logger.debug("fetching sample {} from DB", SAMPLE_CODE);
			Sample sample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, SAMPLE_CODE);
			Assert.assertNull(sample);
			
			ctx.readSetExt.sampleCode  = SAMPLE_CODE;
			ctx.readSetExt.projectCode = "BFB";
			ctx.readSetExt.sampleOnContainer = RunMockHelper.newSampleOnContainer(ctx.readSetExt.sampleCode);
			ctx.jsonReadSetExt = Json.toJson(ctx.readSetExt).toString();
			
			logger.debug("creating ReadSet through web API");
			ws.asBot().post("/api/readsets?external=true", ctx.jsonReadSetExt, 200);
			ctx.readSetExt = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, ctx.readSetExt.code);
			logger.debug("read ReadSet from DB {}", ctx.readSetExt.code);
			assertThat(ctx.readSetExt).isNotNull();
			
			//Check sample created
			sample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, "BFB_AABA");
			Assert.assertNotNull(sample);
			logger.debug("read Sample from DB {}", sample);
			//Check sampleOnContainer created
			ctx.readSetExt = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, "rdCode");
			logger.debug("Sample on container " + ctx.readSetExt.sampleOnContainer);
			Assert.assertNotNull(ctx.readSetExt.sampleOnContainer);
			Assert.assertNotNull(ctx.readSetExt.sampleOnContainer.referenceCollab);
			Assert.assertNotNull(ctx.readSetExt.sampleOnContainer.sampleCategoryCode);
			logger.debug("test1saveExt passed");
		}),
		test2Impl = test1ExtImpl.andThen((app,ws,ctx) -> {
			logger.debug("list ReadSet");
			WSResponse response = ws.asBot().get("/api/readsets", 200);
			assertThat(response.asJson()).isNotNull();
		}),
		test3Impl = test2Impl.andThen((app,ws,ctx) -> {
			logger.debug("get ReadSet");
			WSResponse response = ws.asBot().get("/api/readsets/"+ctx.readSet.code, 200);
			assertThat(response.asJson()).isNotNull();			
		}),
		test4Impl = test3Impl.andThen((app,ws,ctx) -> {
			logger.debug("head ReadSet");
			WSResponse response = ws.asBot().head("/api/readsets/"+ctx.readSet.code, 200);
			assertThat(response).isNotNull();
		}),
		test5Impl = test4Impl.andThen((app,ws,ctx) -> {
			logger.debug("update ReadSet");
			Date date = new Date();
			ctx.readSet.archiveDate=date;
			ws.asBot().putObject("/api/readsets/"+ctx.readSet.code,ctx.readSet, 200);
			ctx.readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, ctx.readSet.code);
			assertThat(ctx.readSet.archiveDate).isEqualTo(date);
		}),
		test6Impl = test5Impl.andThen((app,ws,ctx) -> {
			logger.debug("valuation");
			//Create valuation
			
			ReadSetValuation readSetValuation = new ReadSetValuation();
			Valuation valuation = new Valuation();
			valuation.comment="test valuation";
			valuation.date=new Date();
			valuation.valid=TBoolean.FALSE;
			readSetValuation.bioinformaticValuation=valuation;
			readSetValuation.productionValuation=valuation;

			ws.asBot().putObject("/api/readsets/"+ctx.readSet.code+"/valuation", readSetValuation, 200);
			ctx.readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, ctx.readSet.code);
			assertThat(ctx.readSet.bioinformaticValuation.valid).isEqualTo(TBoolean.FALSE);
			assertThat(ctx.readSet.bioinformaticValuation.comment).isEqualTo("test valuation");
			assertThat(ctx.readSet.productionValuation.valid).isEqualTo(TBoolean.FALSE);
			assertThat(ctx.readSet.productionValuation.comment).isEqualTo("test valuation");
		}),
		test8Impl = test6Impl.andThen((app,ws,ctx) -> {
			logger.debug("delete ReadSet");
			ws.asBot().delete("/api/readsets/"+ctx.readSet.code,200);
			ReadSet readSetDB = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, ctx.readSet.code);
			assertThat(readSetDB).isNull();
		}),
		test9Impl = test8Impl.andThen((app,ws,ctx) -> {
			logger.debug("delete ReadSet by runCode");
			ws.asBot().delete("/api/runs/"+ctx.run.code+"/readsets",200);
			List<ReadSet> readSetDB = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class,DBQuery.is("runCode", ctx.run.code)).toList();
			assertThat(readSetDB.size()==0);
		});

	@Test public void test1save()            throws Exception { af.accept(test1Impl); }
	@Test public void test1saveExt()         throws Exception { af.accept(test1ExtImpl); }
	@Test public void test2list()            throws Exception { af.accept(test2Impl); }
	@Test public void test3get()             throws Exception { af.accept(test3Impl); }
	@Test public void test4head()            throws Exception { af.accept(test4Impl); }
	@Test public void test5update()          throws Exception { af.accept(test5Impl); }
	@Test public void test6valuation()       throws Exception { af.accept(test6Impl); }
	@Test public void test8delete()          throws Exception { af.accept(test8Impl); }
	@Test public void test9deleteByRunCode() throws Exception { af.accept(test9Impl); }

//	@Test
//	public void test1save()
//	{
//		logger.debug("save ReadSet");
////		WSHelper.postAsBot(ws, "/api/readsets", jsonReadSet, 200);
//		wsBot.post("/api/readsets", jsonReadSet, 200);
//		readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
//		logger.debug("ReadSet "+readSet.code);
//		assertThat(readSet).isNotNull();
//	}
	
//	@Test
//	public void test1saveExt()
//	{
//		final play.Logger.ALogger logger = play.Logger.of(ReadSetTest.class.getName() + ".test1saveExt");
//		final String SAMPLE_CODE = "BFB_AABA";
//		
//		logger.debug("fetching sample {} from DB", SAMPLE_CODE);
//		Sample sample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, SAMPLE_CODE);
//		Assert.assertNull(sample);
//		
//		readSetExt.sampleCode  = SAMPLE_CODE;
//		readSetExt.projectCode = "BFB";
//		readSetExt.sampleOnContainer = RunMockHelper.newSampleOnContainer(readSetExt.sampleCode);
//		jsonReadSetExt = Json.toJson(readSetExt).toString();
//		
//		logger.debug("creating ReadSet through web API");
////		WSHelper.postAsBot(ws, "/api/readsets?external=true", jsonReadSetExt, 200);
//		wsBot.post("/api/readsets?external=true", jsonReadSetExt, 200);
//		readSetExt = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSetExt.code);
//		logger.debug("read ReadSet from DB {}", readSetExt.code);
//		assertThat(readSetExt).isNotNull();
//		
//		//Check sample created
//		sample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, "BFB_AABA");
//		Assert.assertNotNull(sample);
//		logger.debug("read Sample from DB {}", sample);
//		//Check sampleOnContainer created
//		readSetExt = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, "rdCode");
//		logger.debug("Sample on container " + readSetExt.sampleOnContainer);
//		Assert.assertNotNull(readSetExt.sampleOnContainer);
//		Assert.assertNotNull(readSetExt.sampleOnContainer.referenceCollab);
//		Assert.assertNotNull(readSetExt.sampleOnContainer.sampleCategoryCode);
//		logger.debug("test1saveExt passed");
//	}
	
//	@Test
//	public void test2list()
//	{
//		logger.debug("list ReadSet");
////		WSResponse response = WSHelper.getAsBot(ws, "/api/readsets", 200);
//		WSResponse response = wsBot.get("/api/readsets", 200);
//		assertThat(response.asJson()).isNotNull();
//	}

//	@Test
//	public void test3get()
//	{
//		logger.debug("get ReadSet");
////		WSResponse response = WSHelper.getAsBot(ws, "/api/readsets/"+readSet.code, 200);
//		WSResponse response = wsBot.get("/api/readsets/"+readSet.code, 200);
//		assertThat(response.asJson()).isNotNull();
//	}

//	@Test
//	public void test4head()
//	{
//		logger.debug("head ReadSet");
////		WSResponse response = WSHelper.headAsBot(ws, "/api/readsets/"+readSet.code, 200);
//		WSResponse response = wsBot.head("/api/readsets/"+readSet.code, 200);
//		assertThat(response).isNotNull();
//	}

//	@Test
//	public void test5update()
//	{
//		logger.debug("update ReadSet");
//		Date date = new Date();
//		readSet.archiveDate=date;
////		WSHelper.putObjectAsBot(ws, "/api/readsets/"+readSet.code,readSet, 200);
//		wsBot.putObject("/api/readsets/"+readSet.code,readSet, 200);
//		readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
//		assertThat(readSet.archiveDate).isEqualTo(date);
//	}

//	@Test
//	public void test6valuation()
//	{
//		logger.debug("valuation");
//		//Create valuation
//		
//		ReadSetValuation readSetValuation = new ReadSetValuation();
//		Valuation valuation = new Valuation();
//		valuation.comment="test valuation";
//		valuation.date=new Date();
//		valuation.valid=TBoolean.FALSE;
//		readSetValuation.bioinformaticValuation=valuation;
//		readSetValuation.productionValuation=valuation;
//
////		WSHelper.putObjectAsBot(ws, "/api/readsets/"+readSet.code+"/valuation",readSetValuation, 200);
//		wsBot.putObject("/api/readsets/"+readSet.code+"/valuation", readSetValuation, 200);
//		readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
//		assertThat(readSet.bioinformaticValuation.valid).isEqualTo(TBoolean.FALSE);
//		assertThat(readSet.bioinformaticValuation.comment).isEqualTo("test valuation");
//		assertThat(readSet.productionValuation.valid).isEqualTo(TBoolean.FALSE);
//		assertThat(readSet.productionValuation.comment).isEqualTo("test valuation");
//	}
	
//	@Test
//	public void test8delete()
//	{
//		logger.debug("delete ReadSet");
////		WSHelper.deleteAsBot(ws,"/api/readsets/"+readSet.code,200);
//		wsBot.delete("/api/readsets/"+readSet.code,200);
//		ReadSet readSetDB = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
//		assertThat(readSetDB).isNull();
//	}

//	@Test
//	public void test9deleteByRunCode()
//	{
//		logger.debug("delete ReadSet by runCode");
////		WSHelper.deleteAsBot(ws,"/api/runs/"+run.code+"/readsets",200);
//		wsBot.delete("/api/runs/"+run.code+"/readsets",200);
//		List<ReadSet> readSetDB = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class,DBQuery.is("runCode", run.code)).toList();
//		assertThat(readSetDB.size()==0);
//	}
	
}
