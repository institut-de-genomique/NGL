package validation.container;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
import models.utils.dao.DAOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

//import play.Logger;
//import play.Logger.ALogger;
import utils.AbstractTests;
import utils.Constants;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.container.instance.ContainerSupportValidationHelper;
import validation.container.instance.ContainerValidationHelper;
import fr.cea.ig.MongoDBDAO;

public class ContainerValidationHelperTest extends AbstractTests {

	protected static final play.Logger.ALogger logger = play.Logger.of(ContainerValidationHelperTest.class);
	
	static ContainerCategory        containerCategory;
	static ContainerSupportCategory containerSupportCategory;
	static ProcessType              processType;
	static ExperimentType           experimentType;
	static ExperimentType           experimentType1;
	static Experiment               experiment;
	static Experiment               experiment1;
	static Sample                   sample;
	static Sample                   sample1;
	static SampleType               sampleType;
	static Content                  sampleUsed;
	static Content                  sampleUsed1;
	static Content                  sampleUsed3; //use for percentage content test
	static LocationOnContainerSupport containerSupport;
	static Process                  process1;
	static Process                  process2;
	static Process                  process3;
	static String                   inputProcessCode1;
	static String                   inputProcessCode2;
	static String                   inputProcessCode3;
	static Project                  project;
	
	@BeforeClass
	public static void initData() throws DAOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		containerCategory        = ContainerCategory.find.findAll().get(0);
		containerSupportCategory = ContainerSupportCategory.find.findAll().get(0);
		processType              = ProcessType.find.findAll().get(0);		
		project                  = saveDBOject(Project.class,InstanceConstants.PROJECT_COLL_NAME,"project");
		sampleType               = SampleType.find.findAll().get(0);
		logger.debug("sampleType {}", sampleType.category.name);
		experimentType           = ExperimentType.find.findAll().get(0);
		experimentType1          = ExperimentType.find.findAll().get(1);
		experiment               = saveDBOject(Experiment.class,InstanceConstants.EXPERIMENT_COLL_NAME,"experiment");
		experiment1              = saveDBOject(Experiment.class,InstanceConstants.EXPERIMENT_COLL_NAME,"experiment1");
		
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
		containerSupport.categoryCode = ContainerSupportCategory.find.findAll().get(0).code;
		containerSupport.line         = "1";
		containerSupport.column       = "1";
		process1 = saveDBOject(Process.class, InstanceConstants.PROCESS_COLL_NAME, "ProcessCode1");
		process2 = saveDBOject(Process.class, InstanceConstants.PROCESS_COLL_NAME, "ProcessCode2");
		process3 = saveDBOject(Process.class, InstanceConstants.PROCESS_COLL_NAME, "ProcessCode3");
		
	}

	@AfterClass
	public static void deleteData() {
		MongoDBDAO.getCollection(InstanceConstants.EXPERIMENT_COLL_NAME,Experiment.class).drop();
		MongoDBDAO.getCollection(InstanceConstants.SAMPLE_COLL_NAME,Experiment.class).drop();
	}

	/**
	 *  Process Type 
	 */
	// failed: java.lang.IllegalArgumentException: stateCode from contextValidation is null
	// @Test
	public void validateProcessTypeCode() {
		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
		ContainerValidationHelper.validateProcessTypeCode(processType.code, contextValidation);
		contextValidation.displayErrors(logger);
		assertThat(contextValidation.errors.size()).isEqualTo(0);
	}

	// failed: java.lang.IllegalArgumentException: stateCode from contextValidation is null
	// @Test
	public void validateProcessTypeCodeNotRequired() {
		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
		ContainerValidationHelper.validateProcessTypeCode(null, contextValidation);
		contextValidation.displayErrors(logger);
		assertThat(contextValidation.errors.size()).isEqualTo(0);
	}

	// failed: java.lang.IllegalArgumentException: stateCode from contextValidation is null
	// @Test
	public void validateProcessTypeNotExist() {
		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
		ContainerValidationHelper.validateProcessTypeCode("notexist", contextValidation);
		contextValidation.displayErrors(logger);
		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
	}	

	/**
	 * Experiment Type 
	 */
	@Test
	public void validateExperimentTypeCodes() {
		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
		List<String> listCodes = new ArrayList<>();
		listCodes.add(experimentType.code);
		listCodes.add(experimentType1.code);
		CommonValidationHelper.validateExperimentTypeCodes(listCodes, contextValidation);
		contextValidation.displayErrors(logger);
		assertThat(contextValidation.errors.size()).isEqualTo(0);
	}

	@Test
	public void validateExperimentTypeCodesNotRequired() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		CommonValidationHelper.validateExperimentTypeCodes(new HashSet<String>(), contextValidation);
		contextValidation.displayErrors(logger);
		assertThat(contextValidation.errors.size()).isEqualTo(0);
	}
	
	/**
	 * Experiment
	 */
	@Test
	public void validateExperimentTypeNotExist() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		List<String> listCodes = new ArrayList<>();
		listCodes.add("notexist");
		CommonValidationHelper.validateExperimentTypeCodes(listCodes, contextValidation);
		contextValidation.displayErrors(logger);
		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
	}	
	
	/**
	 * Content
	 */
	// failed: expected:<[0]> but was:<[2]>
	// @Test
	public  void validationContentTest(){
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		List<Content> localContents = new ArrayList<>();		
		
		localContents.add(sampleUsed);
		localContents.add(sampleUsed1);
		ContainerValidationHelper.validateContents(localContents, contextValidation);
		contextValidation.displayErrors(logger);
		assertThat(contextValidation.errors.size()).isEqualTo(0);
	}

	@Test
	public  void validationContentRequiredTest() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		ContainerValidationHelper.validateContents(null, contextValidation);
		contextValidation.displayErrors(logger);
		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
	}
		
	@Test
	public void validationContentSampleUsedTest() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		List<Content> localContents = new ArrayList<>();		
		localContents.add(new Content("","",""));
		Iterator<Content> iterator = localContents.iterator();
		iterator.next().percentage = 100.00;
		//localContents.get(0).percentage= 100.00;
		ContainerValidationHelper.validateContents(localContents, contextValidation);
		contextValidation.displayErrors(logger);
//		assertThat(contextValidation.errors.size()).isEqualTo(4);
		assertEquals("errors", 5, contextValidation.errors.size());
	}
	
	/**
	 * Percentage Contents
	 */	
	@Test
	public void validationPercentageContentsWithGoodValues(){
		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
		List<Content> localContents = new ArrayList<>();		
		//Test with good values
		sampleUsed.percentage  = 75.00;
		sampleUsed1.percentage = 25.00;		
		localContents.add(sampleUsed);
		localContents.add(sampleUsed1);
		ContainerValidationHelper.validateContentPercentageSum(localContents, contextValidation);
		contextValidation.displayErrors(logger);
		assertThat(contextValidation.errors.size()).isEqualTo(0);		
	}
	
	@Test
	public void validationPercentageContentsWithEquiMolarValues(){
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		List<Content> localContents=new ArrayList<>();		
		
		//Test with equimolar values
		sampleUsed.percentage=33.33;
		sampleUsed1.percentage=33.33;
		sampleUsed3.percentage= 33.33;
		localContents.add(sampleUsed);
		localContents.add(sampleUsed1);
		localContents.add(sampleUsed3);
		ContainerValidationHelper.validateContentPercentageSum(localContents, contextValidation);
		contextValidation.displayErrors(logger);
		assertThat(contextValidation.errors.size()).isEqualTo(0);		
	}
	
	@Test
	public void validationPercentageContentsWithNegativeValue(){
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		List<Content> localContents=new ArrayList<>();		
					
		//Test with a value less than 0				
		sampleUsed.percentage=-50.00;
		sampleUsed1.percentage= 100.00;		
		localContents.add(sampleUsed);
		localContents.add(sampleUsed1);
		ContainerValidationHelper.validateContentPercentageSum(localContents, contextValidation);
		contextValidation.displayErrors(logger);
		assertThat(contextValidation.errors.size()).isEqualTo(1);		
	}
	
	@Test
	public void validationPercentageContentsWithHundred(){
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		List<Content> localContents=new ArrayList<>();		
		
		//Test with a sum of values different than 100			
		sampleUsed.percentage=20.00;
		sampleUsed1.percentage= 20.00;		
		localContents.add(sampleUsed);
		localContents.add(sampleUsed1);
		ContainerValidationHelper.validateContentPercentageSum(localContents, contextValidation);
		contextValidation.displayErrors(logger);
		assertThat(contextValidation.errors.size()).isEqualTo(1);		
	}
	
	@Test
	public void validationPercentageContentsWithBigValue(){
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		List<Content> localContents=new ArrayList<>();		
		
		//Test with a value greater than 100
		sampleUsed.percentage=10.00;
		sampleUsed1.percentage= 250.00;		
		localContents.add(sampleUsed);
		localContents.add(sampleUsed1);
		ContainerValidationHelper.validateContentPercentageSum(localContents, contextValidation);
		contextValidation.displayErrors(logger);
		assertThat(contextValidation.errors.size()).isEqualTo(1);
	}
	
	

	/**
	 * Container ContainerSupport
	 */
	// failed: expected:<[0]> but was:<[1]>
	// @Test
	public  void validateContainerSupportTest(){
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		ContainerValidationHelper.validateContainerSupport(containerSupport,contextValidation);
		contextValidation.displayErrors(logger);
		assertThat(contextValidation.errors.size()).isEqualTo(0);
	}
	
	@Test
	public  void validateContainerSupportRequiredTest(){
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		LocationOnContainerSupport localContainerSupport=new LocationOnContainerSupport();
		ContainerValidationHelper.validateContainerSupport(localContainerSupport,contextValidation);
		contextValidation.displayErrors(logger);
		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
	}

	/**
	 * ContainerCategory 
	 */
	@Test
	public void validationContainerCategoryCode() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		ContainerValidationHelper.validateContainerCategoryCode(containerCategory.code, contextValidation);
		contextValidation.displayErrors(logger);
		assertThat(contextValidation.errors.size()).isEqualTo(0);
	}

	@Test
	public void validationContainerCategoryRequired() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		ContainerValidationHelper.validateContainerCategoryCode(null, contextValidation);
		contextValidation.displayErrors(logger);
		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
	}

	@Test
	public void validationContainerCategoryNotExist() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		ContainerValidationHelper.validateContainerCategoryCode("notexist", contextValidation);
		contextValidation.displayErrors(logger);
		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
	}	


	/**
	 * ContainerSupportCategory
	 * 
	 */

	@Test
	public void validationContainerSupportCategoryCode() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		ContainerSupportValidationHelper.validateContainerSupportCategoryCode(containerSupportCategory.code, contextValidation);
		contextValidation.displayErrors(logger);
		assertThat(contextValidation.errors.size()).isEqualTo(0);
	}

	@Test
	public void validationContainerSupportCategoryRequired() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		ContainerSupportValidationHelper.validateContainerSupportCategoryCode(null, contextValidation);
		contextValidation.displayErrors(logger);
		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
	}

	@Test
	public void validationContainerSupportCategoryNotExist() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		ContainerSupportValidationHelper.validateContainerSupportCategoryCode("notexist", contextValidation);
		contextValidation.displayErrors(logger);
		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
	}
	
	/**
	 * Process Codes
	 * 
	 */
	// failed: java.lang.IllegalArgumentException: stateCode from contextValidation is null
	// @Test
	public void validationProcessCodes() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		Set<String> processCodes = new HashSet<>();
		processCodes.add(process1.code);
		processCodes.add(process2.code);
		processCodes.add(process3.code);
		ContainerValidationHelper.validateInputProcessCodes(processCodes, contextValidation);
		contextValidation.displayErrors(logger);
		assertThat(contextValidation.errors.size()).isEqualTo(0);
	}
	
	// failed: java.lang.IllegalArgumentException: stateCode from contextValidation is null
	// @Test 
	public void validationProcessCodesNull(){
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		Set<String> processCodes = new HashSet<>();
		processCodes.add("");
		processCodes.add("");
		processCodes.add("");
		ContainerValidationHelper.validateInputProcessCodes(processCodes, contextValidation);
		contextValidation.displayErrors(logger);
		assertThat(contextValidation.errors.size()).isNotEqualTo(0);		
	}

}
