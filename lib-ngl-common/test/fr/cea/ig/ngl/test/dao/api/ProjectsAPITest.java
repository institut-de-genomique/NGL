package fr.cea.ig.ngl.test.dao.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.Iterator;

import javax.inject.Singleton;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.projects.ProjectsAPI;
import fr.cea.ig.ngl.support.ListFormWrapper;
import fr.cea.ig.ngl.test.AbstractAPITests;
import fr.cea.ig.ngl.test.dao.api.factory.QueryMode;
import fr.cea.ig.ngl.test.dao.api.factory.TestProjectFactory;
import models.laboratory.project.instance.Project;
import play.Logger.ALogger;
import utils.AbstractTests;

/**
 * Test {@link ProjectsAPI} methods
 * 
 * @author ajosso
 *
 */
@Singleton  
public class ProjectsAPITest extends AbstractTests implements AbstractAPITests {

	private static final play.Logger.ALogger logger = play.Logger.of(ProjectsAPITest.class);

	private static ProjectsAPI api;

	private static boolean clean = true;

	private static final String USER = "ngsrg";

	private static Project refProject;

	private Project data;


	@BeforeClass
	public static void setUpClass() {
		assertTrue(app.isDev());
		api = app.injector().instanceOf(ProjectsAPI.class);
		assertNotNull(api);
		refProject = TestProjectFactory.project(USER);
	}

	@Override
	@Before
	public void setUpData() {
		if(clean) {
			try {
				data = api.create(refProject, USER);
			} catch (APIValidationException e) {
				logger.error(e.getMessage());
				logger.error("invalid fields: " + e.getErrors().keySet().toString());
				logValidationErrors(e);
				exit(e.getMessage());
			} catch (Exception e) {
				logger.error(e.getMessage());
				exit(e.getMessage());
			}
		} else {
			deleteData();
			setUpData();
		}
	}

	@Override
	@After
	public void deleteData() {
		try {
			api.delete(data.code);
		} catch (Exception e) {
			logger.error(e.getMessage());
			if(logger.isDebugEnabled()) {
				e.printStackTrace();
			}
		} finally {
			clean = true;
		}

	}

	@Test
	public void createTest() {
		assertNotNull(data);
		logger.debug("Project ID: " + data._id);
		assertEquals(refProject.code, data.code);
		assertEquals(refProject.name, data.name);
		assertEquals(refProject.typeCode, data.typeCode);
		assertEquals(refProject.categoryCode, data.categoryCode);
		assertEquals(refProject.description, data.description);
		assertEquals(refProject.umbrellaProjectCode, data.umbrellaProjectCode);
		assertEquals(refProject.lastSampleCode, data.lastSampleCode);
		assertEquals(refProject.nbCharactersInSampleCode, data.nbCharactersInSampleCode);
		assertEquals(refProject.archive, data.archive);
		assertEquals(refProject.state.code, data.state.code);
		assertEquals(refProject.state.user, data.state.user);
		assertEquals(refProject.authorizedUsers, data.authorizedUsers);
		assertEquals(refProject.comments.size(), data.comments.size());
	}

	@Test
	public void deleteTest() {
		try {
			api.delete(refProject.code);
			Project proj = api.get(refProject.getCode());
			assertNull(proj);
		} catch (Exception e) {
			logger.error(e.getMessage());
			exit(e.getMessage());
		}
	}

	@Test
	public void getTest() {
		try {
			Project proj = api.get(refProject.code);
			assertNotNull(proj);
			assertEquals(data.get_id(), proj.get_id());
			assertEquals(refProject.getCode(), proj.getCode());
		} catch (Exception e) {
			logger.error(e.getMessage());
			exit(e.getMessage());
		}
	}

	@Test
	public void listTest() {
		try {
			//---------- default mode ----------
			logger.debug("default mode");
			ListFormWrapper<Project> wrapper = TestProjectFactory.wrapper(refProject.code);
			Iterable<Project> samples = api.listObjects(wrapper);
			Iterator<Project> iter = samples.iterator();
			int count = 0;
			while(iter.hasNext()) {
				Project p = iter.next();
				Assert.assertEquals(refProject.code, p.code);
				Assert.assertEquals(refProject.categoryCode, p.categoryCode);
				count++;
			}
			Assert.assertEquals(1, count);
			
			//---------- reporting mode----------
			logger.debug("reporting mode");
			wrapper = TestProjectFactory.wrapper(refProject.code, QueryMode.REPORTING, null);
			samples = api.listObjects(wrapper);
			iter = samples.iterator();
			count = 0;
			while(iter.hasNext()) {
				Project p = iter.next();
				Assert.assertEquals(refProject.code, p.code);
				Assert.assertEquals(refProject.categoryCode, p.categoryCode);
				count++;
			}
			Assert.assertEquals(1, count);				
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getCause().getMessage());
			exit(e.getMessage());
		}
	}

	@Test
	public void updateTest() {
		try {
			Project updatedProj = TestProjectFactory.projectArchived(USER);
			updatedProj._id = data._id;
			updatedProj.traceInformation.modifyUser = USER;
			updatedProj.traceInformation.modifyDate = new Date();

			api.update(updatedProj, USER);
			Project proj = api.get(TestProjectFactory.projectArchived(USER).code);
			assertNotEquals(refProject.archive, proj.archive);
			assertEquals(TestProjectFactory.projectArchived(USER).archive, proj.archive);
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
	public ALogger logger() {
		return logger;
	}
}
