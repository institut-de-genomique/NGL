package fr.cea.ig.ngl.dao.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.common.collect.Iterators;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.projects.ProjectsAPI;
import fr.cea.ig.ngl.dao.projects.ProjectsDAO;
import fr.cea.ig.ngl.dao.api.factory.ProjectFactory;
import fr.cea.ig.ngl.utils.TestUtils;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.project.instance.BioinformaticParameters;
import models.laboratory.project.instance.Project;
import play.Logger;
import play.modules.mongojack.MongoDBPlugin;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.project.instance.ProjectValidationHelper;
import workflows.project.ProjectWorkflowHelper;
import workflows.project.ProjectWorkflows;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Test de l'API de l'entité PROJECT.
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ CommonValidationHelper.class, ProjectValidationHelper.class, MongoDBPlugin.class, MongoDBDAO.class })
public class ProjectsAPITest {

	private final ProjectsDAO projectDAO = Mockito.mock(ProjectsDAO.class);

	private final ProjectWorkflowHelper projectWFHelper = Mockito.mock(ProjectWorkflowHelper.class);

	private final ProjectWorkflows projectWF = new ProjectWorkflows(projectWFHelper);

	private final ProjectsAPI projectAPI = new ProjectsAPI(projectDAO, projectWF);

	/**
	 * Si on veut faire des traitements avant l'exécution de la suite de tests. 
	 * Par exemple : initialiser un mock, initialiser une méthode statique, ...
	 */
	@Before
	public void setUp() {
		PowerMockito.mockStatic(MongoDBPlugin.class);
		PowerMockito.mockStatic(MongoDBDAO.class);
		PowerMockito.mockStatic(CommonValidationHelper.class);
		PowerMockito.mockStatic(ProjectValidationHelper.class);

		try {
			PowerMockito.doNothing().when(CommonValidationHelper.class, "validateIdPrimary",
					any(ContextValidation.class), any(Project.class));
			PowerMockito.doNothing().when(CommonValidationHelper.class, "validateCodePrimary",
					any(ContextValidation.class), any(Project.class), any(String.class));
			PowerMockito.doNothing().when(CommonValidationHelper.class, "validateTraceInformationRequired",
					any(ContextValidation.class), any(TraceInformation.class));

			PowerMockito.doNothing().when(ProjectValidationHelper.class, "validateProjectTypeCodeRequired",
					any(ContextValidation.class), any(String.class), any(Map.class));
			PowerMockito.doNothing().when(ProjectValidationHelper.class, "validateProjectCategoryCodeRequired",
					any(ContextValidation.class), any(String.class));
			PowerMockito.doNothing().when(ProjectValidationHelper.class, "validateUmbrellaProjectCodeOptional",
					any(ContextValidation.class), any(String.class));
			PowerMockito.doNothing().when(ProjectValidationHelper.class, "validateBioformaticParametersRequired",
					any(ContextValidation.class), any(BioinformaticParameters.class));
			PowerMockito.doNothing().when(ProjectValidationHelper.class, "validateArchiveProperties",
					any(ContextValidation.class), any(String.class), any(String.class), any(Map.class),
					any(Boolean.class));
		} catch (Exception e) {
			Logger.error("Exception occured during setUp()");
			fail(e.getMessage());
		}
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

		State state = new State();
		state.code = "N";
		state.date = new Date();

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

		State state = new State();
		state.code = "N";
		state.date = new Date();

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
	public void testUpdateValid() {
		Project project = mock(Project.class);
		ContextValidation ctxVal = ContextValidation.createUpdateContext(TestUtils.CURRENT_USER);
		
		when(projectDAO.findByCode(project.code)).thenReturn(project);
		
		project.description = "testUpdate";
		
		when(projectDAO.save(project)).thenReturn(project);
		doNothing().when(project).validate(ctxVal);
		
		Project projectUpdateAPI = null;
		
		try {
			projectUpdateAPI = projectAPI.update(project, TestUtils.CURRENT_USER);
		} catch (APIException e) {
			Logger.error("Exception occured during testUpdateValid()");
			fail(e.getMessage());
		}
		
		assertNotNull(projectUpdateAPI);
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
