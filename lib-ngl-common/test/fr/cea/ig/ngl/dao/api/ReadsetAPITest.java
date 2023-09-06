package fr.cea.ig.ngl.dao.api;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

import java.util.Date;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate.Builder;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import fr.cea.ig.ngl.dao.readsets.ReadSetsAPI;
import fr.cea.ig.ngl.dao.readsets.ReadSetsDAO;
import fr.cea.ig.ngl.dao.runs.RunsDAO;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.dao.api.factory.ReadsetFactory;
import fr.cea.ig.ngl.dao.api.factory.SampleFactory;
import fr.cea.ig.ngl.utils.TestUtils;
import models.laboratory.run.instance.ReadSet;
import play.Logger;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.run.instance.FileValidationHelper;
import validation.run.instance.ReadSetValidationHelper;
import validation.run.instance.TreatmentValidationHelper;
import validation.utils.ValidationHelper;
import workflows.readset.ReadSetWorkflows;

/**
 * Test de l'API de l'entité READSET.
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ FileValidationHelper.class, TreatmentValidationHelper.class, ValidationHelper.class, ReadSetValidationHelper.class, CommonValidationHelper.class, MongoDBDAO.class })
public class ReadsetAPITest {
	
	private final RunsDAO runDAO = Mockito.mock(RunsDAO.class);
	
	private final ReadSetsDAO rsDAO = Mockito.mock(ReadSetsDAO.class);
	
	private final ReadSetWorkflows readsetWF = Mockito.mock(ReadSetWorkflows.class);
	
	private final ReadSetsAPI readsetAPI = new ReadSetsAPI(rsDAO, runDAO, readsetWF);
	
	@Before
	public void setUp()  {
		PowerMockito.mockStatic(CommonValidationHelper.class);
		PowerMockito.mockStatic(ReadSetValidationHelper.class);
		PowerMockito.mockStatic(ValidationHelper.class);
		PowerMockito.mockStatic(TreatmentValidationHelper.class);
		PowerMockito.mockStatic(FileValidationHelper.class);
		PowerMockito.mockStatic(MongoDBDAO.class);
	}
	
	@Test
	public void testGetByCode() {
		String randomCode = UUID.randomUUID().toString();
		
		ReadSet readset = ReadsetFactory.getRandomReadset(true, new Date());
		readset.code = randomCode;
		
		when(rsDAO.findByCode(randomCode)).thenReturn(readset);
		
		ReadSet rsGetApi = readsetAPI.get(randomCode);
		
		assertNotNull(rsGetApi);
		assertTrue("Different code on testGetByCode()", randomCode.equals(rsGetApi.code));
	}
	
	@Test
	public void testCreateValid() {
		String randomCode = UUID.randomUUID().toString();

		ReadSet readset = ReadsetFactory.getRandomReadset(false, new Date());
		readset.code = randomCode;

		when(rsDAO.save(readset)).thenReturn(readset);

		try {
			ReadSet readsetCreateAPI = readsetAPI.create(readset, TestUtils.CURRENT_USER);

			assertTrue("Different code on testCreateValid()", readsetCreateAPI.code.equals(readset.code));
			assertNotNull(readsetCreateAPI);
		} catch (APIException e) {
			Logger.error("Exception occured during testCreateValid()");
			fail(e.getMessage());
		}		
	}
	
	@Test
	public void testCreateInvalid() {
		String randomCode = UUID.randomUUID().toString();
		
		// On tente de créer un run avec un _id déjà existant.
		ReadSet readSet = mock(ReadSet.class);
		readSet._id = "1234";
		readSet.code = randomCode;
		
		when(rsDAO.saveObject(readSet)).thenReturn(readSet);
		
		boolean exceptFired = false;
		
		ReadSet readSetCreateAPI = null;
		
		try {
			readSetCreateAPI = readsetAPI.create(readSet, TestUtils.CURRENT_USER);
		} catch (APIException e) {
			exceptFired = true;
		}

		if (!exceptFired) {
			fail("Invalid readset, should have been refused.");
		}
		
		assertNull(readSetCreateAPI);
	}
	
	@Test
	public void testDeleteValid() {
		ReadSet readset = ReadsetFactory.getRandomReadset(true, new Date());
		
		when(rsDAO.findByCode(any(String.class))).thenReturn(readset);
		when(rsDAO.checkObjectExist(any(DBQuery.Query.class))).thenReturn(true);
		when(rsDAO.checkObjectExistByCode(any(String.class))).thenReturn(true);

		PowerMockito.doNothing().when(rsDAO).update(any(DBQuery.Query.class), any(Builder.class));

		when(MongoDBDAO.findByCode(any(), any(), any())).thenReturn(SampleFactory.getRandomSample(new Date(), true));

		try {
			readsetAPI.delete(readset.code);
		} catch (APIException e) {
			Logger.error("Exception occured during testDeleteValid()");
			fail(e.getMessage());
		}
		
		when(rsDAO.findByCode(readset.code)).thenReturn(null);
		
		ReadSet rsFindAPI = readsetAPI.get(readset.code);
		
		assertNull("Readset still exists on testDelete()", rsFindAPI);
	}
	
	@Test
	public void testDeleteInvalid() {
		ReadSet readset = ReadsetFactory.getRandomReadset(true, new Date());
		
		when(rsDAO.isObjectExist(readset.code)).thenReturn(false);
		when(rsDAO.getElementClass()).thenReturn(ReadSet.class);
		
		boolean exceptFired = false;
		
		try {
			readsetAPI.delete(readset.code);
		} catch (APIException e) {
			exceptFired = true;
		}
		
		if (!exceptFired) {
			fail("Invalid readset, should have been refused.");
		}
	}
	
	@Test
	public void testUpdateValid() {
		ReadSet readset = mock(ReadSet.class);
		ContextValidation ctxVal = ContextValidation.createUpdateContext(TestUtils.CURRENT_USER);
		
		when(rsDAO.isObjectExist(readset.code)).thenReturn(Boolean.TRUE);
		when(rsDAO.findByCode(readset.code)).thenReturn(readset);
		
		when(rsDAO.save(readset)).thenReturn(readset);
		doNothing().when(readset).validate(ctxVal);
		
		ReadSet readsetUpdateAPI = null;
		
		try {
			readsetUpdateAPI = readsetAPI.update(readset, TestUtils.CURRENT_USER);
		} catch (APIException e) {
			Logger.error("Exception occured during testUpdateValid()");
			fail(e.getMessage());
		}
		
		assertNotNull(readsetUpdateAPI);
	} 
	
	@Test
	public void testUpdateInvalid() {
		ReadSet readset = mock(ReadSet.class);
		
		when(rsDAO.isObjectExist(readset.code)).thenReturn(Boolean.FALSE);
		when(rsDAO.getElementClass()).thenReturn(ReadSet.class);
		
		ReadSet rsUpdateAPI = null;
		boolean exceptFired = false;
		
		try {
			rsUpdateAPI = readsetAPI.update(readset, TestUtils.CURRENT_USER);
		} catch (APIException e) {
			exceptFired = true;
		}
		
		if (!exceptFired) {
			fail("Invalid readset, should have been refused.");
		}
		
		assertNull(rsUpdateAPI);
	}
}
