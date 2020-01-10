package workflows;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.callAction;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.status;

import java.util.List;
import java.util.TreeSet;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mongojack.DBQuery;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.run.instance.Lane;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.utils.InstanceConstants;
import play.Logger;
import play.api.libs.json.Reads;
import play.mvc.Result;
import utils.AbstractTestsCNS;
import utils.RunMockHelper;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RunWorkflowsTest extends AbstractTestsCNS{

	static Run run;
	static Run runInvalidLane;
	static ContainerSupport cs;
	static List<Container> containers;
	@BeforeClass
	public static void initData()
	{
		//get run to test
		List<Run> runs =  MongoDBDAO.find("ngl_bi.RunIllumina_dataWF", Run.class, DBQuery.is("state.code", "IP-RG").exists("lanes").exists("lanes.readSetCodes")).toList();
		run = runs.get(0);
		//Remove libProcessTypeCode for rule IP-S
		//Remove projectCodes/sampleCodes for rule IP-S
		run.properties.remove("libProcessTypeCodes");
		run.sampleCodes= new TreeSet<String>();
		run.projectCodes = new TreeSet<String>();
		
		//Get one readSet with treatment ngsrg
		ReadSet readSetNgsrg = MongoDBDAO.find("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class, DBQuery.exists("treatments.ngsrg"), getReadSetKeys()).limit(1).toList().get(0);
		readSetNgsrg = MongoDBDAO.findByCode("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class, readSetNgsrg.code);
		for(Lane lane :  run.lanes){
			if(lane.valuation.valid.equals(TBoolean.FALSE)){
				lane.valuation.valid=TBoolean.TRUE;
				lane.treatments.put("ngsrg", readSetNgsrg.treatments.get("ngsrg"));
			}
			//Save ReadSets
			List<ReadSet> readSets = MongoDBDAO.find("ngl_bi.ReadSetIllumina_dataWF", ReadSet.class, 
					DBQuery.and(DBQuery.is("runCode", run.code), DBQuery.is("laneNumber", lane.number)),getReadSetKeys()).toList();
			for(ReadSet readSet : readSets){
				readSet.treatments.put("ngsrg", readSetNgsrg.treatments.get("ngsrg"));
				MongoDBDAO.save(InstanceConstants.READSET_ILLUMINA_COLL_NAME,readSet);
			}
			
		}
		run.state.code="N";
		MongoDBDAO.save(InstanceConstants.RUN_ILLUMINA_COLL_NAME, run);
		
		runInvalidLane = runs.get(1);
		for(Lane lane : runInvalidLane.lanes){
			lane.valuation.valid=TBoolean.FALSE;
			//Save ReadSets
			List<ReadSet> readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
					DBQuery.and(DBQuery.is("runCode", runInvalidLane.code), DBQuery.is("laneNumber", lane.number)),getReadSetKeys()).toList();
			for(ReadSet readSet : readSets){
				readSet.treatments.put("ngsrg", readSetNgsrg.treatments.get("ngsrg"));
				lane.treatments.put("ngsrg", readSetNgsrg.treatments.get("ngsrg"));
				MongoDBDAO.save(InstanceConstants.READSET_ILLUMINA_COLL_NAME,readSet);
			}
		}
		MongoDBDAO.save(InstanceConstants.RUN_ILLUMINA_COLL_NAME, runInvalidLane);
		
		//Get containerSupport
		cs = MongoDBDAO.findByCode("ngl_sq.ContainerSupport_dataWF", ContainerSupport.class, run.containerSupportCode);
		MongoDBDAO.save(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, cs);
		//Get container
		containers = MongoDBDAO.find("ngl_sq.Container_dataWF", Container.class, DBQuery.is("support.code", run.containerSupportCode)).toList();
		for(Container c : containers){
			MongoDBDAO.save(InstanceConstants.CONTAINER_COLL_NAME, c);
		}
		
	}
	
	@AfterClass
	public static void deleteData()
	{
		//get run to test
		run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, run.code);
		MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, run);
		runInvalidLane = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runInvalidLane.code);
		MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, runInvalidLane);
		cs = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, cs.code);
		MongoDBDAO.delete(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, cs);
		for(Container c : containers){
			c = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, c.code);
			MongoDBDAO.delete(InstanceConstants.CONTAINER_COLL_NAME, c);
		}
	}
	
	@Test
	public void checkData()
	{
		Logger.debug("CheckData");
		run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, run.code);
		Logger.debug("state run "+run.state.code);
		assertThat(run).isNotNull();
		assertThat(run.state.code).isEqualTo("N");
	}
	
	@Test
	public void setStateIPS() throws InterruptedException
	{
		Logger.debug("setStateIPS");
		State state = new State("IP-S","bot");
		Result r = callAction(controllers.runs.api.routes.ref.State.update(run.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
		assertThat(status(r)).isEqualTo(OK);
	
		run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, run.code);
		Logger.debug("state code"+run.state.code);
		assertThat(run.state.code).isEqualTo("IP-S");
	}
	
	@Test
	public void setStateFS()
	{
		State state = new State("F-S","bot");
		Result r = callAction(controllers.runs.api.routes.ref.State.update(run.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
		assertThat(status(r)).isEqualTo(OK);
	
		run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, run.code);
		Logger.debug("state code"+run.state.code);
		assertThat(run.state.code).isEqualTo("IW-RG");
	}
	
	@Test
	public void setStateFRG()
	{
		State state = new State("F-RG","bot");
		Result r = callAction(controllers.runs.api.routes.ref.State.update(run.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
		assertThat(status(r)).isEqualTo(OK);
	
		run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, run.code);
		Logger.debug("state code"+run.state.code);
		assertThat(run.state.code).isEqualTo("IW-V");
		//Check dispatch
		assertThat(run.dispatch).isEqualTo(true);
		for(Lane lane : run.lanes){
			List<ReadSet> readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
					DBQuery.and(DBQuery.is("runCode", run.code), DBQuery.is("laneNumber", lane.number)),getReadSetKeys()).toList();
			for(ReadSet readSet : readSets){
				assertThat(readSet.state.code).isEqualTo("IW-QC");
			}
		}
		
		
	}
	
	@Test
	public void setStateFRGLaneValid()
	{
		State state = new State("F-RG","bot");
		Result r = callAction(controllers.runs.api.routes.ref.State.update(runInvalidLane.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
		assertThat(status(r)).isEqualTo(OK);
	
		runInvalidLane = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runInvalidLane.code);
		Logger.debug("state code"+runInvalidLane.state.code);
		for(Lane lane : runInvalidLane.lanes){
			List<ReadSet> readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
					DBQuery.and(DBQuery.is("runCode", runInvalidLane.code), DBQuery.is("laneNumber", lane.number)),getReadSetKeys()).toList();
			for(ReadSet readSet : readSets){
				assertThat(readSet.productionValuation.valid).isEqualTo(false);
				assertThat(readSet.productionValuation.resolutionCodes).contains("Run-abandonLane");
				assertThat(readSet.bioinformaticValuation.valid).isEqualTo(false);
				assertThat(readSet.state.code).isEqualTo("F-VQC");
			}
		}
	}
	
	@Test
	public void setStateFVLaneValid()
	{
		State state = new State("F-V","bot");
		Result r = callAction(controllers.runs.api.routes.ref.State.update(runInvalidLane.code),fakeRequest().withJsonBody(RunMockHelper.getJsonState(state)).withHeader("User-Agent", "bot"));
		assertThat(status(r)).isEqualTo(OK);
	
		runInvalidLane = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runInvalidLane.code);
		Logger.debug("state code"+runInvalidLane.state.code);
		for(Lane lane : runInvalidLane.lanes){
			List<ReadSet> readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
					DBQuery.and(DBQuery.is("runCode", runInvalidLane.code), DBQuery.is("laneNumber", lane.number)),getReadSetKeys()).toList();
			for(ReadSet readSet : readSets){
				assertThat(readSet.productionValuation.valid).isEqualTo(false);
				assertThat(readSet.productionValuation.resolutionCodes).contains("Run-abandonLane");
				assertThat(readSet.state.code).isEqualTo("F-VQC");
			}
		}
	}
	
	private static BasicDBObject getReadSetKeys() {
		BasicDBObject keys = new BasicDBObject();
		keys.put("treatments", 0);
		return keys;
	}
}
