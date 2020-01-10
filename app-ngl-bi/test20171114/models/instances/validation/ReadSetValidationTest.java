package models.instances.validation;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.callAction;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.status;
import static utils.RunMockHelper.getState;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.Valuation;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.container.instance.LocationOnContainerSupport;
import models.laboratory.project.instance.Project;
import models.laboratory.resolutions.instance.Resolution;
import models.laboratory.resolutions.instance.ResolutionCategory;
import models.laboratory.resolutions.instance.ResolutionConfiguration;
import models.laboratory.run.instance.File;
import models.laboratory.run.instance.InstrumentUsed;
import models.laboratory.run.instance.Lane;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.laboratory.run.instance.Treatment;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mongojack.DBQuery;

import play.Logger;
import play.data.validation.ValidationError;
import play.mvc.Result;
import utils.AbstractTestsCNG;
import utils.Constants;
import utils.RunMockHelper;
import validation.ContextValidation;
import validation.run.instance.LaneValidationHelper;
import validation.utils.ValidationConstants;
import fr.cea.ig.MongoDBDAO;

public class ReadSetValidationTest extends AbstractTestsCNG {

	static Container c;
	static ContainerSupport cs;
	static Sample s;
	static Project p;

	@BeforeClass
	public static void initData() throws InstantiationException, IllegalAccessException, ClassNotFoundException { 

		Logger.debug("Get container support to delete");
		ContainerSupport containerSupport = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, "containerName");
		if (containerSupport!=null) {
			MongoDBDAO.deleteByCode(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class,"containerName");
		}

		Logger.debug("Get container to delete");
		Container container = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, "containerName");
		if (container!=null) {
			MongoDBDAO.deleteByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, "containerName");
		}

		Logger.debug("Get project to delete");
		Project project = MongoDBDAO.findByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, "ProjectCode");
		if (project!=null) {
			MongoDBDAO.deleteByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class,"ProjectCode");
		}

		Logger.debug("Get sample to delete");
		Sample sample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, "SampleCode");
		if (sample!=null) {
			MongoDBDAO.deleteByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, "SampleCode");
		}

		ResolutionConfiguration resolutionConfig = MongoDBDAO.findByCode(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class, "conf-reso-run");
		if(resolutionConfig!=null)
			MongoDBDAO.deleteByCode(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class, "conf-reso-run");

		resolutionConfig = MongoDBDAO.findByCode(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class, "conf-reso-readset");
		if(resolutionConfig!=null)
			MongoDBDAO.deleteByCode(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class, "conf-reso-run-readset");

		Logger.debug("Save container support");
		ContainerSupport cs = new ContainerSupport();
		cs.code = "containerName";
		cs.categoryCode = "lane";

		MongoDBDAO.save(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, cs);

		Logger.debug("Save container ");	
		Container c = new Container();
		c.code ="containerName"; 
		c.support = new LocationOnContainerSupport();
		c.support.code = cs.code; 

		MongoDBDAO.save(InstanceConstants.CONTAINER_COLL_NAME, c);

		Logger.debug("Save project ");	
		Project p = new Project();
		p.code = "ProjectCode";
		p.typeCode = "default-project";
		p.categoryCode = "default";
		p.traceInformation = new TraceInformation(); 		   

		MongoDBDAO.save(InstanceConstants.PROJECT_COLL_NAME, p);
		Logger.debug("Save sample ");	
		Sample s = new Sample();
		s.code = "SampleCode";
		s.typeCode = "default-sample-cng";
		s.categoryCode="default";

		Set<String> lp =new HashSet<String>();
		lp.add("ProjectCode"); 
		s.projectCodes = lp;
		s.traceInformation = new TraceInformation();

		MongoDBDAO.save(InstanceConstants.SAMPLE_COLL_NAME, s);

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
		Logger.debug("Get container support to delete");
		ContainerSupport containerSupport = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, "containerName");
		if (containerSupport!=null) {
			MongoDBDAO.deleteByCode(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class,"containerName");
		}

		Logger.debug("Get container to delete");
		Container container = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, "containerName");
		if (container!=null) {
			MongoDBDAO.deleteByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, "containerName");
		}

		Logger.debug("Get project to delete");
		Project project = MongoDBDAO.findByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, "ProjectCode");
		if (project!=null) {
			MongoDBDAO.deleteByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class,"ProjectCode");
		}

		Logger.debug("Get sample to delete");
		Sample sample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, "SampleCode");
		if (sample!=null) {
			MongoDBDAO.deleteByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, "SampleCode");
		}

		ResolutionConfiguration resolutionConfig = MongoDBDAO.findByCode(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class, "conf-reso-run");
		if(resolutionConfig!=null)
			MongoDBDAO.deleteByCode(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class, "conf-reso-run");

		resolutionConfig = MongoDBDAO.findByCode(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class, "conf-reso-readset");
		if(resolutionConfig!=null)
			MongoDBDAO.deleteByCode(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class, "conf-reso-readset");
	}



	@Test
	public void testEntireReadSetValidationTraceAllErrors() {
		// just to show all errors messages (key, value) and show in that way the manner it looks like !;
	
		ReadSet r = getEmptyReadSet();

		File f = getEmptyFile();
		ArrayList<File> af = new ArrayList<File>();
		af.add(f);
		r.files = af;

		
		ContextValidation ctxVal = new ContextValidation(Constants.TEST_USER); 
		ctxVal.setCreationMode();
		r.validate(ctxVal); 

		for (Map.Entry<String, List<ValidationError>> entry : ctxVal.errors.entrySet()) {
			Logger.debug(entry.getKey() + "---------------------------------->" + entry.getValue().toString());
		}
		Assert.assertTrue(ctxVal.errors.containsKey("code"));
		Assert.assertTrue(ctxVal.errors.containsKey("typeCode"));
		Assert.assertTrue(ctxVal.errors.containsKey("projectCode"));
		Assert.assertTrue(ctxVal.errors.containsKey("sampleCode"));
		Assert.assertTrue(ctxVal.errors.containsKey("state"));
		Assert.assertTrue(ctxVal.errors.containsKey("valuation"));
		Assert.assertTrue(ctxVal.errors.containsKey("traceInformation"));
		Assert.assertTrue(ctxVal.errors.containsKey("runCode"));
		Assert.assertTrue(ctxVal.errors.containsKey("path"));
		Assert.assertTrue(ctxVal.errors.containsKey("files[0].extension"));
		Assert.assertTrue(ctxVal.errors.containsKey("files[0].fullname"));
		Assert.assertTrue(ctxVal.errors.containsKey("files[0].typeCode"));
		Assert.assertTrue(ctxVal.errors.containsKey("files[0].usable"));
	}


	@Test
	public void testCreateReadSetOK() {
		Run runDelete = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code","YANN_TEST1FORREADSET0"));
		if(runDelete!=null){
			MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runDelete._id);
		}	  
		ReadSet readSetDelete = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code","rdCode"));
		if(readSetDelete!=null){
			MongoDBDAO.delete(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSetDelete._id);
		}

		Run run = getFullRun();
		Lane lane1 = getLane();
		ArrayList<Lane> al = new ArrayList<Lane>();
		al.add(lane1);
		run.lanes = al;

		ContextValidation ctxVal2 = new ContextValidation(Constants.TEST_USER);
		ctxVal2.setCreationMode();

		run.validate(ctxVal2);			 
		assertThat(ctxVal2.errors).hasSize(0);

		Result result = callAction(controllers.runs.api.routes.ref.Runs.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
		assertThat(status(result)).isEqualTo(OK);

		ReadSet readset = RunMockHelper.newReadSet("rdCode");
		readset.runCode = run.code;

		ContextValidation ctxVal = new ContextValidation(Constants.TEST_USER);
		ctxVal.setCreationMode();

		readset.validate(ctxVal);
		assertThat(ctxVal.errors).hasSize(0);
	}

	@Test
	public void testCreateReadSetTypeCodeNotExist()
	{
		Run runDelete = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code","YANN_TEST1FORREADSET0"));
		if(runDelete!=null){
			MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runDelete._id);
		}	  
		ReadSet readSetDelete = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code","rdCode"));
		if(readSetDelete!=null){
			MongoDBDAO.delete(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSetDelete._id);
		}

		Run run = getFullRun();
		Lane lane1 = getLane();
		ArrayList<Lane> al = new ArrayList<Lane>();
		al.add(lane1);
		run.lanes = al;

		ContextValidation ctxVal2 = new ContextValidation(Constants.TEST_USER);
		ctxVal2.setCreationMode();

		run.validate(ctxVal2);			 
		assertThat(ctxVal2.errors).hasSize(0);

		Result result = callAction(controllers.runs.api.routes.ref.Runs.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
		assertThat(status(result)).isEqualTo(OK);

		ReadSet readset = RunMockHelper.newReadSet("rdCode");
		readset.runCode = run.code;
		readset.typeCode = "badCode";

		ContextValidation ctxVal = new ContextValidation(Constants.TEST_USER);
		ctxVal.setCreationMode();

		readset.validate(ctxVal);
		assertThat(ctxVal.errors.size()>0);
		assertThat(ctxVal.errors.toString()).contains("typeCode");
	}
	
	@Test
	public void testCreateReadSetWithBadProperties()
	{
		Run runDelete = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code","YANN_TEST1FORREADSET0"));
		if(runDelete!=null){
			MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runDelete._id);
		}	  
		ReadSet readSetDelete = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code","rdCode"));
		if(readSetDelete!=null){
			MongoDBDAO.delete(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSetDelete._id);
		}

		Run run = getFullRun();
		Lane lane1 = getLane();
		ArrayList<Lane> al = new ArrayList<Lane>();
		al.add(lane1);
		run.lanes = al;

		ContextValidation ctxVal2 = new ContextValidation(Constants.TEST_USER);
		ctxVal2.setCreationMode();

		run.validate(ctxVal2);			 
		assertThat(ctxVal2.errors).hasSize(0);
		

		Result result = callAction(controllers.runs.api.routes.ref.Runs.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
		assertThat(status(result)).isEqualTo(OK);

		ReadSet readset = RunMockHelper.newReadSet("rdCode");
		readset.runCode = run.code;
		readset.properties.put("insertSizeGoalErrorKey", new PropertySingleValue("badValue"));
		ContextValidation ctxVal = new ContextValidation(Constants.TEST_USER);
		ctxVal.setCreationMode();

		readset.validate(ctxVal);
		assertThat(ctxVal.errors).hasSize(1);
		assertThat(ctxVal.errors.toString()).contains("properties.insertSizeGoalErrorKey");
		
	}
	
	@Test
	public void testCreateReadSetValuationRequiredError()
	{
		Run runDelete = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code","YANN_TEST1FORREADSET0"));
		if(runDelete!=null){
			MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runDelete._id);
		}	  
		ReadSet readSetDelete = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code","rdCode"));
		if(readSetDelete!=null){
			MongoDBDAO.delete(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSetDelete._id);
		}

		Run run = getFullRun();
		Lane lane1 = getLane();
		ArrayList<Lane> al = new ArrayList<Lane>();
		al.add(lane1);
		run.lanes = al;

		ContextValidation ctxVal2 = new ContextValidation(Constants.TEST_USER);
		ctxVal2.setCreationMode();

		run.validate(ctxVal2);			 
		assertThat(ctxVal2.errors).hasSize(0);
		

		Result result = callAction(controllers.runs.api.routes.ref.Runs.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
		assertThat(status(result)).isEqualTo(OK);

		ReadSet readset = RunMockHelper.newReadSet("rdCode");
		readset.runCode = run.code;
		readset.bioinformaticValuation=null;
		readset.productionValuation = null;
		ContextValidation ctxVal = new ContextValidation(Constants.TEST_USER);
		ctxVal.setCreationMode();

		readset.validate(ctxVal);
		assertThat(ctxVal.errors).hasSize(1);
		assertThat(ctxVal.errors.toString()).contains("valuation");
		
	}
	
	@Test
	public void testCreateReadSetLaneNumberErrorRequired()
	{
		Run runDelete = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code","YANN_TEST1FORREADSET0"));
		if(runDelete!=null){
			MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runDelete._id);
		}	  
		ReadSet readSetDelete = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code","rdCode"));
		if(readSetDelete!=null){
			MongoDBDAO.delete(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSetDelete._id);
		}

		Run run = getFullRun();
		Lane lane1 = getLane();
		ArrayList<Lane> al = new ArrayList<Lane>();
		al.add(lane1);
		run.lanes = al;

		ContextValidation ctxVal2 = new ContextValidation(Constants.TEST_USER);
		ctxVal2.setCreationMode();

		run.validate(ctxVal2);			 
		assertThat(ctxVal2.errors).hasSize(0);
		

		Result result = callAction(controllers.runs.api.routes.ref.Runs.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
		assertThat(status(result)).isEqualTo(OK);

		ReadSet readset = RunMockHelper.newReadSet("rdCode");
		readset.runCode = run.code;
		readset.laneNumber=null;
		ContextValidation ctxVal = new ContextValidation(Constants.TEST_USER);
		ctxVal.setCreationMode();

		readset.validate(ctxVal);
		
		assertThat(ctxVal.errors).hasSize(1);
		assertThat(ctxVal.errors.toString()).contains("laneNumber");
	}
	
	@Test
	public void testCreateReadSetProjectSampleNotExist()
	{
		Run runDelete = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code","YANN_TEST1FORREADSET0"));
		if(runDelete!=null){
			MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runDelete._id);
		}	  
		ReadSet readSetDelete = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code","rdCode"));
		if(readSetDelete!=null){
			MongoDBDAO.delete(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSetDelete._id);
		}

		Run run = getFullRun();
		Lane lane1 = getLane();
		ArrayList<Lane> al = new ArrayList<Lane>();
		al.add(lane1);
		run.lanes = al;

		ContextValidation ctxVal2 = new ContextValidation(Constants.TEST_USER);
		ctxVal2.setCreationMode();

		run.validate(ctxVal2);			 
		assertThat(ctxVal2.errors).hasSize(0);
		

		Result result = callAction(controllers.runs.api.routes.ref.Runs.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
		assertThat(status(result)).isEqualTo(OK);

		ReadSet readset = RunMockHelper.newReadSet("rdCode");
		readset.runCode = run.code;
		readset.projectCode="badCodeProject";
		readset.sampleCode="badSampleCode";
		ContextValidation ctxVal = new ContextValidation(Constants.TEST_USER);
		ctxVal.setCreationMode();

		readset.validate(ctxVal);
		
		assertThat(ctxVal.errors).hasSize(2);
		assertThat(ctxVal.errors.toString()).contains("projectCode");
		assertThat(ctxVal.errors.toString()).contains("sampleCode");
	}
	@Test
	public void testErrorReadSetWithLaneNumber20(){
		Run runDelete = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code","YANN_TEST1FORREADSET0"));
		if(runDelete!=null){
			MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runDelete._id);
		}
		ReadSet readSetDelete = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code",getNewReadSet().code));
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

		Run run = getFullRun();
		ArrayList<Lane> l = new ArrayList<Lane>();
		Lane lane = getLane(); //lane.number = 1
		// correct value
		//lane.state = getState("F"); 
		ReadSet r = getNewReadSet();
		ArrayList<String> a = new ArrayList<String>();
		//a.add(r.code);			 
		lane.readSetCodes = a;
		r.runCode = run.code;

		//MUST GENERATE A ERROR ! 
		r.laneNumber = 20;

		l.add(lane);
		run.lanes = l;

		ContextValidation ctxVal = new ContextValidation(Constants.TEST_USER);
		ctxVal.setCreationMode();
		run.validate(ctxVal);
		assertThat(ctxVal.errors).hasSize(0);	 

		ctxVal.putObject("run", run);
		ctxVal.setCreationMode();
		run.lanes.get(0).validate(ctxVal);
		r.runCode = run.code;
		assertThat(ctxVal.errors).hasSize(0);

		// the run must be saved to validate one of his readset 
		Result result = callAction(controllers.runs.api.routes.ref.Runs.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
		assertThat(status(result)).isEqualTo(OK);

		ctxVal = new ContextValidation(Constants.TEST_USER);
		ctxVal.setCreationMode();
		r.validate(ctxVal);
		assertThat(ctxVal.errors).hasSize(2);
		assertThat(ctxVal.errors.toString()).contains("laneNumber");
		assertThat(ctxVal.errors.toString()).contains("runCode");
	}


	@Test
	public void testReadSetFileValidationOK(){
		Run runDelete = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code","YANN_TEST1FORREADSET21"));
		if(runDelete!=null){
			MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runDelete._id);
		}
		ReadSet readsetDelete = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code","test1"));
		if(readsetDelete!=null){
			MongoDBDAO.delete(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readsetDelete._id);
		}

		Run run = RunMockHelper.newRun("YANN_TEST1FORREADSET21");
		run.dispatch = true; // For the archive test
		Lane lane = RunMockHelper.newLane(1);

		List<File> files =  new ArrayList<File>();
		File file = RunMockHelper.newFile("xxE410_FA_B00FPMH_3_2_C17GFACXX.IND6.fastq");
		files.add(file);

		ReadSet readSet = RunMockHelper.newReadSet("test1");
		readSet.files = files;

		ArrayList<String> a = new ArrayList<String>();
		a.add(readSet.code);
		lane.readSetCodes = a;
		Lane lane2 = RunMockHelper.newLane(2);
		List<Lane> lanes = new ArrayList<Lane>();
		lanes.add(lane);
		lanes.add(lane2);
		run.lanes = lanes;

		ContextValidation ctxVal = new ContextValidation(Constants.TEST_USER);
		ctxVal.putObject("readSet", readSet);
		ctxVal.putObject("objectClass", readSet.getClass());  
		ctxVal.setCreationMode();
		file.validate(ctxVal);

		assertThat(ctxVal.errors).hasSize(0);	
	}


	@Test
	public void testReadSetFileValidationError(){
		// add two identical files to the same readset
		Run runDelete = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code","YANN_TEST1FORREADSET0"));
		if(runDelete!=null){
			MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runDelete._id);
		}
		ReadSet readsetDelete = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code","test1"));
		if(readsetDelete!=null){
			MongoDBDAO.delete(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readsetDelete._id);
		}
		

		Run run = getFullRun();
		run.dispatch = true; // For the archive test
		Lane lane = RunMockHelper.newLane(1);
		List<File> files =  new ArrayList<File>();
		File file = RunMockHelper.newFile("xxE410_FA_B00FPMH_3_2_C17GFACXX.IND6.fastq");
		files.add(file);
		// AGAIN FOR ERROR
		files.add(file);

		ReadSet readSet = RunMockHelper.newReadSet("test1");
		readSet.files = files;
		readSet.runCode = run.code;
		readSet.laneNumber = 1;

		ArrayList<String> a = new ArrayList<String>();
		//a.add(r.code);
		lane.readSetCodes = a;
		Lane lane2 = RunMockHelper.newLane(2);
		List<Lane> lanes = new ArrayList<Lane>();
		lanes.add(lane);
		lanes.add(lane2);
		run.lanes = lanes;	

		ContextValidation ctxVal = new ContextValidation(Constants.TEST_USER);
		ctxVal.putObject("readSet", readSet);
		ctxVal.putObject("objectClass", readSet.getClass());  
		ctxVal.setCreationMode();
		file.validate(ctxVal);
		assertThat(ctxVal.errors).hasSize(0);

		Result result = callAction(controllers.runs.api.routes.ref.Runs.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
		assertThat(status(result)).isEqualTo(OK);

		ctxVal = new ContextValidation(Constants.TEST_USER);
		ctxVal.setCreationMode();
		readSet.validate(ctxVal);

		assertThat(ctxVal.errors).hasSize(1);
		assertThat(ctxVal.errors.toString()).contains("files[1].fullname");
	}

	private Lane getLane() {	
		Lane lane = new Lane();
		lane.number = 1;
		lane.valuation = RunMockHelper.getValuation(TBoolean.TRUE);
		lane.readSetCodes = null;
		return lane;
	}

	private ReadSet getNewReadSet() {
		ReadSet readSet = new ReadSet();
		readSet.code = "CORE_R1";
		readSet.path = "/path/test";
		readSet.projectCode = "ProjectCode"; 
		readSet.sampleCode = "SampleCode";
		readSet.state = getState("F-QC");
		Valuation v = new Valuation();
		readSet.bioinformaticValuation = v;
		readSet.productionValuation = v;		
		readSet.traceInformation = new TraceInformation();
		readSet.traceInformation.setTraceInformation("ngsrg");
		readSet.laneNumber = 1;
		readSet.dispatch = true;
		readSet.typeCode = "default-readset";
		return readSet;
	}

	private ReadSet getEmptyReadSet() {
		ReadSet readSet = new ReadSet();
		readSet.code = "";
		readSet.path = "";
		readSet.projectCode = ""; 
		readSet.sampleCode = "";
		readSet.state = null;
		readSet.typeCode = "";
		readSet.bioinformaticValuation = null;
		readSet.productionValuation = null;
		readSet.traceInformation = null; 
		return readSet;
	}

	private File getEmptyFile() {
		File file = new File();
		file.extension = "";
		file.fullname = "";
		file.typeCode = "";
		file.usable = null;	
		file.properties.put("asciiEncoding", new PropertySingleValue(""));
		file.properties.put("label", new PropertySingleValue(""));
		return file;
	}

	private Run getFullRun() {
		Run run = new Run();
		run.code = "YANN_TEST1FORREADSET0";
		run.containerSupportCode = "containerName";
		run.dispatch = true;
		run.instrumentUsed = new InstrumentUsed();
		run.instrumentUsed.code = "HISEQ2";
		run.instrumentUsed.typeCode = "HISEQ2000";
		run.typeCode = "RHS2000";
		run.categoryCode="illumina";
		Set<String> lResos = new HashSet<String>();
		lResos.add("reso1");
		lResos.add("reso2");
		State state = new State();
		run.state = state;
		run.state.code = "F";
		run.state.user = "tests";
		run.state.date = new Date();
		run.state.resolutionCodes=lResos;
		Valuation v = new Valuation();
		run.valuation = v;
		run.traceInformation = new TraceInformation();
		run.traceInformation.setTraceInformation("test");
		//Map<String, Treatment> lT = new HashMap<String, Treatment>();
		//Treatment ngsrg = new Treatment(); 
		//lT.put("ngsrg", ngsrg);
		//ngsrg.categoryCode = "ngsrg";
		//ngsrg.typeCode = "ngsrg-illumina";
		run.typeCode = "RHS2000";
		run.valuation = new Valuation();
		run.valuation.user = "test";
		run.valuation.valid = TBoolean.TRUE;
		run.valuation.date = new Date(); 
		return run;
	}

}
