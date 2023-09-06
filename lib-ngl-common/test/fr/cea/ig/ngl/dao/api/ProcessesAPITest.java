package fr.cea.ig.ngl.dao.api;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;

import play.Logger;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.processes.instance.ProcessValidationHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import fr.cea.ig.ngl.dao.containers.ContainersDAO;
import fr.cea.ig.ngl.dao.processes.ProcessesAPI;
import fr.cea.ig.ngl.dao.processes.ProcessesDAO;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.ngl.dao.api.factory.ContainerFactory;
import fr.cea.ig.ngl.dao.api.factory.ProcessFactory;
import fr.cea.ig.ngl.dao.api.factory.SampleFactory;
import fr.cea.ig.ngl.dao.api.factory.StateFactory;
import fr.cea.ig.ngl.utils.TestUtils;
import models.laboratory.container.instance.Container;
import models.laboratory.processes.description.ProcessType;
import models.laboratory.processes.instance.Process;
import models.utils.instance.ProcessHelper;
import ngl.refactoring.state.ContainerStateNames;
import workflows.process.ProcWorkflows;

/**
 * Test de l'API de l'entité PROCESSES.
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ ProcessHelper.class, ContextValidation.class, ProcessValidationHelper.class,
		CommonValidationHelper.class, ProcessType.class, MongoDBDAO.class })
public class ProcessesAPITest {

	private final ProcessesDAO processesDAO = Mockito.mock(ProcessesDAO.class);

	private final ContainersDAO containerDAO = Mockito.mock(ContainersDAO.class);

	private final ProcWorkflows proWworkflows = Mockito.mock(ProcWorkflows.class);

	private final ProcessesAPI processesAPI = new ProcessesAPI(processesDAO, containerDAO, proWworkflows);

	/**
	 * Si on veut faire des traitements avant l'exécution de la suite de tests. 
	 * Par exemple : initialiser un mock, initialiser une méthode statique, ...
	 */
	@Before
	public void setUp() {
		PowerMockito.mockStatic(ProcessHelper.class);
		PowerMockito.mockStatic(ContextValidation.class);
		PowerMockito.mockStatic(ProcessValidationHelper.class);
		PowerMockito.mockStatic(CommonValidationHelper.class);
		PowerMockito.mockStatic(ProcessType.class);
		PowerMockito.mockStatic(MongoDBDAO.class);
	}

	@Test
	@SuppressWarnings({"unchecked"})
	public void testCreateProcessesFromSampleValid() {
		Process process = ProcessFactory.getRandomProcess(false, false, false);

		BiFunction<ContextValidation, Process, List<Process>> biFunction = mock(BiFunction.class);
		List<Process> processListMock = ProcessFactory.getRandomProcessList();

		when(biFunction.apply(any(ContextValidation.class), any(Process.class))).thenReturn(processListMock);

		List<Process> processList = null;

		try {
			processList = this.processesAPI.createProcessesFromSample(process, TestUtils.CURRENT_USER);
		} catch (APIValidationException e) {
			Logger.error("Exception occured during testCreateProcessesFromSampleValid()");
			fail(e.getMessage());
		} catch (APISemanticException e) {
			Logger.error("Exception occured during testCreateProcessesFromSampleValid()");
			fail(e.getMessage());
		}

		assertNotNull(processList);
	}

	@Test
	public void testCreateProcessesFromSampleInvalid() {
		Process process = ProcessFactory.getRandomProcess(true, false, false);

		List<Process> processList = null;
		boolean exceptFired = false;

		try {
			processList = this.processesAPI.createProcessesFromSample(process, TestUtils.CURRENT_USER);
		} catch (APIValidationException e) {
			exceptFired = true;
		} catch (APISemanticException e) {
			exceptFired = true;
		}

		if (!exceptFired) {
			fail("Invalid process, should have been refused.");
		}

		assertNull(processList);
	}

	@Test
	@SuppressWarnings({"unchecked"})
	public void testCreateProcessesFromContainerValid() {
		Process process = ProcessFactory.getRandomProcess(false, false, false);

		BiFunction<ContextValidation, Process, List<Process>> biFunction = mock(BiFunction.class);
		List<Process> processListMock = ProcessFactory.getRandomProcessList();

		when(biFunction.apply(any(ContextValidation.class), any(Process.class))).thenReturn(processListMock);

		List<Process> processList = null;

		try {
			processList = this.processesAPI.createProcessesFromContainer(process, TestUtils.CURRENT_USER);
		} catch (APIValidationException e) {
			Logger.error("Exception occured during testCreateProcessesFromContainerValid()");
			fail(e.getMessage());
		} catch (APISemanticException e) {
			Logger.error("Exception occured during testCreateProcessesFromContainerValid()");
			fail(e.getMessage());
		}

		assertNotNull(processList);
	}

	@Test
	public void testCreateProcessesFromContainerInvalid() {
		Process process = ProcessFactory.getRandomProcess(true, false, false);

		List<Process> processList = null;
		boolean exceptFired = false;

		try {
			processList = this.processesAPI.createProcessesFromContainer(process, TestUtils.CURRENT_USER);
		} catch (APIValidationException e) {
			exceptFired = true;
		} catch (APISemanticException e) {
			exceptFired = true;
		}

		if (!exceptFired) {
			fail("Invalid process, should have been refused.");
		}

		assertNull(processList);
	}

	@Test
	@SuppressWarnings({"unchecked"})
	public void testCreateProcessesValid() {
		Process process = ProcessFactory.getRandomProcess(false, false, false);

		BiFunction<ContextValidation, Process, List<Process>> biFunction = mock(BiFunction.class);
		List<Process> processListMock = ProcessFactory.getRandomProcessList();

		when(biFunction.apply(any(ContextValidation.class), any(Process.class))).thenReturn(processListMock);

		List<Process> processList = null;

		try {
			processList = this.processesAPI.createProcesses(process, TestUtils.CURRENT_USER, biFunction);
		} catch (APIValidationException e) {
			Logger.error("Exception occured during testCreateProcessesValid()");
			fail(e.getMessage());
		} catch (APISemanticException e) {
			Logger.error("Exception occured during testCreateProcessesValid()");
			fail(e.getMessage());
		}

		assertNotNull(processList);
	}
	
	@Test
	@SuppressWarnings({"unchecked"})
	public void testCreateProcessesInvalidIdExists() {
		Process process = ProcessFactory.getRandomProcess(true, false, false);

		BiFunction<ContextValidation, Process, List<Process>> biFunction = mock(BiFunction.class);
		List<Process> processListMock = ProcessFactory.getRandomProcessList();

		when(biFunction.apply(any(ContextValidation.class), any(Process.class))).thenReturn(processListMock);

		List<Process> processList = null;
		boolean exceptFired = false;

		try {
			processList = this.processesAPI.createProcesses(process, TestUtils.CURRENT_USER, biFunction);
		} catch (APIValidationException e) {
			exceptFired = true;
		} catch (APISemanticException e) {
			exceptFired = true;
		}

		if (!exceptFired) {
			fail("Invalid process, should have been refused.");
		}

		assertNull(processList);
	}
	
	@Test
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void testDeleteValid() {
		Process process = ProcessFactory.getRandomProcess(true, false, false);
		
		when(processesDAO.findByCode(process.code)).thenReturn(process);
		when(processesDAO.checkObjectExistByCode(process.code)).thenReturn(true);

		Container container = ContainerFactory.getRandomContainer(ContainerStateNames.IW_P);
		container.code = UUID.randomUUID().toString();

		when(containerDAO.findByCode(process.inputContainerCode)).thenReturn(container);

		MongoDBResult dbRes = mock(MongoDBResult.class);

		when(MongoDBDAO.find(any(), any(), any())).thenReturn(dbRes);
		when(dbRes.toList()).thenReturn(SampleFactory.getRandomSampleList());

		try {
			processesAPI.delete(process.code, TestUtils.CURRENT_USER);
		} catch (APIException e) {
			Logger.error("Exception occured during testDeleteValid()");
			fail(e.getMessage());
		}
		
		when(processesDAO.findByCode(process.code)).thenReturn(null);
		
		Process processFindAPI = processesAPI.get(process.code);
		
		assertNull("Process still exists on testDelete()", processFindAPI);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Test
	public void testDeleteInvalidInProgressProcess() {
		Process process = ProcessFactory.getRandomProcess(true, false, false);
		process.state = StateFactory.getInProgressState();

		when(processesDAO.findByCode(process.code)).thenReturn(process);
		when(processesDAO.checkObjectExistByCode(process.code)).thenReturn(true);

		Container container = ContainerFactory.getRandomContainer(ContainerStateNames.IW_P);
		container.code = UUID.randomUUID().toString();

		when(containerDAO.findByCode(process.inputContainerCode)).thenReturn(container);

		MongoDBResult dbRes = mock(MongoDBResult.class);

		when(MongoDBDAO.find(any(), any(), any())).thenReturn(dbRes);
		when(dbRes.toList()).thenReturn(SampleFactory.getRandomSampleList());

		boolean exceptFired = false;

		try {
			processesAPI.delete(process.code, TestUtils.CURRENT_USER);
		} catch (APIException e) {
			exceptFired = true;
		}

		if (!exceptFired) {
			fail("Invalid process, should have been refused.");
		}
	}
	
	@Test
	public void testDeleteInvalidCodeNull() {
		Process process = ProcessFactory.getRandomProcess(true, false, false);
		
		when(processesDAO.checkObjectExistByCode(process.code)).thenReturn(false);
		when(processesDAO.findByCode(process.code)).thenReturn(null);
		
		boolean exceptFired = false;
		
		try {
			processesAPI.delete(process.code, TestUtils.CURRENT_USER);
		} catch (APIException e) {
			exceptFired = true;
		}
		
		if (!exceptFired) {
			fail("Invalid process, should have been refused.");
		}
	}

	@Test
	public void testDeleteInvalidHasExpCodes() {
		Process process = ProcessFactory.getRandomProcess(true, true, false);
		
		when(processesDAO.findByCode(process.code)).thenReturn(process);
		when(processesDAO.checkObjectExistByCode(process.code)).thenReturn(true);

		Container container = ContainerFactory.getRandomContainer(ContainerStateNames.IW_P);
		container.code = UUID.randomUUID().toString();

		when(containerDAO.findByCode(process.inputContainerCode)).thenReturn(container);
		
		boolean exceptFired = false;
		
		try {
			processesAPI.delete(process.code, TestUtils.CURRENT_USER);
		} catch (APIException e) {
			exceptFired = true;
		}
		
		if (!exceptFired) {
			fail("Invalid process, should have been refused.");
		}
	}
	
	@Test
	public void testUpdateValid() {
		Process process = mock(Process.class);
		
		when(processesDAO.findByCode(process.code)).thenReturn(process);
		when(processesDAO.save(process)).thenReturn(process);

		PowerMockito.doNothing().when(proWworkflows).applyPreValidateCurrentStateRules(any(ContextValidation.class), any(Process.class));
		PowerMockito.doNothing().when(proWworkflows).applyPostValidateCurrentStateRules(any(ContextValidation.class), any(Process.class));
		
		Process processUpdateAPI = null;
		
		try {
			processUpdateAPI = processesAPI.update(process, TestUtils.CURRENT_USER);
		} catch (APIException e) {
			Logger.error("Exception occured during testUpdateValid()");
			fail(e.getMessage());
		}
		
		assertNotNull(processUpdateAPI);
	} 
	
	@Test
	public void testUpdateInvalid() {
		Process process = mock(Process.class);
		
		when(processesDAO.findByCode(process.code)).thenReturn(null);
		
		Process processUpdateAPI = null;
		boolean exceptFired = false;
		
		try {
			processUpdateAPI = processesAPI.update(process, TestUtils.CURRENT_USER);
		} catch (APIException e) {
			exceptFired = true;
		}
		
		if (!exceptFired) {
			fail("Invalid processes, should have been refused.");
		}
		
		assertNull(processUpdateAPI);
	}
}
