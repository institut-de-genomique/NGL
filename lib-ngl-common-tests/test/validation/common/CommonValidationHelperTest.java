package validation.common;

import static org.fest.assertions.Assertions.assertThat;
import static utils.TestHelper.saveDBOject;

import org.junit.Test;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.util.function.CC1;
import fr.cea.ig.util.function.CC1Managed;
import fr.cea.ig.util.function.CCActions;
import models.laboratory.sample.instance.Sample;
import ngl.common.Global;
import utils.Constants;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;

public class CommonValidationHelperTest {
	
	private static final play.Logger.ALogger logger = play.Logger.of(CommonValidationHelperTest.class);
	
	private static final String COLLECTION_NAME = "commonvalidationhelpertest";

	private static class TestContext implements CC1Managed {
		
		@Override
		public void setUp() throws Exception {
		}

		@Override
		public void tearDown() {
			logger.debug("Delete collection {}", COLLECTION_NAME);
			MongoDBDAO.drop(COLLECTION_NAME, Sample.class);
		}
		
	}
	
	private static final CC1<TestContext> af =
			Global.afSq.cc1()
			.and(CCActions.managed(TestContext::new))
			.cc1((app,ctx) -> ctx);

	/* 
	 * Unit test CommonValidationHelper.validateId method
	 * @throws DAOException
	 */
	
	@Test
	public void validateIdCreationMode() throws Exception {
		af.accept(ctx -> {
//			ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//			contextValidation.setCreationMode();
			ContextValidation contextValidation = ContextValidation.createCreationContext(Constants.TEST_USER);
			Sample sample = new Sample();
			CommonValidationHelper.validateIdPrimary(contextValidation, sample);
			assertThat(contextValidation.getErrors().size()).isEqualTo(0);
		});
	}
	
	@Test
	public void validateIdCreationModeError() throws Exception {
		af.accept(ctx -> {
//			ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
//			contextValidation.setCreationMode();
			ContextValidation contextValidation = ContextValidation.createCreationContext(Constants.TEST_USER);
			Sample sample=saveDBOject(Sample.class, COLLECTION_NAME, "validateIdCreationModeError");
			CommonValidationHelper.validateIdPrimary(contextValidation, sample);
			assertThat(contextValidation.getErrors().size()).isEqualTo(1);
		});
	}

	@Test
	public void validateIdUpdateMode() throws Exception {
		af.accept(ctx -> {
//			ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
//			contextValidation.setUpdateMode();
			ContextValidation contextValidation = ContextValidation.createUpdateContext(Constants.TEST_USER);
			Sample sample=saveDBOject(Sample.class, COLLECTION_NAME, "validateIdUpdateMode");
			CommonValidationHelper.validateIdPrimary(contextValidation, sample);
			assertThat(contextValidation.getErrors().size()).isEqualTo(0);
		});
	}
	
	@Test
	public void validateIdUpdateModeError() throws Exception {
		af.accept(ctx -> {
//			ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//			contextValidation.setUpdateMode();
			ContextValidation contextValidation = ContextValidation.createUpdateContext(Constants.TEST_USER);
			Sample sample = new Sample();
			CommonValidationHelper.validateIdPrimary(contextValidation, sample);
			assertThat(contextValidation.getErrors().size()).isEqualTo(1);
		});
	}

	/* 
	 *  Unit test CommonValidationHelper.validateCode 
	 */
	@Test
	public void validateCodeRequired() throws Exception {
		af.accept(ctx -> {
//			ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
			ContextValidation contextValidation = ContextValidation.createUndefinedContext(Constants.TEST_USER);
			Sample sample = new Sample();
			CommonValidationHelper.validateCodePrimary(contextValidation, sample, COLLECTION_NAME);
			assertThat(contextValidation.getErrors().size()).isNotEqualTo(0);
		});
	}
	
	@Test
	public void validateUniqueCode() throws Exception {
		af.accept(ctx -> {
//			ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//			contextValidation.setCreationMode();
			ContextValidation contextValidation = ContextValidation.createCreationContext(Constants.TEST_USER);
			Sample sample = saveDBOject(Sample.class,COLLECTION_NAME, "validateUniqueCode");
			CommonValidationHelper.validateCodePrimary(contextValidation, sample, COLLECTION_NAME);
			assertThat(contextValidation.getErrors().size()).isNotEqualTo(0);
		});
	}
	
	@Test
	public void validateExistCode() throws Exception {
		af.accept(ctx -> {
//			ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//			contextValidation.setUpdateMode();
			ContextValidation contextValidation = ContextValidation.createUpdateContext(Constants.TEST_USER);
			Sample sample = new Sample();
			sample.code = "validateExistCode";
			CommonValidationHelper.validateCodePrimary(contextValidation, sample, COLLECTION_NAME);
			assertThat(contextValidation.getErrors().size()).isNotEqualTo(0);

//			contextValidation.clear();
//			contextValidation = new ContextValidation(Constants.TEST_USER);
//			contextValidation.setUpdateMode();
			contextValidation = ContextValidation.createUpdateContext(Constants.TEST_USER);
			sample = saveDBOject(Sample.class,COLLECTION_NAME, "validateExistCode");
			CommonValidationHelper.validateCodePrimary(contextValidation, sample, COLLECTION_NAME);
			assertThat(contextValidation.getErrors().size()).isEqualTo(0);
		});
	}
	
//	@Test
//	public void validateExistCode2() throws Exception {
//		af.accept(ctx -> {
//			ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
//			Sample sample = new Sample();
//			sample.code = "validateExistCode";
//			CommonValidationHelper.validateCode(sample, COLLECTION_NAME, contextValidation);
//			assertThat(contextValidation.errors.size()).isNotEqualTo(0);
//		});
//	}
	
	/*
	 * State Code
	 * @throws DAOException 
	 */
	
//	@Test
//	public void validationStateCode() throws Exception {
//		af.accept(ctx -> {
////			ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
//			ContextValidation contextValidation = ContextValidation.createUndefinedContext(Constants.TEST_USER);
//			CommonValidationHelper.validateStateCodeRequired(contextValidation, State.find.get().findAll().get(0).code);
//			assertThat(contextValidation.getErrors().size()).isEqualTo(0);
//		});
//	}
//	
//	@Test
//	public void validationStateRequired() throws Exception {
//		af.accept(ctx -> {
////			ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
//			ContextValidation contextValidation = ContextValidation.createUndefinedContext(Constants.TEST_USER);
//			CommonValidationHelper.validateStateCodeRequired(contextValidation, null);
//			assertThat(contextValidation.getErrors().size()).isNotEqualTo(0);
//		});
//	}
//	
//	@Test
//	public void validationStateNotExist() throws Exception {
//		af.accept(ctx -> {
////			ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
//			ContextValidation contextValidation = ContextValidation.createUndefinedContext(Constants.TEST_USER);
//			CommonValidationHelper.validateStateCodeRequired(contextValidation, "notexist");
//			assertThat(contextValidation.getErrors().size()).isNotEqualTo(0);
//		});
//	}
	
/* 
 *
		CommonValidationHelper.validateStateCode(this.stateCode, contextValidation);

		CommonValidationHelper.validateResolution(this.resolutionCode,contextValidation);
		CommonValidationHelper.validateTraceInformation(this.traceInformation, contextValidation);
	CommonValidationHelper.validateProjectCodes(projectCodes, contextValidation);
	CommonValidationHelper.validateSampleCodes(sampleCodes, contextValidation);

*/
}

//package validation.common;
//
//import static org.fest.assertions.Assertions.assertThat;
//import models.laboratory.common.description.State;
//import models.laboratory.sample.instance.Sample;
//import models.utils.dao.DAOException;
//
//import org.junit.AfterClass;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
//import play.Logger;
//import utils.AbstractTests;
//import utils.Constants;
//import validation.ContextValidation;
//import validation.common.instance.CommonValidationHelper;
//import fr.cea.ig.MongoDBDAO;
//
//public class CommonValidationHelperTest extends AbstractTests {
//	
//	
//	private static final String COLLECTION_NAME = "commonvalidationhelpertest";
//
//	@BeforeClass
//	public static  void initData() throws DAOException, InstantiationException, IllegalAccessException, ClassNotFoundException{
//		
//	}
//
//	@AfterClass
//	public static  void deleteData() {
//		Logger.debug("Delete collection "+COLLECTION_NAME);
//		MongoDBDAO.getCollection(COLLECTION_NAME, Sample.class).drop();
//	}
//	
//	
//	/***
//	 * 
//	 * Unit test CommonValidationHelper.validateId method
//	 * @throws DAOException
//	 */
//	
//	@Test
//	public void validateIdCreationMode() throws DAOException {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		contextValidation.setCreationMode();
//		Sample sample=new Sample();
//    	CommonValidationHelper.validateId(sample, contextValidation);
//		assertThat(contextValidation.errors.size()).isEqualTo(0);
//	}
//	
//	@Test
//	public void validateIdCreationModeError() throws DAOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		contextValidation.setCreationMode();
//		Sample sample=saveDBOject(Sample.class, COLLECTION_NAME, "validateIdCreationModeError");
//    	CommonValidationHelper.validateId(sample, contextValidation);
//		assertThat(contextValidation.errors.size()).isEqualTo(1);
//	}
//
//	@Test
//	public void validateIdUpdateMode() throws DAOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		contextValidation.setUpdateMode();
//		Sample sample=saveDBOject(Sample.class, COLLECTION_NAME, "validateIdUpdateMode");
//    	CommonValidationHelper.validateId(sample, contextValidation);
//		assertThat(contextValidation.errors.size()).isEqualTo(0);
//	}
//	
//	@Test
//	public void validateIdUpdateModeError() throws DAOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		contextValidation.setUpdateMode();
//		Sample sample=new Sample();
//    	CommonValidationHelper.validateId(sample, contextValidation);
//		assertThat(contextValidation.errors.size()).isEqualTo(1);
//	}
//
//	/**
//	 * 
//	 *  Unit test CommonValidationHelper.validateCode 
//	 */
//	@Test
//	public void validateCodeRequired(){
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		Sample sample=new Sample();
//    	CommonValidationHelper.validateCode(sample, COLLECTION_NAME, contextValidation);
//		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
//	}
//	
//	@Test
//	public void validateUniqueCode() throws InstantiationException, IllegalAccessException, ClassNotFoundException{
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		contextValidation.setCreationMode();
//		Sample sample=saveDBOject(Sample.class,COLLECTION_NAME, "validateUniqueCode");
//    	CommonValidationHelper.validateCode(sample, COLLECTION_NAME, contextValidation);
//		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
//	}
//
//	
//	@Test
//	public void validateExistCode() throws InstantiationException, IllegalAccessException, ClassNotFoundException{
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		contextValidation.setUpdateMode();
//		Sample sample=new Sample();
//		sample.code="validateExistCode";
//    	CommonValidationHelper.validateCode(sample, COLLECTION_NAME, contextValidation);
//		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
//		
//		contextValidation.clear();
//		contextValidation.setUpdateMode();
//		sample=saveDBOject(Sample.class,COLLECTION_NAME, "validateExistCode");
//		CommonValidationHelper.validateCode(sample, COLLECTION_NAME, contextValidation);
//		assertThat(contextValidation.errors.size()).isEqualTo(0);
//		
//	}
//	
//	/**
//	 *  State Code
//	 * @throws DAOException 
//	 */
//	
//	@Test
//	public void validationStateCode() throws DAOException {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		CommonValidationHelper.validateStateCode(State.find.get().findAll().get(0).code, contextValidation);
//		assertThat(contextValidation.errors.size()).isEqualTo(0);
//	}
//	
//	@Test
//	public void validationStateRequired() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		CommonValidationHelper.validateStateCode(null, contextValidation);
//		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
//	}
//	
//	@Test
//	public void validationStateNotExist() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		CommonValidationHelper.validateStateCode("notexist", contextValidation);
//		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
//	}
//	
///* 
// * 	
//		CommonValidationHelper.validateStateCode(this.stateCode, contextValidation);
//
//		CommonValidationHelper.validateResolution(this.resolutionCode,contextValidation);
//		CommonValidationHelper.validateTraceInformation(this.traceInformation, contextValidation);
//	CommonValidationHelper.validateProjectCodes(projectCodes, contextValidation);
//	CommonValidationHelper.validateSampleCodes(sampleCodes, contextValidation);
//
//*/
//}
