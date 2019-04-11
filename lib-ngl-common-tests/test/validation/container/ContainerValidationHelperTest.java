package validation.container;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static utils.TestHelper.saveDBOject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.util.function.CC1;
import fr.cea.ig.util.function.CC1Managed;
import fr.cea.ig.util.function.CCActions;
import models.laboratory.container.description.ContainerCategory;
import models.laboratory.container.description.ContainerSupportCategory;
import models.laboratory.container.instance.Content;
import models.laboratory.container.instance.LocationOnContainerSupport;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.processes.description.ProcessType;
import models.laboratory.processes.instance.Process;
import models.laboratory.project.instance.Project;
import models.laboratory.sample.description.SampleType;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import ngl.common.Global;
import utils.Constants;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.container.instance.ContainerSupportValidationHelper;
import validation.container.instance.ContainerValidationHelper;

public class ContainerValidationHelperTest {

	protected static final play.Logger.ALogger logger = play.Logger.of(ContainerValidationHelperTest.class);
	
	private static class TestContext implements CC1Managed {

		ContainerCategory        containerCategory;
		ContainerSupportCategory containerSupportCategory;
		ProcessType              processType;
		ExperimentType           experimentType;
		ExperimentType           experimentType1;
//		Experiment               experiment;
//		Experiment               experiment1;
		Sample                   sample;
		Sample                   sample1;
		SampleType               sampleType;
		Content                  sampleUsed;
		Content                  sampleUsed1;
		Content                  sampleUsed3; //use for percentage content test
		LocationOnContainerSupport containerSupport;
		Process                  process1;
		Process                  process2;
		Process                  process3;
//		String                   inputProcessCode1;
//		String                   inputProcessCode2;
//		String                   inputProcessCode3;
		Project                  project;

		@Override
		public void setUp() throws Exception {
			containerCategory        = ContainerCategory.find.get().findAll().get(0);
			containerSupportCategory = ContainerSupportCategory.find.get().findAll().get(0);
			processType              = ProcessType.find.get().findAll().get(0);		
			project                  = saveDBOject(Project.class,InstanceConstants.PROJECT_COLL_NAME,"project");
			sampleType               = SampleType.find.get().findAll().get(0);
			logger.debug("sampleType {}", sampleType.category.name);
			experimentType           = ExperimentType.find.get().findAll().get(0);
			experimentType1          = ExperimentType.find.get().findAll().get(1);
//			experiment               = 
					saveDBOject(Experiment.class,InstanceConstants.EXPERIMENT_COLL_NAME,"experiment");
//			experiment1              = 
					saveDBOject(Experiment.class,InstanceConstants.EXPERIMENT_COLL_NAME,"experiment1");

			sample                   = saveDBOject(Sample.class,InstanceConstants.SAMPLE_COLL_NAME,"sample");
			sample.typeCode          = sampleType.code;
			sample.categoryCode      = sampleType.category.code;

			sample1                  = saveDBOject(Sample.class,InstanceConstants.SAMPLE_COLL_NAME,"sample1");
			sample1.typeCode         = sampleType.code;
			sample1.categoryCode     = sampleType.category.code;

			sampleUsed = new Content(sample.code,sample.typeCode,sample.categoryCode);
			sampleUsed.percentage  = 80.0;
			sampleUsed.projectCode = project.code;
			sampleUsed1 = new Content(sample1.code,sample1.typeCode, sample1.categoryCode);
			sampleUsed1.projectCode = project.code;
			sampleUsed1.percentage  = 20.0;
			sampleUsed3 = new Content();

			containerSupport = new LocationOnContainerSupport();
			containerSupport.code         = "test";
			containerSupport.categoryCode = ContainerSupportCategory.find.get().findAll().get(0).code;
			containerSupport.line         = "1";
			containerSupport.column       = "1";
			process1 = saveDBOject(Process.class, InstanceConstants.PROCESS_COLL_NAME, "ProcessCode1");
			process2 = saveDBOject(Process.class, InstanceConstants.PROCESS_COLL_NAME, "ProcessCode2");
			process3 = saveDBOject(Process.class, InstanceConstants.PROCESS_COLL_NAME, "ProcessCode3");

		}

		@Override
		public void tearDown() {
			MongoDBDAO.getCollection(InstanceConstants.EXPERIMENT_COLL_NAME,Experiment.class).drop();
			MongoDBDAO.getCollection(InstanceConstants.SAMPLE_COLL_NAME,Experiment.class).drop();
		}

	}
	
	private static final CC1<TestContext> af =
			Global.afSq.cc1()
			.and(CCActions.managed(TestContext::new))
			.cc1((app,ctx) -> ctx);
	
	/*
	 * Process Type 
	 */
	// failed: java.lang.IllegalArgumentException: stateCode from contextValidation is null
	// @Test
	public void validateProcessTypeCode() throws Exception {
		af.accept(ctx -> {
			ContextValidation contextValidation = ContextValidation.createUndefinedContext(Constants.TEST_USER);
			ContainerValidationHelper.validateProcessTypeCode(ctx.processType.code, contextValidation);
			contextValidation.displayErrors(logger);
			assertThat(contextValidation.getErrors().size()).isEqualTo(0);
		});
	}

	// failed: java.lang.IllegalArgumentException: stateCode from contextValidation is null
	// @Test
	public void validateProcessTypeCodeNotRequired() {
		ContextValidation contextValidation = ContextValidation.createUndefinedContext(Constants.TEST_USER);
		ContainerValidationHelper.validateProcessTypeCode(null, contextValidation);
		contextValidation.displayErrors(logger);
		assertThat(contextValidation.getErrors().size()).isEqualTo(0);
	}

	// failed: java.lang.IllegalArgumentException: stateCode from contextValidation is null
	// @Test
	public void validateProcessTypeNotExist() throws Exception {
		af.accept(ctx -> {
			ContextValidation contextValidation = ContextValidation.createUndefinedContext(Constants.TEST_USER);
			ContainerValidationHelper.validateProcessTypeCode("notexist", contextValidation);
			contextValidation.displayErrors(logger);
			assertThat(contextValidation.getErrors().size()).isNotEqualTo(0);
		});
	}	

	/*
	 * Experiment Type 
	 */
	@Test
	public void validateExperimentTypeCodes() throws Exception {
		af.accept(ctx -> {
			ContextValidation contextValidation = ContextValidation.createUndefinedContext(Constants.TEST_USER);
			List<String> listCodes = new ArrayList<>();
			listCodes.add(ctx.experimentType.code);
			listCodes.add(ctx.experimentType1.code);
			CommonValidationHelper.validateExperimentTypeCodes(contextValidation, listCodes);
			contextValidation.displayErrors(logger);
			assertThat(contextValidation.getErrors().size()).isEqualTo(0);
		});
	}

	@Test
	public void validateExperimentTypeCodesNotRequired() throws Exception {
		af.accept(ctx -> {
			ContextValidation contextValidation = ContextValidation.createUndefinedContext(Constants.TEST_USER);
			CommonValidationHelper.validateExperimentTypeCodes(contextValidation, new HashSet<String>());
			contextValidation.displayErrors(logger);
			assertThat(contextValidation.getErrors().size()).isEqualTo(0);
		});
	}
	
	/*
	 * Experiment
	 */
	@Test
	public void validateExperimentTypeNotExist() throws Exception {
		af.accept(ctx -> {
			ContextValidation contextValidation = ContextValidation.createUndefinedContext(Constants.TEST_USER);
			List<String> listCodes = new ArrayList<>();
			listCodes.add("notexist");
			CommonValidationHelper.validateExperimentTypeCodes(contextValidation, listCodes);
			contextValidation.displayErrors(logger);
			assertThat(contextValidation.getErrors().size()).isNotEqualTo(0);
		});
	}	
	
	/*
	 * Content
	 */
	// failed: expected:<[0]> but was:<[2]>
	// @Test
	public  void validationContentTest() throws Exception {
		af.accept(ctx -> {
			ContextValidation contextValidation = ContextValidation.createUndefinedContext(Constants.TEST_USER);
			List<Content> localContents = new ArrayList<>();		

			localContents.add(ctx.sampleUsed);
			localContents.add(ctx.sampleUsed1);
//			ContainerValidationHelper.validateContents(contextValidation, localContents);
			ContainerValidationHelper.validateContents(contextValidation, localContents, null);
			contextValidation.displayErrors(logger);
			assertThat(contextValidation.getErrors().size()).isEqualTo(0);
		});
	}

	@Test
	public  void validationContentRequiredTest() throws Exception {
		af.accept(ctx -> { 
			ContextValidation contextValidation = ContextValidation.createUndefinedContext(Constants.TEST_USER);
//			ContainerValidationHelper.validateContents(contextValidation, null);
			ContainerValidationHelper.validateContents(contextValidation, null, null);
			contextValidation.displayErrors(logger);
			assertThat(contextValidation.getErrors().size()).isNotEqualTo(0);
		});
	}
		
	@Test
	public void validationContentSampleUsedTest() throws Exception {
		af.accept(ctx -> {
			ContextValidation contextValidation = ContextValidation.createUndefinedContext(Constants.TEST_USER);
			List<Content> localContents = new ArrayList<>();		
			localContents.add(new Content("","",""));
			Iterator<Content> iterator = localContents.iterator();
			iterator.next().percentage = 100.00;
			//localContents.get(0).percentage= 100.00;
//			ContainerValidationHelper.validateContents(contextValidation, localContents);
			ContainerValidationHelper.validateContents(contextValidation, localContents, null);
			contextValidation.displayErrors(logger);
			//		assertThat(contextValidation.errors.size()).isEqualTo(4);
			assertEquals("errors", 5, contextValidation.getErrors().size());
		});
	}
	
	/*
	 * Percentage Contents
	 */	
	@Test
	public void validationPercentageContentsWithGoodValues() throws Exception {
		af.accept(ctx -> {
			ContextValidation contextValidation = ContextValidation.createUndefinedContext(Constants.TEST_USER);
			List<Content> localContents = new ArrayList<>();		
			//Test with good values
			ctx.sampleUsed.percentage  = 75.00;
			ctx.sampleUsed1.percentage = 25.00;		
			localContents.add(ctx.sampleUsed);
			localContents.add(ctx.sampleUsed1);
			ContainerValidationHelper.validateContentPercentageSum(localContents, contextValidation);
			contextValidation.displayErrors(logger);
			assertThat(contextValidation.getErrors().size()).isEqualTo(0);		
		});
	}
	
	@Test
	public void validationPercentageContentsWithEquiMolarValues() throws Exception {
		af.accept(ctx -> {
			ContextValidation contextValidation = ContextValidation.createUndefinedContext(Constants.TEST_USER);
			List<Content> localContents = new ArrayList<>();		

			// Test with equimolar values
			ctx.sampleUsed.percentage  = 33.33;
			ctx.sampleUsed1.percentage = 33.33;
			ctx.sampleUsed3.percentage = 33.33;
			localContents.add(ctx.sampleUsed);
			localContents.add(ctx.sampleUsed1);
			localContents.add(ctx.sampleUsed3);
			ContainerValidationHelper.validateContentPercentageSum(localContents, contextValidation);
			contextValidation.displayErrors(logger);
			assertThat(contextValidation.getErrors().size()).isEqualTo(0);
		});
	}
	
	@Test
	public void validationPercentageContentsWithNegativeValue() throws Exception {
		af.accept(ctx -> {
			ContextValidation contextValidation = ContextValidation.createUndefinedContext(Constants.TEST_USER);
			List<Content> localContents = new ArrayList<>();		

			//Test with a value less than 0				
			ctx.sampleUsed.percentage=-50.00;
			ctx.sampleUsed1.percentage= 100.00;		
			localContents.add(ctx.sampleUsed);
			localContents.add(ctx.sampleUsed1);
			ContainerValidationHelper.validateContentPercentageSum(localContents, contextValidation);
			contextValidation.displayErrors(logger);
			assertThat(contextValidation.getErrors().size()).isEqualTo(1);
		});
	}
	
	@Test
	public void validationPercentageContentsWithHundred() throws Exception {
		af.accept(ctx -> {
			ContextValidation contextValidation = ContextValidation.createUndefinedContext(Constants.TEST_USER);
			List<Content> localContents = new ArrayList<>();		

			//Test with a sum of values different than 100			
			ctx.sampleUsed.percentage  = 20.00;
			ctx.sampleUsed1.percentage = 20.00;		
			localContents.add(ctx.sampleUsed);
			localContents.add(ctx.sampleUsed1);
			ContainerValidationHelper.validateContentPercentageSum(localContents, contextValidation);
			contextValidation.displayErrors(logger);
			assertThat(contextValidation.getErrors().size()).isEqualTo(1);
		});
	}
	
	@Test
	public void validationPercentageContentsWithBigValue() throws Exception {
		af.accept(ctx -> {
			ContextValidation contextValidation = ContextValidation.createUndefinedContext(Constants.TEST_USER);
			List<Content> localContents = new ArrayList<>();		

			//Test with a value greater than 100
			ctx.sampleUsed.percentage  =  10.00;
			ctx.sampleUsed1.percentage = 250.00;		
			localContents.add(ctx.sampleUsed);
			localContents.add(ctx.sampleUsed1);
			ContainerValidationHelper.validateContentPercentageSum(localContents, contextValidation);
			contextValidation.displayErrors(logger);
			assertThat(contextValidation.getErrors().size()).isEqualTo(1);
		});
	}
	
	/*
	 * Container ContainerSupport
	 */
	// failed: expected:<[0]> but was:<[1]>
	// @Test
	public  void validateContainerSupportTest() throws Exception {
		af.accept(ctx -> {
			ContextValidation contextValidation = ContextValidation.createUndefinedContext(Constants.TEST_USER);
			ContainerValidationHelper.validateContainerSupportRequired(contextValidation,ctx.containerSupport);
			contextValidation.displayErrors(logger);
			assertThat(contextValidation.getErrors().size()).isEqualTo(0);
		});
	}
	
	@Test
	public void validateContainerSupportRequiredTest() throws Exception {
		af.accept(ctx -> {
			ContextValidation contextValidation = ContextValidation.createUndefinedContext(Constants.TEST_USER);
			LocationOnContainerSupport localContainerSupport = new LocationOnContainerSupport();
			ContainerValidationHelper.validateContainerSupportRequired(contextValidation,localContainerSupport);
			contextValidation.displayErrors(logger);
			assertThat(contextValidation.getErrors().size()).isNotEqualTo(0);
		});
	}

	/*
	 * ContainerCategory 
	 */
	// @Test
	public void validationContainerCategoryCode() throws Exception {
		af.accept(ctx -> {
			ContextValidation contextValidation = ContextValidation.createUndefinedContext(Constants.TEST_USER);
			ContainerValidationHelper.validateContainerCategoryCodeRequired(contextValidation, ctx.containerCategory.code, "");
			contextValidation.displayErrors(logger);
			assertThat(contextValidation.getErrors().size()).isEqualTo(0);
		});
	}

	// FIXME: failed: org.springframework.dao.EmptyResultDataAccessException: Incorrect result size: expected 1 
	// @Test
	public void validationContainerCategoryRequired() throws Exception {
		af.accept(ctx -> {
			ContextValidation contextValidation = ContextValidation.createUndefinedContext(Constants.TEST_USER);
			ContainerValidationHelper.validateContainerCategoryCodeRequired(contextValidation, null, "");
			contextValidation.displayErrors(logger);
			assertThat(contextValidation.getErrors().size()).isNotEqualTo(0);
		});
	}

	// FIXME: failed: org.springframework.dao.EmptyResultDataAccessException: Incorrect result size: expected 1
	// @Test
	public void validationContainerCategoryNotExist() throws Exception {
		af.accept(ctx -> {
			ContextValidation contextValidation = ContextValidation.createUndefinedContext(Constants.TEST_USER);
			ContainerValidationHelper.validateContainerCategoryCodeRequired(contextValidation, "notexist", "");
			contextValidation.displayErrors(logger);
			assertThat(contextValidation.getErrors().size()).isNotEqualTo(0);
		});
	}	


	/*
	 * ContainerSupportCategory 
	 */
	@Test
	public void validationContainerSupportCategoryCode() throws Exception {
		af.accept(ctx -> {
			ContextValidation contextValidation = ContextValidation.createUndefinedContext(Constants.TEST_USER);
			ContainerSupportValidationHelper.validateContainerSupportCategoryCodeRequired(contextValidation, ctx.containerSupportCategory.code);
			contextValidation.displayErrors(logger);
			assertThat(contextValidation.getErrors().size()).isEqualTo(0);
		});
	}

	
	// @Test
	public void validationContainerSupportCategoryRequired() {
		ContextValidation contextValidation = ContextValidation.createUndefinedContext(Constants.TEST_USER);
		ContainerSupportValidationHelper.validateContainerSupportCategoryCodeRequired(contextValidation, null);
		contextValidation.displayErrors(logger);
		assertThat(contextValidation.getErrors().size()).isNotEqualTo(0);
	}

	// @Test
	public void validationContainerSupportCategoryNotExist() {
		ContextValidation contextValidation = ContextValidation.createUndefinedContext(Constants.TEST_USER);
		ContainerSupportValidationHelper.validateContainerSupportCategoryCodeRequired(contextValidation, "notexist");
		contextValidation.displayErrors(logger);
		assertThat(contextValidation.getErrors().size()).isNotEqualTo(0);
	}
	
	/*
	 * Process Codes 
	 */
	// failed: java.lang.IllegalArgumentException: stateCode from contextValidation is null
	// @Test
	public void validationProcessCodes() throws Exception {
		af.accept(ctx -> {
			ContextValidation contextValidation = ContextValidation.createUndefinedContext(Constants.TEST_USER);
			Set<String> processCodes = new HashSet<>();
			processCodes.add(ctx.process1.code);
			processCodes.add(ctx.process2.code);
			processCodes.add(ctx.process3.code);
			ContainerValidationHelper.validateInputProcessCodes(contextValidation, processCodes, "LOL");
			contextValidation.displayErrors(logger);
			assertThat(contextValidation.getErrors().size()).isEqualTo(0);
		});
	}
	
	// failed: java.lang.IllegalArgumentException: stateCode from contextValidation is null
	// @Test 
	public void validationProcessCodesNull() {
		ContextValidation contextValidation = ContextValidation.createUndefinedContext(Constants.TEST_USER);
		Set<String> processCodes = new HashSet<>();
		processCodes.add("");
		processCodes.add("");
		processCodes.add("");
		ContainerValidationHelper.validateInputProcessCodes(contextValidation, processCodes, "LOL");
		contextValidation.displayErrors(logger);
		assertThat(contextValidation.getErrors().size()).isNotEqualTo(0);		
	}

}


//public class ContainerValidationHelperTest {
//
//	protected static final play.Logger.ALogger logger = play.Logger.of(ContainerValidationHelperTest.class);
//	
//	static ContainerCategory        containerCategory;
//	static ContainerSupportCategory containerSupportCategory;
//	static ProcessType              processType;
//	static ExperimentType           experimentType;
//	static ExperimentType           experimentType1;
//	static Experiment               experiment;
//	static Experiment               experiment1;
//	static Sample                   sample;
//	static Sample                   sample1;
//	static SampleType               sampleType;
//	static Content                  sampleUsed;
//	static Content                  sampleUsed1;
//	static Content                  sampleUsed3; //use for percentage content test
//	static LocationOnContainerSupport containerSupport;
//	static Process                  process1;
//	static Process                  process2;
//	static Process                  process3;
//	static String                   inputProcessCode1;
//	static String                   inputProcessCode2;
//	static String                   inputProcessCode3;
//	static Project                  project;
//	
//	@BeforeClass
//	public static void initData() throws DAOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
//		containerCategory        = ContainerCategory.find.get().findAll().get(0);
//		containerSupportCategory = ContainerSupportCategory.find.get().findAll().get(0);
//		processType              = ProcessType.find.get().findAll().get(0);		
//		project                  = saveDBOject(Project.class,InstanceConstants.PROJECT_COLL_NAME,"project");
//		sampleType               = SampleType.find.get().findAll().get(0);
//		logger.debug("sampleType {}", sampleType.category.name);
//		experimentType           = ExperimentType.find.get().findAll().get(0);
//		experimentType1          = ExperimentType.find.get().findAll().get(1);
//		experiment               = saveDBOject(Experiment.class,InstanceConstants.EXPERIMENT_COLL_NAME,"experiment");
//		experiment1              = saveDBOject(Experiment.class,InstanceConstants.EXPERIMENT_COLL_NAME,"experiment1");
//		
//		sample                   = saveDBOject(Sample.class,InstanceConstants.SAMPLE_COLL_NAME,"sample");
//		sample.typeCode          = sampleType.code;
//		sample.categoryCode      = sampleType.category.code;
//		
//		sample1                  = saveDBOject(Sample.class,InstanceConstants.SAMPLE_COLL_NAME,"sample1");
//		sample1.typeCode         = sampleType.code;
//		sample1.categoryCode     = sampleType.category.code;
//				
//		sampleUsed = new Content(sample.code,sample.typeCode,sample.categoryCode);
//		sampleUsed.percentage  = 80.0;
//		sampleUsed.projectCode = project.code;
//		sampleUsed1 = new Content(sample1.code,sample1.typeCode, sample1.categoryCode);
//		sampleUsed1.projectCode = project.code;
//		sampleUsed1.percentage  = 20.0;
//		sampleUsed3 = new Content();
//		
//		containerSupport = new LocationOnContainerSupport();
//		containerSupport.code         = "test";
//		containerSupport.categoryCode = ContainerSupportCategory.find.get().findAll().get(0).code;
//		containerSupport.line         = "1";
//		containerSupport.column       = "1";
//		process1 = saveDBOject(Process.class, InstanceConstants.PROCESS_COLL_NAME, "ProcessCode1");
//		process2 = saveDBOject(Process.class, InstanceConstants.PROCESS_COLL_NAME, "ProcessCode2");
//		process3 = saveDBOject(Process.class, InstanceConstants.PROCESS_COLL_NAME, "ProcessCode3");
//		
//	}
//
//	@AfterClass
//	public static void deleteData() {
//		MongoDBDAO.getCollection(InstanceConstants.EXPERIMENT_COLL_NAME,Experiment.class).drop();
//		MongoDBDAO.getCollection(InstanceConstants.SAMPLE_COLL_NAME,Experiment.class).drop();
//	}
//
//	/**
//	 *  Process Type 
//	 */
//	// failed: java.lang.IllegalArgumentException: stateCode from contextValidation is null
//	// @Test
//	public void validateProcessTypeCode() {
//		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
//		ContainerValidationHelper.validateProcessTypeCode(processType.code, contextValidation);
//		contextValidation.displayErrors(logger);
//		assertThat(contextValidation.errors.size()).isEqualTo(0);
//	}
//
//	// failed: java.lang.IllegalArgumentException: stateCode from contextValidation is null
//	// @Test
//	public void validateProcessTypeCodeNotRequired() {
//		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
//		ContainerValidationHelper.validateProcessTypeCode(null, contextValidation);
//		contextValidation.displayErrors(logger);
//		assertThat(contextValidation.errors.size()).isEqualTo(0);
//	}
//
//	// failed: java.lang.IllegalArgumentException: stateCode from contextValidation is null
//	// @Test
//	public void validateProcessTypeNotExist() {
//		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
//		ContainerValidationHelper.validateProcessTypeCode("notexist", contextValidation);
//		contextValidation.displayErrors(logger);
//		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
//	}	
//
//	/**
//	 * Experiment Type 
//	 */
//	@Test
//	public void validateExperimentTypeCodes() {
//		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
//		List<String> listCodes = new ArrayList<>();
//		listCodes.add(experimentType.code);
//		listCodes.add(experimentType1.code);
//		CommonValidationHelper.validateExperimentTypeCodes(listCodes, contextValidation);
//		contextValidation.displayErrors(logger);
//		assertThat(contextValidation.errors.size()).isEqualTo(0);
//	}
//
//	@Test
//	public void validateExperimentTypeCodesNotRequired() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		CommonValidationHelper.validateExperimentTypeCodes(new HashSet<String>(), contextValidation);
//		contextValidation.displayErrors(logger);
//		assertThat(contextValidation.errors.size()).isEqualTo(0);
//	}
//	
//	/**
//	 * Experiment
//	 */
//	@Test
//	public void validateExperimentTypeNotExist() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		List<String> listCodes = new ArrayList<>();
//		listCodes.add("notexist");
//		CommonValidationHelper.validateExperimentTypeCodes(listCodes, contextValidation);
//		contextValidation.displayErrors(logger);
//		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
//	}	
//	
//	/**
//	 * Content
//	 */
//	// failed: expected:<[0]> but was:<[2]>
//	// @Test
//	public  void validationContentTest(){
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		List<Content> localContents = new ArrayList<>();		
//		
//		localContents.add(sampleUsed);
//		localContents.add(sampleUsed1);
//		ContainerValidationHelper.validateContents(localContents, contextValidation);
//		contextValidation.displayErrors(logger);
//		assertThat(contextValidation.errors.size()).isEqualTo(0);
//	}
//
//	@Test
//	public  void validationContentRequiredTest() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		ContainerValidationHelper.validateContents(null, contextValidation);
//		contextValidation.displayErrors(logger);
//		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
//	}
//		
//	@Test
//	public void validationContentSampleUsedTest() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		List<Content> localContents = new ArrayList<>();		
//		localContents.add(new Content("","",""));
//		Iterator<Content> iterator = localContents.iterator();
//		iterator.next().percentage = 100.00;
//		//localContents.get(0).percentage= 100.00;
//		ContainerValidationHelper.validateContents(localContents, contextValidation);
//		contextValidation.displayErrors(logger);
////		assertThat(contextValidation.errors.size()).isEqualTo(4);
//		assertEquals("errors", 5, contextValidation.errors.size());
//	}
//	
//	/**
//	 * Percentage Contents
//	 */	
//	@Test
//	public void validationPercentageContentsWithGoodValues(){
//		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
//		List<Content> localContents = new ArrayList<>();		
//		//Test with good values
//		sampleUsed.percentage  = 75.00;
//		sampleUsed1.percentage = 25.00;		
//		localContents.add(sampleUsed);
//		localContents.add(sampleUsed1);
//		ContainerValidationHelper.validateContentPercentageSum(localContents, contextValidation);
//		contextValidation.displayErrors(logger);
//		assertThat(contextValidation.errors.size()).isEqualTo(0);		
//	}
//	
//	@Test
//	public void validationPercentageContentsWithEquiMolarValues(){
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		List<Content> localContents=new ArrayList<>();		
//		
//		//Test with equimolar values
//		sampleUsed.percentage=33.33;
//		sampleUsed1.percentage=33.33;
//		sampleUsed3.percentage= 33.33;
//		localContents.add(sampleUsed);
//		localContents.add(sampleUsed1);
//		localContents.add(sampleUsed3);
//		ContainerValidationHelper.validateContentPercentageSum(localContents, contextValidation);
//		contextValidation.displayErrors(logger);
//		assertThat(contextValidation.errors.size()).isEqualTo(0);		
//	}
//	
//	@Test
//	public void validationPercentageContentsWithNegativeValue(){
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		List<Content> localContents=new ArrayList<>();		
//					
//		//Test with a value less than 0				
//		sampleUsed.percentage=-50.00;
//		sampleUsed1.percentage= 100.00;		
//		localContents.add(sampleUsed);
//		localContents.add(sampleUsed1);
//		ContainerValidationHelper.validateContentPercentageSum(localContents, contextValidation);
//		contextValidation.displayErrors(logger);
//		assertThat(contextValidation.errors.size()).isEqualTo(1);		
//	}
//	
//	@Test
//	public void validationPercentageContentsWithHundred(){
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		List<Content> localContents=new ArrayList<>();		
//		
//		//Test with a sum of values different than 100			
//		sampleUsed.percentage=20.00;
//		sampleUsed1.percentage= 20.00;		
//		localContents.add(sampleUsed);
//		localContents.add(sampleUsed1);
//		ContainerValidationHelper.validateContentPercentageSum(localContents, contextValidation);
//		contextValidation.displayErrors(logger);
//		assertThat(contextValidation.errors.size()).isEqualTo(1);		
//	}
//	
//	@Test
//	public void validationPercentageContentsWithBigValue(){
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		List<Content> localContents=new ArrayList<>();		
//		
//		//Test with a value greater than 100
//		sampleUsed.percentage=10.00;
//		sampleUsed1.percentage= 250.00;		
//		localContents.add(sampleUsed);
//		localContents.add(sampleUsed1);
//		ContainerValidationHelper.validateContentPercentageSum(localContents, contextValidation);
//		contextValidation.displayErrors(logger);
//		assertThat(contextValidation.errors.size()).isEqualTo(1);
//	}
//	
//	
//
//	/**
//	 * Container ContainerSupport
//	 */
//	// failed: expected:<[0]> but was:<[1]>
//	// @Test
//	public  void validateContainerSupportTest(){
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		ContainerValidationHelper.validateContainerSupport(containerSupport,contextValidation);
//		contextValidation.displayErrors(logger);
//		assertThat(contextValidation.errors.size()).isEqualTo(0);
//	}
//	
//	@Test
//	public  void validateContainerSupportRequiredTest(){
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		LocationOnContainerSupport localContainerSupport=new LocationOnContainerSupport();
//		ContainerValidationHelper.validateContainerSupport(localContainerSupport,contextValidation);
//		contextValidation.displayErrors(logger);
//		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
//	}
//
//	/**
//	 * ContainerCategory 
//	 */
//	@Test
//	public void validationContainerCategoryCode() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		ContainerValidationHelper.validateContainerCategoryCode(containerCategory.code, "", contextValidation);
//		contextValidation.displayErrors(logger);
//		assertThat(contextValidation.errors.size()).isEqualTo(0);
//	}
//
//	@Test
//	public void validationContainerCategoryRequired() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		ContainerValidationHelper.validateContainerCategoryCode(null, "", contextValidation);
//		contextValidation.displayErrors(logger);
//		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
//	}
//
//	@Test
//	public void validationContainerCategoryNotExist() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		ContainerValidationHelper.validateContainerCategoryCode("notexist", "", contextValidation);
//		contextValidation.displayErrors(logger);
//		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
//	}	
//
//
//	/**
//	 * ContainerSupportCategory
//	 * 
//	 */
//
//	@Test
//	public void validationContainerSupportCategoryCode() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		ContainerSupportValidationHelper.validateContainerSupportCategoryCode(containerSupportCategory.code, contextValidation);
//		contextValidation.displayErrors(logger);
//		assertThat(contextValidation.errors.size()).isEqualTo(0);
//	}
//
//	@Test
//	public void validationContainerSupportCategoryRequired() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		ContainerSupportValidationHelper.validateContainerSupportCategoryCode(null, contextValidation);
//		contextValidation.displayErrors(logger);
//		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
//	}
//
//	@Test
//	public void validationContainerSupportCategoryNotExist() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		ContainerSupportValidationHelper.validateContainerSupportCategoryCode("notexist", contextValidation);
//		contextValidation.displayErrors(logger);
//		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
//	}
//	
//	/**
//	 * Process Codes
//	 * 
//	 */
//	// failed: java.lang.IllegalArgumentException: stateCode from contextValidation is null
//	// @Test
//	public void validationProcessCodes() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		Set<String> processCodes = new HashSet<>();
//		processCodes.add(process1.code);
//		processCodes.add(process2.code);
//		processCodes.add(process3.code);
//		ContainerValidationHelper.validateInputProcessCodes(processCodes, contextValidation);
//		contextValidation.displayErrors(logger);
//		assertThat(contextValidation.errors.size()).isEqualTo(0);
//	}
//	
//	// failed: java.lang.IllegalArgumentException: stateCode from contextValidation is null
//	// @Test 
//	public void validationProcessCodesNull(){
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		Set<String> processCodes = new HashSet<>();
//		processCodes.add("");
//		processCodes.add("");
//		processCodes.add("");
//		ContainerValidationHelper.validateInputProcessCodes(processCodes, contextValidation);
//		contextValidation.displayErrors(logger);
//		assertThat(contextValidation.errors.size()).isNotEqualTo(0);		
//	}
//
//}
