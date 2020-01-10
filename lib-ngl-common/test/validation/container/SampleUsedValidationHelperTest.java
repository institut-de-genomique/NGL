package validation.container;

import static org.fest.assertions.Assertions.assertThat;
import models.laboratory.sample.description.SampleCategory;
import models.laboratory.sample.description.SampleType;
import models.utils.dao.DAOException;

import org.junit.Test;

import utils.AbstractTests;
import utils.Constants;
import validation.ContextValidation;
import validation.container.instance.ContentValidationHelper;
import validation.sample.instance.SampleValidationHelper;

public class SampleUsedValidationHelperTest extends AbstractTests{
	
	
	
	/**
	 * Sample category code 
	 * @throws DAOException 
	 */
	@Test
	public void validateSampleCategoryCode() throws DAOException {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		SampleValidationHelper.validateSampleCategoryCode(SampleCategory.find.findAll().get(0).code,contextValidation);
		assertThat(contextValidation.errors.size()).isEqualTo(0);
	}

	@Test
	public void validateSampleCategoryCodeRequired() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		SampleValidationHelper.validateSampleCategoryCode(null,contextValidation);
		assertThat(contextValidation.errors.size()).isEqualTo(1);
	}

	@Test
	public void validateSampleCategoryNotExist() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		SampleValidationHelper.validateSampleCategoryCode("notexist",contextValidation);
		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
	}	
	
	/**
	 * 
	 * Sample Type
	 * @throws DAOException 
	 * 
	 */
	@Test
	public void validateSampleTypeCode() throws DAOException {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		ContentValidationHelper.validateSampleTypeCode(SampleType.find.findAll().get(0).code,contextValidation);
		assertThat(contextValidation.errors.size()).isEqualTo(0);
	}

	@Test
	public void validateSampleTypeCodeRequired() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		ContentValidationHelper.validateSampleTypeCode(null,contextValidation);
		assertThat(contextValidation.errors.size()).isEqualTo(1);
	}

	@Test
	public void validateSampleTypeNotExist() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		ContentValidationHelper.validateSampleTypeCode("notexist",contextValidation);
		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
	}	
	

}
