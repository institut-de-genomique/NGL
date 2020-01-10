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

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.Valuation;
import models.laboratory.common.instance.property.PropertyListValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.container.instance.LocationOnContainerSupport;
import models.laboratory.project.instance.Project;
import models.laboratory.resolutions.instance.Resolution;
import models.laboratory.resolutions.instance.ResolutionCategory;
import models.laboratory.resolutions.instance.ResolutionConfiguration;
import models.laboratory.run.instance.InstrumentUsed;
import models.laboratory.run.instance.Lane;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.laboratory.run.instance.Treatment;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import play.Logger;
import play.data.validation.ValidationError;
import play.mvc.Result;
import utils.AbstractTestsCNG;
import utils.Constants;
import utils.RunMockHelper;
import validation.ContextValidation;
import validation.run.instance.LaneValidationHelper;
import validation.utils.ValidationConstants;

public class RunValidationTest extends AbstractTestsCNG {

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
	public void testEntireRunValidationTraceAllErrors() {
		// just to show all errors messages (key, value) and show in that way the manner it looks like !;
		Run run = getEmptyRun();

		ArrayList<Lane> al = new ArrayList<Lane>();
		Lane l = getEmptyLane();
		
		al.add(l);
		run.lanes = al;

		ContextValidation ctxVal = new ContextValidation(Constants.TEST_USER);
		ctxVal.setCreationMode();
		run.validate(ctxVal); 

		Logger.debug("------------------------------------------------------");


		for (Map.Entry<String, List<ValidationError>> entry : ctxVal.errors.entrySet()) {
			Logger.debug(entry.getKey() + "---------------------------------->" + entry.getValue().toString());
		}

		Logger.debug("-------------------------------------------------------");


		//Check validation error
		Assert.assertTrue(ctxVal.errors.containsKey("code"));
		Assert.assertTrue(ctxVal.errors.containsKey("typeCode"));
		Assert.assertTrue(ctxVal.errors.containsKey("categoryCode"));
		Assert.assertTrue(ctxVal.errors.containsKey("state"));
		Assert.assertTrue(ctxVal.errors.containsKey("valuation"));
		Assert.assertTrue(ctxVal.errors.containsKey("traceInformation"));
		Assert.assertTrue(ctxVal.errors.containsKey("containerSupportCode"));
		Assert.assertTrue(ctxVal.errors.containsKey("instrumentUsed"));
		Assert.assertTrue(ctxVal.errors.containsKey("lanes[0].readSetCodes[0]"));

	}


	@Test
	public void testCreateRunValidationOK() {
		Run runDelete = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, DBQuery.is("code","YANN_TEST1FORREADSET0"));
		if(runDelete!=null){
			MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runDelete._id);
		}	   
		Run run = getFullRun();
		ContextValidation ctxVal = new ContextValidation(Constants.TEST_USER);
		ctxVal.setCreationMode();
		run.validate(ctxVal);

		assertThat(ctxVal.errors).hasSize(0);

	}



	@Test
	public void testCreateRunWithProjectCodeValidationOK() {
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

		result = null;	
		result = callAction(controllers.readsets.api.routes.ref.ReadSets.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonReadSet(readset)));
		assertThat(status(result)).isEqualTo(OK);

		run = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code","YANN_TEST1FORREADSET0"));

		run.traceInformation.modifyUser = "test";
		run.traceInformation.modifyDate = new Date();
		
		run.projectCodes.add("ProjectCode");
		run.sampleCodes.add("SampleCode");

		ContextValidation ctxVal3 = new ContextValidation(Constants.TEST_USER);
		ctxVal3.setUpdateMode();

		run.validate(ctxVal3);


		assertThat(ctxVal3.errors).hasSize(0);


		// the fact that we have save the readset must have be saved the projectCode and the sampleCode in the run...
		assertThat(run.projectCodes.contains("ProjectCode"));
		assertThat(run.sampleCodes.contains("SampleCode"));
	}


	@Test
	public void testCreateRunWithProjectCodeAndSampleCodeValidationErr() {
		Run runDelete = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code","YANN_TEST1FORREADSET0"));
		if(runDelete!=null){
			MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runDelete._id);
		}	   
		Run run = getFullRunWithBadProjectCode();
		ContextValidation ctxVal = new ContextValidation(Constants.TEST_USER);
		ctxVal.setCreationMode();
		run.validate(ctxVal);

		assertThat(ctxVal.errors.size()).isGreaterThan(1);

		assertThat(ctxVal.errors.toString().contains("projectCode[0]"));
		assertThat(ctxVal.errors.toString().contains("salmpleCode[0]"));
		
		for (Map.Entry<String, List<ValidationError>> entry : ctxVal.errors.entrySet()) {
			Logger.debug(entry.getKey() + "---------------------------------->" + entry.getValue().toString());
		}
	}

	@Test
	public void testCreateRunWithBadProperties()
	{
		Run runDelete = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code","YANN_TEST1FORREADSET0"));
		if(runDelete!=null){
			MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runDelete._id);
		}	   
		Run run = getFullRun();
		run.properties.put("wrongKey", new PropertySingleValue("wrongValue"));
		ContextValidation ctxVal = new ContextValidation(Constants.TEST_USER);
		ctxVal.setCreationMode();
		run.validate(ctxVal);

		assertThat(ctxVal.errors).hasSize(1);
		assertThat(ctxVal.errors.toString().contains("properties.wrongKey"));
		
	}

	@Test
	public void testUpdateRunValidationOK() {
		Run runDelete = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code","YANN_TEST1FORREADSET0"));
		if(runDelete!=null){
			MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runDelete._id);
		}	   
		Run run = getFullRun();
		ContextValidation ctxVal = new ContextValidation(Constants.TEST_USER);
		ctxVal.setCreationMode();
		run.validate(ctxVal);

		Result result = callAction(controllers.runs.api.routes.ref.Runs.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
		assertThat(status(result)).isEqualTo(OK);

		run = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code",run.code));

		run.containerSupportCode = "containerName";
		run.traceInformation.modifyDate = new Date();
		run.traceInformation.modifyUser = "dnoisett";

		ctxVal = new ContextValidation(Constants.TEST_USER);
		ctxVal.setUpdateMode();
		run.validate(ctxVal);

		assertThat(ctxVal.errors).hasSize(0);
	}


	@Test
	public void testUpdateRunValidationError() {
		Run runDelete = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code","YANN_TEST1FORREADSET0"));
		if(runDelete!=null){
			MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runDelete._id);
		}	   
		Run run = getFullRun();
		ContextValidation ctxVal = new ContextValidation(Constants.TEST_USER);
		ctxVal.setCreationMode();
		run.validate(ctxVal);

		Result result = callAction(controllers.runs.api.routes.ref.Runs.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
		assertThat(status(result)).isEqualTo(OK);

		run = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code",run.code));

		run.containerSupportCode = "ZOUIOUI";
		run.traceInformation.modifyDate = new Date();
		run.traceInformation.modifyUser = "dnoisett";

		ctxVal = new ContextValidation(Constants.TEST_USER);
		ctxVal.setUpdateMode();
		run.validate(ctxVal);

		assertThat(ctxVal.errors).hasSize(1);
		assertThat(ctxVal.errors.containsValue("containerSupportCode")); 
	}


	@Test
	public void testValidationErrorBadRunType() {
		Run runDelete = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code","YANN_TEST1FORREADSET0"));
		if(runDelete!=null){
			MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runDelete._id);
		}	   
		Run run = getFullRun();
		run.typeCode = "2500";
		ContextValidation ctxVal = new ContextValidation(Constants.TEST_USER);
		ctxVal.setCreationMode();
		run.validate(ctxVal);

		for (Map.Entry<String, List<ValidationError>> entry : ctxVal.errors.entrySet()) {
			Logger.debug(entry.getKey() + "---------------------------------->" + entry.getValue().toString());
		}
		
		//3 errors for state (code, reso1,reso2
		//1 error for type code
		assertThat(ctxVal.errors).hasSize(4);
		assertThat(ctxVal.errors.toString()).contains("typeCode");
	}


	@Test
	public void testRunWith1TreatmentValidationOK() {
		Run runDelete = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code","YANN_TEST1FORREADSET0"));
		if(runDelete!=null){
			MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runDelete._id);
		}	   
		Run run = getFullRun();

		Treatment t1 = new Treatment();
		t1.code = "ngsrg";
		t1.typeCode = "ngsrg-illumina";
		t1.categoryCode = "ngsrg";
		//define map of single property values
		Map<String,PropertyValue> m = new HashMap<String,PropertyValue>();
		m.put("nbClusterTotal", new PropertySingleValue(100)); // valeur simple
		m.put("nbClusterIlluminaFilter", new PropertySingleValue(100));
		m.put("nbCycle", new PropertySingleValue(100));
		m.put("nbBase", new PropertySingleValue(100));
		m.put("flowcellPosition", new PropertySingleValue("A"));
		m.put("rtaVersion", new PropertySingleValue("v1"));
		m.put("flowcellVersion", new PropertySingleValue("v1"));
		m.put("controlLane", new PropertySingleValue(90));
		m.put("mismatch", new PropertySingleValue(true));
		m.put("percentClusterIlluminaFilter", new PropertySingleValue(96.125));
		t1.set("default", m);

		Map<String, Treatment> mT = new HashMap<String, Treatment>();
		mT.put("ngsrg", t1);
		run.treatments = mT;

		ContextValidation ctxVal = new ContextValidation(Constants.TEST_USER);
		ctxVal.setCreationMode();
		run.validate(ctxVal);

		assertThat(ctxVal.errors).hasSize(0);
	}


	@Test
	public void testError1TreatmentWithoutResult() {
		Run runDelete = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code","YANN_TEST1FORREADSET0"));
		if(runDelete!=null){
			MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runDelete._id);
		}	   
		Run run = getFullRun();

		Treatment t1 = new Treatment();
		t1.code = "ngsrg";
		t1.typeCode = "ngsrg-illumina";
		t1.categoryCode = "ngsrg";
		//define map of single property values
		Map<String,PropertyValue> m = new HashMap<String,PropertyValue>();
		m.put("nbClusterTotal", new PropertySingleValue(100)); // valeur simple
		m.put("nbClusterIlluminaFilter", new PropertySingleValue(100));
		m.put("nbCycle", new PropertySingleValue(100));
		m.put("nbBase", new PropertySingleValue(100));
		m.put("flowcellPosition", new PropertySingleValue("A"));
		m.put("rtaVersion", new PropertySingleValue("v1"));
		m.put("flowcellVersion", new PropertySingleValue("v1"));
		m.put("controlLane", new PropertySingleValue(90));
		m.put("mismatch", new PropertySingleValue(true));
		m.put("percentClusterIlluminaFilter", new PropertySingleValue(96.125));
		t1.set("default", m);

		Treatment t2 = new Treatment();
		t2.code = "sav";
		t2.typeCode = "sav";
		t2.categoryCode = "sequencing";

		Map<String, Treatment> mT = new HashMap<String, Treatment>();
		mT.put("ngsrg", t1);
		mT.put("sav", t2);
		run.treatments = mT;

		ContextValidation ctxVal = new ContextValidation(Constants.TEST_USER);
		ctxVal.setCreationMode();
		run.validate(ctxVal);

		assertThat(ctxVal.errors).hasSize(1);
		assertThat(ctxVal.errors.toString()).contains("treatments.sav.result");
	}


	@Test
	public void testError2IdenticalTreatments() {
		Run runDelete = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code","YANN_TEST1FORREADSET0"));
		if(runDelete!=null){
			MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runDelete._id);
		}	   
		Run run = getFullRun();

		Treatment t1 = new Treatment();
		t1.code = "ngsrg";
		t1.typeCode = "ngsrg-illumina";
		t1.categoryCode = "ngsrg";
		//define map of single property values
		Map<String,PropertyValue> m = new HashMap<String,PropertyValue>();
		m.put("nbClusterTotal", new PropertySingleValue(100)); // valeur simple
		m.put("nbClusterIlluminaFilter", new PropertySingleValue(100));
		m.put("nbCycle", new PropertySingleValue(100));
		m.put("nbBase", new PropertySingleValue(100));
		m.put("flowcellPosition", new PropertySingleValue("A"));
		m.put("rtaVersion", new PropertySingleValue("v1"));
		m.put("flowcellVersion", new PropertySingleValue("v1"));
		m.put("controlLane", new PropertySingleValue(90));
		m.put("mismatch", new PropertySingleValue(true));
		m.put("percentClusterIlluminaFilter", new PropertySingleValue(96.125));
		t1.set("default", m);

		Treatment t2 = new Treatment();
		t2.code = "ngsrg";
		t2.typeCode = "ngsrg-illumina";
		t2.categoryCode = "ngsrg";
		//define map of single property values
		m = new HashMap<String,PropertyValue>();
		m.put("nbClusterTotal", new PropertySingleValue(100)); // valeur simple
		m.put("nbClusterIlluminaFilter", new PropertySingleValue(100));
		m.put("nbCycle", new PropertySingleValue(100));
		m.put("nbBase", new PropertySingleValue(100));
		m.put("flowcellPosition", new PropertySingleValue("A"));
		m.put("rtaVersion", new PropertySingleValue("v1"));
		m.put("flowcellVersion", new PropertySingleValue("v1"));
		m.put("controlLane", new PropertySingleValue(90));
		m.put("mismatch", new PropertySingleValue(true));
		m.put("percentClusterIlluminaFilter", new PropertySingleValue(96.125));
		t2.set("default", m);

		Map<String, Treatment> mT = new HashMap<String, Treatment>();
		mT.put("ngsrg", t1);
		mT.put("ngsrg2", t2);
		run.treatments = mT;

		System.out.println(""); 
		System.out.println(""+mT.size()); 
		System.out.println(""); 

		ContextValidation ctxVal = new ContextValidation(Constants.TEST_USER);
		ctxVal.setCreationMode();
		run.validate(ctxVal);

		assertThat(ctxVal.errors).hasSize(1);
		assertThat(ctxVal.errors.toString()).contains("treatments.ngsrg.code");
	}


	@Test
	public void testCreateLanesValidationOK() {
		Run runDelete = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code","YANN_TEST1FORREADSET0"));
		if(runDelete!=null){
			MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runDelete._id);
		}	   
		Run run = getFullRun();	   
		ArrayList<Lane> l = new ArrayList<Lane>();
		Lane lane = getLane();
		l.add(lane);
		lane = getLane2();
		l.add(lane);
		run.lanes = l;
		ContextValidation ctxVal = new ContextValidation(Constants.TEST_USER); 
		ctxVal.putObject("run", run);
		ctxVal.setCreationMode();
		LaneValidationHelper.validationLanes(run.lanes, ctxVal);
		assertThat(ctxVal.errors).hasSize(0);
	}

	@Test
	public void testCreateLaneValidationOK(){
		Run runDelete = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code","YANN_TEST1FORREADSET0"));
		if(runDelete!=null){
			MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runDelete._id);
		}	   
		Run run = getFullRun();
		ArrayList<Lane> l = new ArrayList<Lane>();
		Lane lane = getLane();
		l.add(lane);
		run.lanes = l;		 
		ContextValidation ctxVal = new ContextValidation(Constants.TEST_USER);
		ctxVal.putObject("run", run);	 
		ctxVal.setCreationMode();
		run.lanes.get(0).validate(ctxVal);
		assertThat(ctxVal.errors).hasSize(0);
	}

	@Test
	public void testErrorLanesValidation() {
		// run with 2 identical lanes
		Run runDelete = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code","YANN_TEST1FORREADSET0"));
		if(runDelete!=null){
			MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runDelete._id);
		}	   
		Run run = getFullRun();
		ArrayList<Lane> l = new ArrayList<Lane>();
		Lane lane = getLane();
		l.add(lane);
		lane = getLane();
		l.add(lane); // adding again the same lane ! 
		run.lanes = l;
		ContextValidation ctxVal = new ContextValidation(Constants.TEST_USER); 
		ctxVal.putObject("run", run);
		ctxVal.setCreationMode();
		LaneValidationHelper.validationLanes(run.lanes, ctxVal);

		assertThat(ctxVal.errors).hasSize(1);
		assertThat(ctxVal.errors.toString()).contains(ValidationConstants.ERROR_NOTUNIQUE_MSG); 
	}

	@Test
	public void testReadSetCodeLaneValidationError(){
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
		Lane lane = getLane();

		ReadSet r = getNewReadSet();
		ArrayList<String> a = new ArrayList<String>();
		//a.add(r.code);			 
		lane.readSetCodes = a;
		r.runCode = run.code;

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
		assertThat(ctxVal.errors).hasSize(0);

		result = callAction(controllers.readsets.api.routes.ref.ReadSets.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonReadSet(r)));
		assertThat(status(result)).isEqualTo(OK);		

		a.add(r.code);
		a.add("toto");
		run.lanes.get(0).readSetCodes = a;

		ctxVal = new ContextValidation(Constants.TEST_USER);
		ctxVal.putObject("run", run);
		ctxVal.setUpdateMode();
		run.lanes.get(0).validate(ctxVal);
		assertThat(ctxVal.errors).hasSize(1); 
		assertThat(ctxVal.errors.toString()).contains("toto");
	}

	
	private Lane getLane() {	
		Lane lane = new Lane();
		lane.number = 1;
		lane.valuation = RunMockHelper.getValuation(TBoolean.TRUE);
		lane.readSetCodes = null;
		return lane;
	}

	private Lane getEmptyLane(){
		Lane lane = new Lane();
		lane.number = 0;
		List<String> r = new ArrayList<String>();
		r.add("X"); 
		lane.readSetCodes = r;
		Valuation v = new Valuation();
		lane.valuation = v;
		return lane;
	}

	private Lane getLane2(){
		Lane lane = new Lane();
		lane.number = 3;
		Valuation v = new Valuation();
		lane.valuation = v;
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
		
		//Add properties
		run.properties.put("sequencingProgramType",new PropertySingleValue("SR"));
		List<String> valuesProcessTypeCodes = new ArrayList<>();
		valuesProcessTypeCodes.add("CA");
		run.properties.put("libProcessTypeCodes",new PropertyListValue(valuesProcessTypeCodes));
		
		
		return run;
	}

	private Run getFullRunWithBadProjectCode() {
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
		Set<String> t = new TreeSet<String>();
		t.add("codeProjetBidon");
		t.add("codeProjetBidon2");
		run.projectCodes = t;		
		run.sampleCodes = t;
		return run;
	}

	private Run getEmptyRun() {
		Run run = new Run();
		run.code = "";
		run.containerSupportCode = "";
		run.dispatch = null;
		run.instrumentUsed = null;
		run.typeCode = "";
		run.valuation = null;
		run.state = null;
		run.traceInformation = null;
		return run;
	}
}
