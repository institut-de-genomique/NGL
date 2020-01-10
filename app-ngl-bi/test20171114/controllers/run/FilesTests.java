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
import models.laboratory.run.instance.File;
import models.laboratory.run.instance.Lane;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import play.mvc.Result;
import utils.AbstractTestsCNG;
import utils.RunMockHelper;
import fr.cea.ig.MongoDBDAO;

public class FilesTests extends AbstractTestsCNG{
	
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
	 public void testFileCreate() {
			Run runDelete = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code","YANN_TEST1FORREADSET2"));
			if(runDelete!=null){
				MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runDelete._id);
			}
			ReadSet readSetDelete = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class,DBQuery.is("code","rdCode"));
			if(readSetDelete!=null){
				MongoDBDAO.delete(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSetDelete._id);
			}
			ReadSet readSet = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class,DBQuery.is("fullname","newfiletest"));
			if(readSet!=null){
				MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME,  ReadSet.class, DBQuery.is("code","rdCode"), DBUpdate.unset("files"));
				MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME,  ReadSet.class, DBQuery.is("code","rdCode"), DBUpdate.pull("files",null));	
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
		
			Run run = RunMockHelper.newRun("YANN_TEST1FORREADSET2");
			run.dispatch = true; 
				
			ReadSet rd = RunMockHelper.newReadSet("rdCode"); 
			rd.runCode = run.code;
			
			Lane lane = RunMockHelper.newLane(1);
			lane.readSetCodes = null;
			
			Lane lane2 = RunMockHelper.newLane(2);
			List<Lane> lanes = new ArrayList<Lane>();
			lanes.add(lane);
			lanes.add(lane2);
			run.lanes = lanes;
			 
			Result result = callAction(controllers.runs.api.routes.ref.Runs.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
	        assertThat(status(result)).isEqualTo(OK);
	        
	        result = callAction(controllers.readsets.api.routes.ref.ReadSets.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonReadSet(rd)));
	        assertThat(status(result)).isEqualTo(OK);
	        
	        File file =  RunMockHelper.newFile("newfiletest");
	        List<File> files =  new ArrayList<File>();
	        files.add(file);
	        rd.files = files; 
			
			result = callAction(controllers.readsets.api.routes.ref.Files.save("rdCode"),fakeRequest().withJsonBody(RunMockHelper.getJsonFile(file)));
		 	
	        assertThat(status(result)).isEqualTo(OK);
	        assertThat(contentType(result)).isEqualTo("application/json");
	        assertThat(charset(result)).isEqualTo("utf-8");
	        
		    //query for control
	        readSet = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class,DBQuery.is("files.fullname",file.fullname));
	        assertThat(readSet.files.size()).isEqualTo(1);
	        assertThat(readSet.files.get(0).fullname).isEqualTo(file.fullname);
	 }
	
	
	@Test
	 public void testFileExtensionUpdate() {
			Run runDelete = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code","YANN_TEST1FORREADSET2"));
			if(runDelete!=null){
				MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runDelete._id);
			}
			ReadSet readSetDelete = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class,DBQuery.is("code","rdCode"));
			if(readSetDelete!=null){
				MongoDBDAO.delete(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSetDelete._id);
			}
			ReadSet fileDelete = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class,DBQuery.is("fullname","newfiletest"));
			if(fileDelete!=null){
				MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME,  ReadSet.class, DBQuery.is("code","rdCode"), DBUpdate.unset("files"));
				MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME,  ReadSet.class, DBQuery.is("code","rdCode"), DBUpdate.pull("files",null));	
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
			
			Run run = RunMockHelper.newRun("YANN_TEST1FORREADSET2");
			run.dispatch = true; // For the archive test
			Lane lane = RunMockHelper.newLane(1);
			lane.readSetCodes = null;	
			
			ReadSet rd = RunMockHelper.newReadSet("rdCode");
			rd.runCode = run.code;
			
			Lane lane2 = RunMockHelper.newLane(2);
			List<Lane> lanes = new ArrayList<Lane>();
			lanes.add(lane);
			lanes.add(lane2);
			run.lanes = lanes;
			 
			Result result = callAction(controllers.runs.api.routes.ref.Runs.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
	        assertThat(status(result)).isEqualTo(OK);
	        
	        result = callAction(controllers.readsets.api.routes.ref.ReadSets.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonReadSet(rd)));
	        assertThat(status(result)).isEqualTo(OK);
	        
	        File file =  RunMockHelper.newFile("newfiletest");
	        file.extension = "DOC";
	        List<File> files =  new ArrayList<File>();
	        files.add(file);
	        rd.files = files; 
			
			result = callAction(controllers.readsets.api.routes.ref.Files.save("rdCode"),fakeRequest().withJsonBody(RunMockHelper.getJsonFile(file)));
		 	
	        assertThat(status(result)).isEqualTo(OK);
	        assertThat(contentType(result)).isEqualTo("application/json");
	        assertThat(charset(result)).isEqualTo("utf-8");
	        
	        file.extension = "IMG";

		  	result = callAction(controllers.readsets.api.routes.ref.Files.update("rdCode", "newfiletest"),fakeRequest().withJsonBody(RunMockHelper.getJsonFile(file)));
		  	assertThat(status(result)).isEqualTo(OK);
		  	assertThat(contentType(result)).isEqualTo("application/json");
		  	assertThat(charset(result)).isEqualTo("utf-8");
	      
		    //query for control
	        //ReadSet readSet = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class,DBQuery.is("files.fullname",file.fullname));
		  	//assertThat(readSet.files.size()).isEqualTo(1);
		  	//assertThat(readSet.files.get(0).extension).isEqualTo(file.extension );
	 }
	
	 
	 @Test
	 public void testFileShow() {
			Run runDelete = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code","YANN_TEST1FORREADSET2"));
			if(runDelete!=null){
				MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runDelete._id);
			}
			ReadSet readSetDelete = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class,DBQuery.is("code","rdCode"));
			if(readSetDelete!=null){
				MongoDBDAO.delete(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSetDelete._id);
			}
			ReadSet fileDelete = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class,DBQuery.is("fullname","newfiletest"));
			if(fileDelete!=null){
				MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME,  ReadSet.class, DBQuery.is("code","rdCode"), DBUpdate.unset("files"));
				MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME,  ReadSet.class, DBQuery.is("code","rdCode"), DBUpdate.pull("files",null));	
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
		
			Run run = RunMockHelper.newRun("YANN_TEST1FORREADSET2");
			run.dispatch = true; // For the archive test
			Lane lane = RunMockHelper.newLane(1);
			lane.readSetCodes = null;	
			
			ReadSet rd = RunMockHelper.newReadSet("rdCode"); 
			rd.runCode = run.code;
			
			Lane lane2 = RunMockHelper.newLane(2);
			List<Lane> lanes = new ArrayList<Lane>();
			lanes.add(lane);
			lanes.add(lane2);
			run.lanes = lanes;
			 
			Result result = callAction(controllers.runs.api.routes.ref.Runs.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
	        assertThat(status(result)).isEqualTo(OK);
	        
	        result = callAction(controllers.readsets.api.routes.ref.ReadSets.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonReadSet(rd)));
	        assertThat(status(result)).isEqualTo(OK);
	        
	        File file =  RunMockHelper.newFile("newfiletest");
	        List<File> files =  new ArrayList<File>();
	        files.add(file);
	        rd.files = files; 
			
			result = callAction(controllers.readsets.api.routes.ref.Files.save("rdCode"),fakeRequest().withJsonBody(RunMockHelper.getJsonFile(file)));
		 	
	        assertThat(status(result)).isEqualTo(OK);
	        assertThat(contentType(result)).isEqualTo("application/json");
	        assertThat(charset(result)).isEqualTo("utf-8");
		 
		 
		    // specific code
			  result = callAction(controllers.readsets.api.routes.ref.Files.get("rdCode","newfiletest"),fakeRequest());
		      assertThat(status(result)).isEqualTo(OK);
		      assertThat(contentType(result)).isEqualTo("application/json");
		      assertThat(charset(result)).isEqualTo("utf-8");
	 }
	 
	 
	 @Test
	 public void testDeleteFile(){
			Run runDelete = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code","YANN_TEST1FORREADSET2"));
			if(runDelete!=null){
				MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runDelete._id);
			}
			ReadSet readSetDelete = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class,DBQuery.is("code","rdCode"));
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

		
			Run run = RunMockHelper.newRun("YANN_TEST1FORREADSET2");
			run.dispatch = true; // For the archive test
			Lane lane = RunMockHelper.newLane(1);
			lane.readSetCodes = null;	
			Lane lane2 = RunMockHelper.newLane(2);
			List<Lane> lanes = new ArrayList<Lane>();
			lanes.add(lane);
			lanes.add(lane2);
			run.lanes = lanes;
			
			ReadSet rd = RunMockHelper.newReadSet("rdCode");
			rd.runCode = run.code;
			
			
			Result result = callAction(controllers.runs.api.routes.ref.Runs.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
	        assertThat(status(result)).isEqualTo(OK);
	        
	        result = callAction(controllers.readsets.api.routes.ref.ReadSets.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonReadSet(rd)));
	        assertThat(status(result)).isEqualTo(OK);
	        
	        File file =  RunMockHelper.newFile("newfiletest");
	        List<File> files =  new ArrayList<File>();
	        files.add(file);
	        
			result = callAction(controllers.readsets.api.routes.ref.Files.save("rdCode"),fakeRequest().withJsonBody(RunMockHelper.getJsonFile(file)));
		 	
	        assertThat(status(result)).isEqualTo(OK);
	        assertThat(contentType(result)).isEqualTo("application/json");
	        assertThat(charset(result)).isEqualTo("utf-8");
		 
	        // specific code
			 Result result2 = callAction(controllers.readsets.api.routes.ref.Files.delete("rdCode","newfiletest"),fakeRequest());
	         assertThat(status(result2)).isEqualTo(OK);
	         
	         
	 	    //query for control
	         //ReadSet readSet = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class,DBQuery.is("code","rdCode"));
	         //Logger.debug(String.valueOf(readSet.files.size()));
	         //assertThat(readSet.files.size()).isEqualTo(0);
	 }
	 
	 
	 @Test
	 public void testRemoveFiles(){
			Run runDelete = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code","YANN_TEST1FORREADSET2"));
			if(runDelete!=null){
				MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runDelete._id);
			}
			ReadSet readSetDelete = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class,DBQuery.is("code","rdCode"));
			if(readSetDelete!=null){
				MongoDBDAO.delete(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSetDelete._id);
			}
			ReadSet fileDelete = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class,DBQuery.is("fullname","newfiletest"));
			if(fileDelete!=null){
				MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME,  ReadSet.class, DBQuery.is("code","rdCode"), DBUpdate.unset("files"));
				MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME,  ReadSet.class, DBQuery.is("code","rdCode"), DBUpdate.pull("files",null));	
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
		
			Run run = RunMockHelper.newRun("YANN_TEST1FORREADSET2");
			run.dispatch = true; // For the archive test
			Lane lane = RunMockHelper.newLane(1);
			lane.readSetCodes = null;
			
			ReadSet rd = RunMockHelper.newReadSet("rdCode");
			rd.runCode = run.code;
			
			Lane lane2 = RunMockHelper.newLane(2);
			List<Lane> lanes = new ArrayList<Lane>();
			lanes.add(lane);
			lanes.add(lane2);
			run.lanes = lanes;
			 
			Result result = callAction(controllers.runs.api.routes.ref.Runs.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
	        assertThat(status(result)).isEqualTo(OK);
	        
	        result = callAction(controllers.readsets.api.routes.ref.ReadSets.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonReadSet(rd)));
	        assertThat(status(result)).isEqualTo(OK);
	        
	        File file =  RunMockHelper.newFile("newfiletest");
	        File file2 =  RunMockHelper.newFile("newfiletest2");
	        List<File> files =  new ArrayList<File>();
	        files.add(file);
	        files.add(file2);
	        rd.files = files; 
			
			result = callAction(controllers.readsets.api.routes.ref.Files.save("rdCode"),fakeRequest().withJsonBody(RunMockHelper.getJsonFile(file)));
	        assertThat(status(result)).isEqualTo(OK);
	        
			result = callAction(controllers.readsets.api.routes.ref.Files.save("rdCode"),fakeRequest().withJsonBody(RunMockHelper.getJsonFile(file2)));
	        assertThat(status(result)).isEqualTo(OK);
	 
		     // specific code
			 result = callAction(controllers.readsets.api.routes.ref.Files.deleteByRunCode("YANN_TEST1FORREADSET2"),fakeRequest());
	         assertThat(status(result)).isEqualTo(OK);
	         
		 	    //query for control
	         //ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class,rd.code);
	         //assertThat(readSet.files).isNull();
	 }
	 
}
