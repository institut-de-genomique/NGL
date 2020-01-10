package data.resources;

import static org.fest.assertions.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;

import org.junit.Test;
import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.play.test.NGLWSClient;
import fr.cea.ig.util.function.C3;
import fr.cea.ig.util.function.CC1Managed;
import fr.cea.ig.util.function.CC3;
import fr.cea.ig.util.function.CCActions;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.utils.InstanceConstants;
import ngl.bi.Global;
import play.Application;
import play.libs.ws.WSResponse;

//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
//public class ArchiveTest extends AbstractBIServerTest {
public class ArchiveTest {

	private static final play.Logger.ALogger logger = play.Logger.of(ArchiveTest.class);
	
//	static List<ReadSet> readSets;
//	static ReadSet readSet;
//	
//	@BeforeClass
//	public static void initData() {	
//		List<Run> runs  = MongoDBDAO.find("ngl_bi.RunIllumina_dataWF", Run.class, DBQuery.exists("properties.libProcessTypeCodes")).toList();
//		for (Run runDB : runs) {
//			readSets = MongoDBDAO.find("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class, DBQuery.is("runCode", runDB.code).exists("treatments.ngsrg").exists("treatments.taxonomy")).toList();
//			if(readSets.size()>0){
//				break;
//			}
//		}
//		//get JSON Run to insert
//		//List<ReadSet> readSets  = MongoDBDAO.find("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class).toList();
//		readSet = readSets.remove(0);
//		readSet.treatments= new HashMap<>();
//		MongoDBDAO.save(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet);
//	}
//	
//	@AfterClass
//	public static void deleteData()	{
//		if (MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code) != null) {
//			MongoDBDAO.deleteByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
//		}
//	}
	
	private static class TestContext implements CC1Managed {
		
		List<ReadSet> readSets;
		ReadSet readSet;

		@Override
		public void setUp() {	
			List<Run> runs  = MongoDBDAO.find("ngl_bi.RunIllumina_dataWF", Run.class, DBQuery.exists("properties.libProcessTypeCodes")).toList();
			for (Run runDB : runs) {
				readSets = MongoDBDAO.find("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class, DBQuery.is("runCode", runDB.code).exists("treatments.ngsrg").exists("treatments.taxonomy")).toList();
				if(readSets.size()>0){
					break;
				}
			}
			//get JSON Run to insert
			//List<ReadSet> readSets  = MongoDBDAO.find("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class).toList();
			readSet = readSets.remove(0);
			readSet.treatments= new HashMap<>();
			MongoDBDAO.save(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet);
		}

		@Override
		public void tearDown()	{
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
			logger.debug("update Archive");
			//Get Treatment ngsrg
			ctx.readSet.archiveId = "testArchive";
			ws.putObject("/api/archives/readsets/"+ctx.readSet.code, ctx.readSet, 200);
			ctx.readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, ctx.readSet.code);
			assertThat(ctx.readSet.archiveId).isEqualTo("testArchive");
		},
		test2Impl = test1Impl.andThen((app,ws,ctx) -> {
			logger.debug("list Archive");
			WSResponse response = ws.get("/api/archives/readsets", 200);
			assertThat(response.asJson()).isNotNull();			
		});
	
		
	@Test public void test1update() throws Exception { af.accept(test1Impl); }
	@Test public void test2list()   throws Exception { af.accept(test2Impl); }
	
//	@Test
//	public void test1update() {
//		logger.debug("update Archive");
//		//Get Treatment ngsrg
//		readSet.archiveId="testArchive";
////		WSHelper.putObject(ws, "/api/archives/readsets/"+readSet.code, readSet, 200);
//		ws.putObject("/api/archives/readsets/"+readSet.code, readSet, 200);
//		readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
//		assertThat(readSet.archiveId).isEqualTo("testArchive");
//	}

//		@Test
//	public void test2list()	{
//		logger.debug("list Archive");
////		WSResponse response = WSHelper.get(ws, "/api/archives/readsets", 200);
//		WSResponse response = ws.get("/api/archives/readsets", 200);
//		assertThat(response.asJson()).isNotNull();
//	}
		
}
