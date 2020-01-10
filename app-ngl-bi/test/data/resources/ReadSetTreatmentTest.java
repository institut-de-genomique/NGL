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
//public class ReadSetTreatmentTest extends AbstractBIServerTest {

public class ReadSetTreatmentTest {
	
	private static final play.Logger.ALogger logger = play.Logger.of(ReadSetTreatmentTest.class);
	
//	static List<ReadSet> readSets;
//	static ReadSet       readSet;
//	static String        jsonTaxonomy;
//	
//	@BeforeClass
//	public static void initData()
//	{	
//
//		List<Run> runs  = MongoDBDAO.find("ngl_bi.RunIllumina_dataWF", Run.class, DBQuery.exists("properties.libProcessTypeCodes")).toList();
//		for(Run runDB : runs){
//			readSets = MongoDBDAO.find("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class, DBQuery.is("runCode", runDB.code).exists("treatments.ngsrg").exists("treatments.taxonomy")).toList();
//			if (readSets.size() > 0) {
//				break;
//			}
//		}
//		//get JSON Run to insert
//		//List<ReadSet> readSets  = MongoDBDAO.find("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class).toList();
//		readSet = readSets.remove(0);
//		jsonTaxonomy = Json.toJson(readSet.treatments.get("taxonomy")).toString();
//		readSet.treatments.remove("taxonomy");
//		MongoDBDAO.save(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet);		
//	}
//	
//	@AfterClass
//	public static void deleteData()
//	{
//		if (MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code) != null) {
//			MongoDBDAO.deleteByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
//		}
//	}
	
	private static class TestContext implements CC1Managed {
		
		List<ReadSet> readSets;
		ReadSet       readSet;
		String        jsonTaxonomy;

		@Override
		public void setUp() {	
			List<Run> runs  = MongoDBDAO.find("ngl_bi.RunIllumina_dataWF", Run.class, DBQuery.exists("properties.libProcessTypeCodes")).toList();
			for(Run runDB : runs){
				readSets = MongoDBDAO.find("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class, DBQuery.is("runCode", runDB.code).exists("treatments.ngsrg").exists("treatments.taxonomy")).toList();
				if (readSets.size() > 0) {
					break;
				}
			}
			//get JSON Run to insert
			//List<ReadSet> readSets  = MongoDBDAO.find("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class).toList();
			readSet = readSets.remove(0);
			jsonTaxonomy = Json.toJson(readSet.treatments.get("taxonomy")).toString();
			readSet.treatments.remove("taxonomy");
			MongoDBDAO.save(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet);		
		}

		@Override
		public void tearDown() {
			if (MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code) != null) {
				MongoDBDAO.deleteByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
			}
		}

	}
	
	private static final CC3<Application,NGLWSClient,TestContext> af = 
			Global.af.cc2()
			.and(CCActions.managed(TestContext::new));

	private static final C3<Application,NGLWSClient,TestContext>
		test1Impl = (app,ws,ctx) -> {
			logger.debug("list ReadSetTreatment");
			WSResponse response = ws.asBot().get("/api/readsets/"+ctx.readSet.code+"/treatments", 200);
			assertThat(response.asJson()).isNotNull();
		},
		test2Impl = test1Impl.andThen((app,ws,ctx) -> {
			logger.debug("get ReadSetTreatment");
			WSResponse response = ws.asBot().get("/api/readsets/"+ctx.readSet.code+"/treatments/ngsrg", 200);
			assertThat(response.asJson()).isNotNull();
		}),
		test3Impl = test2Impl.andThen((app,ws,ctx) -> {
			logger.debug("head ReadSetTreatment");
			WSResponse response = ws.asBot().head("/api/readsets/"+ctx.readSet.code+"/treatments/ngsrg", 200);
			assertThat(response).isNotNull();			
		}),
		test4Impl = test3Impl.andThen((app,ws,ctx) -> {
			logger.debug("save ReadSetTreatment");
			ws.asBot().post("/api/readsets/" + ctx.readSet.code + "/treatments", ctx.jsonTaxonomy, 200);
			ctx.readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, ctx.readSet.code);
			logger.debug("ReadSet " + ctx.readSet.code);
			assertThat(ctx.readSet.treatments.get("taxonomy")).isNotNull();
		}),
		test5Impl = test4Impl.andThen((app,ws,ctx) -> {
			logger.debug("update ReadSetTreatment");
			//Get Treatment ngsrg
			Treatment taxonomy = ctx.readSet.treatments.get("taxonomy");
			taxonomy.results.get("read1").put("software", new PropertySingleValue("kraken"));
			ws.asBot().putObject("/api/readsets/"+ctx.readSet.code+"/treatments/taxonomy", taxonomy, 200);
			ctx.readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, ctx.readSet.code);
			assertThat(ctx.readSet.treatments.get("taxonomy").results.get("read1").get("software").getValue()).isEqualTo("kraken");
		}),
		test6Impl = test5Impl.andThen((app,ws,ctx) -> {
			logger.debug("delete ReadSetTreatment");
			ws.asBot().delete("/api/readsets/"+ctx.readSet.code+"/treatments/taxonomy",200);
			ctx.readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, ctx.readSet.code);
			assertThat(ctx.readSet.treatments.get("taxonomy")).isNull();			
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
//		logger.debug("list ReadSetTreatment");
////		WSResponse response = WSHelper.getAsBot(ws, "/api/readsets/"+readSet.code+"/treatments", 200);
//		WSResponse response = wsBot.get("/api/readsets/"+readSet.code+"/treatments", 200);
//		assertThat(response.asJson()).isNotNull();
//	}
	
//		@Test
//	public void test2get()
//	{
//		logger.debug("get ReadSetTreatment");
////		WSResponse response = WSHelper.getAsBot(ws, "/api/readsets/"+readSet.code+"/treatments/ngsrg", 200);
//		WSResponse response = wsBot.get("/api/readsets/"+readSet.code+"/treatments/ngsrg", 200);
//		assertThat(response.asJson()).isNotNull();
//	}
	
//	@Test
//	public void test3head()
//	{
//		logger.debug("head ReadSetTreatment");
////		WSResponse response = WSHelper.headAsBot(ws, "/api/readsets/"+readSet.code+"/treatments/ngsrg", 200);
//		WSResponse response = wsBot.head("/api/readsets/"+readSet.code+"/treatments/ngsrg", 200);
//		assertThat(response).isNotNull();
//	}
	
//	@Test
//	public void test4save()
//	{
//		logger.debug("save ReadSetTreatment");
////		WSHelper.postAsBot(ws, "/api/readsets/" + readSet.code + "/treatments", jsonTaxonomy, 200);
//		wsBot.post("/api/readsets/" + readSet.code + "/treatments", jsonTaxonomy, 200);
//		readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
//		logger.debug("ReadSet " + readSet.code);
//		assertThat(readSet.treatments.get("taxonomy")).isNotNull();
//	}
	
//	@Test
//	public void test5update()
//	{
//		logger.debug("update ReadSetTreatment");
//		//Get Treatment ngsrg
//		Treatment taxonomy = readSet.treatments.get("taxonomy");
//		taxonomy.results.get("read1").put("software", new PropertySingleValue("kraken"));
////		WSHelper.putObjectAsBot(ws, "/api/readsets/"+readSet.code+"/treatments/taxonomy", taxonomy, 200);
//		wsBot.putObject("/api/readsets/"+readSet.code+"/treatments/taxonomy", taxonomy, 200);
//		readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
//		assertThat(readSet.treatments.get("taxonomy").results.get("read1").get("software").getValue()).isEqualTo("kraken");
//	}
	
//	@Test
//	public void test6delete()
//	{
//		logger.debug("delete ReadSetTreatment");
////		WSHelper.deleteAsBot(ws,"/api/readsets/"+readSet.code+"/treatments/taxonomy",200);
//		wsBot.delete("/api/readsets/"+readSet.code+"/treatments/taxonomy",200);
//		readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
//		assertThat(readSet.treatments.get("taxonomy")).isNull();
//	}
	
}
