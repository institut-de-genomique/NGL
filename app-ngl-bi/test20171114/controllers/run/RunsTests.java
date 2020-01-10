package controllers.run;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.test.Helpers.callAction;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.TBoolean;
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
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import play.Logger;
import play.mvc.Result;
import utils.AbstractTestsCNG;
import utils.RunMockHelper;

public class RunsTests extends AbstractTestsCNG {

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
		ContainerSupport containerSupport = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class,"containerName");
		if (containerSupport!=null) 
			MongoDBDAO.deleteByCode(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, "containerName");
		
		Container container = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, "containerTest1");
		if(container!=null)
			MongoDBDAO.deleteByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, "containerTest1");

		ResolutionConfiguration resolutionConfig = MongoDBDAO.findByCode(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class, "conf-reso-run");
		if(resolutionConfig!=null)
			MongoDBDAO.deleteByCode(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class, "conf-reso-run");

		resolutionConfig = MongoDBDAO.findByCode(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class, "conf-reso-readset");
		if(resolutionConfig!=null)
			MongoDBDAO.deleteByCode(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class, "conf-reso-readset");
		
		/*List<Sample> samples = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class).toList();
		for (Sample sample : samples) {
			MongoDBDAO.delete(InstanceConstants.SAMPLE_COLL_NAME, sample);
		}*/
	}

	@Before
	public void initTestData()
	{
		Run runDelete = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code","YANN_TEST1"));
		if(runDelete!=null){
			MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runDelete._id);
		}
		
		runDelete = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code","YANN_TEST2"));
		if(runDelete!=null){
			MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runDelete._id);
		}
		
		ReadSet readSetDelete = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code","ReadSet00"));
		if(readSetDelete!=null){
			MongoDBDAO.delete(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSetDelete._id);
		}
		
		readSetDelete = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code","ReadSet2"));
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

	}
	
	@Test
	public void testRunSave() { 
		
		Sample sample = RunMockHelper.newSample("SampleCode");
		Project project = RunMockHelper.newProject("ProjectCode");

		MongoDBDAO.save(InstanceConstants.SAMPLE_COLL_NAME, sample);
		MongoDBDAO.save(InstanceConstants.PROJECT_COLL_NAME, project);

		Run run = RunMockHelper.newRun("YANN_TEST1");
		Lane lane = RunMockHelper.newLane(1);

		List<Lane> lanes = new ArrayList<Lane>();
		lanes.add(lane);
		run.lanes = lanes;

		Result result = callAction(controllers.runs.api.routes.ref.Runs.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
		Logger.debug("Result "+contentAsString(result));
		assertThat(status(result)).isEqualTo(OK);		

		ReadSet r = RunMockHelper.newReadSet("ReadSet00");		
		r.runCode = run.code;
		r.laneNumber = lane.number;
		result = callAction(controllers.readsets.api.routes.ref.ReadSets.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonReadSet(r)));
		Logger.debug("Result "+contentAsString(result));
		assertThat(status(result)).isEqualTo(OK);

		//query for control
		run = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code",run.code));
		assertThat(run.lanes.size()).isEqualTo(1);
		assertThat(run.lanes.get(0).number).isEqualTo(1);

		r = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class,DBQuery.is("code",r.code));
		assertThat(r.code).isEqualTo("ReadSet00");
		assertThat(r.runCode).isEqualTo(run.code);
		assertThat(r.laneNumber).isEqualTo(lane.number);
	}

	@Test
	public void testSaveRunWithoutContainerWithExternalOption()
	{
		Run run = RunMockHelper.newRun("YANN_TEST1");
		//Must pass the validation
		run.containerSupportCode=null;
		Lane lane = RunMockHelper.newLane(1);

		List<Lane> lanes = new ArrayList<Lane>();
		lanes.add(lane);
		run.lanes = lanes;
		
		Result result = callAction(controllers.runs.api.routes.ref.Runs.save(),fakeRequest(play.test.Helpers.POST, "?external=true").withJsonBody(RunMockHelper.getJsonRun(run)));
		Logger.debug("Result "+contentAsString(result));
		assertThat(status(result)).isEqualTo(OK);	
		
		//Query for control
		run = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code",run.code));
		assertThat(run!=null);
		assertThat(run.containerSupportCode==null);
	}
	
	@Test
	public void testSaveRunWithoutContainerWithoutExternalOption()
	{
		Run run = RunMockHelper.newRun("YANN_TEST1");
		//Must pass the validation
		run.containerSupportCode=null;
		Lane lane = RunMockHelper.newLane(1);

		List<Lane> lanes = new ArrayList<Lane>();
		lanes.add(lane);
		run.lanes = lanes;
		
		Result result = callAction(controllers.runs.api.routes.ref.Runs.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
		Logger.debug("Result "+contentAsString(result));
		assertThat(status(result)).isEqualTo(BAD_REQUEST);
		assertThat(contentAsString(result).contains("containerSupportCode"));
		
		//Query for control
		run = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code",run.code));
		assertThat(run==null);
	}
	
	@Test
	public void testRunUpdate() { 
		
		Run run = RunMockHelper.newRun("YANN_TEST1");
		Lane lane = RunMockHelper.newLane(1);
		List<Lane> lanes = new ArrayList<Lane>();
		lanes.add(lane);
		run.lanes = lanes;

		run.dispatch=false;

		Result result = callAction(controllers.runs.api.routes.ref.Runs.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
		assertThat(status(result)).isEqualTo(OK);

		run = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code","YANN_TEST1"));
		assertThat(run.dispatch).isEqualTo(false);

		run.dispatch=true;
		result = callAction(controllers.runs.api.routes.ref.Runs.update(run.code),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
		Logger.debug(contentAsString(result));
		assertThat(status(result)).isEqualTo(OK);

		//query for control
		run = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code","YANN_TEST1"));
		assertThat(run.dispatch).isEqualTo(true);
	}
	
	@Test
	public void testRunUpdateWithoutContainerSupport()
	{
		Run run = RunMockHelper.newRun("YANN_TEST1");
		Lane lane = RunMockHelper.newLane(1);
		List<Lane> lanes = new ArrayList<Lane>();
		lanes.add(lane);
		run.lanes = lanes;

		Result result = callAction(controllers.runs.api.routes.ref.Runs.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
		assertThat(status(result)).isEqualTo(OK);

		run = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code","YANN_TEST1"));
		run.containerSupportCode=null;
		
		result = callAction(controllers.runs.api.routes.ref.Runs.update(run.code),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
		Logger.debug("Result "+contentAsString(result));
		assertThat(status(result)).isEqualTo(BAD_REQUEST);
		assertThat(contentAsString(result).contains("containerSupportCode"));

		//query for control
		run = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code","YANN_TEST1"));
		assertThat(run.dispatch).isEqualTo(true);
	}

	@Test
	public void testRunSaveWithTwiceSameReadSet() {
		Sample sample = RunMockHelper.newSample("SampleCode");
		Project project = RunMockHelper.newProject("ProjectCode");

		MongoDBDAO.save(InstanceConstants.SAMPLE_COLL_NAME, sample);
		MongoDBDAO.save(InstanceConstants.PROJECT_COLL_NAME, project);

		Run run = RunMockHelper.newRun("YANN_TEST2");
		Lane lane = RunMockHelper.newLane(1);
		List<Lane> lanes = new ArrayList<Lane>();
		lanes.add(lane);
		run.lanes = lanes;

		ReadSet r = RunMockHelper.newReadSet("ReadSet2");
		r.runCode = run.code;
		r.laneNumber = lane.number;

		Result result = callAction(controllers.runs.api.routes.ref.Runs.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
		assertThat(status(result)).isEqualTo(OK);

		result = callAction(controllers.readsets.api.routes.ref.ReadSets.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonReadSet(r)));
		assertThat(status(result)).isEqualTo(OK);

		// update masterReadSetCodes
		List<String> a = new ArrayList<String>();
		a.add(r.code);
		a.add(r.code);
		run.lanes.get(0).readSetCodes = a;

		//insert run with the readset r twice
		result = callAction(controllers.runs.api.routes.ref.Runs.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.BAD_REQUEST);			

		result = callAction(controllers.runs.api.routes.ref.Runs.update(run.code),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.BAD_REQUEST);	

		//query for control
		run = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code",run.code));
		assertThat(run.lanes.get(0).readSetCodes.size()).isEqualTo(1);
		assertThat(run.lanes.get(0).readSetCodes.get(0)).isEqualTo(r.code);
	}

	@Test
	public void testPropertyLaneUpdate() {
		Run run = RunMockHelper.newRun("YANN_TEST1");
		Lane lane = RunMockHelper.newLane(1);
		List<Lane> lanes = new ArrayList<Lane>();
		lanes.add(lane);
		run.lanes = lanes;

		Result result = callAction(controllers.runs.api.routes.ref.Runs.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
		assertThat(status(result)).isEqualTo(OK);

		lane.valuation = RunMockHelper.getValuation(TBoolean.TRUE);

		result = callAction(controllers.runs.api.routes.ref.Lanes.update(run.code, lane.number),fakeRequest().withJsonBody(RunMockHelper.getJsonLane(lane)));
		assertThat(status(result)).isEqualTo(OK);

		//query for control
		run = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code",run.code));
		assertThat(lane.valuation.valid).isEqualTo(TBoolean.TRUE);
	}


	@Test
	public void testPropertyReadSetUpdate() {
		Sample sample = RunMockHelper.newSample("SampleCode");
		Project project = RunMockHelper.newProject("ProjectCode");

		MongoDBDAO.save(InstanceConstants.SAMPLE_COLL_NAME, sample);
		MongoDBDAO.save(InstanceConstants.PROJECT_COLL_NAME, project);

		Run run = RunMockHelper.newRun("YANN_TEST1");
		Lane lane = RunMockHelper.newLane(1);
		List<Lane> lanes = new ArrayList<Lane>();
		lanes.add(lane);
		run.lanes = lanes;

		ReadSet r = RunMockHelper.newReadSet("ReadSet01");
		r.runCode = run.code;
		r.laneNumber = lane.number;

		Result result = callAction(controllers.runs.api.routes.ref.Runs.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
		assertThat(status(result)).isEqualTo(OK);

		result = callAction(controllers.readsets.api.routes.ref.ReadSets.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonReadSet(r)));
		assertThat(status(result)).isEqualTo(OK);

		r = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class,DBQuery.is("code",r.code));
		assertThat(r._id).isNotEqualTo(null);

		r.dispatch = false;

		result = callAction(controllers.readsets.api.routes.ref.ReadSets.update(r.code),fakeRequest().withJsonBody(RunMockHelper.getJsonReadSet(r)));
		assertThat(status(result)).isEqualTo(OK);

		//query for control
		r = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class,DBQuery.is("code",r.code));
		assertThat(r.dispatch).isEqualTo(false);
	}

	@Test
	public void testDeleteRun(){
		Run run = RunMockHelper.newRun("YANN_TEST1");
		Lane lane = RunMockHelper.newLane(1);
		List<Lane> lanes = new ArrayList<Lane>();
		lanes.add(lane);
		run.lanes = lanes;

		Result result = callAction(controllers.runs.api.routes.ref.Runs.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
		assertThat(status(result)).isEqualTo(OK);

		result = callAction(controllers.runs.api.routes.ref.Runs.delete(run.code),fakeRequest());
		assertThat(status(result)).isEqualTo(OK);

		//query for control
		run = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code",run.code));
		assertThat(run).isNull();	
	}



}
