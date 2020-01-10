package controllers.run;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.NOT_FOUND;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.callAction;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.container.instance.LocationOnContainerSupport;
import models.laboratory.project.instance.Project;
import models.laboratory.resolutions.instance.Resolution;
import models.laboratory.resolutions.instance.ResolutionCategory;
import models.laboratory.resolutions.instance.ResolutionConfiguration;
import models.laboratory.run.instance.Lane;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.laboratory.run.instance.SampleOnContainer;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import play.Logger;
import play.libs.Json;
import play.mvc.Result;
import utils.AbstractTestsCNS;
import utils.RunMockHelper;

public class ReadSetsTests extends AbstractTestsCNS {

	static Container c;
	Run run;
	ReadSet readset;
	ReadSet readset2;
	Lane lane;

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

		Sample sample = RunMockHelper.newSample("SampleCode");
		Project project = RunMockHelper.newProject("ProjectCode");

		MongoDBDAO.save(InstanceConstants.SAMPLE_COLL_NAME, sample);
		MongoDBDAO.save(InstanceConstants.PROJECT_COLL_NAME, project);
		
		Project projectInit = MongoDBDAO.findByCode("ngl_project.Project_init", Project.class, "BFB");
		MongoDBDAO.save(InstanceConstants.PROJECT_COLL_NAME, projectInit);
		
		ResolutionConfiguration resoConfig = new ResolutionConfiguration();
		resoConfig.code="conf-reso-run";
		resoConfig.objectTypeCode="Run";
		resoConfig.resolutions=new ArrayList<>();
		resoConfig.typeCodes=new ArrayList<>();
		resoConfig.typeCodes.add("RHS2000");
		ResolutionCategory resoCategory = new ResolutionCategory();
		resoCategory.name="Info";
		Resolution reso1 = new Resolution();
		reso1.code="reso1";
		reso1.name="reso1";
		reso1.displayOrder=1;
		reso1.level="default";
		reso1.category=resoCategory;
		resoConfig.resolutions.add(reso1);
		Resolution reso2 = new Resolution();
		reso2.code="reso2";
		reso2.name="reso2";
		reso2.displayOrder=2;
		reso2.level="default";
		reso2.category=resoCategory;
		resoConfig.resolutions.add(reso2);
		MongoDBDAO.save(InstanceConstants.RESOLUTION_COLL_NAME,resoConfig);


		resoConfig = new ResolutionConfiguration();
		resoConfig.code="conf-reso-readset";
		resoConfig.objectTypeCode="ReadSet";
		resoConfig.resolutions=new ArrayList<>();
		resoConfig.typeCodes=new ArrayList<>();
		resoConfig.typeCodes.add("default-readset");
		resoConfig.resolutions.add(reso1);
		resoConfig.resolutions.add(reso2);
		MongoDBDAO.save(InstanceConstants.RESOLUTION_COLL_NAME,resoConfig);
		
	}


	@AfterClass
	public static void deleteData(){
		ContainerSupport containerSupport = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, "containerName");
		if (containerSupport!=null)
			MongoDBDAO.deleteByCode(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, containerSupport.code);

		Container container = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, "containerTest1");
		if(container!=null)
			MongoDBDAO.deleteByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, container.code);

		//Sample sample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, "SampleCode");
		//if(sample!=null)
		MongoDBDAO.deleteByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, "SampleCode");
		
		MongoDBDAO.deleteByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, "BFB_AABA");

		//Project project = MongoDBDAO.findByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, "ProjectCode");
		//if (project!= null) {
		MongoDBDAO.deleteByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, "ProjectCode");
		//}
		
		MongoDBDAO.deleteByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, "BFB");
		
		ResolutionConfiguration resolutionConfig = MongoDBDAO.findByCode(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class, "conf-reso-run");
		if(resolutionConfig!=null)
			MongoDBDAO.deleteByCode(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class, "conf-reso-run");

		resolutionConfig = MongoDBDAO.findByCode(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class, "conf-reso-readset");
		if(resolutionConfig!=null)
			MongoDBDAO.deleteByCode(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class, "conf-reso-readset");
		
	}

	@Before
	public void createData()
	{
		// create a run with two readsets associated to this run
		run = RunMockHelper.newRun("YANN_TEST1FORREADSET");
		lane = RunMockHelper.newLane(1);
		Lane lane2 = RunMockHelper.newLane(2);
		List<Lane> lanes = new ArrayList<Lane>();

		readset = RunMockHelper.newReadSet("rdCode");
		readset.runCode = run.code;
		lanes.add(lane);
		lanes.add(lane2);
		run.lanes = lanes;

		readset2 = RunMockHelper.newReadSet("rdCode2");
		readset2.runCode = run.code;
	}

	@After
	public void removeData()
	{
		Run runDelete = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code","YANN_TEST1FORREADSET"));
		if(runDelete!=null){
			MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runDelete._id);
		}
		ReadSet readSetDelete = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class,DBQuery.is("code","rdCode"));
		if(readSetDelete!=null){
			MongoDBDAO.delete(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class,readSetDelete._id);
		}
		readSetDelete = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class,DBQuery.is("code","rdCode2"));
		if(readSetDelete!=null){
			MongoDBDAO.delete(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSetDelete._id);
		}
	}

	@Test
	public void testReadSetsCreate() { 
		// create a run with two readsets associated to this run
		run.dispatch = true; // For the archive test

		Result result = callAction(controllers.runs.api.routes.ref.Runs.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
		Logger.debug(contentAsString(result));
		Assert.assertEquals(OK, status(result));

		result = callAction(controllers.readsets.api.routes.ref.ReadSets.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonReadSet(readset)));
		Logger.debug(contentAsString(result));
		Assert.assertEquals(OK, status(result));

		result = callAction(controllers.readsets.api.routes.ref.ReadSets.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonReadSet(readset2)));
		Logger.debug(contentAsString(result));
		Assert.assertEquals(OK, status(result));

		//query for control
		List<ReadSet> lr = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class,DBQuery.in("code","rdCode", "rdCode2")).toList();
		Assert.assertEquals(2, lr.size());
	}


	@Test
	public void testArchiveReadSet() {
		run.dispatch = true; 

		Result result =callAction(controllers.runs.api.routes.ref.Runs.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
		Assert.assertEquals(OK, status(result));

		result = callAction(controllers.readsets.api.routes.ref.ReadSets.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonReadSet(readset)));
		Assert.assertEquals(OK, status(result));

		result = callAction(controllers.archives.api.routes.ref.ReadSets.save(readset.code),fakeRequest().withJsonBody(RunMockHelper.getArchiveJson("codeTestArchive")));
		Assert.assertEquals(OK, status(result));

		result = callAction(controllers.archives.api.routes.ref.ReadSets.save("ReadSetTESTNOTEXIT"),fakeRequest().withJsonBody(RunMockHelper.getArchiveJson("codeTestArchive")));
		Assert.assertEquals(NOT_FOUND, status(result));
		//query for control
		ReadSet r = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class,DBQuery.is("code",readset.code));
		Assert.assertNotNull(r);
		Assert.assertNotNull(r.archiveId);//means that this is a archive
	}


	@Test
	public void testAchiveList(){
		run.dispatch = true; 

		readset.laneNumber = 1;
		readset.dispatch = true;

		readset2.laneNumber = 1;
		readset2.dispatch = false;

		Result result =callAction(controllers.runs.api.routes.ref.Runs.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
		assertThat(status(result)).isEqualTo(OK);

		//save readset
		result = callAction(controllers.readsets.api.routes.ref.ReadSets.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonReadSet(readset)));
		assertThat(status(result)).isEqualTo(OK);

		//save readset2 
		result = callAction(controllers.readsets.api.routes.ref.ReadSets.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonReadSet(readset2)));
		assertThat(status(result)).isEqualTo(OK);

		// result = callAction(controllers.archives.api.routes.ref.ReadSets.list(),fakeRequest());
		// assertThat(status(result)).isEqualTo(OK);
		Logger.debug(contentAsString(result));
		assertThat(contentAsString(result)).isNotEqualTo("[]").contains(readset.code);
	}


	@Test
	public void testDeleteReadsets(){
		run.traceInformation = new TraceInformation();
		run.dispatch = true; // For the archive test

		Result result = callAction(controllers.runs.api.routes.ref.Runs.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
		Assert.assertEquals(OK, status(result));

		result = callAction(controllers.readsets.api.routes.ref.ReadSets.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonReadSet(readset)));
		Assert.assertEquals(OK, status(result));

		ArrayList<String> sCodes = new ArrayList<String>();
		sCodes.add(readset.code);
		lane.readSetCodes = sCodes; 
		result = callAction(controllers.runs.api.routes.ref.Lanes.update(run.code, lane.number),fakeRequest().withJsonBody(RunMockHelper.getJsonLane(lane)));
		Assert.assertEquals(OK, status(result));

		result = callAction(controllers.readsets.api.routes.ref.ReadSets.deleteByRunCode(run.code),fakeRequest());
		Assert.assertEquals(OK, status(result));

		//query for control
		run = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code",run.code));
		Assert.assertEquals(2, run.lanes.size());
		boolean b = (run.lanes.get(0).readSetCodes == null) || (run.lanes.get(0).readSetCodes.size() == 0); 
		Assert.assertTrue(b);

		//b = MongoDBDAO.checkObjectExist(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class,DBQuery.is("runCode",run.code));
		//assertThat(b).isEqualTo(false);  
	}


	@Test 
	public void testRemoveReadset(){

		run.state=null;
		run.dispatch = true; // For the archive test

		Result result = callAction(controllers.runs.api.routes.ref.Runs.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
		Logger.info(contentAsString(result));
		Assert.assertEquals(OK, status(result));

		result = callAction(controllers.readsets.api.routes.ref.ReadSets.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonReadSet(readset)));
		Assert.assertEquals(OK, status(result));

		result = callAction(controllers.readsets.api.routes.ref.ReadSets.delete(readset.code),fakeRequest());
		Assert.assertEquals(OK, status(result));	

		//query for control
		run = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code",run.code));
		Assert.assertEquals(2, run.lanes.size());
		Assert.assertTrue((run.lanes.get(0).readSetCodes).isEmpty());

		readset = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class,DBQuery.is("runCode",run.code));
		Assert.assertNull(readset);
	}
	
	
	@Test
	public void testSaveReadSetExternal()
	{
		Sample sample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, "BFB_AABA");
		Assert.assertNull(sample);
		
		Result result = callAction(controllers.runs.api.routes.ref.Runs.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
		Logger.debug(contentAsString(result));
		Assert.assertEquals(OK, status(result));

		readset.sampleCode="BFB_AABA";
		readset.projectCode="BFB";
		readset.sampleOnContainer=RunMockHelper.newSampleOnContainer(readset.sampleCode);
		
		result = callAction(controllers.readsets.api.routes.ref.ReadSets.save(),fakeRequest(play.test.Helpers.POST, "?external=true").withJsonBody(RunMockHelper.getJsonReadSet(readset)));
		Logger.debug(contentAsString(result));
		Assert.assertEquals(OK, status(result));
		
		//Check sample created
		sample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, "BFB_AABA");
		Assert.assertNotNull(sample);
		
		//Check sampleOnContainer created
		readset = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, "rdCode");
		Logger.debug("Sample on container "+readset.sampleOnContainer);
		Assert.assertNotNull(readset.sampleOnContainer);
		Assert.assertNotNull(readset.sampleOnContainer.referenceCollab);
		Assert.assertNotNull(readset.sampleOnContainer.sampleCategoryCode);

		
	}
	
	@Test
	public void testSaveReadSetExternalWithNoSampleLims()
	{
		Result result = callAction(controllers.runs.api.routes.ref.Runs.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
		Logger.debug(contentAsString(result));
		Assert.assertEquals(OK, status(result));

		readset.sampleCode="testSample";
		readset.projectCode="BFB";
		readset.sampleOnContainer=RunMockHelper.newSampleOnContainer(readset.sampleCode);
		
		result = callAction(controllers.readsets.api.routes.ref.ReadSets.save(),fakeRequest(play.test.Helpers.POST, "?external=true").withJsonBody(RunMockHelper.getJsonReadSet(readset)));
		Logger.debug(contentAsString(result));
		Assert.assertEquals(BAD_REQUEST, status(result));
		Assert.assertTrue(contentAsString(result).contains("No sample testSample in LIMS"));
		
	}
	
	@Test
	public void testSaveReadSetExternalWithNoSampleOnContainer()
	{
		Result result = callAction(controllers.runs.api.routes.ref.Runs.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
		Logger.debug(contentAsString(result));
		Assert.assertEquals(OK, status(result));

		readset.sampleCode="BFB_AABA";
		readset.projectCode="BFB";
		readset.sampleOnContainer=null;
		
		result = callAction(controllers.readsets.api.routes.ref.ReadSets.save(),fakeRequest(play.test.Helpers.POST, "?external=true").withJsonBody(RunMockHelper.getJsonReadSet(readset)));
		Logger.debug(contentAsString(result));
		Assert.assertEquals(BAD_REQUEST, status(result));
		Assert.assertTrue(contentAsString(result).contains("No sampleOnContainer for sample BFB_AABA"));
		
	}
	
	@Test
	public void testSaveReadSetExternalWithIncompleteSampleOnContainer()
	{
		Result result = callAction(controllers.runs.api.routes.ref.Runs.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
		Logger.debug(contentAsString(result));
		Assert.assertEquals(OK, status(result));

		readset.sampleCode="BFB_AABA";
		readset.projectCode="BFB";
		readset.sampleOnContainer=RunMockHelper.newSampleOnContainer("BFB_AABA");
		readset.sampleOnContainer.containerSupportCode=null;
		
		result = callAction(controllers.readsets.api.routes.ref.ReadSets.save(),fakeRequest(play.test.Helpers.POST, "?external=true").withJsonBody(RunMockHelper.getJsonReadSet(readset)));
		Logger.debug(contentAsString(result));
		Assert.assertEquals(BAD_REQUEST, status(result));
	}
	
	@Test
	public void testReadSetExternalStateFRG()
	{
	
		Logger.debug("Save run");
		Result result = callAction(controllers.runs.api.routes.ref.Runs.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
		Logger.debug(contentAsString(result));
		assertThat(status(result)).isEqualTo(OK);

		readset.sampleCode="BFB_AABA";
		readset.projectCode="BFB";
		readset.sampleOnContainer=RunMockHelper.newSampleOnContainer(readset.sampleCode);
		
		Logger.debug("Save readSet");
		result = callAction(controllers.readsets.api.routes.ref.ReadSets.save(),fakeRequest(play.test.Helpers.POST, "?external=true").withJsonBody(RunMockHelper.getJsonReadSet(readset)));
		Logger.debug(contentAsString(result));
		assertThat(status(result)).isEqualTo(OK);
		
		Logger.debug("Find readSet");
		readset = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readset.code);
		Assert.assertNotNull(readset);
		Logger.debug("Set state F-RG");
		State state = new State();
		state.code="F-RG";
		//Set state to F-RG
		result = callAction(controllers.readsets.api.routes.ref.ReadSets.state(readset.code),fakeRequest().withJsonBody(Json.toJson(state)));
		Logger.debug(contentAsString(result));
		assertThat(status(result)).isEqualTo(OK);
	}

	/**
	 * Test impossible du à la generation des getters/setters de play
	 */
	//@Test
	public void shouldUpdatePathReadSet()
	{
		Result result = callAction(controllers.runs.api.routes.ref.Runs.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
		Logger.info(contentAsString(result));
		assertThat(status(result)).isEqualTo(OK);

		result = callAction(controllers.readsets.api.routes.ref.ReadSets.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonReadSet(readset)));
		assertThat(status(result)).isEqualTo(OK);


		ReadSet readSetData = new ReadSet();
		readSetData.path="expectedPath";
		Logger.info("READ SET "+Json.toJson(readSetData).toString());
		result = callAction(controllers.readsets.api.routes.ref.ReadSets.update("rdCode"),fakeRequest("PUT","?fields=path").withJsonBody(Json.toJson(readSetData)));
		Logger.info(contentAsString(result));
		assertThat(status(result)).isEqualTo(OK);

		//Check path of readSet
		readset = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, "rdCode");
		Assert.assertNotNull(readset);
		Assert.assertEquals("expectedPath", readset.path);
	}

	
	
}
