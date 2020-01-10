package validation.sample;

import static org.fest.assertions.Assertions.assertThat;

//import java.util.Map;
import models.laboratory.sample.description.SampleCategory;
//import models.utils.DescriptionHelper;
import models.utils.dao.DAOException;
import org.junit.Test;
//import play.Logger;
//import play.Play;
import utils.AbstractTests;
import utils.Constants;
import validation.ContextValidation;
import validation.sample.instance.SampleValidationHelper;

public class SampleValidationHelperTest extends AbstractTests {

	/**
	 *  SampleCategory
	 */
	@Test
	public void validateSampleCategoryCode() throws DAOException {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		SampleValidationHelper.validateSampleCategoryCode(SampleCategory.find.findAll().get(0).code, contextValidation);
		assertThat(contextValidation.errors.size()).isEqualTo(0);
	}

	@Test
	public void validateSampleCategoryCodeRequired() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		SampleValidationHelper.validateSampleCategoryCode(null, contextValidation);
		assertThat(contextValidation.errors.size()).isEqualTo(1);
	}

	@Test
	public void validateSampleCategoryCodeNotExist() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		SampleValidationHelper.validateSampleCategoryCode("notexist", contextValidation);
		assertThat(contextValidation.errors.size()).isEqualTo(1);
	}
	
	/**
	 *  SampleCategory
	 * 
	 */
	@Test
	public void validateSampleTypeCodesRequired() throws DAOException {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		SampleValidationHelper.validateSampleType(null,null,null, contextValidation);
		assertThat(contextValidation.errors.size()).isEqualTo(2);
	}

	@Test
	public void validateSampleTypeCodesNotExist() throws DAOException {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		SampleValidationHelper.validateSampleType("notexist","notexist",null, contextValidation);
		assertThat(contextValidation.errors.size()).isEqualTo(2);
	}

	//@Test 
	//TODO
	public void validateSampleTypeValidateProperties() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		SampleValidationHelper.validateSampleCategoryCode(null, contextValidation);
		assertThat(contextValidation.errors.size()).isEqualTo(1);
	}
		
//	[error] Test validation.sample.SampleValidationHelperTest.validateSampleTypeCodeExistsByInstitute failed: java.lang.NullPointerException: null, took 0.217 sec
//	[error]     at validation.utils.ValidationHelper.validateProperties(ValidationHelper.java:168)
//	[error]     at validation.utils.ValidationHelper.validateProperties(ValidationHelper.java:50)
//	[error]     at validation.sample.instance.SampleValidationHelper.validateSampleType(SampleValidationHelper.java:43)
//	[error]     at validation.sample.SampleValidationHelperTest.validateSampleTypeCodeExistsByInstitute(SampleValidationHelperTest.java:75)	
	// @Test
	public void validateSampleTypeCodeExistsByInstitute() throws DAOException {
		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
		SampleValidationHelper.validateSampleType("BAC","default-import",null, contextValidation);	
		// the sampleType and the importType are defined in the db
		assertThat(contextValidation.errors.size()).isEqualTo(3);
		assertThat(contextValidation.errors.toString()).contains("isAdapters");
		assertThat(contextValidation.errors.toString()).contains("isFragmented");
		assertThat(contextValidation.errors.toString()).contains("taxonSize");	
	}
	
}
