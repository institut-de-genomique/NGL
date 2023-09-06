package fr.cea.ig.ngl.validation.reagentCatalog;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mongojack.DBQuery;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.dao.api.factory.CatalogFactory;
import fr.cea.ig.ngl.dao.api.factory.ExperimentFactory;
import fr.cea.ig.ngl.dao.api.factory.ExperimentTypeFactory;
import fr.cea.ig.ngl.utils.MockUtils;
import fr.cea.ig.ngl.utils.TestUtils;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.reagent.description.KitCatalog;
import models.utils.InstanceConstants;
import play.modules.jongo.MongoDBPlugin;
import validation.ContextValidation;
import validation.reagentCatalogs.instance.KitCatalogValidationHelper;
import validation.reagentCatalogs.instance.KitCatalogValidationHelper.UpdatedExperimentTypesValidator;


@RunWith(PowerMockRunner.class)
@PrepareForTest({ MongoDBDAO.class, MongoDBPlugin.class})
public class KitCatalogValidationHelperTest {
	
	private KitCatalog mockKitCatalog() {
		KitCatalog kitCatalog = mock(KitCatalog.class);
		kitCatalog.experimentTypeCodes = Collections.emptyList();
		return kitCatalog;
	}
	
	@SuppressWarnings("unchecked")
	@Before
	public void setUp() {
		PowerMockito.mockStatic(MongoDBDAO.class);
		PowerMockito.mockStatic(MongoDBPlugin.class);
		
		//KitCatalog

		when(MongoDBDAO.findByCode(any(String.class), any(Class.class), any(String.class)))
		.thenAnswer(MockUtils.answerSuppliedDBResult(this::mockKitCatalog));
		
		//Empty List
		
		when(MongoDBDAO.find(eq(InstanceConstants.REAGENT_CATALOG_COLL_NAME), any(Class.class), any(DBQuery.Query.class)))
		.thenAnswer(MockUtils.answerEmptyDBResult());
		
		//Experiment List
		
		when(MongoDBDAO.find(eq(InstanceConstants.EXPERIMENT_COLL_NAME), eq(Experiment.class), any(DBQuery.Query.class)))
		.thenAnswer(MockUtils.answerDBResult(ExperimentFactory::getRandomExperimentBionanoList));
	}

	@Test
	public final void testValidateExperimentTypeCodesUpdate() throws Exception {	
		ContextValidation contextValidation = ContextValidation.createUpdateContext(TestUtils.CURRENT_USER);
		List<String> experimentTypeCodes = ExperimentTypeFactory.getExperimentTypeCodes();
		
		UpdatedExperimentTypesValidator validator = mock(UpdatedExperimentTypesValidator.class);
		
		KitCatalogValidationHelper.validateExperimentTypeCodesUpdate(contextValidation, experimentTypeCodes, validator);
		
		verify(validator, times(TestUtils.LIST_SIZE)).validateIsNotRemovingUsedExperimentType(eq(contextValidation), anyString());
	}
	
	@Test
	public final void testIsRemovingThis() {
		String kitCatalogCode = CatalogFactory.getKitCatalogCode();
		List<String> experimentTypeCodes = ExperimentTypeFactory.getExperimentTypeCodes();
		
		List<String> updatedExperimentTypeCodes = experimentTypeCodes.stream()
				.skip(1)
				.collect(Collectors.toList());
		
		
		UpdatedExperimentTypesValidator validator = new UpdatedExperimentTypesValidator(kitCatalogCode, updatedExperimentTypeCodes);
		
		String removedExperimentTypeCode = experimentTypeCodes.get(0);
		String notRemovedExperimentTypeCode = experimentTypeCodes.get(1);
		
		assertTrue(validator.isRemovingThis(removedExperimentTypeCode));
		assertFalse(validator.isRemovingThis(notRemovedExperimentTypeCode));
	}
	
	private final void testValidateIsNotRemovingUsedExperimentType(final int expectedErrors) {
		ContextValidation contextValidation = ContextValidation.createUpdateContext(TestUtils.CURRENT_USER);
		String kitCatalogCode = CatalogFactory.getKitCatalogCode();
		List<String> experimentTypeCodes = ExperimentTypeFactory.getExperimentTypeCodes();
		
		List<String> updatedExperimentTypeCodes = experimentTypeCodes.stream()
				.skip(expectedErrors)
				.collect(Collectors.toList());
		
		UpdatedExperimentTypesValidator validator = new UpdatedExperimentTypesValidator(kitCatalogCode, updatedExperimentTypeCodes);
		
		for(String experimentTypeCode : experimentTypeCodes) {
			validator.validateIsNotRemovingUsedExperimentType(contextValidation, experimentTypeCode);
		}
		
		int errorCount = TestUtils.trueErrorCount(contextValidation);

		assertEquals(expectedErrors, errorCount);
	}
	
	@Test
	public final void testValidateIsNotRemovingUsedExperimentType0() {
		testValidateIsNotRemovingUsedExperimentType(0);
	}
	
	@Test
	public final void testValidateIsNotRemovingUsedExperimentType1() {
		testValidateIsNotRemovingUsedExperimentType(1);
	}
	
	@Test
	public final void testValidateIsNotRemovingUsedExperimentType3() {
		testValidateIsNotRemovingUsedExperimentType(3);
	}

}
