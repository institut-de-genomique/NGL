package fr.cea.ig.ngl.dao.api;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import fr.cea.ig.ngl.dao.containers.ContainersAPI;
import fr.cea.ig.ngl.dao.containers.ContainersDAO;
import fr.cea.ig.ngl.dao.api.factory.ContainerFactory;
import fr.cea.ig.ngl.utils.TestUtils;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.LocationOnContainerSupport;
import ngl.refactoring.state.ContainerStateNames;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.PropertyValue;
import play.Logger;
import play.modules.jongo.MongoDBPlugin;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.container.instance.ContainerValidationHelper;
import workflows.container.ContWorkflows;

/**
 * Test de l'API de l'entité CONTAINER.
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ MongoDBPlugin.class, CommonValidationHelper.class, ContainerValidationHelper.class })
public class ContainerAPITest {
	
	private final ContainersDAO containerDAO = Mockito.mock(ContainersDAO.class);
	
	private final ContWorkflows containerWF = Mockito.mock(ContWorkflows.class);

	private final ContainersAPI containerAPI = new ContainersAPI(containerDAO, containerWF);
	
	/**
	 * Si on veut faire des traitements avant l'exécution de la suite de tests. 
	 * Par exemple : initialiser un mock, initialiser une méthode statique, ...
	 */
	@Before
	public void setUp() {
		PowerMockito.mockStatic(MongoDBPlugin.class);
		PowerMockito.mockStatic(CommonValidationHelper.class);
		PowerMockito.mockStatic(ContainerValidationHelper.class);

		try {
			PowerMockito.doNothing().when(CommonValidationHelper.class, "validateIdPrimary", any(ContextValidation.class), any(Container.class));
			PowerMockito.doNothing().when(CommonValidationHelper.class, "validateCodePrimary", any(ContextValidation.class), any(Container.class), any(String.class));
			PowerMockito.doNothing().when(ContainerValidationHelper.class, "validateContainerStateRequired", any(ContextValidation.class), any(State.class));
			PowerMockito.doNothing().when(CommonValidationHelper.class, "validateTraceInformationRequired", any(ContextValidation.class), any(TraceInformation.class));
			PowerMockito.doNothing().when(ContainerValidationHelper.class, "validateContainerCategoryCodeRequired", any(ContextValidation.class), any(String.class), any(String.class));
			PowerMockito.doNothing().when(ContainerValidationHelper.class, "validateImportTypeOptional", any(ContextValidation.class), any(String.class), any(Map.class));		
			PowerMockito.doNothing().when(ContainerValidationHelper.class, "validateContents", any(ContextValidation.class), any(List.class), any(String.class));
			PowerMockito.doNothing().when(ContainerValidationHelper.class, "validateContainerSupportRequired", any(ContextValidation.class), any(LocationOnContainerSupport.class));
			PowerMockito.doNothing().when(ContainerValidationHelper.class, "validateInputProcessCodes", any(ContextValidation.class), any(Collection.class), any(String.class));		
			PowerMockito.doNothing().when(ContainerValidationHelper.class, "validateQualityControlResults", any(ContextValidation.class), any(List.class));
			PowerMockito.doNothing().when(ContainerValidationHelper.class, "validateConcentrationOptional", any(ContextValidation.class), any(PropertyValue.class));
			PowerMockito.doNothing().when(ContainerValidationHelper.class, "validateQuantityOptional", any(ContextValidation.class), any(PropertyValue.class));
			PowerMockito.doNothing().when(ContainerValidationHelper.class, "validateVolumeOptional", any(ContextValidation.class), any(PropertyValue.class));
			PowerMockito.doNothing().when(ContainerValidationHelper.class, "validateSizeOptional", any(ContextValidation.class), any(PropertyValue.class));
		} catch (Exception e) {
			Logger.error("Exception occured during setUp()");
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testGetByCode() {
		String randomCode = UUID.randomUUID().toString();
		
		Container container = ContainerFactory.getRandomContainer(ContainerStateNames.IW_P);
		container.code = randomCode;
		
		when(containerDAO.findByCode(randomCode)).thenReturn(container);
		
		Container containerGetApi = containerAPI.get(randomCode);
		
		assertNotNull(containerGetApi);
		assertTrue("Different code on testGetByCode()", randomCode.equals(containerGetApi.code));
	}
	
	@Test
	public void testCreateValid() throws Exception {
		String randomCode = UUID.randomUUID().toString();

		Container container = ContainerFactory.getRandomContainer(ContainerStateNames.IW_P);
		container.code = randomCode;

		when(containerDAO.saveObject(container)).thenReturn(container);

		try {
			Container containerCreateAPI = containerAPI.create(container, TestUtils.CURRENT_USER);

			assertTrue("Different code on testCreateValid()", containerCreateAPI.code.equals(container.code));
			assertNotNull(containerCreateAPI);
		} catch (APIException e) {
			Logger.error("Exception occured during testCreateValid()");
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testCreateInvalidCode() {		
		// On tente de créer un container avec un code null.
		Container container = mock(Container.class);
		container.code = null;
		
		boolean exceptFired = false;
		
		Container containerCreateAPI = null;
		
		try {
			containerCreateAPI = containerAPI.create(container, TestUtils.CURRENT_USER);
		} catch (APIException e) {
			exceptFired = true;
		}

		if (!exceptFired) {
			fail("Invalid container, should have been refused.");
		}
		
		assertNull(containerCreateAPI);
	}

	@Test
	public void testCreateInvalidAlreadyExist() {		
		// On tente de créer un container qui existe déjà.
		Container container = mock(Container.class);
		container.code = UUID.randomUUID().toString();
		
		when(containerDAO.isObjectExist(container.code)).thenReturn(true);
		
		boolean exceptFired = false;
		
		Container containerCreateAPI = null;
		
		try {
			containerCreateAPI = containerAPI.create(container, TestUtils.CURRENT_USER);
		} catch (APIException e) {
			exceptFired = true;
		}

		if (!exceptFired) {
			fail("Invalid container, should have been refused.");
		}
		
		assertNull(containerCreateAPI);
	}
	
	@Test
	public void testDeleteValid() {
		Container container = ContainerFactory.getRandomContainer(ContainerStateNames.IW_P);
		
		when(containerDAO.checkObjectExistByCode(container.code)).thenReturn(true);
		
		try {
			containerAPI.delete(container.code);
		} catch (APIException e) {
			Logger.error("Exception occured during testDeleteValid()");
			fail(e.getMessage());
		}
		
		when(containerDAO.findByCode(container.code)).thenReturn(null);
		
		Container containerFindAPI = containerAPI.get(container.code);
		
		assertNull("Container still exists on testDelete()", containerFindAPI);
	}
	
	@Test
	public void testDeleteInvalid() {
		Container container = ContainerFactory.getRandomContainer(ContainerStateNames.IW_P);
		
		when(containerDAO.isObjectExist(container.code)).thenReturn(false);
		when(containerDAO.getElementClass()).thenReturn(Container.class);
		
		boolean exceptFired = false;
		
		try {
			containerAPI.delete(container.code);
		} catch (APIException e) {
			exceptFired = true;
		}
		
		if (!exceptFired) {
			fail("Invalid run, should have been refused.");
		}
	}
	
	@Test
	public void testUpdateValid() throws Exception {
		Container container = mock(Container.class);
		container.code = "1234";
		container.categoryCode = "testUpdate";
		
		when(containerDAO.isCodeExist(container.code)).thenReturn(true);
		when(containerDAO.findByCode(container.code)).thenReturn(container);

		Container containerUpdateAPI = null;
		
		try {
			containerUpdateAPI = containerAPI.update(container, TestUtils.CURRENT_USER);
		} catch (APIException e) {
			Logger.error("Exception occured during testUpdateValid()");
			fail(e.getMessage());
		}
		
		assertNotNull(containerUpdateAPI);
		assertTrue("Different code on testUpdate()", containerUpdateAPI.code.equals(container.code));
	} 
	
	@Test
	public void testUpdateInvalid() {
		Container container = mock(Container.class);
		
		when(containerDAO.isCodeExist(container.code)).thenReturn(false);
		
		Container containerUpdateAPI = null;
		boolean exceptFired = false;
		
		try {
			containerUpdateAPI = containerAPI.update(container, TestUtils.CURRENT_USER);
		} catch (APIException e) {
			exceptFired = true;
		}
		
		if (!exceptFired) {
			fail("Invalid container, should have been refused.");
		}
		
		assertNull(containerUpdateAPI);
	}
}
