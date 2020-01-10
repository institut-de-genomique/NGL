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
import models.laboratory.run.instance.Analysis;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Treatment;
import models.utils.InstanceConstants;
import ngl.bi.Global;
import play.Application;
import play.libs.Json;
import play.libs.ws.WSResponse;

//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
//public class AnalysesTreatmentTest extends AbstractBIServerTest{
public class AnalysesTreatmentTest {

	private static final play.Logger.ALogger logger = play.Logger.of(AnalysesTreatmentTest.class);
	
//	static Analysis analysis;
//	static String jsonAssemblyBA;
//	
//	@BeforeClass
//	public static void initData()
//	{	
//		List<Analysis> analysisList = MongoDBDAO.find("ngl_bi.Analysis_dataWF", Analysis.class, DBQuery.exists("treatments.mergingBA").exists("treatments.assemblyBA")).toList();
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
//		analysis.treatments.get("assemblyBA").results.get("pairs").put("software", new PropertySingleValue("software"));
//		jsonAssemblyBA = Json.toJson(analysis.treatments.get("assemblyBA")).toString();
//		analysis.treatments.remove("assemblyBA");
//		MongoDBDAO.save(InstanceConstants.ANALYSIS_COLL_NAME, analysis);
//		
//		
//		
//	}
//	
//	@AfterClass
//	public static void deleteData()
//	{
//		if (MongoDBDAO.findByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, analysis.code) != null) {
//			MongoDBDAO.deleteByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, analysis.code);
//		}	
//	}
//	

	private static class TestContext implements CC1Managed {
		
		Analysis analysis;
		String jsonAssemblyBA;

		@Override
		public void setUp() {	
			List<Analysis> analysisList = MongoDBDAO.find("ngl_bi.Analysis_dataWF", Analysis.class, DBQuery.exists("treatments.mergingBA").exists("treatments.assemblyBA")).toList();
			for(Analysis a : analysisList){
				analysis=a;
				boolean existReadSetCode=true;
				for(String rsCode : a.masterReadSetCodes){
					if(MongoDBDAO.findByCode("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class,rsCode)==null)
						existReadSetCode=false;
				}
				if(existReadSetCode)
					break;
			}
			analysis.treatments.get("assemblyBA").results.get("pairs").put("software", new PropertySingleValue("software"));
			jsonAssemblyBA = Json.toJson(analysis.treatments.get("assemblyBA")).toString();
			analysis.treatments.remove("assemblyBA");
			MongoDBDAO.save(InstanceConstants.ANALYSIS_COLL_NAME, analysis);
		}

		@Override
		public void tearDown() {
			if (MongoDBDAO.findByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, analysis.code) != null) {
				MongoDBDAO.deleteByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, analysis.code);
			}	
		}
		
	}
	
	private static final CC3<Application,NGLWSClient,TestContext> af = 
			Global.af.cc2()
			.and(CCActions.managed(TestContext::new));

	private static final C3<Application,NGLWSClient,TestContext>
		test1Impl = (app,ws,ctx) -> {
			logger.debug("list AnalysesTreatment");
			WSResponse response = ws.get("/api/analyses/"+ctx.analysis.code+"/treatments", 200);
			assertThat(response.asJson()).isNotNull();			
		},
		test2Impl = test1Impl.andThen((app,ws,ctx) -> {
			logger.debug("get AnalysesTreatment");
			WSResponse response = ws.get("/api/analyses/"+ctx.analysis.code+"/treatments/mergingBA", 200);
			assertThat(response.asJson()).isNotNull();			
		}),
		test3Impl = test2Impl.andThen((app,ws,ctx) -> {
			logger.debug("head AnalysesTreatment");
			WSResponse response = ws.head("/api/analyses/"+ctx.analysis.code+"/treatments/mergingBA", 200);
			assertThat(response).isNotNull();
		}),
		test4Impl = test3Impl.andThen((app,ws,ctx) -> {
			logger.debug("save AnalysesTreatment");
			ws.asBot().post("/api/analyses/"+ctx.analysis.code+"/treatments", ctx.jsonAssemblyBA, 200);
			ctx.analysis = MongoDBDAO.findByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, ctx.analysis.code);
			assertThat(ctx.analysis.treatments.get("assemblyBA")).isNotNull();
		}),
		test5Impl = test4Impl.andThen((app,ws,ctx) -> {
			logger.debug("update AnalysesTreatment");
			//Get Treatment ngsrg
			Treatment assemblyBA = ctx.analysis.treatments.get("assemblyBA");
			assemblyBA.results.get("pairs").put("GCpercent", new PropertySingleValue(new Double(0)));
			ws.asBot().putObject("/api/analyses/"+ctx.analysis.code+"/treatments/assemblyBA", assemblyBA, 200);
			ctx.analysis = MongoDBDAO.findByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, ctx.analysis.code);
			assertThat(ctx.analysis.treatments.get("assemblyBA").results.get("pairs").get("GCpercent").getValue()).isEqualTo(new Double(0));
		}),
		test6Impl = test5Impl.andThen((app,ws,ctx) -> {
			logger.debug("delete AnalysesTreatment");
			ws.asBot().delete("/api/analyses/"+ctx.analysis.code+"/treatments/assemblyBA", 200);
			ctx.analysis = MongoDBDAO.findByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, ctx.analysis.code);
			assertThat(ctx.analysis.treatments.get("assemblyBA")).isNull();			
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
//		logger.debug("list AnalysesTreatment");
////		WSResponse response = WSHelper.get(ws, "/api/analyses/"+analysis.code+"/treatments", 200);
//		WSResponse response = ws.get("/api/analyses/"+analysis.code+"/treatments", 200);
//		assertThat(response.asJson()).isNotNull();
//	}

//	@Test
//	public void test2get()
//	{
//		logger.debug("get AnalysesTreatment");
////		WSResponse response = WSHelper.get(ws, "/api/analyses/"+analysis.code+"/treatments/mergingBA", 200);
//		WSResponse response = ws.get("/api/analyses/"+analysis.code+"/treatments/mergingBA", 200);
//		assertThat(response.asJson()).isNotNull();
//	}

//	@Test
//	public void test3head()
//	{
//		logger.debug("head AnalysesTreatment");
////		WSResponse response = WSHelper.head(ws, "/api/analyses/"+analysis.code+"/treatments/mergingBA", 200);
//		WSResponse response = ws.head("/api/analyses/"+analysis.code+"/treatments/mergingBA", 200);
//		assertThat(response).isNotNull();
//	}
	
//	// failed: POST /api/analyses/BA.BFY_AAAAOSF_1_A737Y.IND1/treatments {"pairs.readsUsed.value":["PropriÚtÚ obligatoire"], ...
//	@Test
//	public void test4save()
//	{
//		logger.debug("save AnalysesTreatment");
////		WSHelper.postAsBot(ws, "/api/analyses/"+analysis.code+"/treatments", jsonAssemblyBA, 200);
//		wsBot.post("/api/analyses/"+analysis.code+"/treatments", jsonAssemblyBA, 200);
//		analysis = MongoDBDAO.findByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, analysis.code);
//		assertThat(analysis.treatments.get("assemblyBA")).isNotNull();
//	}

	// failed: POST /api/analyses/BA.BFY_AAAAOSF_1_A737Y.IND1/treatments {"pairs.readsUsed.value":["PropriÚtÚ obligatoire"], ...
	// -- once the save fails, everything after that fails (e.g: delete -> 404)
	
//	@Test
//	public void test5update()
//	{
//		logger.debug("update AnalysesTreatment");
//		//Get Treatment ngsrg
//		Treatment assemblyBA = analysis.treatments.get("assemblyBA");
//		assemblyBA.results.get("pairs").put("GCpercent", new PropertySingleValue(new Double(0)));
////		WSHelper.putObjectAsBot(ws, "/api/analyses/"+analysis.code+"/treatments/assemblyBA", assemblyBA, 200);
//		wsBot.putObject("/api/analyses/"+analysis.code+"/treatments/assemblyBA", assemblyBA, 200);
//		analysis = MongoDBDAO.findByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, analysis.code);
//		assertThat(analysis.treatments.get("assemblyBA").results.get("pairs").get("GCpercent").getValue()).isEqualTo(new Double(0));
//	}
	
//	@Test
//	public void test6delete()
//	{
//		logger.debug("delete AnalysesTreatment");
////		WSHelper.deleteAsBot(ws, "/api/analyses/"+analysis.code+"/treatments/assemblyBA", 200);
//		wsBot.delete("/api/analyses/"+analysis.code+"/treatments/assemblyBA", 200);
//		analysis = MongoDBDAO.findByCode(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, analysis.code);
//		assertThat(analysis.treatments.get("assemblyBA")).isNull();
//	}
	
}
