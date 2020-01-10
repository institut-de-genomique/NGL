package validation.sample;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

import fr.cea.ig.util.function.CC2;
import fr.cea.ig.util.function.CCActions;
import models.laboratory.sample.description.SampleCategory;
import ngl.common.Global;
import play.Application;
import utils.Constants;
import validation.ContextValidation;
import validation.sample.instance.SampleValidationHelper;

public class SampleValidationHelperTest {

	private static final CC2<Application,ContextValidation> af =
			Global.afSq.cc1()
			.and(CCActions.f0asCC1(() -> ContextValidation.createUndefinedContext(Constants.TEST_USER)));
	
	/*
	 *  SampleCategory
	 */
	@Test
	public void validateSampleCategoryCode() throws Exception {
		af.accept((app,contextValidation) -> {
			SampleValidationHelper.validateSampleCategoryCodeRequired(contextValidation, SampleCategory.find.get().findAll().get(0).code);
			assertThat(contextValidation.getErrors().size()).isEqualTo(0);
		});
	}

	@Test
	public void validateSampleCategoryCodeRequired() throws Exception {
		af.accept((app,contextValidation) -> {
			SampleValidationHelper.validateSampleCategoryCodeRequired(contextValidation, null);
			assertThat(contextValidation.getErrors().size()).isEqualTo(1);
		});
	}

	@Test
	public void validateSampleCategoryCodeNotExist() throws Exception {
		af.accept((app,contextValidation) -> {
			SampleValidationHelper.validateSampleCategoryCodeRequired(contextValidation, "notexist");
			assertThat(contextValidation.getErrors().size()).isEqualTo(1);
		});
	}
	
	/*
	 *  SampleCategory 
	 */
	@Test
	public void validateSampleTypeCodesRequired() throws Exception {
		af.accept((app,contextValidation) -> {
			SampleValidationHelper.validateSampleType(null,null,null, contextValidation);
			assertThat(contextValidation.getErrors().size()).isEqualTo(2);
		});
	}

	@Test
	public void validateSampleTypeCodesNotExist() throws Exception {
		af.accept((app,contextValidation) -> {
			SampleValidationHelper.validateSampleType("notexist","notexist",null, contextValidation);
			assertThat(contextValidation.getErrors().size()).isEqualTo(2);
		});
	}

	//@Test 
	// FIXME
	public void validateSampleTypeValidateProperties() throws Exception {
		af.accept((app,contextValidation) -> {
			SampleValidationHelper.validateSampleCategoryCodeRequired(contextValidation, null);
			assertThat(contextValidation.getErrors().size()).isEqualTo(1);
		});
	}
		
//	[error] Test validation.sample.SampleValidationHelperTest.validateSampleTypeCodeExistsByInstitute failed: java.lang.NullPointerException: null, took 0.217 sec
//	[error]     at validation.utils.ValidationHelper.validateProperties(ValidationHelper.java:168)
//	[error]     at validation.utils.ValidationHelper.validateProperties(ValidationHelper.java:50)
//	[error]     at validation.sample.instance.SampleValidationHelper.validateSampleType(SampleValidationHelper.java:43)
//	[error]     at validation.sample.SampleValidationHelperTest.validateSampleTypeCodeExistsByInstitute(SampleValidationHelperTest.java:75)	
	// @Test
	// FIXME
	public void validateSampleTypeCodeExistsByInstitute() throws Exception {
		af.accept((app,contextValidation) -> {
			SampleValidationHelper.validateSampleType("BAC", "default-import", null, contextValidation);	
			// the sampleType and the importType are defined in the db
			assertThat(contextValidation.getErrors().size()).isEqualTo(3);
			assertThat(contextValidation.getErrors().toString()).contains("isAdapters");
			assertThat(contextValidation.getErrors().toString()).contains("isFragmented");
			assertThat(contextValidation.getErrors().toString()).contains("taxonSize");
		});
	}
	
}

//package validation.sample;
//
//import static org.fest.assertions.Assertions.assertThat;
//
//import models.laboratory.sample.description.SampleCategory;
//import models.utils.dao.DAOException;
//import org.junit.Test;
//import utils.AbstractTests;
//import utils.Constants;
//import validation.ContextValidation;
//import validation.sample.instance.SampleValidationHelper;
//
//public class SampleValidationHelperTest extends AbstractTests {
//
//	/*
//	 *  SampleCategory
//	 */
//	@Test
//	public void validateSampleCategoryCode() throws DAOException {
//		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
//		SampleValidationHelper.validateSampleCategoryCode(SampleCategory.find.get().findAll().get(0).code, contextValidation);
//		assertThat(contextValidation.errors.size()).isEqualTo(0);
//	}
//
//	@Test
//	public void validateSampleCategoryCodeRequired() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		SampleValidationHelper.validateSampleCategoryCode(null, contextValidation);
//		assertThat(contextValidation.errors.size()).isEqualTo(1);
//	}
//
//	@Test
//	public void validateSampleCategoryCodeNotExist() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		SampleValidationHelper.validateSampleCategoryCode("notexist", contextValidation);
//		assertThat(contextValidation.errors.size()).isEqualTo(1);
//	}
//	
//	/*
//	 *  SampleCategory 
//	 */
//	@Test
//	public void validateSampleTypeCodesRequired() throws DAOException {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		SampleValidationHelper.validateSampleType(null,null,null, contextValidation);
//		assertThat(contextValidation.errors.size()).isEqualTo(2);
//	}
//
//	@Test
//	public void validateSampleTypeCodesNotExist() throws DAOException {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		SampleValidationHelper.validateSampleType("notexist","notexist",null, contextValidation);
//		assertThat(contextValidation.errors.size()).isEqualTo(2);
//	}
//
//	//@Test 
//	// TO DO
//	public void validateSampleTypeValidateProperties() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		SampleValidationHelper.validateSampleCategoryCode(null, contextValidation);
//		assertThat(contextValidation.errors.size()).isEqualTo(1);
//	}
//		
////	[error] Test validation.sample.SampleValidationHelperTest.validateSampleTypeCodeExistsByInstitute failed: java.lang.NullPointerException: null, took 0.217 sec
////	[error]     at validation.utils.ValidationHelper.validateProperties(ValidationHelper.java:168)
////	[error]     at validation.utils.ValidationHelper.validateProperties(ValidationHelper.java:50)
////	[error]     at validation.sample.instance.SampleValidationHelper.validateSampleType(SampleValidationHelper.java:43)
////	[error]     at validation.sample.SampleValidationHelperTest.validateSampleTypeCodeExistsByInstitute(SampleValidationHelperTest.java:75)	
//	// @Test
//	public void validateSampleTypeCodeExistsByInstitute() throws DAOException {
//		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
//		SampleValidationHelper.validateSampleType("BAC","default-import",null, contextValidation);	
//		// the sampleType and the importType are defined in the db
//		assertThat(contextValidation.errors.size()).isEqualTo(3);
//		assertThat(contextValidation.errors.toString()).contains("isAdapters");
//		assertThat(contextValidation.errors.toString()).contains("isFragmented");
//		assertThat(contextValidation.errors.toString()).contains("taxonSize");	
//	}
//	
//}
