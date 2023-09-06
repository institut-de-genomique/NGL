package fr.cea.ig.ngl.dao.api;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;

import javax.mail.*;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.common.collect.Iterators;
import com.typesafe.config.*;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.NGLConfig;
import fr.cea.ig.ngl.dao.api.factory.ProjectFactory;
import fr.cea.ig.ngl.dao.api.factory.PropertyDefinitionFactory;
import fr.cea.ig.ngl.dao.api.factory.StateFactory;
import fr.cea.ig.ngl.dao.projects.*;
import fr.cea.ig.ngl.utils.TestUtils;
import fr.cea.ig.play.IGGlobals;
import mail.MailServiceException;
import mail.MailServices;
import models.laboratory.common.instance.State;
import models.laboratory.project.description.ProjectType;
import models.laboratory.project.instance.Project;
import ngl.refactoring.MiniDAO;
import play.Logger;
import play.modules.mongojack.MongoDBPlugin;
import validation.common.instance.CommonValidationHelper;
import validation.project.instance.ProjectValidationHelper;
import workflows.project.*;

/**
 * Test de l'API de l'entité PROJECT.
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ CommonValidationHelper.class, ProjectValidationHelper.class, MongoDBPlugin.class, MongoDBDAO.class, ProjectType.class, ConfigFactory.class, IGGlobals.class, Transport.class })
public class ProjectsAPITest {

	private final Config config = mock(Config.class);

	private final ProjectType projectType = mock(ProjectType.class);

	private final MailServices mailServices = mock(MailServices.class);

	private final MiniDAO<ProjectType> miniDao = PowerMockito.mock(MiniDAO.class);

	private final ProjectsDAO projectDAO = Mockito.mock(ProjectsDAO.class);

	private final ProjectWorkflowHelper projectWFHelper = Mockito.mock(ProjectWorkflowHelper.class);

	private final NGLConfig nglConfig = Mockito.mock(NGLConfig.class);

	private final ProjectWorkflows projectWF = new ProjectWorkflows(projectWFHelper);

	private final ProjectsAPI projectAPI = new ProjectsAPI(projectDAO, projectWF, nglConfig);

	/**
	 * Si on veut faire des traitements avant l'exécution de la suite de tests. 
	 * Par exemple : initialiser un mock, initialiser une méthode statique, ...
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	@Before
	public void setUp() throws IllegalArgumentException, IllegalAccessException {
		PowerMockito.mockStatic(MongoDBPlugin.class);
		PowerMockito.mockStatic(MongoDBDAO.class);
		PowerMockito.mockStatic(CommonValidationHelper.class);
		PowerMockito.mockStatic(ProjectValidationHelper.class);
		PowerMockito.mockStatic(ProjectType.class);
		PowerMockito.mockStatic(ConfigFactory.class);
        PowerMockito.mockStatic(IGGlobals.class);
		PowerMockito.mockStatic(Transport.class);

		Supplier<MiniDAO<ProjectType>> supp = () -> miniDao;

		Field field = PowerMockito.field(ProjectType.class, "miniFind");
		field.set(ProjectType.class, supp);

		projectAPI.setMailServices(mailServices);
	}

	@Test
	public void testFindAll() {
		List<Project> projectList = ProjectFactory.getRandomProjectsList();

		when(projectDAO.all()).thenReturn(projectList);

		Iterable<Project> projectListAPI = null;

		try {
			projectListAPI = projectAPI.all();
		} catch (APIException e) {
			Logger.error("Exception occured during testFindAll()");
			fail(e.getMessage());
		}

		assertNotNull(projectListAPI);
		assertTrue("Different size on testFindAll()",
				Iterators.size(projectListAPI.iterator()) == Iterators.size(projectList.iterator()));
	}

	@Test
	public void testGetByCode() {
		String randomCode = UUID.randomUUID().toString();

		Project project = ProjectFactory.getRandomProject(true);
		project.code = randomCode;

		when(projectDAO.findByCode(randomCode)).thenReturn(project);

		Project projectGetAPI = projectAPI.get(randomCode);

		assertNotNull(projectGetAPI);
		assertTrue("Different code on testGetByCode()", randomCode.equals(projectGetAPI.code));
	}

	@Test
	public void testCreateValid() throws Exception {
		String randomCode = UUID.randomUUID().toString();

		Project project = ProjectFactory.getRandomProject(false);
		project.code = randomCode;

		when(projectDAO.saveObject(project)).thenReturn(project);

		try {
			Project sampCreateAPI = projectAPI.create(project, TestUtils.CURRENT_USER);

			assertTrue("Different code on testCreateValid()", sampCreateAPI.code.equals(project.code));
			assertNotNull(sampCreateAPI);
		} catch (APIException e) {
			Logger.error("Exception occured during testCreateValid()");
			fail(e.getMessage());
		}
	}

	@Test
	public void testCreateInvalidIdExists() {
		String randomCode = UUID.randomUUID().toString();

		Project project = mock(Project.class);
		project._id = "1234";
		project.code = randomCode;

		when(projectDAO.saveObject(project)).thenReturn(project);

		boolean exceptFired = false;

		Project projectCreateAPI = null;

		try {
			projectCreateAPI = projectAPI.create(project, TestUtils.CURRENT_USER);
		} catch (APIException e) {
			exceptFired = true;
		}

		if (!exceptFired) {
			fail("Invalid project, should have been refused.");
		}

		assertNull(projectCreateAPI);
	}
	
	@Test
	public void testDeleteValid() {
		Project project = ProjectFactory.getRandomProject(true);
		
		when(projectDAO.checkObjectExistByCode(project.code)).thenReturn(true);
		
		try {
			projectAPI.delete(project.code);
		} catch (APIException e) {
			Logger.error("Exception occured during testDeleteValid()");
			fail(e.getMessage());
		}
		
		when(projectDAO.findByCode(project.code)).thenReturn(null);
		
		Project projectFindAPI = projectAPI.get(project.code);
		
		assertNull("Project still exists on testDelete()", projectFindAPI);
	}
	
	@Test
	public void testDeleteInvalid() {
		Project project = ProjectFactory.getRandomProject(true);
		
		when(projectDAO.checkObjectExistByCode(project.code)).thenReturn(false);
		when(projectDAO.getElementClass()).thenReturn(Project.class);
		
		doReturn(Project.class).when(projectDAO).getElementClass();
		
		boolean exceptFired = false;
		
		try {
			projectAPI.delete(project.code);
		} catch (APIException e) {
			exceptFired = true;
		}
		
		if (!exceptFired) {
			fail("Invalid project, should have been refused.");
		}
	}

	@Test
	public void testUpdateStateValid() {
		Project project = ProjectFactory.getRandomProject(true);

		State state = StateFactory.getNewState();

		when(projectDAO.findByCode(project.code)).thenReturn(project);

		try {
			this.projectAPI.updateState(project.code, state, TestUtils.CURRENT_USER);
		} catch (APIException e) {
			Logger.error("Exception occured during testUpdateStateValid()");
			fail(e.getMessage());
		}
	}

	@Test
	public void testUpdateStateInvalidIdNotExists() {
		Project project = ProjectFactory.getRandomProject(true);

		State state = StateFactory.getNewState();

		when(projectDAO.findByCode(project.code)).thenReturn(null);

		boolean exceptFired = false;
		Project projectUpdated = null;

		try {
			projectUpdated = this.projectAPI.updateState(project.code, state, TestUtils.CURRENT_USER);
		} catch (APIException e) {
			exceptFired = true;
		}

		if (!exceptFired) {
			fail("Invalid valuation. Should have been refused.");
		}

		assertNull(projectUpdated);
	}

	@Test
	public void testPartialUpdateValid() throws Exception {
		Project project = mock(Project.class);
		project = ProjectFactory.fillRandomProject(project, true);

		List<String> fields = new ArrayList<>();
		fields.add("name");

		when(projectDAO.findByCode(project.code)).thenReturn(project);
		
		project.name = "newName";
		
		when(projectDAO.save(project)).thenReturn(project);
		
		Project projectUpdateAPI = null;
		
		try {
			projectUpdateAPI = projectAPI.update(project, TestUtils.CURRENT_USER, fields);
		} catch (APIException e) {
			Logger.error("Exception occured during testUpdateValid()");
			fail(e.getMessage());
		}
		
		assertNotNull(projectUpdateAPI);
		assertEquals("Name was not updated correctly", "newName", project.name);
	}

	@Test
	public void testPartialUpdateInvalidNotExist() {
		Project project = mock(Project.class);
		project = ProjectFactory.fillRandomProject(project, true);

		List<String> fields = new ArrayList<>();
		fields.add("name");

		when(projectDAO.findByCode(project.code)).thenReturn(null);
		when(projectDAO.getElementClass()).thenReturn(Project.class);
		
		boolean exceptFired = false;

		Project projectUpdateAPI = null;
		
		try {
			projectUpdateAPI = projectAPI.update(project, TestUtils.CURRENT_USER, fields);
		} catch (APIException e) {
			exceptFired = true;
		}

		if (!exceptFired) {
			fail("Invalid project, should have been refused.");
		}
		
		assertNull(projectUpdateAPI);
	}

	@Test
	public void testPartialUpdateInvalidFields() {
		Project project = mock(Project.class);
		project = ProjectFactory.fillRandomProject(project, true);

		List<String> fields = new ArrayList<>();
		fields.add("invalidFields");

		when(projectDAO.findByCode(project.code)).thenReturn(project);
		when(projectDAO.getElementClass()).thenReturn(Project.class);
		
		boolean exceptFired = false;

		Project projectUpdateAPI = null;
		
		try {
			projectUpdateAPI = projectAPI.update(project, TestUtils.CURRENT_USER, fields);
		} catch (APIException e) {
			exceptFired = true;
		}

		if (!exceptFired) {
			fail("Invalid project, should have been refused.");
		}
		
		assertNull(projectUpdateAPI);
	}

	@Test
	public void testUpdateValidCNG() throws MailServiceException {
		Project project = mock(Project.class);
		project = ProjectFactory.fillRandomProject(project, false);

		when(projectDAO.findByCode(project.code)).thenReturn(project);
		
		project.description = "testUpdate";
		
		when(projectDAO.save(project)).thenReturn(project);
		when(nglConfig.getInstitute()).thenReturn("CNG");
		
		Project projectUpdateAPI = null;
		
		try {
			projectUpdateAPI = projectAPI.update(project, TestUtils.CURRENT_USER);
		} catch (APIException e) {
			Logger.error("Exception occured during testUpdateValid()");
			fail(e.getMessage());
		}
		
		assertNotNull(projectUpdateAPI);

		verify(mailServices, times(0)).sendMail(anyString(), anySet(), anyString(), anyString());
	} 

	@Test
	public void testUpdateValidCNSNoMail() throws MailServiceException {
		Project project = mock(Project.class);
		project = ProjectFactory.fillRandomProject(project, true);
		
		when(projectDAO.findByCode(project.code)).thenReturn(project);
		
		project.description = "testUpdate";
		
		when(projectDAO.save(project)).thenReturn(project);
		when(nglConfig.getInstitute()).thenReturn("CNS");
		
		Project projectUpdateAPI = null;
		
		try {
			projectUpdateAPI = projectAPI.update(project, TestUtils.CURRENT_USER);
		} catch (APIException e) {
			Logger.error("Exception occured during testUpdateValid()");
			fail(e.getMessage());
		}
		
		assertNotNull(projectUpdateAPI);

		verify(mailServices, times(0)).sendMail(anyString(), anySet(), anyString(), anyString());
	}

	@Test
	public void testUpdateValidCNSMail() throws MailServiceException {
		Project project = mock(Project.class);
		project = ProjectFactory.fillRandomProject(project, true);
		
		when(projectDAO.findByCode(project.code)).thenReturn(project);
		
		Project project2 = mock(Project.class);
		project2 = ProjectFactory.fillRandomProject(project, true, true);

		project2._id = project._id;
		project2.code = project.code;
		project2.description = "testUpdate";
		project2.bioinformaticParameters.biologicalAnalysis = Boolean.TRUE;
		
		when(projectDAO.save(project)).thenReturn(project);
		when(nglConfig.getInstitute()).thenReturn("CNS");

		when(ConfigFactory.load()).thenReturn(config);
        when(IGGlobals.configuration()).thenReturn(config);

		when(config.getString("project.email.from")).thenReturn(TestUtils.MAIL_TEST);
		when(config.getString("project.email.joe")).thenReturn(TestUtils.MAIL_TEST);
		when(config.getString("mail.smtp.host")).thenReturn(TestUtils.SMTP_TEST);

		projectType.propertiesDefinitions = PropertyDefinitionFactory.getPropertyDefinitionListWithAnalysisTypes();

		when(miniDao.findByCode(anyString())).thenReturn(projectType);
		when(projectType.getValueFromPropertyDefinitionByCode(anyString(), anyString())).thenReturn(PropertyDefinitionFactory.getValue());

		try {
            PowerMockito.doNothing().when(Transport.class, "send", any(Message.class));
        } catch (Exception e) {
            Logger.error("Exception occured during testBuildAndSendMail()");
			fail(e.getMessage());
        }

		Project projectUpdateAPI = null;

		try {
			projectUpdateAPI = projectAPI.update(project2, TestUtils.CURRENT_USER);
		} catch (APIException e) {
			Logger.error("Exception occured during testUpdateValid()");
			fail(e.getMessage());
		}
		
		assertNotNull(projectUpdateAPI);
		
        verify(mailServices, times(1)).sendMail(anyString(), anySet(), anyString(), anyString());
	}
	
	@Test
	public void testUpdateInvalidNotExist() {
		Project project = mock(Project.class);
		
		when(projectDAO.findByCode(project.code)).thenReturn(null);
		
		Project projectUpdateAPI = null;
		boolean exceptFired = false;
		
		try {
			projectUpdateAPI = projectAPI.update(project, TestUtils.CURRENT_USER);
		} catch (APIException e) {
			exceptFired = true;
		}
		
		if (!exceptFired) {
			fail("Invalid project, should have been refused.");
		}
		
		assertNull(projectUpdateAPI);
	}
}
