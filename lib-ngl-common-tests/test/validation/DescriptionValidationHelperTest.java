package validation;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

import fr.cea.ig.util.function.CC1Managed;
import fr.cea.ig.util.function.CC2;
import fr.cea.ig.util.function.CCActions;
import models.laboratory.instrument.description.Instrument;
import models.laboratory.instrument.description.InstrumentCategory;
import models.laboratory.instrument.description.InstrumentUsedType;
import models.laboratory.processes.description.ProcessType;
import models.laboratory.project.description.ProjectCategory;
import models.laboratory.sample.description.SampleCategory;
import ngl.common.Global;
import utils.Constants;
import validation.container.instance.ContainerValidationHelper;
import validation.experiment.instance.InstrumentUsedValidationHelper;
import validation.processes.instance.ProcessValidationHelper;
import validation.project.instance.ProjectValidationHelper;
import validation.sample.instance.SampleValidationHelper;

public class DescriptionValidationHelperTest {
	
	private static class TestContext implements CC1Managed {

		InstrumentUsedType instrumentUsedType;
//		ExperimentType     experimentType;
//		ExperimentCategory experimentCategory;
		Instrument         instrument;
		InstrumentCategory instrumentCategory;
		ProcessType        processType;
		ProjectCategory    projectCategory;
//		ReagentCatalog     reagentType;
		SampleCategory     sampleCategory;
//		SampleType         sampleType;
//		State              state;

		@Override
		public void setUp() throws Exception {
			instrumentUsedType = InstrumentUsedType.find.get().findAll().get(0);
//			experimentType     = ExperimentType    .find.get().findAll().get(0);
//			experimentCategory = ExperimentCategory.find.get().findAll().get(0);
			instrument         = Instrument        .find.get().findAll().get(0);
			instrumentCategory = InstrumentCategory.find.get().findAll().get(0);
			processType        = ProcessType       .find.get().findAll().get(0);
			projectCategory    = ProjectCategory   .find.get().findAll().get(0);
			//	reagentType =ReagentType.find.findAll().get(0);
			sampleCategory     = SampleCategory    .find.get().findAll().get(0);		
//			sampleType         = SampleType        .find.get().findAll().get(0);
//			state              = State             .find.get().findAll().get(0);
		}

		@Override
		public void tearDown() {

		}

	}
	
	private static final CC2<TestContext,ContextValidation> af_ =
			Global.afSq.cc1()
			.and(CCActions.timeCC1("data", CCActions.managed(TestContext::new)))
			.cc1((app,ctx) -> ctx)
			.and(CCActions.f0asCC1(() -> ContextValidation.createUndefinedContext(Constants.TEST_USER)));

	private static final CC2<TestContext,ContextValidation> af =
			CCActions.timeCC2("setup", af_);
	
	
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

	/*
	 * InstrumentUsedType
	 */	
	@Test
	public void validationInstrumentUsedTypeCode() throws Exception {
		af.accept((ctx,contextValidation) -> {
			InstrumentUsedValidationHelper.validateInstrumentUsedTypeCodeRequired(contextValidation, ctx.instrumentUsedType.code);
			assertThat(contextValidation.getErrors().size()).isEqualTo(0);
		});
	}
	
	@Test
	public void validationInstrumentUsedTypeRequired() throws Exception {
		af.accept((ctx,contextValidation) -> {
			InstrumentUsedValidationHelper.validateInstrumentUsedTypeCodeRequired(contextValidation, null);
			assertThat(contextValidation.getErrors().size()).isNotEqualTo(0);
		});
	}
	
	@Test
	public void validationInstrumentUsedTypeNotExist() throws Exception {
		af.accept((ctx,contextValidation) -> {
			InstrumentUsedValidationHelper.validateInstrumentUsedTypeCodeRequired(contextValidation, "notexist");
			assertThat(contextValidation.getErrors().size()).isNotEqualTo(0);
		});
	}
	
	/*
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
	/*
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
	
	/*
	 * Instrument
	 */
	@Test
	public void validationInstrumentCode() throws Exception {
		af.accept((ctx,contextValidation) -> {
			InstrumentUsedValidationHelper.validateInstrumentCodeRequired(contextValidation, ctx.instrument.code);
			assertThat(contextValidation.getErrors().size()).isEqualTo(0);
		});
	}
	
	@Test
	public void validationInstrumentRequired() throws Exception {
		af.accept((ctx,contextValidation) -> {
			InstrumentUsedValidationHelper.validateInstrumentCodeRequired(contextValidation, null);
			assertThat(contextValidation.getErrors().size()).isNotEqualTo(0);
		});
	}
	
	@Test
	public void validationInstrumentNotExist() throws Exception {
		af.accept((ctx,contextValidation) -> {
			InstrumentUsedValidationHelper.validateInstrumentCodeRequired(contextValidation, "notexist");
			assertThat(contextValidation.getErrors().size()).isNotEqualTo(0);
		});
	}
	
	/*
	 * InstrumentCategory
	 */
	@Test
	public void validationInstrumentCategoryCode() throws Exception {
		af.accept((ctx,contextValidation) -> {
			InstrumentUsedValidationHelper.validateInstrumentCategoryCodeRequired(contextValidation, ctx.instrumentCategory.code);
			assertThat(contextValidation.getErrors().size()).isEqualTo(0);
		});
	}
	
	@Test
	public void validationInstrumentCategoryRequired() throws Exception {
		af.accept((ctx,contextValidation) -> {
			InstrumentUsedValidationHelper.validateInstrumentCategoryCodeRequired(contextValidation, null);
			assertThat(contextValidation.getErrors().size()).isNotEqualTo(0);
		});
	}
	
	@Test
	public void validationInstrumentCategoryNotExist() throws Exception {
		af.accept((ctx,contextValidation) -> {
			InstrumentUsedValidationHelper.validateInstrumentCategoryCodeRequired(contextValidation, "notexist");
			assertThat(contextValidation.getErrors().size()).isNotEqualTo(0);
		});
	}
	
	/*
	 * ProcessType
	 */
	// FIXME: failed: java.lang.IllegalArgumentException: stateCode from contextValidation is null
	// @Test
	public void validationProcessTypeCode() throws Exception {
		af.accept((ctx,contextValidation) -> {
			ContainerValidationHelper.validateProcessTypeCode(ctx.processType.code, contextValidation);
			assertThat(contextValidation.getErrors().size()).isEqualTo(0);
		});
	}
	
	// FIXME: java.lang.IllegalArgumentException: stateCode from contextValidation is null
	// @Test
	public void validationProcessTypeNotRequired() throws Exception {
		af.accept((ctx,contextValidation) -> {
			ContainerValidationHelper.validateProcessTypeCode(null, contextValidation);
			assertThat(contextValidation.getErrors().size()).isEqualTo(0);
		});
	}
	
	// FIXME: java.lang.IllegalArgumentException: stateCode from contextValidation is null
	// @Test
	public void validationProcessTypeNotExist() throws Exception {
		af.accept((ctx,contextValidation) -> {
			ContainerValidationHelper.validateProcessTypeCode("notexist", contextValidation);
			assertThat(contextValidation.getErrors().size()).isNotEqualTo(0);
		});
	}
	
	/*
	 * ProjectCategory
	 */
	@Test
	public void validationProjectCategoryCode() throws Exception {
		af.accept((ctx,contextValidation) -> {
			ProjectValidationHelper.validateProjectCategoryCodeRequired(contextValidation, ctx.projectCategory.code);
			assertThat(contextValidation.getErrors().size()).isEqualTo(0);
		});
	}
	
	@Test
	public void validationProjectCategoryRequired() throws Exception {
		af.accept((ctx,contextValidation) -> {
			ProjectValidationHelper.validateProjectCategoryCodeRequired(contextValidation, null);
			assertThat(contextValidation.getErrors().size()).isNotEqualTo(0);
		});
	}
	
	@Test
	public void validationProjectCategoryNotExist() throws Exception {
		af.accept((ctx,contextValidation) -> {
			ProjectValidationHelper.validateProjectCategoryCodeRequired(contextValidation, "notexist");
			assertThat(contextValidation.getErrors().size()).isNotEqualTo(0);
		});
	}
	
	/*
	 *  ReagentType
	 */

	/*
	 * SampleCategory 
	 */
	@Test
	public void validationSampleCategoryCode() throws Exception {
		af.accept((ctx,contextValidation) -> {
			SampleValidationHelper.validateSampleCategoryCodeRequired(contextValidation, ctx.sampleCategory.code);
			assertThat(contextValidation.getErrors().size()).isEqualTo(0);
		});
	}
	
	@Test
	public void validationSampleCategoryRequired() throws Exception {
		af.accept((ctx,contextValidation) -> {
			SampleValidationHelper.validateSampleCategoryCodeRequired(contextValidation, null);
			assertThat(contextValidation.getErrors().size()).isNotEqualTo(0);
		});
	}
	
	@Test
	public void validationSampleCategoryNotExist() throws Exception {
		af.accept((ctx,contextValidation) -> {
			SampleValidationHelper.validateSampleCategoryCodeRequired(contextValidation, "notexist");
			assertThat(contextValidation.getErrors().size()).isNotEqualTo(0);
		});
	}	
	
	// TEST: to do
	public void validationProjectTest() {
		//ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		//DescriptionValidationHelper.validationProcess(null, null, contextValidation);
	}

	// TEST: to do
	public void validationProcessTest() {
		ContextValidation contextValidation = ContextValidation.createUndefinedContext(Constants.TEST_USER);
		ProcessValidationHelper.validateProcessTypeRequired(contextValidation, null, null);		
	}

}

//package validation;
//
//import static org.fest.assertions.Assertions.assertThat;
//import models.laboratory.common.description.State;
//import models.laboratory.experiment.description.ExperimentCategory;
//import models.laboratory.experiment.description.ExperimentType;
//import models.laboratory.instrument.description.Instrument;
//import models.laboratory.instrument.description.InstrumentCategory;
//import models.laboratory.instrument.description.InstrumentUsedType;
//import models.laboratory.processes.description.ProcessType;
//import models.laboratory.project.description.ProjectCategory;
//import models.laboratory.reagent.description.ReagentCatalog;
//import models.laboratory.sample.description.SampleCategory;
//import models.laboratory.sample.description.SampleType;
//import models.utils.dao.DAOException;
//
//import org.junit.BeforeClass;
//import org.junit.Test;
//
//import utils.AbstractTests;
//import utils.Constants;
//import validation.container.instance.ContainerValidationHelper;
//import validation.experiment.instance.InstrumentUsedValidationHelper;
//import validation.processes.instance.ProcessValidationHelper;
//import validation.project.instance.ProjectValidationHelper;
//import validation.sample.instance.SampleValidationHelper;
//
//public class DescriptionValidationHelperTest extends AbstractTests{
//	
//	static InstrumentUsedType instrumentUsedType;
//	static ExperimentType experimentType;
//	static ExperimentCategory experimentCategory;
//	static Instrument instrument;
//	static InstrumentCategory instrumentCategory;
//	static ProcessType processType;
//	static ProjectCategory projectCategory;
//	static ReagentCatalog reagentType;
//	static SampleCategory sampleCategory;
//	static SampleType sampleType;
//	
//	static State state;
//	
//	@BeforeClass
//	public static void initData() throws DAOException{
//		
//		instrumentUsedType = InstrumentUsedType.find.get().findAll().get(0);
//		experimentType     = ExperimentType    .find.get().findAll().get(0);
//		experimentCategory = ExperimentCategory.find.get().findAll().get(0);
//		instrument         = Instrument        .find.get().findAll().get(0);
//		instrumentCategory = InstrumentCategory.find.get().findAll().get(0);
//		processType        = ProcessType       .find.get().findAll().get(0);
//		projectCategory    = ProjectCategory   .find.get().findAll().get(0);
//	//	reagentType =ReagentType.find.findAll().get(0);
//		sampleCategory     = SampleCategory    .find.get().findAll().get(0);		
//		sampleType         = SampleType        .find.get().findAll().get(0);
//		state              = State             .find.get().findAll().get(0);
//		
//	}
//
//	/*@Test
//	public void validationProtocol() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		DescriptionValidationHelper.validationProtocol(proto.code, contextValidation);
//		assertThat(contextValidation.errors.size()).isEqualTo(0);
//	}
//
//	@Test
//	public void validationProtocolNotRequired() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		DescriptionValidationHelper.validationProtocol(null, contextValidation);
//		assertThat(contextValidation.errors.size()).isEqualTo(0);
//	}
//	
//	@Test
//	public void validationProtocolNotExist() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		DescriptionValidationHelper.validationProtocol("notexist", contextValidation);
//		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
//	}*/
//
//	/**
//	 * InstrumentUsedType
//	 */
//	
//	@Test
//	public void validationInstrumentUsedTypeCode() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		InstrumentUsedValidationHelper.validationTypeCode(instrumentUsedType.code, contextValidation);
//		assertThat(contextValidation.errors.size()).isEqualTo(0);
//	}
//	
//	@Test
//	public void validationInstrumentUsedTypeRequired() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		InstrumentUsedValidationHelper.validationTypeCode(null, contextValidation);
//		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
//	}
//	
//	@Test
//	public void validationInstrumentUsedTypeNotExist() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		InstrumentUsedValidationHelper.validationTypeCode("notexist", contextValidation);
//		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
//	}
//
//	
//	/***
//	 * ExperimentType
//	 */
//	/*@Test
//	public void validationExperimentTypeCode() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		DescriptionValidationHelper.validationExperimentTypeCode(experimentType.code, contextValidation);
//		assertThat(contextValidation.errors.size()).isEqualTo(0);
//	}
//	
//	@Test
//	public void validationExperimentTypeRequired() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		DescriptionValidationHelper.validationExperimentTypeCode(null, contextValidation);
//		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
//	}
//	
//	@Test
//	public void validationExperimentTypeNotExist() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		DescriptionValidationHelper.validationExperimentTypeCode("notexist", contextValidation);
//		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
//	}
//*/
//	/**
//	 * ExperimentCategory
//	 */
//	/*
//	@Test
//	public void validationExperimentCategoryCode() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		DescriptionValidationHelper.validationExperimentCategoryCode(experimentCategory.code, contextValidation);
//		assertThat(contextValidation.errors.size()).isEqualTo(0);
//	}
//	
//	@Test
//	public void validationExperimentCategoryRequired() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		DescriptionValidationHelper.validationExperimentCategoryCode(null, contextValidation);
//		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
//	}
//	
//	@Test
//	public void validationExperimentCategoryNotExist() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		DescriptionValidationHelper.validationExperimentCategoryCode("notexist", contextValidation);
//		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
//	}
//*/
//	
//	/**
//	 * Instrument
//	 */
//
//	@Test
//	public void validationInstrumentCode() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		InstrumentUsedValidationHelper.validationCode(instrument.code, contextValidation);
//		assertThat(contextValidation.errors.size()).isEqualTo(0);
//	}
//	
//	@Test
//	public void validationInstrumentRequired() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		InstrumentUsedValidationHelper.validationCode(null, contextValidation);
//		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
//	}
//	
//	@Test
//	public void validationInstrumentNotExist() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		InstrumentUsedValidationHelper.validationCode("notexist", contextValidation);
//		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
//	}
//	
//	/**
//	 * InstrumentCategory
//	 */
//
//	@Test
//	public void validationInstrumentCategoryCode() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		InstrumentUsedValidationHelper.validationCategoryCode(instrumentCategory.code, contextValidation);
//		assertThat(contextValidation.errors.size()).isEqualTo(0);
//	}
//	
//	@Test
//	public void validationInstrumentCategoryRequired() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		InstrumentUsedValidationHelper.validationCategoryCode(null, contextValidation);
//		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
//	}
//	
//	@Test
//	public void validationInstrumentCategoryNotExist() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		InstrumentUsedValidationHelper.validationCategoryCode("notexist", contextValidation);
//		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
//	}
//	
//	/*
//	 * ProcessType
//	 */
//	
//	// TO DO : failed: java.lang.IllegalArgumentException: stateCode from contextValidation is null
//	// @Test
//	public void validationProcessTypeCode() {
//		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
//		ContainerValidationHelper.validateProcessTypeCode(processType.code, contextValidation);
//		assertThat(contextValidation.errors.size()).isEqualTo(0);
//	}
//	
//	// TO DO : java.lang.IllegalArgumentException: stateCode from contextValidation is null
//	// @Test
//	public void validationProcessTypeNotRequired() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		ContainerValidationHelper.validateProcessTypeCode(null, contextValidation);
//		assertThat(contextValidation.errors.size()).isEqualTo(0);
//	}
//	
//	// TO DO : java.lang.IllegalArgumentException: stateCode from contextValidation is null
//	// @Test
//	public void validationProcessTypeNotExist() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		ContainerValidationHelper.validateProcessTypeCode("notexist", contextValidation);
//		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
//	}
//	
//	/*
//	 * ProjectCategory
//	 */
//	@Test
//	public void validationProjectCategoryCode() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		ProjectValidationHelper.validateProjectCategoryCode(projectCategory.code, contextValidation);
//		assertThat(contextValidation.errors.size()).isEqualTo(0);
//	}
//	
//	@Test
//	public void validationProjectCategoryRequired() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		ProjectValidationHelper.validateProjectCategoryCode(null, contextValidation);
//		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
//	}
//	
//	@Test
//	public void validationProjectCategoryNotExist() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		ProjectValidationHelper.validateProjectCategoryCode("notexist", contextValidation);
//		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
//	}
//	
//	/**
//	 *  ReagentType
//	 */
//
//	
//	/**
//	 * SampleCategory 
//	 */
//	@Test
//	public void validationSampleCategoryCode() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		SampleValidationHelper.validateSampleCategoryCode(sampleCategory.code, contextValidation);
//		assertThat(contextValidation.errors.size()).isEqualTo(0);
//	}
//	
//	@Test
//	public void validationSampleCategoryRequired() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		SampleValidationHelper.validateSampleCategoryCode(null, contextValidation);
//		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
//	}
//	
//	@Test
//	public void validationSampleCategoryNotExist() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		SampleValidationHelper.validateSampleCategoryCode("notexist", contextValidation);
//		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
//	}	
//	
//	
//	// TO DO
//	public void validationProjectTest() {
//		//ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		//DescriptionValidationHelper.validationProcess(null, null, contextValidation);
//	}
//
//	// TO DO
//	public void validationProcessTest() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		ProcessValidationHelper.validateProcessType(null, null, contextValidation);		
//	}
//	
//	
//
//}
