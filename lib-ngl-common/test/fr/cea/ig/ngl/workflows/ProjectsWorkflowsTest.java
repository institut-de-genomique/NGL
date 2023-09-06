package fr.cea.ig.ngl.workflows;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import fr.cea.ig.ngl.NGLConfig;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.projects.ProjectsAPI;
import fr.cea.ig.ngl.dao.projects.ProjectsDAO;
import fr.cea.ig.ngl.dao.api.factory.ProjectFactory;
import fr.cea.ig.ngl.utils.TestUtils;
import models.laboratory.common.description.dao.StateDAO;
import models.laboratory.common.instance.State;
import models.laboratory.project.instance.Project;
import play.Logger;
import workflows.project.ProjectWorkflowHelper;
import workflows.project.ProjectWorkflows;

/**
 * Test du workflow de l'entité PROJECT.
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ models.laboratory.common.description.State.class })
public class ProjectsWorkflowsTest {

	private final StateDAO stateDAO = Mockito.mock(StateDAO.class);

    private final ProjectsDAO projectDAO = Mockito.mock(ProjectsDAO.class);

	private final ProjectWorkflowHelper projectWFHelper = Mockito.mock(ProjectWorkflowHelper.class);

	private final NGLConfig nglConfig = Mockito.mock(NGLConfig.class);

	private final ProjectWorkflows projectWF = new ProjectWorkflows(projectWFHelper);

    private final ProjectsAPI projectAPI = new ProjectsAPI(projectDAO, projectWF, nglConfig);
	
	/**
	 * Si on veut faire des traitements avant l'exécution de la suite de tests. 
	 * Par exemple : initialiser un mock, initialiser une méthode statique, ...
	 */
    @Before
	public void setUp() {
        PowerMockito.mockStatic(models.laboratory.common.description.State.class);

		Supplier<StateDAO> supp = () -> PowerMockito.mock(StateDAO.class);
		Field field = PowerMockito.field(models.laboratory.common.description.State.class, "find");

		try {
			field.set(models.laboratory.common.description.State.class, supp);
		} catch (IllegalArgumentException e) {
			Logger.error("Exception occured during testValidateProjectCategoryCodeRequiredValid()");
			fail(e.getMessage());
		} catch (IllegalAccessException e) {
			Logger.error("Exception occured during testValidateProjectCategoryCodeRequiredValid()");
			fail(e.getMessage());
		}
    }

    @Test
	public void testUpdateStateInvalidWrongState() {
		Project project = ProjectFactory.getRandomProject(true);

		State state = new State();
		state.code = "N";
		state.date = new Date();

		when(projectDAO.findByCode(project.code)).thenReturn(project);
		when(stateDAO.isCodeExistForTypeCode("N", "typeCodeRandom")).thenReturn(false);

		Project projectUpdated = null;
		boolean exceptFired = false;

		try {
			projectUpdated = this.projectAPI.updateState(project.code, state, TestUtils.CURRENT_USER);
		} catch (APIException e) {
			exceptFired = true;
        }

		if (!exceptFired) {
			fail("Invalid state, should have been refused.");
		}

		assertNull(projectUpdated);
	}
}
