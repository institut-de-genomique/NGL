package validation;

import static org.fest.assertions.Assertions.assertThat;
import models.laboratory.common.description.State;
import models.laboratory.experiment.description.ExperimentCategory;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.instrument.description.Instrument;
import models.laboratory.instrument.description.InstrumentCategory;
import models.laboratory.instrument.description.InstrumentUsedType;
import models.laboratory.processes.description.ProcessType;
import models.laboratory.project.description.ProjectCategory;
import models.laboratory.reagent.description.ReagentCatalog;
import models.laboratory.sample.description.SampleCategory;
import models.laboratory.sample.description.SampleType;
import models.utils.dao.DAOException;

import org.junit.BeforeClass;
import org.junit.Test;

import utils.AbstractTests;
import utils.Constants;
import validation.container.instance.ContainerValidationHelper;
import validation.experiment.instance.InstrumentUsedValidationHelper;
import validation.processes.instance.ProcessValidationHelper;
import validation.project.instance.ProjectValidationHelper;
import validation.sample.instance.SampleValidationHelper;

public class DescriptionValidationHelperTest extends AbstractTests{
	
	static InstrumentUsedType instrumentUsedType;
	static ExperimentType experimentType;
	static ExperimentCategory experimentCategory;
	static Instrument instrument;
	static InstrumentCategory instrumentCategory;
	static ProcessType processType;
	static ProjectCategory projectCategory;
	static ReagentCatalog reagentType;
	static SampleCategory sampleCategory;
	static SampleType sampleType;
	
	static State state;
	
	@BeforeClass
	public static void initData() throws DAOException{
		
		instrumentUsedType=InstrumentUsedType.find.findAll().get(0);
		
		experimentType=ExperimentType.find.findAll().get(0);
		
		experimentCategory=ExperimentCategory.find.findAll().get(0);
		
		instrument=Instrument.find.findAll().get(0);
		
		instrumentCategory=InstrumentCategory.find.findAll().get(0);
		
		processType=ProcessType.find.findAll().get(0);
		
		projectCategory=ProjectCategory.find.findAll().get(0);
		
	//	reagentType =ReagentType.find.findAll().get(0);
		
		sampleCategory=SampleCategory.find.findAll().get(0);
		
		sampleType=SampleType.find.findAll().get(0);

		state=State.find.findAll().get(0);
		
	}

	/*@Test
	public void validationProtocol() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		DescriptionValidationHelper.validationProtocol(proto.code, contextValidation);
		assertThat(contextValidation.errors.size()).isEqualTo(0);
	}

	@Test
	public void validationProtocolNotRequired() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		DescriptionValidationHelper.validationProtocol(null, contextValidation);
		assertThat(contextValidation.errors.size()).isEqualTo(0);
	}
	
	@Test
	public void validationProtocolNotExist() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		DescriptionValidationHelper.validationProtocol("notexist", contextValidation);
		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
	}*/

	/**
	 * InstrumentUsedType
	 */
	
	@Test
	public void validationInstrumentUsedTypeCode() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		InstrumentUsedValidationHelper.validationTypeCode(instrumentUsedType.code, contextValidation);
		assertThat(contextValidation.errors.size()).isEqualTo(0);
	}
	
	@Test
	public void validationInstrumentUsedTypeRequired() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		InstrumentUsedValidationHelper.validationTypeCode(null, contextValidation);
		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
	}
	
	@Test
	public void validationInstrumentUsedTypeNotExist() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		InstrumentUsedValidationHelper.validationTypeCode("notexist", contextValidation);
		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
	}

	
	/***
	 * ExperimentType
	 */
	/*@Test
	public void validationExperimentTypeCode() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		DescriptionValidationHelper.validationExperimentTypeCode(experimentType.code, contextValidation);
		assertThat(contextValidation.errors.size()).isEqualTo(0);
	}
	
	@Test
	public void validationExperimentTypeRequired() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		DescriptionValidationHelper.validationExperimentTypeCode(null, contextValidation);
		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
	}
	
	@Test
	public void validationExperimentTypeNotExist() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		DescriptionValidationHelper.validationExperimentTypeCode("notexist", contextValidation);
		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
	}
*/
	/**
	 * ExperimentCategory
	 */
	/*
	@Test
	public void validationExperimentCategoryCode() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		DescriptionValidationHelper.validationExperimentCategoryCode(experimentCategory.code, contextValidation);
		assertThat(contextValidation.errors.size()).isEqualTo(0);
	}
	
	@Test
	public void validationExperimentCategoryRequired() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		DescriptionValidationHelper.validationExperimentCategoryCode(null, contextValidation);
		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
	}
	
	@Test
	public void validationExperimentCategoryNotExist() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		DescriptionValidationHelper.validationExperimentCategoryCode("notexist", contextValidation);
		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
	}
*/
	
	/**
	 * Instrument
	 */

	@Test
	public void validationInstrumentCode() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		InstrumentUsedValidationHelper.validationCode(instrument.code, contextValidation);
		assertThat(contextValidation.errors.size()).isEqualTo(0);
	}
	
	@Test
	public void validationInstrumentRequired() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		InstrumentUsedValidationHelper.validationCode(null, contextValidation);
		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
	}
	
	@Test
	public void validationInstrumentNotExist() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		InstrumentUsedValidationHelper.validationCode("notexist", contextValidation);
		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
	}
	
	/**
	 * InstrumentCategory
	 */

	@Test
	public void validationInstrumentCategoryCode() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		InstrumentUsedValidationHelper.validationCategoryCode(instrumentCategory.code, contextValidation);
		assertThat(contextValidation.errors.size()).isEqualTo(0);
	}
	
	@Test
	public void validationInstrumentCategoryRequired() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		InstrumentUsedValidationHelper.validationCategoryCode(null, contextValidation);
		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
	}
	
	@Test
	public void validationInstrumentCategoryNotExist() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		InstrumentUsedValidationHelper.validationCategoryCode("notexist", contextValidation);
		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
	}
	
	/*
	 * ProcessType
	 */
	
	// TODO : failed: java.lang.IllegalArgumentException: stateCode from contextValidation is null
	// @Test
	public void validationProcessTypeCode() {
		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
		ContainerValidationHelper.validateProcessTypeCode(processType.code, contextValidation);
		assertThat(contextValidation.errors.size()).isEqualTo(0);
	}
	
	// TODO : java.lang.IllegalArgumentException: stateCode from contextValidation is null
	// @Test
	public void validationProcessTypeNotRequired() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		ContainerValidationHelper.validateProcessTypeCode(null, contextValidation);
		assertThat(contextValidation.errors.size()).isEqualTo(0);
	}
	
	// TODO : java.lang.IllegalArgumentException: stateCode from contextValidation is null
	// @Test
	public void validationProcessTypeNotExist() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		ContainerValidationHelper.validateProcessTypeCode("notexist", contextValidation);
		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
	}
	
	/*
	 * ProjectCategory
	 */
	@Test
	public void validationProjectCategoryCode() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		ProjectValidationHelper.validateProjectCategoryCode(projectCategory.code, contextValidation);
		assertThat(contextValidation.errors.size()).isEqualTo(0);
	}
	
	@Test
	public void validationProjectCategoryRequired() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		ProjectValidationHelper.validateProjectCategoryCode(null, contextValidation);
		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
	}
	
	@Test
	public void validationProjectCategoryNotExist() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		ProjectValidationHelper.validateProjectCategoryCode("notexist", contextValidation);
		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
	}
	
	/**
	 *  ReagentType
	 */

	
	/**
	 * SampleCategory 
	 */
	@Test
	public void validationSampleCategoryCode() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		SampleValidationHelper.validateSampleCategoryCode(sampleCategory.code, contextValidation);
		assertThat(contextValidation.errors.size()).isEqualTo(0);
	}
	
	@Test
	public void validationSampleCategoryRequired() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		SampleValidationHelper.validateSampleCategoryCode(null, contextValidation);
		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
	}
	
	@Test
	public void validationSampleCategoryNotExist() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		SampleValidationHelper.validateSampleCategoryCode("notexist", contextValidation);
		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
	}	
	
	
	//TODO
	public void validationProjectTest() {
		//ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		//DescriptionValidationHelper.validationProcess(null, null, contextValidation);
	}

	//TODO
	public void validationProcessTest() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		ProcessValidationHelper.validateProcessType(null, null, contextValidation);		
	}
	
	

}
