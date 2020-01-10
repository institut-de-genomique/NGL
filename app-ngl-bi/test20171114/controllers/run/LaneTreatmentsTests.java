package controllers.run;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.callAction;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.container.instance.LocationOnContainerSupport;
import models.laboratory.run.instance.Lane;
import models.laboratory.run.instance.Run;
import models.laboratory.run.instance.Treatment;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mongojack.DBQuery;

import play.Logger;
import play.mvc.Result;
import utils.AbstractTestsCNG;
import utils.RunMockHelper;
import fr.cea.ig.MongoDBDAO;

public class LaneTreatmentsTests extends AbstractTestsCNG {
	
	static Container c;
	
	@BeforeClass
	public static void initData() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		ContainerSupport cs = new ContainerSupport();
		cs.code = "containerName";
		cs.categoryCode = "lane";
		   
		MongoDBDAO.save(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, cs);
		
	   Container c = new Container();
	   c.code ="containerTest1";
	   c.support = new LocationOnContainerSupport(); 
	   c.support.code = cs.code; 
	   
	   MongoDBDAO.save(InstanceConstants.CONTAINER_COLL_NAME, c);
	}
	
	
	@AfterClass
	public static void deleteData(){
		List<ContainerSupport> containerSupports = MongoDBDAO.find(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class).toList();
		for (ContainerSupport cs : containerSupports) {
			if (cs.code.equals("containerName")) {
				MongoDBDAO.delete(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, cs);
			}
		}
		List<Container> containers = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class).toList();
		for (Container container : containers) {
			MongoDBDAO.delete(InstanceConstants.CONTAINER_COLL_NAME, container);
		}
		List<Sample> samples = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class).toList();
		for (Sample sample : samples) {
			MongoDBDAO.delete(InstanceConstants.SAMPLE_COLL_NAME, sample);
		}
	}
	
	private void createRunWithLaneCode() {
		Run runDelete = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code","DIDIER_TESTFORTRT"));
		if(runDelete!=null){
			MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runDelete._id);
		}
		Run run = RunMockHelper.newRun("DIDIER_TESTFORTRT");
		run.dispatch = true; // For the archive test
		Lane lane = RunMockHelper.newLane(1);
		Lane lane2 = RunMockHelper.newLane(2);
		List<Lane> lanes = new ArrayList<Lane>();		
		lanes.add(lane);
		lanes.add(lane2);
		run.lanes = lanes;
		Result result = callAction(controllers.runs.api.routes.ref.Runs.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
		assertThat(status(result)).isEqualTo(OK);
	}
	
	private Treatment getNewTreatmentForLane() {
		Treatment t = new Treatment();
		t.code =  "ngsrg";		
		t.typeCode = "ngsrg-illumina";
		t.categoryCode = "ngsrg";
		//define map of single property values
		Map<String,PropertyValue> m = new HashMap<String,PropertyValue>();
		m.put("nbCycleRead1", new PropertySingleValue(100)); // valeur simple
		m.put("nbCycleReadIndex1", new PropertySingleValue(100));
		m.put("nbCycleRead2", new PropertySingleValue(100));
		m.put("nbCycleReadIndex2", new PropertySingleValue(100));
		m.put("nbCluster", new PropertySingleValue(100000));
		m.put("nbBaseInternalAndIlluminaFilter", new PropertySingleValue(153654));
		m.put("phasing", new PropertySingleValue("OK"));
		m.put("prephasing", new PropertySingleValue("OK"));
		m.put("nbClusterInternalAndIlluminaFilter", new PropertySingleValue(75894));
		m.put("nbClusterIlluminaFilter", new PropertySingleValue(546723));
		m.put("percentClusterIlluminaFilter", new PropertySingleValue(50.03));
		m.put("percentClusterInternalAndIlluminaFilter", new PropertySingleValue(52.12));
		t.set("default", m);
		
		return t;
	}
	
	
	@Test
	public void testSave() {
		    createRunWithLaneCode();
			    	 
			Treatment t = getNewTreatmentForLane();
			
			Result result = callAction(controllers.runs.api.routes.ref.LaneTreatments.save("DIDIER_TESTFORTRT", 1),fakeRequest().withJsonBody(RunMockHelper.getJsonTreatment(t)));
			Logger.debug(contentAsString(result));
			assertThat(status(result)).isEqualTo(OK);
			
			//query for control
	        Run r = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, DBQuery.is("code","DIDIER_TESTFORTRT"));
	        assertThat(r.lanes.get(0).treatments.size()).isEqualTo(1);
	        Map.Entry<String, Treatment> entry = r.lanes.get(0).treatments.entrySet().iterator().next();
	        assertThat(entry.getKey()).isEqualTo("ngsrg");
	}
	
	
	@Test
	public void testUpdate() { 
		    createRunWithLaneCode();
			    	 
			Treatment t = getNewTreatmentForLane();
			
			Result result = callAction(controllers.runs.api.routes.ref.LaneTreatments.save("DIDIER_TESTFORTRT", 1),fakeRequest().withJsonBody(RunMockHelper.getJsonTreatment(t)));
			assertThat(status(result)).isEqualTo(OK);
			
			//define map of single property values
			Map<String,PropertyValue> m2 = new HashMap<String,PropertyValue>();
			m2.put("nbCycleRead1", new PropertySingleValue(80)); // valeur simple
			m2.put("nbCycleReadIndex1", new PropertySingleValue(120));
			m2.put("nbCycleRead2", new PropertySingleValue(130));
			m2.put("nbCycleReadIndex2", new PropertySingleValue(77));
			m2.put("nbCluster", new PropertySingleValue(92929));
			m2.put("nbBaseInternalAndIlluminaFilter", new PropertySingleValue(123456));
			m2.put("phasing", new PropertySingleValue("KO"));
			m2.put("prephasing", new PropertySingleValue("MOYEN"));
			m2.put("nbClusterInternalAndIlluminaFilter", new PropertySingleValue(654321));
			m2.put("nbClusterIlluminaFilter", new PropertySingleValue(123987));
			m2.put("percentClusterIlluminaFilter", new PropertySingleValue(1.23));
			m2.put("percentClusterInternalAndIlluminaFilter", new PropertySingleValue(2.345));
			
			t.results().remove("default");
			t.set("default", m2);
			
			result = callAction(controllers.runs.api.routes.ref.LaneTreatments.update("DIDIER_TESTFORTRT", 1, t.code),fakeRequest().withJsonBody(RunMockHelper.getJsonTreatment(t)));
			assertThat(status(result)).isEqualTo(OK);
			
			//query for control
	        Run r = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, DBQuery.is("code","DIDIER_TESTFORTRT"));
	        assertThat(r.lanes.get(0).treatments.size()).isEqualTo(1);
	        Map.Entry<String, Treatment> entry = r.lanes.get(0).treatments.entrySet().iterator().next();
	        assertThat(entry.getKey()).isEqualTo("ngsrg");
	        assertThat(entry.getValue().results().get("default").get("nbCycleReadIndex1").value.toString()).isEqualTo("120");
	}
	
	
	
	@Test
	public void testDelete() { 
		    createRunWithLaneCode();
			    	 
			Treatment t = getNewTreatmentForLane();
			
			Result result = callAction(controllers.runs.api.routes.ref.LaneTreatments.save("DIDIER_TESTFORTRT", 1),fakeRequest().withJsonBody(RunMockHelper.getJsonTreatment(t)));
			assertThat(status(result)).isEqualTo(OK);
			
			
			result = callAction(controllers.runs.api.routes.ref.LaneTreatments.delete("DIDIER_TESTFORTRT", 1, t.code),fakeRequest().withJsonBody(RunMockHelper.getJsonTreatment(t)));
			assertThat(status(result)).isEqualTo(OK);
			
			//query for control
	        Run r = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, DBQuery.is("code","DIDIER_TESTFORTRT"));
	        assertThat(r.lanes.get(0).treatments.size()).isEqualTo(0);
	}
	
	
	@Test
	public void testGet() { 
		    createRunWithLaneCode();
			    	 
			Treatment t = getNewTreatmentForLane();
			
			Result result = callAction(controllers.runs.api.routes.ref.LaneTreatments.save("DIDIER_TESTFORTRT", 1),fakeRequest().withJsonBody(RunMockHelper.getJsonTreatment(t)));
			assertThat(status(result)).isEqualTo(OK);
			
			result = callAction(controllers.runs.api.routes.ref.LaneTreatments.get("DIDIER_TESTFORTRT", 1, t.code),fakeRequest().withJsonBody(RunMockHelper.getJsonTreatment(t)));
			assertThat(status(result)).isEqualTo(OK);
	}
	
	
	@Test
	public void testHead() {
		    createRunWithLaneCode();
			    	 
			Treatment t = getNewTreatmentForLane();
	
			Result result = callAction(controllers.runs.api.routes.ref.LaneTreatments.save("DIDIER_TESTFORTRT", 1),fakeRequest().withJsonBody(RunMockHelper.getJsonTreatment(t)));
			assertThat(status(result)).isEqualTo(OK);
			
			result = callAction(controllers.runs.api.routes.ref.LaneTreatments.head("DIDIER_TESTFORTRT", 1, t.code),fakeRequest().withJsonBody(RunMockHelper.getJsonTreatment(t)));
			assertThat(status(result)).isEqualTo(OK);
	}
	
	
}

