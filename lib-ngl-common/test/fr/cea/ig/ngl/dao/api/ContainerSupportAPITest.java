package fr.cea.ig.ngl.dao.api;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import fr.cea.ig.ngl.dao.containers.ContainerSupportsAPI;
import fr.cea.ig.ngl.dao.containers.ContainerSupportsDAO;
import fr.cea.ig.ngl.dao.containers.ContainersAPI;
import fr.cea.ig.ngl.dao.api.factory.ContainerSupportFactory;
import fr.cea.ig.ngl.dao.api.factory.StateFactory;
import fr.cea.ig.ngl.utils.TestUtils;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import play.Logger;
import play.modules.jongo.MongoDBPlugin;
import validation.common.instance.CommonValidationHelper;
import validation.container.instance.ContainerSupportValidationHelper;
import workflows.container.ContSupportWorkflows;

/**
 * Test de l'API de l'entité CONTAINER.
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ MongoDBPlugin.class, CommonValidationHelper.class, ContainerSupportValidationHelper.class })
public class ContainerSupportAPITest {
	
	private final ContainerSupportsDAO containerSupportDAO = Mockito.mock(ContainerSupportsDAO.class);
	
	private final ContSupportWorkflows containerSupportWF = Mockito.mock(ContSupportWorkflows.class);

	private final ContainersAPI containerAPI = Mockito.mock(ContainersAPI.class);

	private final ContainerSupportsAPI containerSupportAPI = new ContainerSupportsAPI(containerSupportDAO, containerAPI, containerSupportWF);
	
	/**
	 * Si on veut faire des traitements avant l'exécution de la suite de tests. 
	 * Par exemple : initialiser un mock, initialiser une méthode statique, ...
	 */
	@Before
	public void setUp() {
		PowerMockito.mockStatic(MongoDBPlugin.class);
		PowerMockito.mockStatic(CommonValidationHelper.class);
		PowerMockito.mockStatic(ContainerSupportValidationHelper.class);
	}
	
	@Test
	public void testGetByCode() {
		String randomCode = UUID.randomUUID().toString();
		
		ContainerSupport containerSupport = ContainerSupportFactory.getRandomContainerSupport(true, true, StateFactory.getInProgressState());
		containerSupport.code = randomCode;
		
		when(containerSupportDAO.findByCode(randomCode)).thenReturn(containerSupport);
		
		ContainerSupport containerSupportGetApi = containerSupportAPI.get(randomCode);
		
		assertNotNull(containerSupportGetApi);
		assertTrue("Different code on testGetByCode()", randomCode.equals(containerSupportGetApi.code));
	}
	
	@Test
	public void testCreateValid() throws Exception {
		String randomCode = UUID.randomUUID().toString();

		ContainerSupport containerSupport = ContainerSupportFactory.getRandomContainerSupport(true, true, StateFactory.getInProgressState());
		containerSupport.code = randomCode;

		when(containerSupportDAO.saveObject(containerSupport)).thenReturn(containerSupport);

		try {
			ContainerSupport containerSupportCreateAPI = containerSupportAPI.create(containerSupport, TestUtils.CURRENT_USER);

			assertTrue("Different code on testCreateValid()", containerSupportCreateAPI.code.equals(containerSupport.code));
			assertNotNull(containerSupportCreateAPI);
		} catch (APIException e) {
			Logger.error("Exception occured during testCreateValid()");
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testCreateInvalidCode() {		
		// On tente de créer un container avec un code null.
		ContainerSupport containerSupport = mock(ContainerSupport.class);
		containerSupport.code = null;
		
		boolean exceptFired = false;
		
		ContainerSupport containerSupportCreateAPI = null;
		
		try {
			containerSupportCreateAPI = containerSupportAPI.create(containerSupport, TestUtils.CURRENT_USER);
		} catch (APIException e) {
			exceptFired = true;
		}

		if (!exceptFired) {
			fail("Invalid container support, should have been refused.");
		}
		
		assertNull(containerSupportCreateAPI);
	}

	@Test
	public void testCreateInvalidAlreadyExist() {		
		// On tente de créer un container support qui existe déjà.
		ContainerSupport containerSupport = mock(ContainerSupport.class);
		containerSupport.code = UUID.randomUUID().toString();
		
		when(containerSupportDAO.isObjectExist(containerSupport.code)).thenReturn(true);
		
		boolean exceptFired = false;
		
		ContainerSupport containerSupportCreateAPI = null;
		
		try {
			containerSupportCreateAPI = containerSupportAPI.create(containerSupport, TestUtils.CURRENT_USER);
		} catch (APIException e) {
			exceptFired = true;
		}

		if (!exceptFired) {
			fail("Invalid container support, should have been refused.");
		}
		
		assertNull(containerSupportCreateAPI);
	}
	
	@Test
	public void testDeleteValid() {
		ContainerSupport containerSupport = ContainerSupportFactory.getRandomContainerSupport(true, true, StateFactory.getInProgressState());
		
		when(containerSupportDAO.checkObjectExistByCode(containerSupport.code)).thenReturn(true);
		
		try {
			containerSupportAPI.delete(containerSupport.code);
		} catch (APIException e) {
			Logger.error("Exception occured during testDeleteValid()");
			fail(e.getMessage());
		}
		
		when(containerSupportDAO.findByCode(containerSupport.code)).thenReturn(null);
		
		Container containerFindAPI = containerAPI.get(containerSupport.code);
		
		assertNull("Container support still exists on testDelete()", containerFindAPI);
	}
	
	@Test
	public void testDeleteInvalid() {
		ContainerSupport containerSupport = ContainerSupportFactory.getRandomContainerSupport(true, true, StateFactory.getInProgressState());
		
		when(containerSupportDAO.isObjectExist(containerSupport.code)).thenReturn(false);
		when(containerSupportDAO.getElementClass()).thenReturn(ContainerSupport.class);
		
		boolean exceptFired = false;
		
		try {
			containerSupportAPI.delete(containerSupport.code);
		} catch (APIException e) {
			exceptFired = true;
		}
		
		if (!exceptFired) {
			fail("Invalid container support, should have been refused.");
		}
	}
	
	@Test
	public void testUpdateValid() throws Exception {
		ContainerSupport containerSupport = mock(ContainerSupport.class);
		containerSupport.code = "1234";
		containerSupport.categoryCode = "testUpdate";
		
		when(containerSupportDAO.isCodeExist(containerSupport.code)).thenReturn(true);
		when(containerSupportDAO.findByCode(containerSupport.code)).thenReturn(containerSupport);

		ContainerSupport containerSupportUpdateAPI = null;
		
		try {
			containerSupportUpdateAPI = containerSupportAPI.update(containerSupport, TestUtils.CURRENT_USER);
		} catch (APIException e) {
			Logger.error("Exception occured during testUpdateValid()");
			fail(e.getMessage());
		}
		
		assertNotNull(containerSupportUpdateAPI);
		assertTrue("Different code on testUpdate()", containerSupportUpdateAPI.code.equals(containerSupport.code));
	} 
	
	@Test
	public void testUpdateInvalid() {
		ContainerSupport containerSupport = mock(ContainerSupport.class);
		
		when(containerSupportDAO.isCodeExist(containerSupport.code)).thenReturn(false);
		
		ContainerSupport containerSupportUpdateAPI = null;
		boolean exceptFired = false;
		
		try {
			containerSupportUpdateAPI = containerSupportAPI.update(containerSupport, TestUtils.CURRENT_USER);
		} catch (APIException e) {
			exceptFired = true;
		}
		
		if (!exceptFired) {
			fail("Invalid container support, should have been refused.");
		}
		
		assertNull(containerSupportUpdateAPI);
	}
}
