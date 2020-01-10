package controllers.run;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.callAction;
import static play.test.Helpers.charset;
import static play.test.Helpers.contentType;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.status;

import java.util.ArrayList;
import java.util.List;

import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.container.instance.LocationOnContainerSupport;
import models.laboratory.project.instance.Project;
import models.laboratory.run.instance.Lane;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mongojack.DBQuery;

import play.mvc.Result;
import utils.AbstractTestsCNG;
import utils.RunMockHelper;
import fr.cea.ig.MongoDBDAO;


public class LanesTests extends AbstractTestsCNG {
	
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
	
	@Test
	public void testAddLaneOnARunWithALaneOK() {
			Run runDelete = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code","YANN_TEST1FORLANES"));
			if(runDelete!=null){
				MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runDelete._id);
			}
			Run run = RunMockHelper.newRun("YANN_TEST1FORLANES");
			Lane lane = RunMockHelper.newLane(1);
			List<Lane> lanes = new ArrayList<Lane>();
		 	lanes.add(lane);
			run.lanes = lanes;
			
			Result result = callAction(controllers.runs.api.routes.ref.Runs.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
		 	assertThat(status(result)).isEqualTo(OK);
		 			
			lane = RunMockHelper.newLane(2);
		 	lanes.add(lane);
			run.lanes = lanes;
		 	
		 	result = callAction(controllers.runs.api.routes.ref.Lanes.save(run.code),fakeRequest().withJsonBody(RunMockHelper.getJsonLane(lane)));
		    assertThat(status(result)).isEqualTo(OK);  
		    
		    //query for control
	        run = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code",run.code));
	        assertThat(run.lanes.size()).isEqualTo(2);
	}
	 
	 @Test
	 public void testAddLaneOnAEmptyRunOK(){
			Run runDelete = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code","YANN_TEST1FORLANES"));
			if(runDelete!=null){
				MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runDelete._id);
			}
			Run run = RunMockHelper.newRun("YANN_TEST1FORLANES");
			Result result = callAction(controllers.runs.api.routes.ref.Runs.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
			assertThat(run).isNotNull(); 
		 	assertThat(status(result)).isEqualTo(OK);
	        
		 	Lane lane = RunMockHelper.newLane(1);
		 	result = callAction(controllers.runs.api.routes.ref.Lanes.save(run.code),fakeRequest().withJsonBody(RunMockHelper.getJsonLane(lane)));
		 	
		 	assertThat(lane).isNotNull(); 
		    assertThat(status(result)).isEqualTo(OK);
	        assertThat(contentType(result)).isEqualTo("application/json");
	        assertThat(charset(result)).isEqualTo("utf-8");
	        
		    //query for control
	        run = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code",run.code));
	        assertThat(run.lanes.size()).isEqualTo(1);
		}
	 
	 
	 @Test
	 public void testAddLaneKO(){
		    //verify that we can't save lanes which refers to readsets which don't exist ! (test KO)
			Run runDelete = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code","YANN_TEST1FORLANES"));
			if(runDelete!=null){
				MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runDelete._id);
			}
			ReadSet readSetDelete = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code","ReadSetLanes2"));
			if(readSetDelete!=null){
				MongoDBDAO.delete(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSetDelete._id);
			}	
			readSetDelete = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code","ReadSetLanes3"));
			if(readSetDelete!=null){
				MongoDBDAO.delete(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSetDelete._id);
			}
			readSetDelete = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code","ReadSetLanes4"));
			if(readSetDelete!=null){
				MongoDBDAO.delete(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSetDelete._id);
			}
			Sample sample = MongoDBDAO.findOne(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.is("code","SampleCode"));
			if (sample!= null) {
				MongoDBDAO.delete(InstanceConstants.SAMPLE_COLL_NAME, Sample.class,sample._id);
			}
			Project project = MongoDBDAO.findOne(InstanceConstants.PROJECT_COLL_NAME, Project.class, DBQuery.is("code","ProjectCode"));
			if (project!= null) {
				MongoDBDAO.delete(InstanceConstants.PROJECT_COLL_NAME, Project.class, project._id);
			}
			
			sample = RunMockHelper.newSample("SampleCode");
			project = RunMockHelper.newProject("ProjectCode");
			
			MongoDBDAO.save(InstanceConstants.SAMPLE_COLL_NAME, sample);
			MongoDBDAO.save(InstanceConstants.PROJECT_COLL_NAME, project);
			
			Run run = RunMockHelper.newRun("YANN_TEST1FORLANES");
		 	Result result = callAction(controllers.runs.api.routes.ref.Runs.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
		 	assertThat(status(result)).isEqualTo(OK);
		
			Lane lane = RunMockHelper.newLane(1);
			ReadSet r = RunMockHelper.newReadSet("ReadSetLanes2");
			r.runCode = run.code;
			ReadSet r2 = RunMockHelper.newReadSet("ReadSetLanes3");
			r2.runCode = run.code;
			ReadSet r3 = RunMockHelper.newReadSet("ReadSetLanes4");
			r3.runCode = run.code;
			
			List<String> a = new ArrayList<String>();
			a.add(r.code);
			a.add(r2.code);
			lane.readSetCodes = a;
			
			List<Lane> c = new ArrayList<Lane>();
			c.add(lane);
			run.lanes = c;
		 	
			result = callAction(controllers.runs.api.routes.ref.Lanes.save(run.code),fakeRequest().withJsonBody(RunMockHelper.getJsonLane(lane)));
			assertThat(status(result)).isEqualTo(play.mvc.Http.Status.BAD_REQUEST);
			
			//query for control
	        run = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code",run.code));
	        assertThat(run.lanes).isNull(); 
		}
	 
	 
	 @Test
	 public void testAddLaneAddReadSetOK(){
		    //verify that we can save lanes because readsets exist (test OK) !
			Run runDelete = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code","YANN_TEST1FORLANES"));
			if(runDelete!=null){
				MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runDelete._id);
			}
			ReadSet readSetDelete = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code","ReadSetLanes2"));
			if(readSetDelete!=null){
				MongoDBDAO.delete(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSetDelete._id);
			}	
			readSetDelete = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code","ReadSetLanes3"));
			if(readSetDelete!=null){
				MongoDBDAO.delete(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSetDelete._id);
			}
			Sample sample = MongoDBDAO.findOne(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.is("code","SampleCode"));
			if (sample!= null) {
				MongoDBDAO.delete(InstanceConstants.SAMPLE_COLL_NAME, Sample.class,sample._id);
			}
			Project project = MongoDBDAO.findOne(InstanceConstants.PROJECT_COLL_NAME, Project.class, DBQuery.is("code","ProjectCode"));
			if (project!= null) {
				MongoDBDAO.delete(InstanceConstants.PROJECT_COLL_NAME, Project.class, project._id);
			}
			
			sample = RunMockHelper.newSample("SampleCode");
			project = RunMockHelper.newProject("ProjectCode");
			
			MongoDBDAO.save(InstanceConstants.SAMPLE_COLL_NAME, sample);
			MongoDBDAO.save(InstanceConstants.PROJECT_COLL_NAME, project);
			
			Run run = RunMockHelper.newRun("YANN_TEST1FORLANES");
		 	Result result = callAction(controllers.runs.api.routes.ref.Runs.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
		 	assertThat(status(result)).isEqualTo(OK);
		
			Lane lane = RunMockHelper.newLane(1);
			ReadSet r = RunMockHelper.newReadSet("ReadSetLanes2");
			r.runCode = run.code;
			ReadSet r2 = RunMockHelper.newReadSet("ReadSetLanes3");
			r2.runCode = run.code;
			
			List<String> a = new ArrayList<String>();
			a.add(r.code);
			a.add(r2.code);
			//lane.readSetCodes = a;
						
			List<Lane> c = new ArrayList<Lane>();
			c.add(lane);
			run.lanes = c;
			
			result = callAction(controllers.runs.api.routes.ref.Lanes.save(run.code),fakeRequest().withJsonBody(RunMockHelper.getJsonLane(lane)));
			assertThat(status(result)).isEqualTo(OK);
			
			result =  callAction(controllers.readsets.api.routes.ref.ReadSets.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonReadSet(r)));
			assertThat(status(result)).isEqualTo(OK);
			
			result =  callAction(controllers.readsets.api.routes.ref.ReadSets.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonReadSet(r2)));
			assertThat(status(result)).isEqualTo(OK);
			
			//query for control
	        run = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code",run.code));
	        assertThat(run.lanes.size()).isEqualTo(1);
		}
	 
	 
	  
	 @Test
	 public void testAdd2LanesWithSameLaneNumberKO(){
			Run runDelete = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code","YANN_TEST1FORLANES"));
			if(runDelete!=null){
				MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runDelete._id);
			}
			Run run = RunMockHelper.newRun("YANN_TEST1FORLANES");
			Result result = callAction(controllers.runs.api.routes.ref.Runs.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
			assertThat(run).isNotNull(); 
		 	assertThat(status(result)).isEqualTo(OK);
	        
		 	Lane lane = RunMockHelper.newLane(1);
		 	Lane lane2 = RunMockHelper.newLane(1);
		 	result = callAction(controllers.runs.api.routes.ref.Lanes.save(run.code),fakeRequest().withJsonBody(RunMockHelper.getJsonLane(lane)));
		 	
		 	assertThat(lane).isNotNull(); 
		    assertThat(status(result)).isEqualTo(OK);
	        assertThat(contentType(result)).isEqualTo("application/json");
	        assertThat(charset(result)).isEqualTo("utf-8");	        
	        
		 	result = callAction(controllers.runs.api.routes.ref.Lanes.save(run.code),fakeRequest().withJsonBody(RunMockHelper.getJsonLane(lane2)));
		    assertThat(status(result)).isNotEqualTo(OK);
	        
		    //query for control
	        run = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code",run.code));
	        assertThat(run.lanes.size()).isEqualTo(1);
	}
	 
	 
	 @Test
	 public void testAddLanesWithSameReadSetCodesKO(){
		    //verify that we can't save a lane witch refers to a readset on another lane !
			Run runDelete = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code","YANN_TEST1FORLANES"));
			if(runDelete!=null){
				MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runDelete._id);
			}
			ReadSet readSetDelete = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code","ReadSetLanes2"));
			if(readSetDelete!=null){
				MongoDBDAO.delete(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSetDelete._id);
			}	
			readSetDelete = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code","ReadSetLanes3"));
			if(readSetDelete!=null){
				MongoDBDAO.delete(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSetDelete._id);
			}
			Sample sample = MongoDBDAO.findOne(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.is("code","SampleCode"));
			if (sample!= null) {
				MongoDBDAO.delete(InstanceConstants.SAMPLE_COLL_NAME, Sample.class,sample._id);
			}
			Project project = MongoDBDAO.findOne(InstanceConstants.PROJECT_COLL_NAME, Project.class, DBQuery.is("code","ProjectCode"));
			if (project!= null) {
				MongoDBDAO.delete(InstanceConstants.PROJECT_COLL_NAME, Project.class, project._id);
			}
			
			sample = RunMockHelper.newSample("SampleCode");
			project = RunMockHelper.newProject("ProjectCode");
			
			MongoDBDAO.save(InstanceConstants.SAMPLE_COLL_NAME, sample);
			MongoDBDAO.save(InstanceConstants.PROJECT_COLL_NAME, project);
			
			Run run = RunMockHelper.newRun("YANN_TEST1FORLANES");
		 	Result result = callAction(controllers.runs.api.routes.ref.Runs.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
		 	assertThat(status(result)).isEqualTo(OK);
		
			Lane lane = RunMockHelper.newLane(1);
			Lane lane2 = RunMockHelper.newLane(2);
			ReadSet r = RunMockHelper.newReadSet("ReadSetLanes2");
			r.runCode = run.code;
			ReadSet r2 = RunMockHelper.newReadSet("ReadSetLanes3");
			r2.runCode = run.code;
			
			List<String> a = new ArrayList<String>();
			a.add(r.code);
			a.add(r2.code);
			//lane.readSetCodes = a;
			
			List<Lane> c = new ArrayList<Lane>();
			c.add(lane);
			run.lanes = c;
			
			result = callAction(controllers.runs.api.routes.ref.Lanes.save(run.code),fakeRequest().withJsonBody(RunMockHelper.getJsonLane(lane)));
			assertThat(status(result)).isEqualTo(OK);
			
			result =  callAction(controllers.readsets.api.routes.ref.ReadSets.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonReadSet(r)));
			assertThat(status(result)).isEqualTo(OK);
			
			result =  callAction(controllers.readsets.api.routes.ref.ReadSets.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonReadSet(r2)));
			assertThat(status(result)).isEqualTo(OK);
			
			lane2.readSetCodes = a; // the same readSet codes are affected to the lane2 !
			
			result = callAction(controllers.runs.api.routes.ref.Lanes.save(run.code),fakeRequest().withJsonBody(RunMockHelper.getJsonLane(lane2)));
			assertThat(status(result)).isNotEqualTo(OK);
			
			//query for control
	        run = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code",run.code));
	        assertThat(run.lanes.size()).isEqualTo(1);
		}
}