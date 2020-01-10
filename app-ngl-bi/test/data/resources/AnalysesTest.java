package data.resources;

import static org.fest.assertions.Assertions.assertThat;

import java.util.Date;
import java.util.List;

import org.junit.Test;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.play.test.NGLWSClient;
import fr.cea.ig.util.function.C3;
import fr.cea.ig.util.function.CC1Managed;
import fr.cea.ig.util.function.CC3;
import fr.cea.ig.util.function.CCActions;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.common.instance.Valuation;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.project.instance.Project;
import models.laboratory.run.instance.Analysis;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import ngl.bi.Global;
import play.Application;
import play.libs.Json;
import play.libs.ws.WSResponse;

//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
//public class AnalysesTest extends AbstractBIServerTest {
public class AnalysesTest {

//	private static final play.Logger.ALogger logger = play.Logger.of(AnalysesTest.class);
//	
//	static Analysis analysis;
//	static String jsonAnalysis;
//	
//	@BeforeClass
//	public static void initData() {	
//		List<Analysis> analysisList = MongoDBDAO.find("ngl_bi.Analysis_dataWF", Analysis.class).toList();
//		for(Analysis a : analysisList){
//			analysis=a;
//			boolean existReadSetCode=true;
//			for(String rsCode : a.masterReadSetCodes){
//				if(MongoDBDAO.findByCode("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class,rsCode)==null)
//					existReadSetCode=false;
//			}
//			if(existReadSetCode)
//				break;
//		}
//		analysis._id=null;
//		analysis.treatments.get("assemblyBA").results.get("pairs").put("software", new PropertySingleValue("software"));
//		jsonAnalysis = Json.toJson(analysis).toString();
//		//MongoDBDAO.save(InstanceConstants.ANALYSIS_COLL_NAME, analysis);
//		
//		//Save ReadSetCode
//		for(String readSetCode : analysis.masterReadSetCodes){
//			ReadSet readSet = MongoDBDAO.findByCode("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class, readSetCode);
//			readSet.state.code="IW-BA";
//			MongoDBDAO.save(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet);
//		}
//		for(String readSetCode : analysis.readSetCodes){
//			ReadSet readSet = MongoDBDAO.findByCode("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class, readSetCode);
//			readSet.state.code="IW-BA";
//			MongoDBDAO.save(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet);
//		}
//		
//		for(String sampleCode : analysis.sampleCodes){
//			Sample sample = MongoDBDAO.findByCode("ngl_bq.Sample_dataWF", Sample.class, sampleCode);
//			MongoDBDAO.save(InstanceConstants.SAMPLE_COLL_NAME,sample);
//		}
//		for(String codeProjet : analysis.projectCodes){
//			Project project = MongoDBDAO.findByCode("ngl_project.Project_dataWF", Project.class, codeProjet);
//			MongoDBDAO.save(InstanceConstants.PROJECT_COLL_NAME, project);
//		}
//	}
//	
//	@AfterClass
//	public static void deleteData()	{
//		if(MongoDBDAO.findByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, analysis.code)!=null){
//			MongoDBDAO.deleteByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, analysis.code);
//		}
//		
//		for(String readSetCode: analysis.masterReadSetCodes){
//			MongoDBDAO.deleteByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSetCode);
//		}
//		
//		for(String readSetCode: analysis.readSetCodes){
//			MongoDBDAO.deleteByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSetCode);
//		}
//
//		for(String codeProjet : analysis.projectCodes){
//			MongoDBDAO.deleteByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, codeProjet);
//		}
//		for(String sampleCode:analysis.sampleCodes){
//			MongoDBDAO.deleteByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, sampleCode);
//		}
//	}
	
	private static final play.Logger.ALogger logger = play.Logger.of(AnalysesTest.class);

	private static class TestContext implements CC1Managed {
		Analysis analysis;
		String jsonAnalysis;

		@Override
		public  void setUp() {	
			List<Analysis> analysisList = MongoDBDAO.find("ngl_bi.Analysis_dataWF", Analysis.class).toList();
			for (Analysis a : analysisList) {
				analysis = a;
				boolean existReadSetCode = true;
				for (String rsCode : a.masterReadSetCodes) {
					if (MongoDBDAO.findByCode("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class,rsCode) == null)
						existReadSetCode = false;
				}
				if(existReadSetCode)
					break;
			}
			logger.debug("using analysis {}", analysis.code);
			analysis._id = null;
			analysis.treatments.get("assemblyBA").results.get("pairs").put("software", new PropertySingleValue("software"));
			jsonAnalysis = Json.toJson(analysis).toString();
			//MongoDBDAO.save(InstanceConstants.ANALYSIS_COLL_NAME, analysis);

			//Save ReadSetCode
			for (String readSetCode : analysis.masterReadSetCodes) {
				ReadSet readSet = MongoDBDAO.findByCode("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class, readSetCode);
				readSet.state.code="IW-BA";
				MongoDBDAO.save(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet);
			}
			for (String readSetCode : analysis.readSetCodes) {
				ReadSet readSet = MongoDBDAO.findByCode("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class, readSetCode);
				readSet.state.code="IW-BA";
				MongoDBDAO.save(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet);
			}

			for (String sampleCode : analysis.sampleCodes) {
				Sample sample = MongoDBDAO.findByCode("ngl_bq.Sample_dataWF", Sample.class, sampleCode);
				MongoDBDAO.save(InstanceConstants.SAMPLE_COLL_NAME,sample);
			}
			for (String codeProjet : analysis.projectCodes) {
				Project project = MongoDBDAO.findByCode("ngl_project.Project_dataWF", Project.class, codeProjet);
				MongoDBDAO.save(InstanceConstants.PROJECT_COLL_NAME, project);
			}
		}

		@Override
		public void tearDown()	{
			if (MongoDBDAO.findByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, analysis.code) != null) {
				MongoDBDAO.deleteByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, analysis.code);
			}

			for (String readSetCode : analysis.masterReadSetCodes) {
				MongoDBDAO.deleteByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSetCode);
			}

			for (String readSetCode : analysis.readSetCodes){ 
				MongoDBDAO.deleteByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSetCode);
			}

			for (String codeProjet : analysis.projectCodes) {
				MongoDBDAO.deleteByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, codeProjet);
			}
			for (String sampleCode : analysis.sampleCodes) {
				MongoDBDAO.deleteByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, sampleCode);
			}
		}
		
	}
	
	private static final CC3<Application,NGLWSClient,TestContext> af = 
			Global.af.cc2()
			.and(CCActions.managed(TestContext::new));

	private static final C3<Application,NGLWSClient,TestContext>
		test1Impl = (app,ws,ctx) -> {
			logger.debug("save Analysis");
			ws.asBot().post("/api/analyses", ctx.jsonAnalysis, 200);
			ctx.analysis = MongoDBDAO.findByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, ctx.analysis.code);
			assertThat(ctx.analysis).isNotNull();
		},
		test2Impl = test1Impl.andThen((app,ws,ctx) -> {
			logger.debug("list Analysis");
			WSResponse response = ws.asBot().get("/api/analyses", 200);
			assertThat(response.asJson()).isNotNull();			
		}),
		test3Impl = test2Impl.andThen((app,ws,ctx) -> {
			logger.debug("get Analysis");
			WSResponse response = ws.asBot().get("/api/analyses/" + ctx.analysis.code, 200);
			assertThat(response.asJson()).isNotNull();
		}),
		test4Impl = test3Impl.andThen((app,ws,ctx) -> {
			logger.debug("head Analysis");
			WSResponse response = ws.asBot().head("/api/analyses/"+ctx.analysis.code, 200);
			assertThat(response).isNotNull();
		}),
		test5Impl = test4Impl.andThen((app,ws,ctx) -> {
			logger.debug("update Analyses");
			ctx.analysis.path = "test";
			ws.asBot().putObject("/api/analyses/"+ctx.analysis.code,ctx.analysis, 200);
			ctx.analysis = MongoDBDAO.findByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, ctx.analysis.code);
			assertThat(ctx.analysis.path).isEqualTo("test");
		}),
		test6Impl = test5Impl.andThen((app,ws,ctx) -> {
			logger.debug("valuation");
			//Create valuation
			
			Valuation valuation = new Valuation();
			valuation.comment="test valuation";
			valuation.date=new Date();
			valuation.valid=TBoolean.FALSE;

			ws.asBot().putObject("/api/analyses/"+ctx.analysis.code+"/valuation",valuation, 200);
			ctx.analysis = MongoDBDAO.findByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, ctx.analysis.code);
			assertThat(ctx.analysis.valuation.valid).isEqualTo(TBoolean.FALSE);
			assertThat(ctx.analysis.valuation.comment).isEqualTo("test valuation");
		}),
		test7Impl = test6Impl.andThen((app,ws,ctx) -> {
			ctx.analysis.properties.put("test", new PropertySingleValue("test"));
			ws.asBot().putObject("/api/analyses/"+ctx.analysis.code+"/properties",ctx.analysis, 200);
			ctx.analysis = MongoDBDAO.findByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, ctx.analysis.code);
			assertThat(ctx.analysis.properties.get("test").value).isEqualTo("test");
		}),
		test8Impl = test7Impl.andThen((app,ws,ctx) -> {
			logger.debug("delete Analysis");
			ws.asBot().delete("/api/analyses/"+ctx.analysis.code,200);
			Analysis analysisDB = MongoDBDAO.findByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, ctx.analysis.code);
			assertThat(analysisDB).isNull();			
		});
		
	@Test public void test1save()       throws Exception { af.accept(test1Impl); }
	@Test public void test2list()       throws Exception { af.accept(test2Impl); }
	@Test public void test3get()        throws Exception { af.accept(test3Impl); }
	@Test public void test4head()       throws Exception { af.accept(test4Impl); }
	@Test public void test5update()     throws Exception { af.accept(test5Impl); }
	@Test public void test6valuation()  throws Exception { af.accept(test6Impl); }
	@Test public void test7properties() throws Exception { af.accept(test7Impl); }
	@Test public void test8delete()     throws Exception { af.accept(test8Impl); }

//	@Test
//	public void test1save()
//	{
//		logger.debug("save Analysis");
//		// WSHelper.postAsBot(ws, "/api/analyses", jsonAnalysis, 200);
//		wsBot.post("/api/analyses", jsonAnalysis, 200);
//		analysis = MongoDBDAO.findByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, analysis.code);
//		assertThat(analysis).isNotNull();
//	}
	
//	@Test
//	public void test2list()
//	{
//		logger.debug("list Analysis");
////		WSResponse response = WSHelper.getAsBot(ws, "/api/analyses", 200);
//		WSResponse response = wsBot.get("/api/analyses", 200);
//		assertThat(response.asJson()).isNotNull();
//	}

//	@Test
//	public void test3get()
//	{
//		logger.debug("get Analysis");
////		WSResponse response = WSHelper.getAsBot(ws, "/api/analyses/"+analysis.code, 200);
//		WSResponse response = wsBot.get("/api/analyses/" + analysis.code, 200);
//		assertThat(response.asJson()).isNotNull();
//	}
	
//	@Test
//	public void test4head()
//	{
//		logger.debug("head Analysis");
////		WSResponse response = WSHelper.headAsBot(ws, "/api/analyses/"+analysis.code, 200);
//		WSResponse response = wsBot.head("/api/analyses/"+analysis.code, 200);
//		assertThat(response).isNotNull();
//	}
	
//	@Test
//	public void test5update()
//	{
//		logger.debug("update Analyses");
//		analysis.path="test";
////		WSHelper.putObjectAsBot(ws, "/api/analyses/"+analysis.code,analysis, 200);
//		wsBot.putObject("/api/analyses/"+analysis.code,analysis, 200);
//		analysis = MongoDBDAO.findByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, analysis.code);
//		assertThat(analysis.path).isEqualTo("test");
//	}
	
//	@Test
//	public void test6valuation()
//	{
//		logger.debug("valuation");
//		//Create valuation
//		
//		Valuation valuation = new Valuation();
//		valuation.comment="test valuation";
//		valuation.date=new Date();
//		valuation.valid=TBoolean.FALSE;
//
////		WSHelper.putObjectAsBot(ws, "/api/analyses/"+analysis.code+"/valuation",valuation, 200);
//		wsBot.putObject("/api/analyses/"+analysis.code+"/valuation",valuation, 200);
//		analysis = MongoDBDAO.findByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, analysis.code);
//		assertThat(analysis.valuation.valid).isEqualTo(TBoolean.FALSE);
//		assertThat(analysis.valuation.comment).isEqualTo("test valuation");
//	}
	
//	@Test
//	public void test7properties()
//	{
//		analysis.properties.put("test", new PropertySingleValue("test"));
////		WSHelper.putObjectAsBot(ws, "/api/analyses/"+analysis.code+"/properties",analysis, 200);
//		wsBot.putObject("/api/analyses/"+analysis.code+"/properties",analysis, 200);
//		analysis = MongoDBDAO.findByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, analysis.code);
//		assertThat(analysis.properties.get("test").value).isEqualTo("test");
//	}

//	@Test
//	public void test8delete() {
//		logger.debug("delete Analysis");
////		WSHelper.deleteAsBot(ws,"/api/analyses/"+analysis.code,200);
//		wsBot.delete("/api/analyses/"+analysis.code,200);
//		Analysis analysisDB = MongoDBDAO.findByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, analysis.code);
//		assertThat(analysisDB).isNull();
//	}

}
