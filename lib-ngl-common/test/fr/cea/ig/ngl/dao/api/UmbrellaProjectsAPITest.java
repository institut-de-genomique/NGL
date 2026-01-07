package fr.cea.ig.ngl.dao.api;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.*;

import javax.mail.Transport;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.common.collect.Iterators;
import com.typesafe.config.ConfigFactory;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.dao.api.factory.UmbrellaProjectFactory;
import fr.cea.ig.ngl.dao.projects.*;
import fr.cea.ig.ngl.utils.TestUtils;
import fr.cea.ig.play.IGGlobals;
import models.laboratory.project.description.ProjectType;
import models.laboratory.project.instance.UmbrellaProject;
import models.utils.dao.DAOException;
import play.Logger;
import play.modules.mongojack.MongoDBPlugin;
import validation.common.instance.CommonValidationHelper;
import validation.project.instance.UmbrellaProjectValidationHelper;

/**
 * Test de l'API de l'entité UMBRELLAPROJECT.
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ CommonValidationHelper.class, UmbrellaProjectValidationHelper.class, MongoDBPlugin.class, MongoDBDAO.class, ProjectType.class, ConfigFactory.class, IGGlobals.class, Transport.class })
public class UmbrellaProjectsAPITest {

    private final UmbrellaProjectsDAO umbrellaProjectDAO = Mockito.mock(UmbrellaProjectsDAO.class);

    private final UmbrellaProjectsAPI umbrellaProjectAPI = new UmbrellaProjectsAPI(umbrellaProjectDAO);

    @Before
	public void setUp() throws IllegalArgumentException, IllegalAccessException {
        PowerMockito.mockStatic(MongoDBPlugin.class);
		PowerMockito.mockStatic(MongoDBDAO.class);
		PowerMockito.mockStatic(CommonValidationHelper.class);
		PowerMockito.mockStatic(UmbrellaProjectValidationHelper.class);
		PowerMockito.mockStatic(ProjectType.class);
		PowerMockito.mockStatic(ConfigFactory.class);
        PowerMockito.mockStatic(IGGlobals.class);
		PowerMockito.mockStatic(Transport.class);
    }
	 
    @Test
	public void testFindAll() {
        List<UmbrellaProject> umbrellaProjectList = UmbrellaProjectFactory.getRandomUmbrellaProjectsList();

		when(umbrellaProjectDAO.all()).thenReturn(umbrellaProjectList);

		Iterable<UmbrellaProject> umbrellaProjectListAPI = null;

		try {
			umbrellaProjectListAPI = umbrellaProjectDAO.all();
		} catch (DAOException e) {
			Logger.error("Exception occured during testFindAll()");
			fail(e.getMessage());
		}

		assertNotNull(umbrellaProjectListAPI);
		assertTrue("Different size on testFindAll()",
				Iterators.size(umbrellaProjectListAPI.iterator()) == Iterators.size(umbrellaProjectList.iterator()));
    }

    @Test
	public void testGetByCode() {
        String randomCode = UUID.randomUUID().toString();

		UmbrellaProject umbrellaProject = UmbrellaProjectFactory.getRandomUmbrellaProject(true);
		umbrellaProject.code = randomCode;

		when(umbrellaProjectDAO.findByCode(randomCode)).thenReturn(umbrellaProject);

		UmbrellaProject umbrellaProjectGetAPI = umbrellaProjectAPI.get(randomCode);

		assertNotNull(umbrellaProjectAPI);
		assertTrue("Different code on testGetByCode()", randomCode.equals(umbrellaProjectGetAPI.code));
    }

    @Test
	public void testCreateValid() throws Exception {
        String randomCode = UUID.randomUUID().toString();

		UmbrellaProject umbrellaProject = UmbrellaProjectFactory.getRandomUmbrellaProject(false);
		umbrellaProject.code = randomCode;

		when(umbrellaProjectDAO.saveObject(umbrellaProject)).thenReturn(umbrellaProject);

		try {
			UmbrellaProject umbrellaProjectCrea = umbrellaProjectAPI.create(umbrellaProject, TestUtils.CURRENT_USER);

            assertNotNull(umbrellaProjectCrea);
			assertTrue("Different code on testCreateValid()", umbrellaProjectCrea.code.equals(umbrellaProject.code));
		} catch (APIException e) {
			Logger.error("Exception occured during testCreateValid()");
			fail(e.getMessage());
		}
    }

    @Test
	public void testCreateInvalidIdExists() {
        String randomCode = UUID.randomUUID().toString();

		UmbrellaProject umbrellaProject = mock(UmbrellaProject.class);
		umbrellaProject._id = "1234";
		umbrellaProject.code = randomCode;

		when(umbrellaProjectDAO.saveObject(umbrellaProject)).thenReturn(umbrellaProject);

		boolean exceptFired = false;

		UmbrellaProject umbProjCrea = null;

		try {
			umbProjCrea = umbrellaProjectAPI.create(umbrellaProject, TestUtils.CURRENT_USER);
		} catch (APIException e) {
			exceptFired = true;
		}

		if (!exceptFired) {
			fail("Invalid project, should have been refused.");
		}

		assertNull(umbProjCrea);
    }
}