package rules;

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
import org.mongojack.DBUpdate;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TBoolean;
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

public class ReadSetsWorkflowTestsCNS extends AbstractTestsCNS {

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
		project.bioinformaticParameters.fgGroup="fg001";

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
	
	/**
	 * Test workflow for transfert CCRT
	 * ReadSet available nextState = In Waiting Transfert CCRT 
	 * For Project france genomique
	 * And no data sent to CCRT
	 */
	//@Test
	public void shouldReadSetAvalaibleToInWaitingTransfertCCRT()
	{
		Logger.debug("Save run");
		Result result = callAction(controllers.runs.api.routes.ref.Runs.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
		Logger.debug(contentAsString(result));
		assertThat(status(result)).isEqualTo(OK);

		Logger.debug("Save readSet");
		readset.state.code="N";
		
		readset.bioinformaticValuation.valid=TBoolean.TRUE;
		result = callAction(controllers.readsets.api.routes.ref.ReadSets.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonReadSet(readset)));
		Logger.debug(contentAsString(result));
		assertThat(status(result)).isEqualTo(OK);
		
		//pour ne pas declencher synchronisation LIMS
		MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code", readset.code), DBUpdate.set("typeCode", "rsnanopore"));
		
		Logger.debug("Find readSet");
		readset = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readset.code);
		Assert.assertNotNull(readset);
		Logger.debug("Set state A");
		State state = new State();
		state.code="A";
		//Set state to F-RG
		result = callAction(controllers.readsets.api.routes.ref.ReadSets.state(readset.code),fakeRequest().withJsonBody(Json.toJson(state)));
		Logger.debug(contentAsString(result));
		assertThat(status(result)).isEqualTo(OK);
		
		
		//Get ReadSet
		readset=MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readset.code);
		Logger.debug("State readSet "+readset.state.code);
		Assert.assertEquals("IW-TF", readset.state.code);
	}
	
	/**
	 * Test workflow transfert CCRT
	 * ReadSet available no change state
	 * if no france genomique project
	 * if data already sent to CCRT
	 */
	//@Test
	public void shouldReadSetAvalaibleNoChangeStateTransfertCCRT()
	{
		Logger.debug("Save run");
		Result result = callAction(controllers.runs.api.routes.ref.Runs.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
		Logger.debug(contentAsString(result));
		assertThat(status(result)).isEqualTo(OK);

		Logger.debug("Save readSet");
		readset.state.code="N";
		
		readset.bioinformaticValuation.valid=TBoolean.TRUE;
		result = callAction(controllers.readsets.api.routes.ref.ReadSets.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonReadSet(readset)));
		Logger.debug(contentAsString(result));
		assertThat(status(result)).isEqualTo(OK);
		
		//pour ne pas declencher synchronisation LIMS
		MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code", readset.code), DBUpdate.set("typeCode", "rsnanopore"));
				
		//Readset already sent to CCRT no change state
		MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, Project.class, DBQuery.is("code", readset.code), DBUpdate.set("location", "CCRT"));
		Logger.debug("Find readSet");
		readset = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readset.code);
		Assert.assertNotNull(readset);
		Logger.debug("Set state A");
		State state = new State();
		state.code="A";
		result = callAction(controllers.readsets.api.routes.ref.ReadSets.state(readset.code),fakeRequest().withJsonBody(Json.toJson(state)));
		Logger.debug(contentAsString(result));
		assertThat(status(result)).isEqualTo(OK);
		
		
		//Get ReadSet
		readset=MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readset.code);
		Logger.debug("State readSet "+readset.state.code);
		Assert.assertEquals("A", readset.state.code);
		
		//No project france genomique no change state
		MongoDBDAO.update(InstanceConstants.PROJECT_COLL_NAME, Project.class, DBQuery.is("code", "ProjectCode"), DBUpdate.set("bioinformaticParameters.fgGroup", null));
		
		result = callAction(controllers.readsets.api.routes.ref.ReadSets.state(readset.code),fakeRequest().withJsonBody(Json.toJson(state)));
		Logger.debug(contentAsString(result));
		assertThat(status(result)).isEqualTo(OK);
		//Get ReadSet
		readset=MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readset.code);
		Logger.debug("State readSet "+readset.state.code);
		Assert.assertEquals("A", readset.state.code);
		
	}
	
	
	/**
	 * Test workflow for transfert CCRT
	 * ReadSet Finished Transfert = Avalaible 
	 * For Project france genomique
	 * And data sent to CCRT
	 */
	@Test
	public void shouldReadSetTransfertCCRTToAvailable()
	{
		Logger.debug("Save run");
		Result result = callAction(controllers.runs.api.routes.ref.Runs.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
		Logger.debug(contentAsString(result));
		assertThat(status(result)).isEqualTo(OK);

		Logger.debug("Save readSet");
		readset.state.code="N";
		
		readset.bioinformaticValuation.valid=TBoolean.TRUE;
		result = callAction(controllers.readsets.api.routes.ref.ReadSets.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonReadSet(readset)));
		Logger.debug(contentAsString(result));
		assertThat(status(result)).isEqualTo(OK);
		
		//pour ne pas declencher synchronisation LIMS
		MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code", readset.code), DBUpdate.set("typeCode", "rsnanopore").set("location", "CCRT"));
		
		Logger.debug("Find readSet");
		readset = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readset.code);
		Assert.assertNotNull(readset);
		Logger.debug("Set state F-TF");
		State state = new State();
		state.code="F-TF";
		//Set state to F-RG
		result = callAction(controllers.readsets.api.routes.ref.ReadSets.state(readset.code),fakeRequest().withJsonBody(Json.toJson(state)));
		Logger.debug(contentAsString(result));
		assertThat(status(result)).isEqualTo(OK);
		
		
		//Get ReadSet
		readset=MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readset.code);
		Logger.debug("State readSet "+readset.state.code);
		Assert.assertEquals("A", readset.state.code);
	}

}