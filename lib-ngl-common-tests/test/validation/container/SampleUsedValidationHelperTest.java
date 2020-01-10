package validation.container;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

import fr.cea.ig.util.function.CC2;
import fr.cea.ig.util.function.CCActions;
import models.laboratory.sample.description.SampleCategory;
import models.laboratory.sample.description.SampleType;
import ngl.common.Global;
import play.Application;
import utils.Constants;
import validation.ContextValidation;
import validation.container.instance.ContentValidationHelper;
import validation.sample.instance.SampleValidationHelper;

public class SampleUsedValidationHelperTest {
	
	private static final CC2<Application,ContextValidation> af =
			Global.afSq.cc1()
			.and(CCActions.f0asCC1(() -> ContextValidation.createUndefinedContext(Constants.TEST_USER)));
	
	/*
	 * Sample category code 
	 * @throws DAOException 
	 */
	@Test
	public void validateSampleCategoryCode() throws Exception {
		af.accept((app,contextValidation) -> {
			SampleValidationHelper.validateSampleCategoryCodeRequired(contextValidation,SampleCategory.find.get().findAll().get(0).code);
			assertThat(contextValidation.getErrors().size()).isEqualTo(0);
		});
	}

	@Test
	public void validateSampleCategoryCodeRequired() throws Exception {
		af.accept((app,contextValidation) -> {
			SampleValidationHelper.validateSampleCategoryCodeRequired(contextValidation,null);
			assertThat(contextValidation.getErrors().size()).isEqualTo(1);
		});
	}

	@Test
	public void validateSampleCategoryNotExist() throws Exception {
		af.accept((app,contextValidation) -> {
			SampleValidationHelper.validateSampleCategoryCodeRequired(contextValidation,"notexist");
			assertThat(contextValidation.getErrors().size()).isNotEqualTo(0);
		});
	}	
	
	/* 
	 * Sample Type
	 * @throws DAOException  
	 */
	@Test
	public void validateSampleTypeCode() throws Exception {
		af.accept((app,contextValidation) -> {
			ContentValidationHelper.validateSampleTypeCodeRequired(contextValidation,SampleType.find.get().findAll().get(0).code);
			assertThat(contextValidation.getErrors().size()).isEqualTo(0);
		});
	}

	@Test
	public void validateSampleTypeCodeRequired() throws Exception {
		af.accept((app,contextValidation) -> {
			ContentValidationHelper.validateSampleTypeCodeRequired(contextValidation,null);
			assertThat(contextValidation.getErrors().size()).isEqualTo(1);
		});
	}

	@Test
	public void validateSampleTypeNotExist() throws Exception {
		af.accept((app,contextValidation) -> {
			ContentValidationHelper.validateSampleTypeCodeRequired(contextValidation,"notexist");
			assertThat(contextValidation.getErrors().size()).isNotEqualTo(0);
		});
	}

}

//package validation.container;
//
//import static org.fest.assertions.Assertions.assertThat;
//import models.laboratory.sample.description.SampleCategory;
//import models.laboratory.sample.description.SampleType;
//import models.utils.dao.DAOException;
//
//import org.junit.Test;
//
//import utils.AbstractTests;
//import utils.Constants;
//import validation.ContextValidation;
//import validation.container.instance.ContentValidationHelper;
//import validation.sample.instance.SampleValidationHelper;
//
//public class SampleUsedValidationHelperTest extends AbstractTests{
//	
//	
//	
//	/**
//	 * Sample category code 
//	 * @throws DAOException 
//	 */
//	@Test
//	public void validateSampleCategoryCode() throws DAOException {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		SampleValidationHelper.validateSampleCategoryCode(SampleCategory.find.get().findAll().get(0).code,contextValidation);
//		assertThat(contextValidation.errors.size()).isEqualTo(0);
//	}
//
//	@Test
//	public void validateSampleCategoryCodeRequired() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		SampleValidationHelper.validateSampleCategoryCode(null,contextValidation);
//		assertThat(contextValidation.errors.size()).isEqualTo(1);
//	}
//
//	@Test
//	public void validateSampleCategoryNotExist() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		SampleValidationHelper.validateSampleCategoryCode("notexist",contextValidation);
//		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
//	}	
//	
//	/**
//	 * 
//	 * Sample Type
//	 * @throws DAOException 
//	 * 
//	 */
//	@Test
//	public void validateSampleTypeCode() throws DAOException {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		ContentValidationHelper.validateSampleTypeCode(SampleType.find.get().findAll().get(0).code,contextValidation);
//		assertThat(contextValidation.errors.size()).isEqualTo(0);
//	}
//
//	@Test
//	public void validateSampleTypeCodeRequired() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		ContentValidationHelper.validateSampleTypeCode(null,contextValidation);
//		assertThat(contextValidation.errors.size()).isEqualTo(1);
//	}
//
//	@Test
//	public void validateSampleTypeNotExist() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		ContentValidationHelper.validateSampleTypeCode("notexist",contextValidation);
//		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
//	}	
//	
//
//}
