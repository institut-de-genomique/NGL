package fr.cea.ig.ngl.dao.api;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.dao.samples.SamplesAPI;
import fr.cea.ig.ngl.dao.samples.SamplesDAO;
import fr.cea.ig.ngl.dao.api.factory.SampleFactory;
import fr.cea.ig.ngl.utils.TestUtils;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.sample.instance.Sample;
import models.utils.instance.SampleHelper;
import play.Logger;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.sample.instance.SampleValidationHelper;

/**
 * Test de l'API de l'entité SAMPLE.
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ MongoDBDAO.class, SampleHelper.class, CommonValidationHelper.class, SampleValidationHelper.class })
public class SampleAPITest {
	
	private final SamplesDAO sampleDAO = Mockito.mock(SamplesDAO.class);
	
	private final SamplesAPI sampleAPI = new SamplesAPI(sampleDAO);
	
	/**
	 * Si on veut faire des traitements avant l'exécution de la suite de tests. 
	 * Par exemple : initialiser un mock, initialiser une méthode statique, ...
	 */
	@Before
	public void setUp() {
		PowerMockito.mockStatic(MongoDBDAO.class);
		PowerMockito.mockStatic(SampleHelper.class);
		PowerMockito.mockStatic(CommonValidationHelper.class);
		PowerMockito.mockStatic(SampleValidationHelper.class);

		try {
			PowerMockito.doNothing().when(CommonValidationHelper.class, "validateIdPrimary", any(ContextValidation.class), any(Sample.class));
			PowerMockito.doNothing().when(SampleValidationHelper.class, "validateSampleCategoryCodeRequired", any(ContextValidation.class), any(String.class));
			PowerMockito.doNothing().when(SampleValidationHelper.class, "validateSampleType", any(String.class), any(String.class), any(Map.class), any(ContextValidation.class));
			PowerMockito.doNothing().when(CommonValidationHelper.class, "validateTraceInformationRequired", any(ContextValidation.class), any(TraceInformation.class));
			PowerMockito.doNothing().when(CommonValidationHelper.class, "validateRulesWithObjects", any(ContextValidation.class), any(Sample.class));
		} catch (Exception e) {
			Logger.error("Exception occured during setUp()");
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testGetByCode() {
		String randomCode = UUID.randomUUID().toString();
		
		Sample sample = SampleFactory.getRandomSample(new Date(), true);
		sample.code = randomCode;
		
		when(sampleDAO.findByCode(randomCode)).thenReturn(sample);
		
		Sample sampleGetApi = sampleAPI.get(randomCode);
		
		assertNotNull(sampleGetApi);
		assertTrue("Different code on testGetByCode()", randomCode.equals(sampleGetApi.code));
	}
	
	@Test
	public void testCreateValid() {
		String randomCode = UUID.randomUUID().toString();

		Sample sample = SampleFactory.getRandomSample(new Date(), true);
		sample.code = randomCode;

		when(sampleDAO.saveObject(sample)).thenReturn(sample);

		try {
			Sample sampCreateAPI = sampleAPI.create(sample, TestUtils.CURRENT_USER);

			assertTrue("Different code on testCreateValid()", sampCreateAPI.code.equals(sample.code));
			assertNotNull(sampCreateAPI);
		} catch (APIException e) {
			Logger.error("Exception occured during testCreateValid()");
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testCreateInvalid() {
		String randomCode = UUID.randomUUID().toString();
		
		// On tente de créer une experience avec un _id déjà existant.
		Sample sample = mock(Sample.class);
		sample._id = "1234";
		sample.code = randomCode;
		
		when(sampleDAO.saveObject(sample)).thenReturn(sample);
		
		boolean exceptFired = false;
		
		Sample sampleCreateAPI = null;
		
		try {
			sampleCreateAPI = sampleAPI.create(sample, TestUtils.CURRENT_USER);
		} catch (APIException e) {
			exceptFired = true;
		}

		if (!exceptFired) {
			fail("Invalid sample, should have been refused.");
		}
		
		assertNull(sampleCreateAPI);
	}
	
	@Test
	public void testDeleteValid() {
		Sample sample = SampleFactory.getRandomSample(new Date(), true);
		
		when(sampleDAO.checkObjectExistByCode(sample.code)).thenReturn(true);
		
		try {
			sampleAPI.delete(sample.code);
		} catch (APIException e) {
			Logger.error("Exception occured during testDeleteValid()");
			fail(e.getMessage());
		}
		
		when(sampleDAO.findByCode(sample.code)).thenReturn(null);
		
		Sample sampleFindAPI = sampleAPI.get(sample.code);
		
		assertNull("Sample still exists on testDelete()", sampleFindAPI);
	}
	
	@Test
	public void testDeleteInvalid() {
		Sample sample = SampleFactory.getRandomSample(new Date(), true);
		
		when(sampleDAO.isObjectExist(sample.code)).thenReturn(false);
		when(sampleDAO.getElementClass()).thenReturn(Sample.class);
		
		boolean exceptFired = false;
		
		try {
			sampleAPI.delete(sample.code);
		} catch (APIException e) {
			exceptFired = true;
		}
		
		if (!exceptFired) {
			fail("Invalid sample, should have been refused.");
		}
	}
	
	@Test
	public void testUpdateValid() {
		Sample sample = mock(Sample.class);
		sample.code = "1234";
		sample.categoryCode = "testUpdate";
		
		when(sampleDAO.findByCode(sample.code)).thenReturn(sample);
		
		Sample sampleUpdateAPI = null;
		
		try {
			sampleUpdateAPI = sampleAPI.update(sample, TestUtils.CURRENT_USER);
		} catch (APIException e) {
			Logger.error("Exception occured during testUpdateValid()");
			fail(e.getMessage());
		}
		
		assertNotNull(sampleUpdateAPI);
		assertTrue("Different code on testUpdate()", sampleUpdateAPI.code.equals(sample.code));
	} 
	
	@Test
	public void testUpdateInvalid() {
		Sample sample = mock(Sample.class);
		
		when(sampleDAO.findByCode(sample.code)).thenReturn(null);
		
		Sample sampleUpdateAPI = null;
		boolean exceptFired = false;
		
		try {
			sampleUpdateAPI = sampleAPI.update(sample, TestUtils.CURRENT_USER);
		} catch (APIException e) {
			exceptFired = true;
		}
		
		if (!exceptFired) {
			fail("Invalid sample, should have been refused.");
		}
		
		assertNull(sampleUpdateAPI);
	}
}
