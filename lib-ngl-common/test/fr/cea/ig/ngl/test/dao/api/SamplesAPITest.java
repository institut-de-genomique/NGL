package fr.cea.ig.ngl.test.dao.api;

import java.util.Date;
import java.util.Iterator;

import javax.inject.Singleton;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.projects.ProjectsAPI;
import fr.cea.ig.ngl.dao.samples.SamplesAPI;
import fr.cea.ig.ngl.support.ListFormWrapper;
import fr.cea.ig.ngl.test.AbstractAPITests;
import fr.cea.ig.ngl.test.dao.api.factory.QueryMode;
import fr.cea.ig.ngl.test.dao.api.factory.TestProjectFactory;
import fr.cea.ig.ngl.test.dao.api.factory.TestSampleFactory;
import models.laboratory.project.instance.Project;
import models.laboratory.sample.instance.Sample;
import utils.AbstractSQTests;

/**
 * Test {@link SamplesAPI} methods.
 * 
 * @author ajosso
 *
 */
@Singleton  
public class SamplesAPITest extends AbstractSQTests implements AbstractAPITests {

	private static final play.Logger.ALogger logger = play.Logger.of(SamplesAPITest.class);

	//Tested API
	private static SamplesAPI api;
	
	// required APIs
	private static ProjectsAPI projectApi;

	private static final String USER = "ngsrg";
	
	// Reference objects
	private static Sample refSample;
	private static Project refProject;
	
	private Sample data;
	
	@Rule
    public ExpectedException exceptions = ExpectedException.none();

	private static boolean clean = true;
	
	@BeforeClass
	public static void setUpClass() {
		Assert.assertTrue(app.isDev());
		projectApi = app.injector().instanceOf(ProjectsAPI.class);
		Assert.assertNotNull(projectApi);
		api = app.injector().instanceOf(SamplesAPI.class);
		Assert.assertNotNull(api);
		logger.debug("define ref objects");
		refProject = TestProjectFactory.project(USER);
		refSample = TestSampleFactory.sample(USER, refProject);
	}
	
	@After
	public void tearDown() {
		if(! clean) {
			deleteData();
		} else {
			logger.trace("data already cleaned");
		}
	}

	@Override
	@Before
	public void setUpData() {
		if(clean) {
			logger.debug("create dep objects and test data");
			try {
				projectApi.create(refProject, USER);
				data = api.create(refSample, USER);
			} catch (APIValidationException e) {
				logger.error(e.getMessage());
				logger.error("invalid fields: " + e.getErrors().keySet().toString());
				logValidationErrors(e);
				exit(e.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage());
				exit(e.getMessage());
			} finally {
				clean = false;
			}
		} else {
			deleteData();
			setUpData();
		}
	}

	@Override
	@After
	public void deleteData() {
		logger.debug("delete dep objects and test data");
		try {
			api.delete(data.code);
		} catch (Exception e) {
			logger.error(e.getMessage());
			if(logger.isDebugEnabled()) {
				e.printStackTrace();
			}
		} finally {
			logger.debug("remove dep objects");
			projectApi.delete(refProject.code);	
			clean = true;
		}
	}

	@Test
	public void createTest() {
		logger.debug("Creation test");
		Assert.assertEquals(refSample.categoryCode, data.categoryCode);
		Assert.assertEquals(refSample.code, data.code);
		Assert.assertEquals(refSample.importTypeCode, data.importTypeCode);
		Assert.assertEquals(refSample.name, data. name);
		Assert.assertEquals(refSample.typeCode, data.typeCode);
	}

	@Test
	public void deleteTest() throws APIException {
		logger.debug("Delete test");
		api.delete(refSample.code);
		Assert.assertNull(api.get(refSample.getCode()));
	}

	@Test
	public void getTest() {
		logger.debug("Get test");
		try {
			Sample sample = api.get(refSample.code);
			Assert.assertNotNull(sample);
			Assert.assertEquals(data.get_id(), sample.get_id());
			Assert.assertEquals(refSample.getCode(), sample.getCode());
		} catch (Exception e) {
			logger.error(e.getMessage());
			exit(e.getMessage());
		}
	}

	@Test
	public void listTest() {
		logger.debug("List test");
		
		// Testing rendering ?
//		ListFormWrapper<Sample> listing = TestSampleFactory.wrapper(refProject.code, true, false, false);
//		ListFormWrapper<Sample> datatabling = TestSampleFactory.wrapper(refProject.code, false, true, false);
//		ListFormWrapper<Sample> counting = TestSampleFactory.wrapper(refProject.code, false, false, true);
//		ListFormWrapper<Sample> othering = TestSampleFactory.wrapper(refProject.code, false, false, false);
		
		try {
			//---------- default mode ----------
			logger.debug("default mode");
			ListFormWrapper<Sample> wrapper = TestSampleFactory.wrapper(refProject.code);
			Iterable<Sample> samples = api.listObjects(wrapper);
			Iterator<Sample> iter = samples.iterator();
			int count = 0;
			while(iter.hasNext()) {
				Sample s = iter.next();
				Assert.assertEquals(refSample.code, s.code);
				Assert.assertEquals(refSample.categoryCode, s.categoryCode);
				count++;
			}
			Assert.assertEquals(1, count);
			
			//---------- reporting mode----------
			logger.debug("reporting mode");
			wrapper = TestSampleFactory.wrapper(refProject.code, QueryMode.REPORTING, null);
			samples = api.listObjects(wrapper);
			iter = samples.iterator();
			count = 0;
			while(iter.hasNext()) {
				Sample s = iter.next();
				Assert.assertEquals(refSample.code, s.code);
				Assert.assertEquals(refSample.categoryCode, s.categoryCode);
				count++;
			}
			Assert.assertEquals(1, count);
			
			// Not used in Sample
//			//---------- aggregate mode----------
//			logger.debug("aggregate mode");
//			wrapper = TestSampleFactory.wrapper(refProject.code, TestSampleFactory.QueryMode.AGGREGATE, null);
//			samples = api.listObjects(wrapper);
//			iter = samples.iterator();
//			count = 0;
//			while(iter.hasNext()) {
//				Sample s = iter.next();
//				Assert.assertEquals(refSample.code, s.code);
//				Assert.assertEquals(refSample.categoryCode, s.categoryCode);
//				count++;
//			}
//			Assert.assertEquals(1, count);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getCause().getMessage());
			exit(e.getMessage());
		}
	}
	
	@Test
	public void isObjectExistsTest() {
		Assert.assertTrue(api.isObjectExist(refSample.code));
	}
	
	@Test
	public void isObjectNotExistsTest() {
		Assert.assertFalse(api.isObjectExist(refSample.code));
	}

	@Test
	public void updateTest() {
		logger.debug("Update test");
		try {
			Sample sampleToUpdate = data;
			String newName = "New name after update";
			sampleToUpdate.name = newName;
			sampleToUpdate.traceInformation.modifyUser = USER;
			sampleToUpdate.traceInformation.modifyDate = new Date();
			api.update(sampleToUpdate, USER);
			Sample sample = api.get(refSample.code);
			Assert.assertEquals(refSample.categoryCode, sample.categoryCode);
			Assert.assertNotEquals(refSample.name, sample.name);
			Assert.assertEquals(newName, sample.name);
		} catch (APIValidationException e) {
			logger.error(e.getMessage());
			logValidationErrors(e);
			exit(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
			exit(e.getMessage());
		}
	}

	@Override
	public play.Logger.ALogger logger() {
		return logger;
	}

}
